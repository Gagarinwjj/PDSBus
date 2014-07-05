package wedo.oa.beans;

public class People {
	private String UserID;
	private String UserName;
	private boolean isChecked;

	public People(String id, String name) {
		this.UserID = id;
		this.UserName = name;

	}

	public People() {
	}

	public String getUserID() {
		return UserID;
	}

	public void setUserID(String userID) {
		UserID = userID;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	// 关键：重写比较方法
	@Override
	public boolean equals(Object o) {
		People p = (People) o;
		return this.getUserID().equals(p.getUserID())
				&& this.getUserName().equals(p.getUserName());
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return UserID+"-"+UserName;
	}
}
