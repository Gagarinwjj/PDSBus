package wedo.oa.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.ServiceConnectionSE;

import wedo.oa.acitvity.AppUserInfo;
import wedo.oa.beans.LoginState;
import wedo.oa.beans.News;
import wedo.oa.beans.NewsDetails;
import wedo.oa.beans.NextStep;
import wedo.oa.beans.ProcessResult;
import wedo.oa.beans.RepDep;
import wedo.oa.beans.UserForm;
import wedo.oa.beans.UserProfit;

public class WebServiceUtils {
	// 1.验证登录信息
	private static final String checkLogin = "CheckLogin";
	// 2.根据信息内码获取详细信息
	private static final String getDetailInfo = "GetDetailInfo ";
	// 3.获取内部新闻
	private static final String getNewsInfoPage = "GetNewsInfoPage";// 支持分页
	// 4.获取特殊信息
	private static final String getProFit = "GetProFit";
	// 5.日工作汇报部门
	private static final String getRepDep = "GetRepDep";
	// 6.日工作汇报详细
	private static final String getRepDetail = "GetRepDetail";
	// 7.工作汇报列表
	private static final String getRepList = "GetRepList";

	// --工作流部分--//
	// 8.待办工作 表单类型
	private static final String getWFFrom = "GetWFForm";
	// 9.获取用户表单
	private static final String getUserForm = "GetUserForm";
	// 10.用户常用批注
	private static final String getRemarks = "ExecSql";// 直接拼接语句
	// 11.用户详情
	private static final String getAppUserInfo = "ExecSql";// 直接拼接语句
	// 12.执行sql语句
	private static final String execSql = "ExecSql";// 直接拼接语句

	// 13、处理委托
	private static final String processWt = "ProcessWt";
	// 14、保存
	private static final String save = "ProcessSave";
	// 15、保存并通过
	private static final String saveAndPass = "ProcessSaveAndPass";
	// 16、保存并驳回
	private static final String saveAndReject = "ProcessSaveAndReject";
	// 17、提交
	private static final String submit = "ProcessSubmit";
	// 18、驳回
	private static final String reject = "ProcessReject";

	private static final String NAMESPACE = "http://pdsandroid.org/";

	// 1、测试接口
	// private static final String URL =
	// "http://oa.233100.net/OAServiceAndroidApp.asmx";//测试发现，数据比较旧

	// 2、正式接口 端口号如果不带则为默认的80端口
	/*
	 * 对于密码提示错误的问题，是由于重新发布替换了AppCode.dll所致，修复方式为：
	 * 方式一：重新改写接口地址为OAServiceAndroidApp.asmx，需要重新发布程序，重新安装（非常繁琐）
	 * 方式二：将服务器上的所有有接口均定向到OAServiceAndroidApp.asmx（内容复制粘贴即可），不用重新发布和安装（非常简洁）
	 * 即OAService.asmx(AppCode.dll)、OAServiceAndroid.asmx(AppCode.dll)
	 * 均定向到最新的OAServiceAndroidApp.asmx(bin\OAServiceApp.dll)
	 * 所以服务器的OAService.asmx
	 * =OAServiceAndroid.asmx=OAServiceAndroidApp.asmx(bin\OAServiceApp.dll)
	 * 本地的可以不改，OAService.asmx(PDS
	 * AppCode.dll)!=OAServiceAndroid.asmx(PDSOAService
	 * AppCode.dll)!=OAServiceAndroidApp.asmx(WebServiceApp
	 * bin\OAServiceApp.dll)，测试用
	 */
	private static final String URL = "http://info.pdsgj.com/OAServiceAndroidApp.asmx";

	// 3、VS IIS Express 接口
	// private static final String URL =
	// "http://10.0.2.2:51195/PDS/OAServiceAndroid.asmx";

	// 4、模拟器 IIS7发布接口
	// private static final String URL =
	// "http://10.0.2.2/PDSOAService/OAServiceAndroid.asmx";

	// 5、手机、模拟器 IIS7发布接口
	// private static final String URL =
	// "http://192.168.0.23/PDSOAService/OAServiceAndroid.asmx";

	// 6、本地服务器的域名
	// private static final String URL =
	// "http://192.168.0.23/PDS/OAServiceAndroid.asmx";

