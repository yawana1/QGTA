package main.jobDef;

import java.util.List;

import data.xml.objects.JobDefinitions;
import data.xml.objects.Trial;

public interface JobDefProcesser {

	public List<Trial> processJobDef(JobDefinitions jobDef,  List<String> exp) throws Exception;
	
}
