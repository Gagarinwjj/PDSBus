package wedo.oa.beans;

public class ProcessResult {
	private int Result;
	private String Msg;

	public ProcessResult() {
	}

	public ProcessResult(int result, String msg) {
		Result = result;
		Msg = msg;
	}

	public int getResult() {
		return Result;
	}

	public void setResult(int result) {
		Result = result;
	}

	public String getMsg() {
		return Msg;
	}

	public void setMsg(String msg) {
		Msg = msg;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return Result + ":" + Msg;
	}
}