	// 参数能不能不传，能不能传null;参数不能不传，可以为null
	// 1.查询Web Services支持地址数据
	/**
	 * @param floginid
	 * @param fpassword
	 * @return 0 异常 1 成功 2 失败 3 特殊
	 */
	public static LoginState checkLogin(String floginid, String fpassword) {
		LoginState loginState = new LoginState();
		SoapObject request = new SoapObject(NAMESPACE, checkLogin);
		// 参数位置要在 envelope.bodyOut=request之前，之后来不及，会报未绑定对象到实例错误
		request.addProperty("floginid", floginid);
		request.addProperty("fpassword", fpassword);
		// //System.out.println("request: " + request);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;// 必须启用，否则 服务器无法处理请求。 ---> 未将对象引用设置到对象的实例
		envelope.setOutputSoapObject(request);

		// //System.out.println("bodyin: " + envelope.bodyIn);
		// //System.out.println("bodyout: " + envelope.bodyOut);
		HttpTransportSE ht = new HttpTransportSE(URL);

		try {

			String soap_action = NAMESPACE + checkLogin;
			ht.call(soap_action, envelope);

			// SoapObject responseSoapObject = (SoapObject)
			// envelope.getResponse();
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			// //System.out.println("bodyIn:" + envelope.bodyIn);

			// 解析
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("CheckLoginResult")).getProperty(1);
			// //System.out.println("detail:" + detail);
			// 格式：propertyname=typename{propertyname=typename{... ... }}
			// 可解析的为： propertyname=typename{key=value;key=value}
			// getAttribute获得key值，getProperty获得value值？？？
			detail = (SoapObject) ((SoapObject) detail
					.getProperty("NewDataSet")).getProperty("ds");
			System.out.println("登陆账户：" + detail);
			// 如果不是0 1 2而是字符，则大小写一定要注意
			loginState.setState(Integer.valueOf(detail.getProperty("State")
					.toString()));
			loginState.setUserId(Integer.valueOf(detail.getProperty("UserId")
					.toString()));
			loginState.setInfo(detail.getProperty("Info").toString());
			return loginState;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loginState;
	}

	public static AppUserInfo getAppUserInfo(int UserId) {
		AppUserInfo aui = new AppUserInfo();
		SoapObject request = new SoapObject(NAMESPACE, getAppUserInfo);
		request.addProperty("sql", "SELECT *  FROM BAS_UserInfo WHERE UserID="
				+ UserId);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;// 必须启用，否则 服务器无法处理请求。 ---> 未将对象引用设置到对象的实例
		envelope.setOutputSoapObject(request);

		HttpTransportSE ht = new HttpTransportSE(URL);

		try {
			String soap_action = NAMESPACE + getAppUserInfo;
			ht.call(soap_action, envelope);
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			// 解析
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("ExecSqlResult")).getProperty(1);
			detail = (SoapObject) ((SoapObject) detail
					.getProperty("NewDataSet")).getProperty("ds");// 一条数据，精确到''ds''
																	// 或者 0
			return (AppUserInfo) MyUtils.fillDataByReflect(AppUserInfo.class,
					detail);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aui;
	}

	// 2.根据信息内码获取详细信息
	/**
	 * @param fid
	 * @return 返回新闻 如果请求失败 则返回 NewsDetais默认实例 而不是null
	 */
	public static NewsDetails getDetailInfo(int fid) {
		NewsDetails newsDetails = new NewsDetails();

		SoapObject request = new SoapObject(NAMESPACE, getDetailInfo);
		request.addProperty("fid", fid);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);// 12 不行，11，10 可以

		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;

		envelope.dotNet = true;
		HttpTransportSE ht = new HttpTransportSE(URL);

		try {
			String soap_action = NAMESPACE + getDetailInfo;
			ht.call(soap_action, envelope);

			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("GetDetailInfoResult")).getProperty(1);
			detail = (SoapObject) ((SoapObject) detail
					.getProperty("NewDataSet")).getProperty("ds");

