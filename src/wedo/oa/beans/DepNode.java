package wedo.oa.beans;

public class DepNode {
	private String DepID;
	private String DepName;

	public DepNode() {
	}

	public DepNode(String depid, String depName) {
		DepID = depid;
		DepName = depName;
	}

	public String getDepID() {
		return DepID;
	}

	public void setDepID(String depID) {
		DepID = depID;
	}

	public String getDepName() {
		return DepName;
	}

	public void setDepName(String depName) {
		DepName = depName;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return DepName;//只返回部门名称，显示用
	}

}
