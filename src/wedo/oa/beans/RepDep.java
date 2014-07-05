package wedo.oa.beans;

/**
 * @author 王佳佳
 */

public class RepDep {
	/**
	 * 自动编号
	 */
	private int RepID;

	/**
	 * 汇报时间
	 */
	private String RepTime;
	/**
	 * 汇报内容
	 */
	private String RepCont;
	/**
	 * 汇报部门
	 */
	private String RepDep;
	/**
	 * 汇报部门ID号
	 */
	private int RepDepID;
	/**
	 * 汇报标记 是否正式提交的标记
	 */
	private int Flag;
	/**
	 * 实际时间
	 */
	private String RealTime;

	public int getRepID() {
		return RepID;
	}

	public void setRepID(int repID) {
		RepID = repID;
	}

	public String getRepTime() {
		return RepTime;
	}

	public void setRepTime(String repTime) {
		RepTime = repTime;
	}

	public String getRepCont() {
		return RepCont;
	}

	public void setRepCont(String repCont) {
		RepCont = repCont;
	}

	public String getRepDep() {
		return RepDep;
	}

	public void setRepDep(String repDep) {
		if (repDep.equals("anyType{}")) {
			RepDep = "未知部门";
			return;
		}
		RepDep = repDep;
	}

	public int getRepDepID() {
		return RepDepID;
	}

	public void setRepDepID(int repDepID) {
		RepDepID = repDepID;
	}

	public int getFlag() {
		return Flag;
	}

	public void setFlag(int flag) {
		Flag = flag;
	}

	public String getRealTime() {
		return RealTime;
	}

	public void setRealTime(String realTime) {
		RealTime = realTime;
	}

	@Override
	public String toString() {
		// <xs:element name="RepID" type="xs:int" minOccurs="0" />
		// <xs:element name="RepTime" type="xs:dateTime" minOccurs="0"
		// />
		// <xs:element name="RepCont" type="xs:string" minOccurs="0" />
		// <xs:element name="RepDep" type="xs:string" minOccurs="0" />
		// <xs:element name="RepDepID" type="xs:int" minOccurs="0" />
		// <xs:element name="Flag" type="xs:int" minOccurs="0" />
		// <xs:element name="RealTime" type="xs:dateTime" minOccurs="0"
		// />
		return RepID + " " + RepTime + " RepCont" + " " + RepDep + " "
				+ RepDepID + " " + Flag + " " + RealTime;
	}
}
