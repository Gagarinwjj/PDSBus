package wedo.oa.beans;

import java.io.Serializable;

import wedo.oa.utils.MyUtils;

public class UserForm implements Serializable {

	private static final long serialVersionUID = -4231135253986292771L;
	private String Sequence;
	private String FormName;//表单名
	private String Name;//流程名
	private String FqUserName;// 以原表字段为准
	private String State;
	private String FileContent;
	private String NowTimes;
	private String WtUsername;
	//private String id;//其实，服务器可以返回 as Id
	private String Id;
	private String UpNodeId;
	private String FlowNumber;
	private String FormId;
	private String FormNumber;
	private String JBRObjectId;
	private String JBRObjectName;
	private String SpModle;
	private String Number;
	private String FlowId;
	private String FlowName;
	private String UpNodeNumber;
	private String UpNodeNum;
	private String UpNodeName;//上一节点名
	private String FqUserID;
	private String YJBObjectId;// 以原表字段为准
	private String YJBObjecName;// 以原表字段为准

	public String getSequence() {
		return Sequence;
	}

	public void setSequence(String sequence) {
		Sequence = sequence;
	}

	public String getFormName() {
		return FormName;
	}

	public void setFormName(String formName) {
		FormName = formName;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getState() {
		return State;
	}

	public void setState(String state) {
		State = state;
	}

	public String getFileContent() {
		return FileContent;
	}

	public void setFileContent(String FileContent) {
		this.FileContent = FileContent;
	}

	public String getNowTimes() {
		return NowTimes;
	}

	public void setNowTimes(String nowTimes) {
		this.NowTimes = MyUtils.formatDate(nowTimes);
	}

	public String getWtUsername() {
		return WtUsername;
	}

	public void setWtUsername(String wtUsername) {
		WtUsername = wtUsername;
	}

	public String getId() {
		return Id;
	}

	// id不符合规范，反射时，用到的方法
	// public void setid(String id) {
	// setId(id);
	// }

	public void setId(String id) {
		this.Id = id;
	}

	public String getUpNodeId() {
		return UpNodeId;
	}

	public void setUpNodeId(String upNodeId) {
		UpNodeId = upNodeId;
	}

	public String getFlowNumber() {
		return FlowNumber;
	}

	public void setFlowNumber(String flowNumber) {
		FlowNumber = flowNumber;
	}

	public String getFormId() {
		return FormId;
	}

	public void setFormId(String formId) {
		FormId = formId;
	}

	public String getJBRObjectId() {
		return JBRObjectId;
	}

	public void setJBRObjectId(String jBRObjectId) {
		JBRObjectId = jBRObjectId;
	}

	public String getSpModle() {
		return SpModle;
	}

	public void setSpModle(String spModle) {
		SpModle = spModle;
	}

	public String getJBRObjectName() {
		return JBRObjectName;
	}

	public void setJBRObjectName(String jBRObjectName) {
		JBRObjectName = jBRObjectName;
	}

	public String getNumber() {
		return Number;
	}

	public void setNumber(String number) {
		Number = number;
	}

	public String getFlowId() {
		return FlowId;
	}

	public void setFlowId(String flowId) {
		FlowId = flowId;
	}

	public String getFlowName() {
		return FlowName;
	}

	public void setFlowName(String flowName) {
		FlowName = flowName;
	}

	public String getUpNodeNumber() {
		return UpNodeNumber;
	}

	public void setUpNodeNumber(String upNodeNumber) {
		UpNodeNumber = upNodeNumber;
	}

	public String getUpNodeNum() {
		return UpNodeNum;
	}

	public void setUpNodeNum(String upNodeNum) {
		UpNodeNum = upNodeNum;
	}

	public String getUpNodeName() {
		return UpNodeName;
	}

	public void setUpNodeName(String upNodeName) {
		UpNodeName = upNodeName;
	}

	public String getFqUserID() {
		return FqUserID;
	}

	public void setFqUserID(String fqUserID) {
		FqUserID = fqUserID;
	}

	public String getFqUserName() {
		return FqUserName;
	}

	public void setFqUserName(String fqUserName) {
		FqUserName = fqUserName;
	}

	public String getYJBObjectId() {
		return YJBObjectId;
	}

	public void setYJBObjectId(String yJBObjectId) {
		YJBObjectId = yJBObjectId;
	}

	public String getYJBObjecName() {
		return YJBObjecName;
	}

	public void setYJBObjecName(String yJBObjecName) {
		YJBObjecName = yJBObjecName;
	}

	public String getFormNumber() {
		return FormNumber;
	}

	public void setFormNumber(String formNumber) {
		FormNumber = formNumber;
	}

	@Override
	public String toString() {
		return "UserForm [Sequence=" + Sequence + ", FormName=" + FormName
				+ ", Name=" + Name + ", FqUsername=" + FqUserName + ", State="
				+ State + ", FileContent=" + FileContent + ", NowTimes="
				+ NowTimes + ", WtUsername=" + WtUsername + "]";
	}

}
