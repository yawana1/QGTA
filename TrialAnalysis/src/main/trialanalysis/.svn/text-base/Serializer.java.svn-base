package main.trialanalysis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.log4j.Logger;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;

import data.xml.objects.Constants;
import data.xml.objects.Trial;

public class Serializer {

	private static Logger log = Logger.getLogger(Serializer.class.getName());
	public static final Serializer INSTANCE = new Serializer();
	private Kryo kryo;
	
	private Serializer(){
		kryo = new Kryo();
		kryo.register(Date.class, new DefaultSerializers.DateSerializer()); //won't pick up correct Date Serialize by default.
		kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
	}
	
	public <T> void serialize(T t, Path serializedFile) throws Exception{
		if(serializedFile != null && t != null){
			try(com.esotericsoftware.kryo.io.Output output = new com.esotericsoftware.kryo.io.Output(new FileOutputStream(serializedFile.toString(), false))){
				kryo.writeObject(output, t);
			}
			catch(Exception e){
				throw e;
			}
		}
	}
	
	public <T> T deserialize(Class<T> c, Path serializedFile) throws KryoException, FileNotFoundException{
		T result = null;
		if(serializedFile != null && Files.isReadable(serializedFile)){
			try(Input input = new Input(new FileInputStream(serializedFile.toString()))){
				result = kryo.readObject(input, c);
			}
			catch(Exception e){
				throw e;
			}
		}

		return result;
	}

	public void checkpoint(Trial trial, String trialWorkDirectory) throws Exception {
		String fileName = getBinaryFileName();
		if(fileName != null){		
			Path path = Paths.get(trialWorkDirectory, fileName);
			if(trial == null){
				deserialize(Trial.class, path);
			}
			else{
				serialize(trial, path);
			}
		}
	}
	
	public static String getBinaryFileName(){
		String fileName = null;
		if(Constants.INSTANCE.getConstant("serialized_file_name") != null){
			fileName = Constants.INSTANCE.getConstant("serialized_file_name").toString();
		}
		else{
			log.error("Missing serialized_file_name in Constants file");
		}
		return fileName;
	}
}
