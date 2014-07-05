package wedo.oa.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ksoap2.serialization.SoapObject;

import wedo.oa.beans.People;
import android.content.Context;
import android.net.ConnectivityManager;
import android.view.View;
import android.widget.RadioGroup;

public class MyUtils {
	public static final int PAGE_SIZE = 10;
	public static final int RETRY_TIMES = 5;

	public static final String APP_IMG_URL = "/WebUI/";// 平顶山的发布页（印章路径2，不得不说被这个误导了，还以为发布映射为WebUI/呢，只是服务器的另外一个路径而已），确保平顶山web页面显示图片
	// public static String APP_IMG_URL = "/pds/";// 本地的发布页（印章路径）,确保本地web页面显示图片
	/**
	 * 最后有/的完整域名
	 * <p>
	 * loadDataWithBaseURL中<br/>
	 * baseUrl：形如 http://192.168.0.23/PDS/ ,最后有"/" <br/>
	 * 相对Url：形如 ewebeditor/uploadfiles/xx.jpg ,开头没有"/"
	 * <p>
	 * 以上测试不对 <br/>
	 * baseUrl： Phone端是否"/"结尾无所谓 <br/>
	 * 相对路径Url： "/"打头 PC端必须，客户端无所谓，为了兼容PC和Phone端，必须"/"打头
	 * */
	public static final String APP_URL = "http://info.pdsgj.com/";// 平顶山发布域名
	// 不是http://info.pdsgj.com/WebUI/
	// public static String APP_URL = "http://192.168.0.23/PDS/";// 本地发布域名

	/** 去掉最后/的域名 */
	public static final String APP_URL_LESS = "http://info.pdsgj.com";
	// public static String APP_URL_LESS = "http://192.168.0.23/PDS";

	public static final String IMG_PATH_PATTERN = "/ewebeditor";

	public static String formatDate(String date) {
		// 2012-04-21T16:09:54.6+08:00
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// return sdf.format(new Date(date)).toString();
		try {
			return date.substring(0, 10) + " " + date.substring(11, 19);
		} catch (Exception e) {
			e.printStackTrace();
			return date;
		}
	}

	/**
	 * @param clazz
	 * @param item
	 * @return 
	 *         clazz的实例对象(注意，使用该方法一定要注意bean首字母大写的命名规范:Abc(如果不大写，则需要提供一个将首字母转为大写的方法
	 *         ), getAbc(), setAbc())
	 */
	public static Object fillDataByReflect(Class<?> clazz, SoapObject item) {
		Field[] fields = clazz.getDeclaredFields();
		Object obj = null;
		try {
			obj = clazz.newInstance();// clazz需要无参构造函数
		} catch (Exception e) {
			System.err.println("非常严重！！！clazz需要无参构造函数");
			e.printStackTrace();
		}
		for (int i = 0; i < fields.length; i++) {
			// 逐一对每个字段赋值，捕获异常，不干扰
			String fieldName = fields[i].getName();
			try {
				// 通过字段赋值，规范为：bean字段和服务器返回的字段大小写一致
				// fields[i].setAccessible(true);//打开private访问权限
				// fields[i].set(obj,
				// item.getProperty(fieldName).toString().trim());

				// 通过set方法赋值，规范为：bean首字母大写,方便些。否则首字母要UP为大写字母
				if (fields[i].getType() == int.class) {
					Method method = clazz.getDeclaredMethod("set" + fieldName,
							int.class);
					method.invoke(
							obj,
							Integer.valueOf(item.getProperty(fieldName)
									.toString().trim()));

				} else if (fields[i].getType() == String.class) {
					Method method = clazz.getDeclaredMethod("set" + fieldName,
							String.class);
					method.invoke(obj, item.getProperty(fieldName).toString());
				}
			} catch (Exception e) {
				// e.printStackTrace();
				System.out.println(fieldName + "不存在于 SoapObject 中");
			}
		}
		return obj;
	}

	public static String checkString(String str) {
		if (str == null || str.equals("anyType{}")) {
			return "";
		}
		return str;
	}

