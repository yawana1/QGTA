package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

/**
 * Wrapper to run a list of Callable or Runnable tasks in parallel.
 * Use a thread pool of specified size.
 * 
 * @author Scott Smith
 *
 */
public class Parallel {

	private static Logger log = Logger.getLogger(Parallel.class.getClass());
	
	private int poolSize = 0;
	
	public Parallel(int concurrentProcessMax){
		poolSize = concurrentProcessMax;
	}
	
	/**
	 * Run Collection of Callable tasks in parallel and block until all are finished.
	 * 
	 * @param tasks
	 * @return
	 * @throws Exception
	 */
	public <T> Collection<T> call(Collection<Callable<T>> tasks) throws Exception{
		ExecutorService pool = null;
		Collection<T> jobResults = null;
		try{
			int totalRunCount = tasks.size();
			jobResults = new ArrayList<T>(totalRunCount);
			
			pool = Executors.newFixedThreadPool(poolSize);
			CompletionService<T> results = new ExecutorCompletionService<>(pool);
			
			for(Callable<T> job : tasks){
				//start the thread
    			results.submit(job);
			}
			
			//wait for all threads to finish
			for(int i = 0; i < totalRunCount; i++){
				Future<T> result = results.take();
				T commandResult = result.get();
				jobResults.add(commandResult);
			}
		}
		catch(Exception e){
			log.warn("", e);
			throw e;
		}
    	finally{
    		if(pool != null){
        		pool.shutdown();	
    		}
    	}
		return jobResults;
	}
	
	/**
	 * Run Collection of Runnable tasks in parallel and block until all are finished.
	 * 
	 * @param tasks
	 */
	public void run(Collection<Runnable> tasks){
		
		//Promote Runnable tasks to callable
		Collection<Callable<Object>> callables = new ArrayList<>();
		for(Runnable task : tasks){
			Callable<Object> callable = Executors.callable(task);
			callables.add(callable);
		}

		//since Runnable eat the exception
		try {
			call(callables);
		} catch (Exception e) {

		}
	}
}