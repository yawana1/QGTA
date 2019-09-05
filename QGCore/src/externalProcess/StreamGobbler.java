/*
 * Using JRE 1.6.0_02
 * 
 * @package 	utils
 * @class 		StreamGobbler.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package externalProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * Originally from
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 * 
 * @author modified by ng36271
 * @version 1.0
 */

class StreamGobbler extends Thread
{
	private static Logger log = Logger.getLogger(StreamGobbler.class);
	
	/** The input stream. */
	InputStream is;
	
	/** The type. */
	String type;
	
	/** The output stream. */
	OutputStream os;
	
	/** The quiet. */
	boolean quiet;

	/**
	 * Instantiates a new stream gobbler.
	 * 
	 * @param is
	 *            the is
	 * @param type
	 *            the type
	 */
	StreamGobbler(InputStream is, String type)
	{
		this(is, type, null, false);
	}
	
	/**
	 * Instantiates a new stream gobbler.
	 * 
	 * @param is
	 *            the is
	 */
	StreamGobbler(InputStream is)
	{
		this(is, null, null, true);
	}
	
	/**
	 * Instantiates a new stream gobbler.
	 * 
	 * @param is
	 *            the is
	 * @param type
	 *            the type
	 * @param redirect
	 *            the redirect
	 */
	StreamGobbler(InputStream is, String type, OutputStream redirect)
	{
		this(is, type, null, false);
	}
	
	/**
	 * Instantiates a new stream gobbler.
	 * 
	 * @param is
	 *            the is
	 * @param redirect
	 *            the redirect
	 */
	StreamGobbler(InputStream is, OutputStream redirect)
	{
		this(is, null, redirect, true);
	}
	
	/**
	 * Instantiates a new stream gobbler.
	 * 
	 * @param is
	 *            the is
	 * @param type
	 *            the type
	 * @param redirect
	 *            the redirect
	 * @param quiet
	 *            the quiet
	 */
	StreamGobbler(InputStream is, String type, OutputStream redirect, boolean quiet)
	{
		this.is = is;
		this.type = type;
		this.os = redirect;
		this.quiet = quiet;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		try
		{
			PrintWriter pw = null;
			if (os != null)
				pw = new PrintWriter(os);

			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ( (line = br.readLine()) != null)
			{
				if (pw != null)
					//pw.println(line);
					log.error(line);
				if (!quiet)
					System.out.println(type + ">" + line);
			}
			if (pw != null)
				pw.flush();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}