	/**
	 * 通过正则表达式 解决图片显示问题 修改路径为绝对路径：Less域名+地址 "http://192.168.0.23/PDS" +
	 * "/ewebeditor/..."
	 * <p>
	 * 另外replaceAll 也很好用
	 * </p>
	 * <p>
	 * 注意，这里可以在客户端这么做，不要保存修改后的数据到服务器
	 * </p>
	 * 
	 * @param content
	 * @return
	 */
	public static String modifyImgAsFullUrls(String content) {
		// 1、处理 /ewebeditor 为全路径 http://192.168.0.23/PDS + /ewebeditor
		Pattern pattern = Pattern.compile(IMG_PATH_PATTERN);
		Matcher m = pattern.matcher(content);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			// System.out.println(m.group());
			m.appendReplacement(sb, APP_URL_LESS + m.group());// Less域名(http://192.168.0.23/PDS)+地址(/ewebeditor)
		}
		m.appendTail(sb);
		String finalStr = sb.toString();
		// 2、进一步处理 (http://localhost)?/WebUI 为路径 http://192.168.0.23/PDS 。不得不说
		// /webui/ 对我照成了误导。。。。
		finalStr = finalStr.replaceAll("(?i)(http://localhost)?/WebUI",
				APP_URL_LESS);
		// 3、其他路径就不处理成全路径了，反正相对路径也能用
		return finalStr;
	}

	/**
	 * @param dir
	 *            目录
	 * @param filename
	 *            文件名
	 * @param newFilecontent
	 *            文件内容
	 */
	public static void saveHTML(String dir, String filename,
			String newFilecontent) {
		// 检查缓存目录
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			// dirFile.mkdir();//在已存在目录下，只能创建一层目录。 已存：/mnt/sdcard
			// 可以创建：/mnt/sdcard/xx
			dirFile.mkdirs();// 在已存在目录下，可以创建多层目录。已存：/mnt/sdcard 可以创建：/mnt
								// /sdcard/xx/xx/xx...
		}
		if (!FileUtils.checkDirSize(dirFile)) {
			// 已知里面都是html文件，可以简洁写
			for (File file : dirFile.listFiles()) {
				if (file.isFile()) {
					file.delete();
				}
			}
		}

		File file = new File(dir, filename);
		// 如果文件不存在，则新建并存储,如果存在，则跳过。
		// -->文件存在，删除
		if (file.exists())
			file.delete();
		try {
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			fw.write(newFilecontent);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * 判断网络是否有效
	 * 
	 * @param context
	 *            上下文对象
	 * @return boolean -- TRUE 有效 -- FALSE 无效
	 * 
	 */
	public static boolean isNetworkAvailable(Context context) {
		boolean flag = false;
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (mConnectivityManager.getActiveNetworkInfo() != null) {
			flag = mConnectivityManager.getActiveNetworkInfo().isAvailable();
		}
		return flag;
	}

	public static int getVisibleNumInRadioGroup(RadioGroup rg) {
		int visableNum = 0;
		int all = rg.getChildCount();
		for (int i = 0; i < all; i++) {
			if (rg.getChildAt(i).getVisibility() == View.VISIBLE) {
				visableNum++;
			}
		}
		return visableNum;
	}

	/**
	 * @param html
	 * @return 拼接了JS代码的HTML 该段JS作用是同步TextArea和Input值
	 */
	public static String createJSHtml(String html) {
		return "<html><head><style>.wjjbg { background-color: #EFEFEF }</style><script type='text/javascript'>"
				// 移除只读属性
				+ "function removeReadonly() { "
				+ "  var tas= document.getElementsByTagName('TextArea');"
				+ "    for (var i=0;i<tas.length;i++ )"
				+ "    {tas[i].removeAttribute('readonly');} "

				+ "  var is= document.getElementsByTagName('Input');"
				+ "    for (var j=0;j<is.length;j++ )"
				+ "   { is[j].removeAttribute('readonly');}"
				+ "}"
				// 服务器脚本 本地化
				+ "function DateSelect(eleId) {"
				+ "    var dateChoose = document.getElementById(eleId);"
				+ "     javascript:window.aliasInHtml.chooseDate(dateChoose.value,dateChoose.id);"
				+ "  }"
				// 设置时间
				+ "  function setDate(dateStr,eleId){"
				+ "    var dateChoose = document.getElementById(eleId);"
				+ "     dateChoose.setAttribute('value',dateStr);"
				+ " }"
				// 同步脚本
				+ "function JSHtml() {"
				// TextArea 同步值
				+ "var elsTA= document.getElementsByTagName('TextArea');"
				+ "for (var i=0;i<elsTA.length;i++ )  "
				+ "{elsTA[i].innerHTML=elsTA[i].value;}"
				// Input 同步值
				+ "var elsInput= document.getElementsByTagName('Input');"
				+ "for (var i=0;i<elsInput.length;i++ )  "
				+ "{elsInput[i].setAttribute('value',elsInput[i].value);}"
				// select 同步值
				+ "var selects = document.getElementsByTagName('select');"
				+ " for (var s = 0; s < selects.length; s++) {"
				+ "  var selectItem = selects[s];"
				+ " for (var k = 0; k < selectItem.options.length; k++) {"
				+ "     if (selectItem.options[k].selected) {"
				+ "         selectItem.options[k].setAttribute('selected', '');"
				+ "     } else {"
				+ "         selectItem.options[k].removeAttribute('selected');"
				+ "     }"
				+ "   }"
				+ "}"
				// +"alert(document.getElementsByTagName('wjj')[0].innerHTML);"
				+ " javascript:window.aliasInHtml.getHTML(document.getElementsByTagName('wjj')[0].innerHTML);}</script></head><body><wjj>"
				+ html + "</wjj></body> </html> ";
	}

	public static ArrayList<People> getPeoples(String namesStr, String idsStr) {

		ArrayList<People> peoples = new ArrayList<People>();
		try {
			String[] names = namesStr.split(",");
			String[] ids = idsStr.split(",");
			if (names.length == ids.length) {
				for (int i = 0; i < names.length; i++) {
					peoples.add(new People(ids[i], names[i]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return peoples;

	}

	/**
	 * @param peoples
	 * @return [0]= 1,2,3, [1]=a,b,c,
	 */
	public static String[] getIdsAndNames(ArrayList<People> peoples) {

		String ids = "";
		String names = "";
		if (peoples == null)
			return new String[] { ids, names };

		for (People p : peoples) {
			ids += p.getUserID() + ",";
			names += p.getUserName() + ",";
		}

		return new String[] { ids, names };

	}
}