			newsDetails.setAtricleID(detail.getProperty(0).toString());
			newsDetails.setClaID(detail.getProperty(1).toString());
			newsDetails.setTitle(detail.getProperty(2).toString());
			newsDetails.setArticlCont(detail.getProperty(3).toString());
			newsDetails.setSendTime(detail.getProperty(4).toString());
			newsDetails.setHits(detail.getProperty(5).toString());
			newsDetails.setIfImgNews(detail.getProperty(6).toString());
			newsDetails.setIfInNews(detail.getProperty(7).toString());
			newsDetails.setSendUserName(detail.getProperty(8).toString());
		} catch (Exception e) {
			// e.printStackTrace();
			System.err
					.println("获取新闻详情 getDetailInfo SocketException: recvfrom failed: ECONNRESET (Connection reset by peer)");
		}
		return newsDetails;
	}

	// 3.获取newsType=1内部新闻,newsType=2通知公告,newsType=3公交一天
	public static List<News> getNewsInfo(int newsType, int pageFor, int pageSize) {
		List<News> newslist = new ArrayList<News>();
		SoapObject request = new SoapObject(NAMESPACE, getNewsInfoPage);
		request.addProperty("newsType", newsType);
		request.addProperty("pageFor", pageFor);
		request.addProperty("pageSize", pageSize);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);// 12 不行，11，10 可以
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;

		HttpTransportSE ht = new HttpTransportSE(URL);

		try {
			String soap_action = NAMESPACE + getNewsInfoPage;
			ht.call(soap_action, envelope);// double id 错误 经过半天的测试、调试、猜测 原因是
											// ds的第11个与ds1的第1个 均是
											// diffgr:id="ds11"所导致
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("GetNewsInfoPageResult")).getProperty(1);
			// System.out.println(detail);
			detail = ((SoapObject) detail.getProperty("NewDataSet"));
			int count = detail.getPropertyCount();
			for (int i = 0; i < count - 1; i++) {
				News newsInfo = new News();
				SoapObject item = (SoapObject) detail.getProperty(i);
				newsInfo.setArticleID(item.getProperty(0).toString());
				newsInfo.setTitle(item.getProperty(1).toString());
				newsInfo.setSendTime(item.getProperty(2).toString());
				newsInfo.setSendUsername(item.getProperty(3).toString());
				newsInfo.setClickCount(item.getProperty(4).toString());
				newslist.add(newsInfo);
			}
			// 最后一个是总数
			SoapObject lastObj = (SoapObject) detail.getProperty(count - 1);
			News lastNewsInfo = new News();
			lastNewsInfo.setArticleID(lastObj.getProperty(0).toString());
			newslist.add(lastNewsInfo);
		} catch (Exception e) {
			// e.printStackTrace();
			System.err
					.println("获取新闻列表getNewsInfo SocketException: recvfrom failed: ECONNRESET (Connection reset by peer)");
		}
		return newslist;
	}

	public static List<UserProfit> getProFit(int fyear, int fperiod) {
		List<UserProfit> userprofits = new ArrayList<UserProfit>();
		SoapObject request = new SoapObject(NAMESPACE, getProFit);
		request.addProperty("fyear", fyear);
		request.addProperty("fperiod", fperiod);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);// 12 不行，11，10 可以

		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;

		envelope.dotNet = true;
		HttpTransportSE ht = new HttpTransportSE(URL);

		try {
			String soap_action = NAMESPACE + getProFit;
			ht.call(soap_action, envelope);
			// //System.out.println("profit: " + envelope.bodyIn);
			SoapObject bodyin = (SoapObject) envelope.bodyIn;// 如果不能访问，bodyIn返回错误提示，则这里会有强转异常。

			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("GetProFitResult")).getProperty(1);
			detail = (SoapObject) detail.getProperty("NewDataSet");
			// System.out.println(detail);
			int count = detail.getPropertyCount();
			for (int i = 0; i < count; i++) {
				SoapObject item = (SoapObject) detail.getProperty(i);
				UserProfit uProfit = new UserProfit();
				uProfit.setId(Integer
						.valueOf(item.getProperty("id").toString()));
				// step by step
				try {
					uProfit.setIdesc(item.getProperty("idesc").toString());
				} catch (Exception e) {
					uProfit.setIdesc("");
					// e.printStackTrace();
				}
				try {
					uProfit.setIdcode(Integer.valueOf(item
							.getProperty("idcode").toString()));
				} catch (Exception e) {
					uProfit.setIdcode(-1);
					// e.printStackTrace();
				}
				try {
					uProfit.setSumY(item.getProperty("SumY").toString());
				} catch (Exception e) {
					uProfit.setSumY("-1");
					// e.printStackTrace();
				}
				try {
					uProfit.setSumP(item.getProperty("SumP").toString());
				} catch (Exception e) {
					uProfit.setSumP("-1");
					// e.printStackTrace();
				}
				userprofits.add(uProfit);
			}
			// System.out.println(userprofits);
		} catch (Exception e) {
			System.err.println("财务查询 连接重置");
		}
		return userprofits;
	}

	public static String[] getRepList() {
		// String[] repTimes = null;
		// 如果网络不通，这里会有异常，返回值为初始值null。处理端如果不做null判断，则会报告空指针异常
		String[] repTimes = {};// 最好初始化非空对象
		SoapObject request = new SoapObject(NAMESPACE, getRepList);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);// 12 不行，11，10 可以
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		envelope.dotNet = true;
		HttpTransportSE ht = new HttpTransportSE(URL);
		try {
			String soap_action = NAMESPACE + getRepList;
			ht.call(soap_action, envelope);
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("GetRepListResult")).getProperty(1);
			detail = (SoapObject) detail.getProperty("NewDataSet");
			int count = detail.getPropertyCount();
			repTimes = new String[count];
			for (int i = 0; i < count; i++) {
				SoapObject item = (SoapObject) detail.getProperty(i);
				// 服务器端日期格式没处理，则由客户端处理
				repTimes[i] = item.getProperty(0).toString().substring(0, 10)
						+ "各单位工作汇报";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return repTimes;
	}

	public static List<RepDep> getRepDep(String repTime) {
		List<RepDep> repDeps = new ArrayList<RepDep>();
		SoapObject request = new SoapObject(NAMESPACE, getRepDep);
		request.addProperty("repTime", repTime);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);// 12 不行，11，10 可以
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		envelope.dotNet = true;
		HttpTransportSE ht = new HttpTransportSE(URL);
		try {
			String soap_action = NAMESPACE + getRepDep;
			ht.call(soap_action, envelope);
			// //System.out.println(envelope.bodyIn);
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			// //System.out.println(bodyin);
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("GetRepDepResult")).getProperty(1);
			detail = (SoapObject) detail.getProperty("NewDataSet");
			// System.out.println("getRepDep:" + detail);
			int count = detail.getPropertyCount();
			for (int i = 0; i < count; i++) {
				SoapObject item = (SoapObject) detail.getProperty(i);
				RepDep repDep = new RepDep();

				// 一下7个字段，有时候全返回 有时候返回部分。item.getProperty(int)有严格的对应关系，所以不靠谱。
				// <xs:element name="RepID" type="xs:int" minOccurs="0" />
				// <xs:element name="RepTime" type="xs:dateTime" minOccurs="0"
				// />
				// <xs:element name="RepCont" type="xs:string" minOccurs="0" />
				// <xs:element name="RepDep" type="xs:string" minOccurs="0" />
				// <xs:element name="RepDepID" type="xs:int" minOccurs="0" />
				// <xs:element name="Flag" type="xs:int" minOccurs="0" />
				// <xs:element name="RealTime" type="xs:dateTime" minOccurs="0"
				// />
				// repDep.setRepID(Integer.valueOf(item.getProperty(0).toString()));
				// repDep.setRepTime(item.getProperty(1).toString());
				// repDep.setRepCont(item.getProperty(2).toString());
				// repDep.setRepDep(item.getProperty(3).toString());
				// repDep.setRepDepID(Integer.valueOf(item.getProperty(4)
				// .toString()));
				// repDep.setFlag(Integer.valueOf(item.getProperty(5).toString()));
				// repDep.setRealTime(item.getProperty(6).toString());
				// repDeps.add(repDep);
				repDep.setRepID(Integer.valueOf(item.getProperty("RepID")
						.toString()));
				repDep.setRepTime(item.getProperty("RepTime").toString());
				repDep.setRepCont(item.getProperty("RepCont").toString());
				repDep.setRepDep(item.getProperty("RepDep").toString());
				try {
					repDep.setRepDepID(Integer.valueOf(item.getProperty(
							"RepDepID").toString()));
				} catch (Exception e) {
					repDep.setRepDepID(-1);// 没有返回该字段
					// e.printStackTrace();
				}
				repDep.setFlag(Integer.valueOf(item.getProperty("Flag")
						.toString()));
				repDep.setRealTime(item.getProperty("RealTime").toString());
				repDeps.add(repDep);
			}
			// System.out.println(repDeps);
		} catch (Exception e) {
			// e.printStackTrace();
			System.err
					.println("获取部门列表 SocketException: recvfrom failed: ECONNRESET (Connection reset by peer)");
		}
		return repDeps;
	}

	public static RepDep getRepDetail(int repID) {
		RepDep repDep = new RepDep();
		SoapObject request = new SoapObject(NAMESPACE, getRepDetail);
		request.addProperty("repID", repID);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);// 12 不行，11，10 可以
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		envelope.dotNet = true;
		HttpTransportSE ht = new HttpTransportSE(URL);
		try {
			String soap_action = NAMESPACE + getRepDetail;
			ht.call(soap_action, envelope);
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("GetRepDetailResult")).getProperty(1);
			detail = (SoapObject) ((SoapObject) detail
					.getProperty("NewDataSet")).getProperty("ds");
			// //System.out.println(detail);
			repDep.setRepID(Integer.valueOf(detail.getProperty(0).toString()));
			repDep.setRepTime(detail.getProperty(1).toString());
			repDep.setRepCont(detail.getProperty(2).toString());
			repDep.setRepDep(detail.getProperty(3).toString());
			repDep.setFlag(Integer.valueOf(detail.getProperty(4).toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return repDep;
	}

	public static String[] getWFForm() {
		String[] wfforms = {};// 最好初始化非空对象
		SoapObject request = new SoapObject(NAMESPACE, getWFFrom);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);// 12 不行，11，10 可以
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		envelope.dotNet = true;
		HttpTransportSE ht = new HttpTransportSE(URL);
		try {
			String soap_action = NAMESPACE + getWFFrom;
			ht.call(soap_action, envelope);
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			// System.out.println(bodyin);
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("GetWFFormResult")).getProperty(1);
			detail = (SoapObject) detail.getProperty("NewDataSet");
			int count = detail.getPropertyCount();
			wfforms = new String[count + 1];
			wfforms[0] = "所有表单类型";
			for (int i = 0; i < count; i++) {
				SoapObject item = (SoapObject) detail.getProperty(i);
				wfforms[i + 1] = item.getProperty(0).toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wfforms;
	}

	public static String[] getRemarks(int UserId) {

		String[] remarks = {};// 初始化时，不要赋值为null，否则 在没有批注时 报告空指针
		SoapObject request = new SoapObject(NAMESPACE, getRemarks);
		request.addProperty("sql",
				" SELECT Title  FROM   WF_UseSpRemark where UserID='" + UserId
						+ "'");
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);// 12 不行，11，10 可以
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		envelope.dotNet = true;
		HttpTransportSE ht = new HttpTransportSE(URL);
		try {
			String soap_action = NAMESPACE + getRemarks;
			ht.call(soap_action, envelope);
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			// //System.out.println(bodyin);
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("ExecSqlResult")).getProperty(1);
			// 如果没有数据，则抛异常 illegal property: NewDataSet
			detail = (SoapObject) detail.getProperty("NewDataSet");
			if (detail != null) {
				int count = detail.getPropertyCount();
				remarks = new String[count];
				for (int i = 0; i < count; i++) {
					SoapObject item = (SoapObject) detail.getProperty(i);
					remarks[i] = item.getProperty(0).toString();
					// System.out.println(remarks[i]);
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("没有常用批注");
		}
		return remarks;
	}

	/**
	 * @param userid
	 * @param formname
	 * @param state
	 * @param pageFor
	 * @param pageSize
	 * @return 会报告异常Exception:Connection reset by peer
	 */
	public static List<UserForm> getUserForm(String userid, String formname,
			String state, int pageFor, int pageSize) {
		List<UserForm> userForms = new ArrayList<UserForm>();
		SoapObject request = new SoapObject(NAMESPACE, getUserForm);
		// 实测，参数名需要和服务器的同名
		request.addProperty("userid", userid);
		request.addProperty("formname", formname);
		request.addProperty("state", state);
		request.addProperty("pageFor", pageFor);
		request.addProperty("pageSize", pageSize);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);// 12 不行，11，10 可以
		envelope.dotNet = true;
		envelope.encodingStyle = "UTF-8";
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		HttpTransportSE ht = new HttpTransportSE(URL);
		HttpURLConnection connection = null;
		try {
			// 通过反射 设置超时
			// Method[] methods = HttpTransportSE.class.getDeclaredMethods();
			// for (Method m : methods) {
			// System.out.println(m.getName());
			// }

			Method method = HttpTransportSE.class.getDeclaredMethod(
					"getServiceConnection", null);

			method.setAccessible(true);// 禁用访问权限检查，可以反射private方法
			ServiceConnectionSE serviceConnectionSE = (ServiceConnectionSE) method
					.invoke(ht, null);
			// 实际上connection就是java.net.HttpURLConnection。获得灵感
			// http://sparrow82.iteye.com/blog/750833
			// http://blog.csdn.net/a859522265/article/details/7965971
			Field connectionField = ServiceConnectionSE.class
					.getDeclaredField("connection");
			connectionField.setAccessible(true);
			// System.err(1005): java.lang.ClassCastException:
			// libcore.net.http.HttpURLConnectionImpl cannot be cast to
			// org.apache.commons.httpclient.util.HttpURLConnection
			// eclipse丢失包->引入包->eclipse自动导入->eclipse导入apache的包，错了->bug调试->打开异常->发现问题->重新导包
			connection = (HttpURLConnection) connectionField
					.get(serviceConnectionSE);
			connection.setConnectTimeout(5000);// 链接超时
			connection.setReadTimeout(5000);// 读取超时
			// connection.setChunkedStreamingMode(100);
			// connection.setRequestProperty("Connection", "close");
			String soap_action = NAMESPACE + getUserForm;
			ht.call(soap_action, envelope);// Exception:Connection reset by peer
											// 可能是数据量太多了98.5 KB左右
			// System.out.println("获取数据：" + connection.getContentLength());//
			// 等于471
			// 难道只是头的大小？

			// 解析
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("GetUserFormResult")).getProperty(1);
			// //System.out.println(detail);
			detail = ((SoapObject) detail.getProperty("NewDataSet"));
			int count = detail.getPropertyCount();
			for (int i = 0; i < count - 1; i++) {
				SoapObject item = (SoapObject) detail.getProperty(i);
				UserForm userForm = (UserForm) MyUtils.fillDataByReflect(
						UserForm.class, item);
				userForms.add(userForm);
			}
			// 总页数
			SoapObject lastItem = (SoapObject) detail.getProperty(count - 1);
			UserForm lastUserForm = new UserForm();
			lastUserForm.setId(lastItem.getProperty(0).toString());
			userForms.add(lastUserForm);
			// System.out.println(userForms);
		} catch (Exception e) {

			e.printStackTrace();
			System.err
					.println("获取表单 getUserForm SocketException: recvfrom failed: ECONNRESET (Connection reset by peer)");
		}
		if (connection != null)
			connection.disconnect();
		return userForms;
	}

	/**
	 * @param sql
	 * @return true 执行sql成功 false 执行sql失败
	 */
	public static boolean execSql(String sql) {
		SoapObject request = new SoapObject(NAMESPACE, execSql);
		request.addProperty("sql", sql);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);// 12 不行，11，10 可以
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		envelope.dotNet = true;
		HttpTransportSE ht = new HttpTransportSE(URL);
		try {
			String soap_action = NAMESPACE + execSql;
			ht.call(soap_action, envelope);
			// 没有数据需要解析
			// 没有异常，执行成功
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			// 出现异常，执行失败
			return false;
		}
	}

	/**
	 * 这是一个综合接口，有效避免接口过多，当然这也是非常危险的，能够执行自定义语句！使用时， 需小心谨慎，发布时，接口一定不对外公开
	 * 
	 * @param sql
	 *            需要执行的sql语句
	 * @param clazz
	 *            泛型类型
	 * @return 泛型列表
	 */
	public static <E> List<E> execSql(String sql, Class<E> clazz) {
		List<E> results = new ArrayList<E>();
		SoapObject request = new SoapObject(NAMESPACE, execSql);
		request.addProperty("sql", sql);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);// 12 不行，11，10 可以
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		envelope.dotNet = true;
		HttpTransportSE ht = new HttpTransportSE(URL);
		try {
			String soap_action = NAMESPACE + execSql;
			ht.call(soap_action, envelope);
			// 解析
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("ExecSqlResult")).getProperty(1);
			// System.out.println(detail);//打印sql语句的执行结果
			// 如果一条数据都没有，则异常报错 么有NewDataSet
			detail = ((SoapObject) detail.getProperty("NewDataSet"));
			int count = detail.getPropertyCount();
			for (int i = 0; i < count; i++) {
				SoapObject item = (SoapObject) detail.getProperty(i);
				E result = (E) MyUtils.fillDataByReflect(clazz, item);
				// System.out.println(result);
				results.add(result);
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
			System.err.println("illegal property: NewDataSet 即一条数据都没有");
		}
		return results;
	}

	public static ArrayList<NextStep> getNextStep(String id) {
		ArrayList<NextStep> results = new ArrayList<NextStep>();
		// 模拟数据
		results.add(new NextStep("12", "经办人"));
		results.add(new NextStep("10", "部门主管领导"));
		return results;
	}

	public static ProcessResult getWTProcessResult(String id, String userID,
			String userName, String wtUserID, String wtUserName) {
		ProcessResult pr = new ProcessResult(-1, "数据异常，处理失败");
		SoapObject request = new SoapObject(NAMESPACE, processWt);
		// 1、参数名需要和服务器的参数名同名
		// 2、这里形参无所谓，para1 para2都行
		// 3、SQL服务器上不区分大小写 @id 可以写成 @Id
		request.addProperty("id", id);
		request.addProperty("userID", userID);
		request.addProperty("userName", userName);
		request.addProperty("wtUserID", wtUserID);
		request.addProperty("wtUserName", wtUserName);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);// 12 不行，11，10 可以
		envelope.dotNet = true;
		envelope.encodingStyle = "UTF-8";
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		HttpTransportSE ht = new HttpTransportSE(URL);

		try {
			String soap_action = NAMESPACE + processWt;
			ht.call(soap_action, envelope);

			// 解析
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("ProcessWtResult")).getProperty(1);
			detail = (SoapObject) ((SoapObject) detail
					.getProperty("NewDataSet")).getProperty("ds");
			pr.setResult(Integer.valueOf(detail.getProperty("Result")
					.toString()));
			pr.setMsg(detail.getProperty("Msg").toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return pr;
	}

	public static ProcessResult getSaveProcessResult(String id, String userId,
			String userName, String fileContent, String spContent) {
		ProcessResult pr = new ProcessResult(-1, "请求发送失败，请重试！");// 返回默认值
																// 有3种情况：1、网络有问题，导致异常
																// 2、应用缓存，需要先卸载
																// 3、服务器业务有问题（极少）
		SoapObject request = new SoapObject(NAMESPACE, save);
		// 1、参数名需要和服务器的参数名同名
		// 2、这里形参无所谓，para1 para2都行
		// 3、SQL服务器上不区分大小写 @id 可以写成 @Id
		request.addProperty("id", id);
		request.addProperty("userId", userId);
		request.addProperty("userName", userName);
		request.addProperty("fileContent", fileContent);
		request.addProperty("spContent", spContent);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);// 12 不行，11，10 可以
		envelope.dotNet = true;
		envelope.encodingStyle = "UTF-8";
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		HttpTransportSE ht = new HttpTransportSE(URL);

		try {
			String soap_action = NAMESPACE + save;
			ht.call(soap_action, envelope);

			// 解析
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("ProcessSaveResult")).getProperty(1);
			detail = (SoapObject) ((SoapObject) detail
					.getProperty("NewDataSet")).getProperty("ds");
			pr.setResult(Integer.valueOf(detail.getProperty("Result")
					.toString()));
			pr.setMsg(detail.getProperty("Msg").toString());

		} catch (Exception e) {
			// e.printStackTrace();
			System.err.println("getSaveProcessResult ECONNRESET 连接重置");
		}
		return pr;
	}

	public static ProcessResult getSaveAndPassProcessResult(String id,
			String userId, String userName, String fileContent, String spContent) {
		ProcessResult pr = new ProcessResult(-1, "请求发送失败，请重试！");
		SoapObject request = new SoapObject(NAMESPACE, saveAndPass);
		// 1、参数名需要和服务器的参数名同名
		// 2、这里形参无所谓，para1 para2都行
		// 3、SQL服务器上不区分大小写 @id 可以写成 @Id
		request.addProperty("id", id);
		request.addProperty("userId", userId);
		request.addProperty("userName", userName);
		request.addProperty("fileContent", fileContent);
		request.addProperty("spContent", spContent);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);// 12 不行，11，10 可以
		envelope.dotNet = true;
		envelope.encodingStyle = "UTF-8";
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		HttpTransportSE ht = new HttpTransportSE(URL);

		try {
			String soap_action = NAMESPACE + saveAndPass;
			ht.call(soap_action, envelope);

			// 解析
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("ProcessSaveAndPassResult")).getProperty(1);
			detail = (SoapObject) ((SoapObject) detail
					.getProperty("NewDataSet")).getProperty("ds");
			pr.setResult(Integer.valueOf(detail.getProperty("Result")
					.toString()));
			pr.setMsg(detail.getProperty("Msg").toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return pr;
	}

	public static ProcessResult getSaveAndRejectProcessResult(String id,
			String userID, String userName, String fileContent, String spContent) {
		ProcessResult pr = new ProcessResult(-1, "请求发送失败，请重试！");
		SoapObject request = new SoapObject(NAMESPACE, saveAndReject);
		// 1、参数名需要和服务器的参数名同名
		// 2、这里形参无所谓，para1 para2都行
		// 3、SQL服务器上不区分大小写 @id 可以写成 @Id
		request.addProperty("id", id);
		request.addProperty("userId", userID);
		request.addProperty("userName", userName);
		request.addProperty("fileContent", fileContent);
		request.addProperty("spContent", spContent);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);// 12 不行，11，10 可以
		envelope.dotNet = true;
		envelope.encodingStyle = "UTF-8";
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		HttpTransportSE ht = new HttpTransportSE(URL);

		try {
			String soap_action = NAMESPACE + saveAndReject;
			ht.call(soap_action, envelope);

			// 解析
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("ProcessSaveAndRejectResult")).getProperty(1);
			detail = (SoapObject) ((SoapObject) detail
					.getProperty("NewDataSet")).getProperty("ds");
			pr.setResult(Integer.valueOf(detail.getProperty("Result")
					.toString()));
			pr.setMsg(detail.getProperty("Msg").toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return pr;
	}

	public static ProcessResult getSubmitProcessResult(String id,
			String userId, String userName, String jbrUserID,
			String jbrUserName, String selectedValue, String innerSms,
			String phoneSms) {
		ProcessResult pr = new ProcessResult(-1, "请求发送失败，请重试！");
		SoapObject request = new SoapObject(NAMESPACE, submit);
		// 1、参数名需要和服务器的参数名同名
		// 2、这里形参无所谓，para1 para2都行
		// 3、SQL服务器上不区分大小写 @id 可以写成 @Id
		request.addProperty("id", id);
		request.addProperty("userId", userId);
		request.addProperty("userName", userName);
		request.addProperty("jbrUserId", jbrUserID);
		request.addProperty("jbrUserName", jbrUserName);
		request.addProperty("selectedValue", selectedValue);
		request.addProperty("innerSms", innerSms);
		request.addProperty("phoneSms", phoneSms);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);// 12 不行，11，10 可以
		envelope.dotNet = true;
		envelope.encodingStyle = "UTF-8";
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		HttpTransportSE ht = new HttpTransportSE(URL);

		try {
			String soap_action = NAMESPACE + submit;
			ht.call(soap_action, envelope);

			// 解析
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("ProcessSubmitResult")).getProperty(1);
			detail = (SoapObject) ((SoapObject) detail
					.getProperty("NewDataSet")).getProperty("ds");
			pr.setResult(Integer.valueOf(detail.getProperty("Result")
					.toString()));
			pr.setMsg(detail.getProperty("Msg").toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return pr;
	}

	public static ProcessResult getRejectProcessResult(String id,
			String userId, String userName, String jbrUserID,
			String jbrUserName, String selectedValue) {
		ProcessResult pr = new ProcessResult(-1, "请求发送失败，请重试！");
		SoapObject request = new SoapObject(NAMESPACE, reject);
		// 1、参数名需要和服务器的参数名同名
		// 2、这里形参无所谓，para1 para2都行
		// 3、SQL服务器上不区分大小写 @id 可以写成 @Id
		request.addProperty("id", id);
		request.addProperty("userId", userId);
		request.addProperty("userName", userName);
		request.addProperty("jbrUserId", jbrUserID);
		request.addProperty("jbrUserName", jbrUserName);
		request.addProperty("selectedValue", selectedValue);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);// 12 不行，11，10 可以
		envelope.dotNet = true;
		envelope.encodingStyle = "UTF-8";
		envelope.setOutputSoapObject(request);// envelope.bodyOut=request;
		HttpTransportSE ht = new HttpTransportSE(URL);

		try {
			String soap_action = NAMESPACE + reject;
			ht.call(soap_action, envelope);

			// 解析
			SoapObject bodyin = (SoapObject) envelope.bodyIn;
			SoapObject detail = (SoapObject) ((SoapObject) bodyin
					.getProperty("ProcessRejectResult")).getProperty(1);
			detail = (SoapObject) ((SoapObject) detail
					.getProperty("NewDataSet")).getProperty("ds");
			pr.setResult(Integer.valueOf(detail.getProperty("Result")
					.toString()));
			pr.setMsg(detail.getProperty("Msg").toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return pr;
	}

}
