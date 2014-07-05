package wedo.oa.beans;

public class UserProfit {
	private int id;
	private String idesc;
	private int idcode;
	private String SumY;
	private String SumP;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIdesc() {
		return idesc;
	}

	public void setIdesc(String idesc) {
		this.idesc = idesc;
	}

	public int getIdcode() {
		return idcode;
	}

	public void setIdcode(int idcode) {
		this.idcode = idcode;
	}



	public String getSumY() {
		return SumY;
	}

	public void setSumY(String sumY) {
		SumY = sumY;
	}

	public String getSumP() {
		return SumP;
	}

	public void setSumP(String sumP) {
		SumP = sumP;
	}

	@Override
	public String toString() {
		return id + " : " + idesc + " : " + SumY + " : " + SumP;
	}
}
