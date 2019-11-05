package data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;

/**
 * Serialize and Deserialize xml data into objects using XStream
 * 
 * @author Scott Smith
 *
 * @see <a href="http://xstream.codehaus.org/index.html">http://xstream.codehaus.org/index.html</a>
 */
public class XML {
	public static XML INSTANCE = null;
	protected XStream xstream;
	
	public XML(){
		xstream = new XStream(new Dom4JDriver(new NoNameCoder())); //default _ escapes to __
		//set default data format
		String dateFormat = "yyyy-MM-dd HH:mm:ss.s";
		String noTimeFormat = "yyyy-MM-dd";
		String[] acceptableFormats = {noTimeFormat};
		xstream.registerConverter(new DateConverter(dateFormat, acceptableFormats));
	}
	
	/***
	 * Not Aliased
	 * @param <T>
	 * @param file
	 * @return
	 */
	public <T> T deserialize(String file){
		@SuppressWarnings("unchecked")
		T result = (T) xstream.fromXML(new File(file));
		return result;
	}

	/***
	 * Alias all objects in the graph of Object root to there class.getSimpleName
	 * ex. xstream.alisas("Trait", Trait.class);
	 * @param file
	 * @param root
	 * @throws IOException 
	 */
	public void deserialize(String file, Object root){
		objectTree(root.getClass(), new ArrayList<String>());
		xstream.fromXML(new File(file), root);
	}

	public void deserialize(Object root, Map<String,String> replace){
		try{
			String text = serialize(root);
			objectTree(root.getClass(), new ArrayList<String>());

			if(replace != null){
				for(Entry<String, String> replacement : replace.entrySet()){
					text = text.replace("$"+replacement.getKey(), replacement.getValue());
				}
			}
	
			xstream.fromXML(text, root);
		}
		catch(Exception e){
			throw e;
		}
	}
	
	/***
	 * Alias all objects in the graph of Object root to there class.getSimpleName
	 * ex. xstream.alisas("Trait", Trait.class);
	 * @param file - Full path of the xml file to deserialize
	 * @param root - Object to load xml into.
	 * @throws IOException 
	 */
	public void deserialize(String file, Object root, Map<String,String> replace){
		try {
			//check file
			Path path = Paths.get(file);
			String text = null;
			
			byte[] bytes;
			bytes = Files.readAllBytes(path);
			text = new String(bytes);

			objectTree(root.getClass(), new ArrayList<String>());

			if(replace != null){
				for(Entry<String, String> replacement : replace.entrySet()){
					text = text.replace("$"+replacement.getKey(), replacement.getValue());
				}
			}
	
			xstream.fromXML(text, root);
		} catch (IOException e) {
			System.err.println("File - " + file);
			System.err.println(e.getStackTrace());
		}

	}
	
	public String serialize(Object o){
		objectTree(o.getClass(), new ArrayList<String>());
		return xstream.toXML(o);
	}
	
	public void serialize(Object o, String file) throws IOException{
		Writer writer = null;
		try{
			writer = new FileWriter(new File(file));
			objectTree(o.getClass(), new ArrayList<String>());
			xstream.toXML(o, writer);
		}
		finally{
			if(null != writer){
				writer.close();
			}
		}
	}

	/***
	 * Traverse object graph perform action on each node
	 * @param c
	 * @param visited
	 */
	//TODO pull out into own class.
	public void objectTree(Class<?> c,List<String> visited){
		if(!visited.contains(c.getName())){
			visited.add(c.getName());
			xstream.alias(c.getSimpleName(), c);//actual performed action
			for(Field field : c.getDeclaredFields()){
				Type type = field.getGenericType();
				if(type instanceof ParameterizedType){
					ptRecurse((ParameterizedType) type, visited);
				}
				else{
					recurse(field, visited);
				}
			}
		}
	}

	private void recurse(Field field, List<String> visited){
		try {
			field.setAccessible(true);  //access private memebers
		}
		catch(Exception e) {
			//eat errors
		}
		if(field.getGenericType() instanceof Class<?>){
			Class<?> c = (Class<?>)field.getGenericType();
			if(!isPrimitive(c)){
				objectTree(c, visited);
			}
		}
	}
	
	/***
	 * Traverse List a Objects that are use as Generics
	 * Can be nested.
	 * @param pType
	 * @param visited
	 */
	private void ptRecurse(ParameterizedType pType, List<String> visited){
		for(Type genericType:pType.getActualTypeArguments()){
			if(genericType instanceof Class){
				objectTree((Class<?>)genericType, visited);
			}
			else if(genericType instanceof ParameterizedType){
				ptRecurse((ParameterizedType) genericType, visited);
			}
		}
	}
	
	public static boolean isPrimitive(Class<?> clazz){
		return clazz.isPrimitive() ||
		clazz.equals(Boolean.class) ||
		clazz.equals(Byte.class) ||
		clazz.equals(Short.class) ||
		clazz.equals(Integer.class) ||
		clazz.equals(Long.class) ||
		clazz.equals(Float.class) ||
		clazz.equals(Double.class) ||
		clazz.equals(String.class) ||
		clazz.equals(Character.class);
	}
}