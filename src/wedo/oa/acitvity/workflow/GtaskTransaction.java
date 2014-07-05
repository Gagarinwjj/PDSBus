package wedo.oa.acitvity.workflow;

import java.util.ArrayList;
import java.util.Calendar;

import wedo.oa.acitvity.AppUserInfo;
import wedo.oa.acitvity.R;
import wedo.oa.acitvity.news.NewsDetailActivity;
import wedo.oa.beans.FormFile;
import wedo.oa.beans.FormTypes;
import wedo.oa.beans.ProcessResult;
import wedo.oa.beans.UserForm;
import wedo.oa.utils.MyUtils;
import wedo.oa.utils.WebServiceUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class GtaskTransaction extends Activity {
	WebView wv_info_show;
	TextView tv_newsTitle;
	TextView tv_details;
	EditText suggestionET;
	UserForm userForm;
	int articleId;
	String newFilecontent = "";
	PopupWindow pp_trans;
	String remarks[] = new String[] {};
	ArrayList<FormFile> formFiles = new ArrayList<FormFile>();// 只读表单
	AppUserInfo aui;
	ArrayAdapter<String> adapter;
	String processStep = "";
	ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		initWidget();
		aui = (AppUserInfo) getApplication();
		// 只有为办理的才要处理表单。
		int type = this.getIntent().getIntExtra("type", 0);
		if (type == FormTypes.UNHANDLED) {
			new GetDetailsTask().execute(); 
		} else {// 已办理和已委托直接加载原页面即可。
			newFilecontent = userForm.getFileContent();
			initUI();
		}
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// Activity wedo.oa.acitvity.workflow.GtaskTransaction has leaked window
		// android.widget.PopupWindow$PopupViewContainer{41bdf540 V.E.....
		// ......I. 0,0-480,375} that was originally added here
		// 上诉原因是因为finish时没有及时dismiss掉dialog
		if (pp_trans != null) {
			pp_trans.dismiss();
		}
		// 另外还有其他资源需要释放 如webview
		wv_info_show.setVisibility(View.GONE);
		wv_info_show.destroy();// java.lang.Throwable: Error: WebView.destroy()
								// called while still attached!

		super.onDestroy();
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			wv_info_show.loadUrl("javascript:setDate('"
					+ bundle.getString("dateString") + "','"
					+ bundle.getString("eleId") + "');");
			// wv_info_show.loadUrl("javascript:setDate('2011-11-11','123');");要加上单引号，否则设置失败
		};

	};

	class JavaScriptInterface {
		public void getHTML(String html) {
			// System.out.println("获得网页同步源码");
			newFilecontent = html;
			// newFilecontent = "test to see see";
			// if (processStep.equals("save")) {
			// new ProcessTask().execute("save");
			// } else if (processStep.equals("saveandpass")) {
			// new ProcessTask().execute("saveandpass");
			// } else if (processStep.equals("saveandreject")) {
			// new ProcessTask().execute("saveandreject");
			// }
			new ProcessTask().execute(processStep);
		}

		public void chooseDate(String date, final String eleId) {
			// 验证date是否合法
			String[] dateParts = date.split("-");
			if (dateParts.length != 3) {
				// 要设置2013-11-11 参数为2013-10-11。即设置时月份-1 。取值时月份+1.
				Calendar calendar = Calendar.getInstance();
				date = calendar.get(Calendar.YEAR) + "-"
						+ (calendar.get(Calendar.MONTH) + 1) + "-"
						+ calendar.get(Calendar.DAY_OF_MONTH);
				dateParts = date.split("-");
			}
			// 如果日期不规范，那么dateParts[0]、[1]、[2]不是int，DatePickerDialog初始化失败，模拟器上不显示弹窗被容错，真机崩溃
			// Uncatched Exception from java call。
			new DatePickerDialog(GtaskTransaction.this,
					DatePickerDialog.THEME_DEVICE_DEFAULT_LIGHT,
					new OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							// Toast.makeText(MainActivity.this,
							// year + "-" + monthOfYear + "-" + dayOfMonth, 0)
							// .show();
							String dateString = year
									+ "-"
									+ ((monthOfYear + 1) < 10 ? "0"
											+ (monthOfYear + 1)
											: (monthOfYear + 1))
									+ "-"
									+ (dayOfMonth < 10 ? "0" + dayOfMonth
											: dayOfMonth);
							// 不支持非UI线程调用
							// wv.loadUrl("javascript:setDate(" + dateString +
							// "," + eleId
							// + ")");
							Message msg = new Message();

							Bundle bundle = new Bundle();
							bundle.putString("dateString", dateString);
							bundle.putString("eleId", eleId);

							msg.setData(bundle);
							handler.sendMessage(msg);

						}
					}, Integer.parseInt(dateParts[0]),
					Integer.parseInt(dateParts[1]) - 1,
					Integer.parseInt(dateParts[2])).show();

		}
	}

	/**
	 * 根据状态 初始化 办理逻辑
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void initWidget() {
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		wv_info_show = ((WebView) findViewById(R.id.news_webview));
		WebSettings ws = wv_info_show.getSettings();
		// 设置可以支持缩放
		ws.setSupportZoom(true);
		// 设置出现缩放工具
		ws.setBuiltInZoomControls(true);

		// 动态设置默认缩放方式尺寸
		// •FAR makes 100% looking like in 240dpi 缩放比例小
		// •MEDIUM makes 100% looking like in 160dpi 缩放比例中
		// •CLOSE makes 100% looking like in 120dpi 缩放比例大

		int screenDensity = getResources().getDisplayMetrics().densityDpi;
		// CustomToast.show(GtaskTransaction.this, screenDensity + "");//显示320
		WebSettings.ZoomDensity zoomDensity = null;
		switch (screenDensity) {
		case DisplayMetrics.DENSITY_LOW:
			zoomDensity = WebSettings.ZoomDensity.CLOSE;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			zoomDensity = WebSettings.ZoomDensity.MEDIUM;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			zoomDensity = WebSettings.ZoomDensity.FAR;
			break;
		default:// 一定要有默认值，否则，如果不匹配 120 160 240 那么 zoomDensity=null，空指针异常
			zoomDensity = WebSettings.ZoomDensity.FAR;
		}
		ws.setDefaultZoom(zoomDensity);// 如果不设置，本机测试 缩放比例比far 大一点，比medium 小一点

		// 让网页自适应
		// ws.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);//占满屏幕宽度，1列
		// ws.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
		// ws.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);

		// ws.setUseWideViewPort(true);// 使用广视图,默认为false。
		// 如果使用广视图 并且 用CLOSE即缩放比例大时 ,填写文本值时会较容易让页面除焦点控件外 白屏
		// 其他情况也能出现，只不过这种情况下发生几率高。

		ws.setDefaultTextEncodingName("utf-8");
		ws.setJavaScriptEnabled(true);
		wv_info_show.addJavascriptInterface(new JavaScriptInterface(),
				"aliasInHtml");
		wv_info_show.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				// Toast.makeText(GtaskTransaction.this, "页面加载完成",
				// Toast.LENGTH_LONG).show();
				// wv_info_show.loadUrl("javascript:removeReadonly()");// 去除只读属性
				if (progressBar.getVisibility() == View.VISIBLE) {
					progressBar.setVisibility(View.GONE);
				}
			};

			String flag = null;

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				if (flag == null) {
					view.loadUrl("file:///android_asset/404.html");
					flag = "";
				} else {
					flag = null;
					view.goBack();// 返回到空白页
				}
			}
		});

		wv_info_show.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// TODO Auto-generated method stub
				super.onProgressChanged(view, newProgress);
				// System.out.println("加载进度："+newProgress);
				if (newProgress == 100) {
					progressBar.setVisibility(View.GONE);
				} else {
					if (progressBar.getVisibility() == View.GONE) {
						progressBar.setVisibility(View.VISIBLE);
					}
					progressBar.setProgress(newProgress);
				}

			}

		});
		tv_newsTitle = (TextView) findViewById(R.id.news_detail_title);
		tv_details = (TextView) findViewById(R.id.news_detail_ds);

		((Button) findViewById(R.id.titlebar_left))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// canGoBack判断不准确，if判断来弥补
						if (wv_info_show.getUrl().equals("about:blank")) { // 起始页返回
							GtaskTransaction.this.finish();
							return;// 以下语句均忽略
						}
						if (wv_info_show.canGoBack()) {
							wv_info_show.goBack();
							// 延迟200ms后发起特殊判断
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									if (wv_info_show.getUrl().equals(
											"about:blank"))
										wv_info_show.loadDataWithBaseURL(
												MyUtils.APP_URL,
												MyUtils.createJSHtml(newFilecontent),
												null, "utf-8", null);
									wv_info_show.clearHistory();// 必须清空，让canGoBack=false，否则死循环。实际不准确
								}
							}, 200);
						} else {
							GtaskTransaction.this.finish();
						}
					}
				});
		userForm = (UserForm) this.getIntent().getSerializableExtra("detail");

		Button transaction = ((Button) findViewById(R.id.workButton));
		// 判断处理类型 未办理、已办理、已委托
		int type = this.getIntent().getIntExtra("type", 0);
		if (type == FormTypes.UNHANDLED) {
			((TextView) findViewById(R.id.header_title)).setText("工作办理");
			transaction.setVisibility(View.VISIBLE);
			transaction.setText("办理");

			transaction.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// 不存在，则初始化；存在，则忽略
					if (pp_trans == null) {
						View transView = getLayoutInflater().inflate(
								R.layout.trans, null);
						initTransView(transView);// 初始化事件处理逻辑
						pp_trans = new PopupWindow(
								transView,
								android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
								android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
						pp_trans.setAnimationStyle(R.style.AnimationFade);
						pp_trans.setFocusable(true);
						// 点击外部消失,并且解决了2次触发问题
						// 即click后：1、外部点击关闭pop；click事件又显示pop。显示->关闭->显示，即关不掉。
						pp_trans.setBackgroundDrawable(new BitmapDrawable());
						pp_trans.setOutsideTouchable(true);
					}
					// 显示则关闭、关闭则显示
					if (pp_trans.isShowing()) {
						pp_trans.dismiss();
					} else {
						pp_trans.showAtLocation(findViewById(R.id.details),
								Gravity.CENTER, 0, 0);
					}
				}
			});

			// (?i)大小写不铭感 newFilecontent会同步到服务器，所以修改要留意了
			// html_1、过滤杂质
			newFilecontent = userForm
					.getFileContent()
					.replaceAll("(?i)readonly=\"\"", "")
					// 先替换复杂的
					.replaceAll("(?i)readonly", "")
					// 再替换简单的，顺序不能变
					.replaceAll("(?i)BACKGROUND-COLOR: #EFEFEF(;)?", "")
					.replaceAll("style=\"\"", "")
					.replaceAll("class=\"wjjbg\"", "")
					.replaceAll("\\s(=\"\")+", "")// 去除如 <textarea
													// id="TextArea1"
													// =""="" name="201">中的
													// =""=""
					// .replaceAll("(?i)src=\".*?/userfiles",
					// "src=\"userfiles");
					// 将服务器印章路径改为本地能识别的路径，即
					// 删除[]中的内容?表示慵懒模式。（删除相对路径"/"打头，则下面保存时需要还原"/"打头，以保证PC浏览器正常显示，多此一举）
					// src="[http://localhost/WebUI/]userfiles、
					// src="[/WebUI/]userfiles、
					// src="[/]userfiles
					// src="[]userfiles //src="userfiles 替换成
					// src="userfiles，自己替换自己，用.+而非.* 可以避免

					.replaceAll("(?i)src=\".+?/WebUI/userfiles",
							"src=\"/WebUI/userfiles");
			// 将服务器错误印章路径修复，即 删除[]中的内容?表示慵懒模式。（没有删除相对路径"/"打头）
			// src="[http://localhost]/WebUI/userfiles

			// 权限控制第一步：去除原来的只读限制、背景色
		} else if (type == FormTypes.HANDLED) {
			((TextView) findViewById(R.id.header_title)).setText("已办查看");
		} else if (type == FormTypes.DELEGATED) {
			((TextView) findViewById(R.id.header_title)).setText("委托查看");
		}
	}

	/**
	 * 处理逻辑: 点击时，初始化，同意或者驳回 (业务逻辑不应该出现在客户端！！！)
	 * 
	 * @param transView
	 */
	private void initTransView(View transView) {
		// 1、事务逻辑的绑定
		Spinner comments = ((Spinner) transView.findViewById(R.id.comments));
		suggestionET = ((EditText) transView.findViewById(R.id.suggestion));
		suggestionET.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 解决软键盘不弹出的问题
				suggestionET.requestFocus();
				InputMethodManager imm = (InputMethodManager) GtaskTransaction.this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				// imm.showSoftInput(suggestionET,
				// InputMethodManager.SHOW_FORCED);
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		});
		if (adapter != null) {
			comments.setAdapter(adapter);// 会自动绑定第一个
			comments.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// 重复选择同一个，自动忽略
					// System.out.println("选择了"+position);
					String original = suggestionET.getText().toString();
					suggestionET.setText(original + remarks[position] + "\n");
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});

		}
		// 保存
		((Button) transView.findViewById(R.id.agree))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// System.out.println("agree");
						processStep = "save";
						wv_info_show.loadUrl("javascript:JSHtml();");// 异步执行?

						// System.out.println("立即显示:" + newFilecontent);
						// new Handler().postDelayed(new Runnable() {
						//
						// @Override
						// public void run() {
						// // TODO Auto-generated method stub
						// System.out.println("延迟显示:" + newFilecontent);
						// }
						// }, 200);
					}
				});
		// 保存并通过
		((Button) transView.findViewById(R.id.agreeandpass))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						processStep = "saveandpass";
						wv_info_show.loadUrl("javascript:JSHtml();");

					}
				});
		// 保存并驳回
		((Button) transView.findViewById(R.id.disagree))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						processStep = "saveandreject";
						wv_info_show.loadUrl("javascript:JSHtml();");

					}
				});

		// 2、按钮的显示
		int m = userForm.getJBRObjectId().length();
		int n = userForm.getJBRObjectId().replaceAll(",", "").length();
		int k = m - n;
		if (k <= 1) {// 还剩一个人处理 保存，通过，驳回
			((Button) transView.findViewById(R.id.agree))
					.setVisibility(View.VISIBLE);// gone
			((Button) transView.findViewById(R.id.agreeandpass))
					.setVisibility(View.VISIBLE);
			((Button) transView.findViewById(R.id.disagree))
					.setVisibility(View.VISIBLE);
		} else {// 还剩多人处理
			if (userForm.getSpModle().equals("只要有一人通过审批即可向下流转")) {
				// 通过可见
				((Button) transView.findViewById(R.id.agreeandpass))
						.setVisibility(View.VISIBLE);
			} else {
				// 通过不可见。
				((Button) transView.findViewById(R.id.agreeandpass))
						.setVisibility(View.GONE);
			}
			// 只有 保存，驳回。 最后一人转到 k<=1
			((Button) transView.findViewById(R.id.agree))
					.setVisibility(View.VISIBLE);
			((Button) transView.findViewById(R.id.disagree))
					.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 数据展示
	 */
	private void initUI() {
		// 每次都会从网络获取而不缓存起来，是为了获得"点击数"的最新值
		tv_newsTitle.setText(userForm.getName());
		String comString = "发起人: " + userForm.getFqUserName() + " 发起时间: "
				+ MyUtils.formatDate(userForm.getNowTimes());
		tv_details.setText(comString);

		// 1、从文件加载页面
		// String sdcard = Environment.getExternalStorageDirectory().getPath();
		// String dir = sdcard + "/buscache/html";
		// String filename = userForm.getSequence() + ".html";
		// MyUtils.saveHTML(dir, filename,
		// MyUtils.createJSHtml(newFilecontent));// 保存便于观察测试

		// String wv_htmlPath = "file://" + dir + "/" + filename;//
		// file:///mnt/buscache/html/xxx.html
		// wv_info_show.loadUrl(wv_htmlPath);

		// 2、从字符串加载页面 空白页面
		// wv_info_show.loadData(MyUtils.createJSHtml(newFilecontent), null,
		// "utf-8");

		// 3、从字符串加载页面。 还解决了 ：点击某个控件， 其他区域出现空白的情况
		wv_info_show.loadDataWithBaseURL(MyUtils.APP_URL,
				MyUtils.createJSHtml(newFilecontent), null, "utf-8", null);

		// wv_info_show.loadUrl("javascript:removeReadonly()");//去除只读属性,调用太早了

	}

	/**
	 * @author Administrator 获取常用审批批注和只读表单，加入只读属性，最终 显示内容。
	 */
	private class GetDetailsTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog dialog = new ProgressDialog(
				GtaskTransaction.this);
		View contentView = null;

		@Override
		protected void onPreExecute() {
			if (contentView == null) {
				LayoutInflater inflater = getLayoutInflater();
				contentView = inflater.inflate(R.layout.customdialog, null);
			}

			((TextView) contentView.findViewById(R.id.textView1))
					.setText("正在获取数据，请稍后...");
			this.dialog.show();
			this.dialog.setContentView(contentView);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// 为了避免remarks返回的值为null，接口方法初始化时赋值{}
			remarks = WebServiceUtils.getRemarks(Integer.parseInt(aui
					.getUserID()));
			formFiles.clear();
			formFiles.addAll((ArrayList<FormFile>) WebServiceUtils.execSql(
					"getReadOnly @UpNodeId=" + userForm.getUpNodeId(),
					FormFile.class));
			// System.out.println("UpNodeId确保了：初始加载，只读表单少；保存后加载，只读表单多。本次加载，只读表单有"+
			// formFiles.size() + "个：\n");//实测发现，限制只读的一样多，但是初始加载 只读为什么没生效？
			// 原因在于替换表达式的双引号。
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			this.dialog.dismiss();
			if (remarks.length == 0) {
				remarks = new String[] { "同意", "不同意" };
			}
			// 及时初始化，避免空指针,remarks 要初始化，否则也有空指针
			adapter = new ArrayAdapter<String>(GtaskTransaction.this,
					R.layout.spinner_item, remarks);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// html_2、加入只读
			if (formFiles.size() > 0) {
				// 权限控制第二步：加入只读限制
				for (FormFile ff : formFiles) {
					// System.out.println("GTaskTransaction 添加只读属性:"
					// + ff.getFileNumber());
					// 测试发现，后面的style对前面style的作用：存在则替换（覆盖），不存在则补充
					/*
					 * 以上测试不充分 多个style测试结果：</br>
					 * 高速模式下(火狐、谷歌、Safari、android手机),采用第一个style 忽略第二个style</br>
					 * 兼容模式下（IE），第二个style是对第一个的补充，已经指定的无法替换
					 * VS2012查看模式，第二个style是对第一个的补充和替换
					 * 
					 * 解决办法 如果使用样式，会对style起到一个补充的效果，但是权限不够，无法替换
					 * 对于高速而言，加载第一个style
					 * ,样式bg起到补充作用，高速下效果满足。（先去掉第一个style中的样式属性，这里才能起到补充作用）
					 * 对于兼容而言，加载第一个和第二个style，样式bg虽然加载，但是无法替换，IE下效果满足。
					 * 
					 * 对比服务器端： 低版本IE上可以显示只读颜色，高版本IE和其他高速浏览器则不可以显示只读颜色
					 * 以上解决办法可以兼容所有浏览器。 在服务器端加入 wjjbg样式也可以解决兼容问题。
					 */
					// 初始 字符串中是 name=12345 无法替换 。加载后（高速模式下）， name="12345" 保存至服务器
					// 字符串中是 name="12345" 。再次取值时， 可以替换。
					newFilecontent = newFilecontent
							.replace(
									"name=\"" + ff.getFileNumber() + "\"",
									"name=\""
											+ ff.getFileNumber()
											+ "\" style=\"BACKGROUND-COLOR: #EFEFEF\" readonly=\"\" class=\"wjjbg\"");
				}

			}

			initUI();
		}
	}

	private class ProcessTask extends AsyncTask<String, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(
				GtaskTransaction.this);
		View contentView = null;
		ProcessResult pr;
		String how = "";

		@Override
		protected void onPreExecute() {
			if (contentView == null) {
				LayoutInflater inflater = getLayoutInflater();
				contentView = inflater.inflate(R.layout.customdialog, null);
			}
			((TextView) contentView.findViewById(R.id.textView1))
					.setText("正在处理数据，请稍后...");
			this.dialog.show();
			this.dialog.setContentView(contentView);

		}

		@Override
		protected Void doInBackground(String... params) {
			how = params[0];
			// 经过测试发现如下规律：
			// 手机图片路径 uploadfiles/
			// 本地服务器图片路径 /pds/uploadfiles/
			// pds服务器图片路径/webui/uploadfiles/

			// 将本地印章路径改为服务器能识别的路径 。事实上，服务器端存在2个印章链接
			// http://info.pdsgj.com/userfiles/image/image/yz.gif @1
			// http://info.pdsgj.com/WebUI/userfiles/image/image/yz.jpg @2

			// 对@2进行恢复，会把@1的所有路径变成类似的http://info.pdsgj.com/webui/userfiles/image/image/yz.gif
			// 然而服务器没有这张图片。则照成不显示。

			// 远程项目 以前测试认为 域名后要加"/" 相对路径前不加"/" 最新测试发现
			// 域名后的"/"可有可无 相对路径前的"/"可有可无
			// 服务器上的相对路径都必须以"/"打头 所以 WebUI/userfiles/image/image/yz.jpg
			// 在浏览器中无法识别图片;/WebUI/userfiles/image/image/yz.jpg 才能识别。
			// 所以客户端在处理时 不可以把相对路径中打头的"/"给去掉，因为虽然对客户端没影响 但是服务器端就不识别了。
			// 所以，服务器端加载过来的数据不要变，指定个域名就可以识别。
			// 所以最好的方法是
			// 1、指定域名，不对链接处理。这样以"/"打头的相对路径在PC端和客户端浏览器均能正常显示。如果不显示，说明图片丢了。
			// 2、对链接处理，对于@1造成的影响，可以在对应的路径http://info.pdsgj.com/webui/userfiles/image/image/yz.gif下
			// 提供一张 yz.gif图片
			// 将src="userfiles 还原成 src="/WebUI/userfiles (多此一举的后果)
			// String webNewFileContent = newFilecontent.replaceAll(
			// "(?i)src=\"userfiles", "src=\"" + MyUtils.APP_IMG_URL
			// + "userfiles");
			if (how.equals("save")) {
				pr = WebServiceUtils.getSaveProcessResult(userForm.getId(),
						aui.getUserID(), aui.getUserName(), newFilecontent,
						suggestionET.getText().toString());
			} else if (how.equals("saveandpass")) {
				pr = WebServiceUtils.getSaveAndPassProcessResult(
						userForm.getId(), aui.getUserID(), aui.getUserName(),
						newFilecontent, suggestionET.getText().toString());

			} else if (how.equals("saveandreject")) {
				pr = WebServiceUtils.getSaveAndRejectProcessResult(
						userForm.getId(), aui.getUserID(), aui.getUserName(),
						newFilecontent, suggestionET.getText().toString());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			this.dialog.dismiss();
			if (pr != null) {
				Toast.makeText(GtaskTransaction.this, pr.getMsg(),
						Toast.LENGTH_SHORT).show();

				if (how.equals("save")) {
					showSaveResult(pr);
				} else if (how.equals("saveandpass")) {
					showSaveAndPassResult(pr);
				} else if (how.equals("saveandreject")) {
					showSaveAndRejectResult(pr);
				}
			}

		}
	}

	public void showSaveResult(ProcessResult pr) {
		// Toast.makeText(this, pr.getMsg(), Toast.LENGTH_SHORT).show();
		if (pr.getResult() == 1) {
			// 清理处理框，否则报错 leak....
			if (pp_trans != null && pp_trans.isShowing()) {
				pp_trans.dismiss();
				pp_trans = null;
			}
			this.finish();
		}

	}

	public void showSaveAndPassResult(ProcessResult pr) {
		if (pr.getResult() == 1) {
			startActivity(new Intent(GtaskTransaction.this, GtaskSpNext.class)
					.putExtra("detail", userForm));
		}
	}

	public void showSaveAndRejectResult(ProcessResult pr) {
		if (pr.getResult() == 1) {
			startActivity(new Intent(GtaskTransaction.this, GtaskSpBhNext.class)
					.putExtra("detail", userForm));
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && wv_info_show.canGoBack()) {
			if (wv_info_show.getUrl().equals("about:blank")) { // 起始页返回
				GtaskTransaction.this.finish();
				return true;// 以下语句均忽略
			}
			wv_info_show.goBack();
			// 延迟200ms后发起特殊判断
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (wv_info_show.getUrl().equals("about:blank"))
						wv_info_show.loadDataWithBaseURL(MyUtils.APP_URL,
								MyUtils.createJSHtml(newFilecontent), null,
								"utf-8", null);
					wv_info_show.clearHistory();// 必须清空，让canGoBack=false，否则死循环。实际不准确
				}
			}, 200);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
			// GtaskTransaction.this.finish();
			// return true;
		}
	}
}
