package wedo.oa.acitvity;

import android.app.Application;

public class AppUserInfo extends Application {
	// <UserID>96</UserID>
	// <UserName>刘晓兵</UserName>
	// <LoginID>lxb</LoginID>
	// <Password>81DC9BDB52D04DC20036DBD8313ED055</Password>
	// <AllowLogin>是</AllowLogin>
	// <NickName>刘晓兵</NickName>
	// <MainIDOfDep>是</MainIDOfDep>
	// <UserOrder>99</UserOrder>
	// <RoleID>13</RoleID>
	// <RegTime>2012-02-17T08:37:51.53+08:00</RegTime>
	// <PersonCard>0004630</PersonCard>
	//主要使用的只是 UserID 和 UserName
	private String UserID;
	private String UserName;
	private String LoginID;
	private String Password;
	private String AllowLogin;
	private String NickName;
	private String MainIDOfDep;
	private String UserOrder;
	private String RoleID;
	private String RegTime;
	private String PersonCard;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
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

	public String getLoginID() {
		return LoginID;
	}

	public void setLoginID(String loginID) {
		LoginID = loginID;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public String getAllowLogin() {
		return AllowLogin;
	}

	public void setAllowLogin(String allowLogin) {
		AllowLogin = allowLogin;
	}

	public String getNickName() {
		return NickName;
	}

	public void setNickName(String nickName) {
		NickName = nickName;
	}

	public String getMainIDOfDep() {
		return MainIDOfDep;
	}

	public void setMainIDOfDep(String mainIDOfDep) {
		MainIDOfDep = mainIDOfDep;
	}

	public String getUserOrder() {
		return UserOrder;
	}

	public void setUserOrder(String userOrder) {
		UserOrder = userOrder;
	}

	public String getRoleID() {
		return RoleID;
	}

	public void setRoleID(String roleID) {
		RoleID = roleID;
	}

	public String getRegTime() {
		return RegTime;
	}

	public void setRegTime(String regTime) {
		RegTime = regTime;
	}

	public String getPersonCard() {
		return PersonCard;
	}

	public void setPersonCard(String personCard) {
		PersonCard = personCard;
	}

	@Override
	public String toString() {
		return "UserInfo [UserID=" + UserID + ", UserName=" + UserName
				+ ", LoginID=" + LoginID + ", NickName=" + NickName
				+ ", RoleID=" + RoleID + "]";
	}

}
