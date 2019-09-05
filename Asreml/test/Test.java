

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import asreml.AsremlTrait;
import asreml.output.Slns;

public class Test {

	@org.junit.Test
	public void test() throws InterruptedException {
		
		//Path filename = Paths.get("/data/QG/trialAnalysis/dev/work/Yield_Trial/13E/gws_training_zone2/corn.spatial.genomic/yield/asreml");
		List<AsremlTrait> traits = new ArrayList<>();
		traits.add(new AsremlTrait("yield", 0));
		//AsrData asr = new AsrData(filename, 3, 2, 1, traits, null);
		
		
		System.out.println(getUsedMemory() + "Start");
		long startTime = System.currentTimeMillis();
		
		Slns sln = new Slns(Paths.get("/home/Scott Smith/tmp/asreml"), ".csv", ",");
		
		for(String col : sln.getData().keySet()){
			System.out.println(col + " " + sln.getData().get(col).size());
		}
		
		System.out.println(getUsedMemory() + "End");
		System.out.println(System.currentTimeMillis() - startTime + "End");
		
		System.gc();
		Thread.sleep(100000);
		
		System.out.println(getUsedMemory() + "GC");
	}
	
	public static long getUsedMemory(){
		int size = 1024*1024;
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / size;
	}

}