package data.xml.objects;

import utils.Globals.CheckType;

public class Checks {

	Double core;
	Double perf;
	Double bmr;
	Double genetic;
	Double susceptable;
	int numCoreCheck = 0;
	int numPerfCheck = 0;
	int numBmrCheck = 0;
	int numGeneticCheck = 0;
	int numSusceptableCheck = 0;
	
	public Double getCore() {
		return core;
	}
	public void setCore(Double core) {
		this.core = core;
	}
	public Double getPerf() {
		return perf;
	}
	public void setPerf(Double perf) {
		this.perf = perf;
	}
	public Double getBmr() {
		return bmr;
	}
	public void setBmr(Double bmr) {
		this.bmr = bmr;
	}
	public Double getGenetic() {
		return genetic;
	}
	public void setGenetic(Double genetic) {
		this.genetic = genetic;
	}
	public int getNumCoreCheck() {
		return numCoreCheck;
	}
	public void setNumCoreCheck(int numCoreCheck) {
		this.numCoreCheck = numCoreCheck;
	}
	public int getNumPerfCheck() {
		return numPerfCheck;
	}
	public void setNumPerfCheck(int numPerfCheck) {
		this.numPerfCheck = numPerfCheck;
	}
	public int getNumBmrCheck() {
		return numBmrCheck;
	}
	public void setNumBmrCheck(int numBmrCheck) {
		this.numBmrCheck = numBmrCheck;
	}
	public int getNumGeneticCheck() {
		return numGeneticCheck;
	}
	public void setNumGeneticCheck(int numGeneticCheck) {
		this.numGeneticCheck = numGeneticCheck;
	}
	
	public Double getSusceptable() {
		return susceptable;
	}
	public void setSusceptable(Double susceptable) {
		this.susceptable = susceptable;
	}
	public int getNumSusceptableCheck() {
		return numSusceptableCheck;
	}
	public void setNumSusceptableCheck(int numSusceptable) {
		this.numSusceptableCheck = numSusceptable;
	}
	
	public double getAvg(int count, double sum, CheckType checkType){
		return count == 0 ? 0 : (sum / stats.Checks.getNumChecks(checkType, count));
	}
	
	public double getAvgCore(CheckType checkType){
		return getAvg(numCoreCheck, core, checkType);
	}
	
	public double getAvgPerf(CheckType checkType){
		return getAvg(numPerfCheck, perf, checkType);
	}
	
	public double getAvgBmr(CheckType checkType){
		return getAvg(numBmrCheck, bmr, checkType);
	}
	
	public double getAvgGenetic(CheckType checkType){
		return getAvg(numGeneticCheck, genetic, checkType);
	}
	
	public double getAvgSusceptable(CheckType checkType){
		return getAvg(numSusceptableCheck, susceptable, checkType);
	}
}
