package asreml.input;

import asreml.AsremlTrait;

/**
 * Class to create a unix command to run the Asreml.sh executable program.
 * 
 * @author Scott Smith
 *
 */
public class AsremlShellScript {

	private AsremlModel model;
	private AsremlTrait trait;
	private String cmd;
	
	public AsremlModel getModel() {
		return model;
	}
	public void setModel(AsremlModel model) {
		this.model = model;
	}
	public AsremlTrait getTrait() {
		return trait;
	}
	public void setTrait(AsremlTrait trait) {
		this.trait = trait;
	}
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	
	public AsremlShellScript(AsremlModel model, AsremlTrait trait, String cmd) {
		super();
		this.model = model;
		this.trait = trait;
		this.cmd = cmd;
	}
	
	public String toString(){
		String result = "";
		if(cmd != null){
			result = cmd;
		}
		return result;
	}
}
