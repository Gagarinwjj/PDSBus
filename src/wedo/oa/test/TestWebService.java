package wedo.oa.test;

import junit.framework.Assert;
import wedo.oa.beans.NextStep;
import wedo.oa.beans.People;
import wedo.oa.utils.WebServiceUtils;
import android.test.AndroidTestCase;

public class TestWebService extends AndroidTestCase {

	public void testLoging() {
		WebServiceUtils.checkLogin("admin", "1234");
	}

	public void testGetDetailInfo() {
		WebServiceUtils.getDetailInfo(1468);
	}

	public void testGetNewsInfo() {
		WebServiceUtils.getNewsInfo(1, 1, 15);
	}

	public void testGetProFit() {
		WebServiceUtils.getProFit(2012, 5);
	}

	public void testGetRepList() {
		WebServiceUtils.getRepList();
	}

	public void testGetRepDep() {
		WebServiceUtils.getRepDep("2012-6-28");
	}

	public void testGetRepDetail() {
		WebServiceUtils.getRepDetail(4572);
	}

	public void testGetWFForm() {
		WebServiceUtils.getWFForm();
	}

	public void testGetUserFrom() {
		WebServiceUtils.getUserForm("95", "收公文", "正在办理", 1, 10);
	}

	public void testGetRemarks() {
		WebServiceUtils.getRemarks(84);
	}

	public void testGetAppUserInfo() {
		System.out.println(WebServiceUtils.getAppUserInfo(84));
	}

	public void testNextStep() {
		WebServiceUtils.execSql("proc_transaction_getNextStep @id=1470",
				NextStep.class);
	}

	public void testJBJSPeoples() {
		WebServiceUtils.execSql("getJBJSPeoples @roleids='8,1,'", People.class);
	}

	public void testDepPeoples() {
		WebServiceUtils.execSql("getDepPeoples @depid=25", People.class);
	}
	public void testWT(){
		System.out.println(WebServiceUtils.getWTProcessResult("",
				"","","",""));
		
	}
}
