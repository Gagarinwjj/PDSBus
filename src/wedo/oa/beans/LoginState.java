package wedo.oa.beans;

import java.io.Serializable;

public class LoginState implements Serializable {

	private static final long serialVersionUID = -1566546516079427174L;
	/**
	 * 0 异常 1 成功 2 失败 3 特殊
	 */
	private int State = 2;// 默认为登陆失败.比如没有联网时
	private int UserId;
	private String Info;

	/**
	 * @return 0 异常 1 成功 2 失败 3 特殊 默认为2，即失败
	 */
	public int getState() {
		return State;
	}

	public void setState(int state) {
		State = state;
	}

	
	public int getUserId() {
		return UserId;
	}

	public void setUserId(int userId) {
		UserId = userId;
	}

	public String getInfo() {
		return Info;
	}

	public void setInfo(String info) {
		Info = info;
	}

}
