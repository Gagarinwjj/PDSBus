package wedo.oa.beans;

public class NextStep {
	private String ID;
	private String NodeName;

	//newInstance 必须提供该方法
	public NextStep() {
	};

	public NextStep(String iD, String nodeName) {
		super();
		ID = iD;
		NodeName = nodeName;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getNodeName() {
		return NodeName;
	}

	public void setNodeName(String nodeName) {
		NodeName = nodeName;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return NodeName;
	}
}
