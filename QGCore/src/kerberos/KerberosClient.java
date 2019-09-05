package kerberos;

import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import com.sun.security.auth.module.Krb5LoginModule;

/**
 * To initialize and make http calls to a system that uses Kerberos security.
 * @author Scott Smith
 *
 */
public class KerberosClient {
	private final static String  JDBCDRIVER = "com.cloudera.impala.jdbc4.Driver";
	private static Logger log = Logger.getLogger(KerberosClient.class.getName());
	private String principal = "";
	private String password = "";
	private HttpClient spnegoHttpClient;
	private Subject subject;
	private Krb5LoginModule lc;

	public HttpClient getSpnegoHttpClient() {
		return spnegoHttpClient;
	}

	public void setSpnegoHttpClient(HttpClient spnegoHttpClient) {
		this.spnegoHttpClient = spnegoHttpClient;
	}

	public KerberosClient() {
	}

	public KerberosClient(String principal, String password) {
		super();
		this.principal = principal;
		this.password = password;
		System.setProperty("java.security.krb5.conf", "krb5.conf");
	}

	public KerberosClient(String principal, String password, boolean isDebug) throws Exception {
		this(principal, password);
		if (isDebug) {
			System.setProperty("sun.security.spnego.debug", "true");
			System.setProperty("sun.security.krb5.debug", "true");
		}
		login();
	}

	
	/**
	 * Init a Httpclient will use SPNEGO to find authentication method
	 * @param principle
	 * @param password
	 * @return
	 */
	public static HttpClient buildSpengoHttpClient(final String principle, final String password) {
		HttpClientBuilder builder = HttpClientBuilder.create();
		
		//set authentication scheme
		Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider> create()
				.register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build();
		builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
		
		//set credentials provider to send user name and password to authenicate with the server
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(null, -1, null), new Credentials() {
			@Override
			public Principal getUserPrincipal() {
				return new KerberosPrincipal(principle);
			}

			@Override
			public String getPassword() {
				return password;
			}
		});
		builder.setDefaultCredentialsProvider(credentialsProvider);
		
		CloseableHttpClient httpClient = builder.build();
		return httpClient;
	}

	/**
	 * Use Krb5LoginModule as KDC module
	 * Note most commit after login to get the principle 
	 * @throws Exception
	 */
	public void login() throws Exception{
		try {
			//config kerborse
			lc = new Krb5LoginModule();
			Map<String, Object> config = new HashMap<String, Object>();
			config.put("useTicketCache", "true");
			config.put("tryFirstPass", "true"); //check shared memory for password
			// Krb5 in GSS API needs to be refreshed
			// so it does not throw the error
			// Specified version of key is not
			// available
			config.put("storePass", "true");
			config.put("refreshKrb5Config", "false");  //refresh after krb5.conf is loaded
			config.put("doNotPrompt", "true"); //don't prompt for username/password
			config.put("isInitiator", "true");
			config.put("debug", "false");
			
			//set shared mem values of name/pass
			Map<String,Object> shared = new HashMap<>();
		    shared.put("javax.security.auth.login.name", principal);
		    shared.put("javax.security.auth.login.password", password.toCharArray());
		    
		    subject = new Subject();
		    lc.initialize(subject, new CallbackHandler() {
		        @Override
		        public void handle(Callback[] callbacks) {
		            for(Callback callback: callbacks) {
		                if (callback instanceof NameCallback) {
		                    ((NameCallback)callback).setName(principal);
		                }
		                if (callback instanceof PasswordCallback) {
		                    ((PasswordCallback)callback).setPassword(password.toCharArray());
		                }
		            }
		        }
		    }, shared, config);
			lc.login();
			//SystemEx.run(String.format(" kinit %s -k -t /home/%s/.kinit/%s.keytab", principal, principal.split("@")[0], principal.split("@")[0]));
			
		} catch (Exception e) {
			throw e;
		}
		lc.commit();
		//log.warn(subject.toString());
	}
	
	public void logout(){
		try{
			lc.logout();
		}
		catch(Exception e){
			log.warn("", e);
		}
	}
	
	/**
	 * Execute a http request as the current authorized subject.
	 * 
	 * @param httpUriRequest
	 * @return
	 * @throws Exception
	 */
	public HttpResponse callHttpUriRequest(final HttpUriRequest httpUriRequest) throws Exception {		
		return Subject.doAs(subject, new PrivilegedAction<HttpResponse>() {
			HttpResponse httpResponse = null;

			@Override
			public HttpResponse run() {
				try {
					httpResponse = spnegoHttpClient.execute(httpUriRequest);
					return httpResponse;
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				return httpResponse;
			}
		});
	}
	
	/**
	 * Make JDBC connection as the Authenticated Subject.
	 * @param connectionURL
	 * @param JDBCDriver
	 * @param query
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public Statement callConnection(final String connectionURL) throws Exception {		
		Connection conn = (Connection) Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
			
			public Object run() throws ClassNotFoundException, SQLException{
				Class.forName(JDBCDRIVER);
				Connection conn = DriverManager.getConnection(connectionURL);
				return conn;
			}
		});
		
		Statement stmt = conn.createStatement();
		
		return stmt;
	}
}
