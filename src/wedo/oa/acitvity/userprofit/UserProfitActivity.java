package wedo.oa.acitvity.userprofit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import wedo.oa.acitvity.R;
import wedo.oa.beans.UserProfit;
import wedo.oa.utils.FileUtils;
import wedo.oa.utils.MyUtils;
import wedo.oa.utils.WebServiceUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class UserProfitActivity extends Activity {
	LinearLayout layout_search;
	EditText et_year;
	EditText et_month;
	Button btn_search;
	WebView profit_webview;
	LinearLayout profit_webview_ll;
	boolean isShowing = true;
	Thread UICheckThread;
	boolean UICheck = true;
	int fperiod;
	int fyear;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Toast.makeText(this, "onCreate", 0).show();
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userprofit);
		initWidget();
		btn_search.setOnClickListener(new MyListener());
	}

	private void initWidget() {
		layout_search = (LinearLayout) this.findViewById(R.id.layout_search);
		et_year = (EditText) this.findViewById(R.id.et_yaer);
		et_month = (EditText) this.findViewById(R.id.et_month);
		btn_search = (Button) this.findViewById(R.id.btn_search);
		profit_webview = (WebView) this.findViewById(R.id.profit_webview);
		profit_webview.getSettings().setBuiltInZoomControls(true);
		profit_webview.getSettings().setDefaultTextEncodingName("utf-8");
		profit_webview_ll = (LinearLayout) findViewById(R.id.profit_webview_ll);
	}

	private void initUI(List<UserProfit> userProfits) {
		profit_webview_ll.setVisibility(View.VISIBLE);
		Spanned title = Html.fromHtml("<font color='black'>" + fyear
				+ "</font>" + " 年" + "<font color='black'>" + fperiod
				+ "</font>" + " 月 <b><font color='black'>利润表</font></b>");
		((TextView) findViewById(R.id.title)).setText(title);

		// 构建数据表格
		// 1.构建html字符串(直接显示|先存储后读取显示) 2.javascript html 交互
		// <table border="1" cellpadding="10" cellspacing="0" frame=above>
		// frame属性：控制表格外边框
		// Box：显示所有边框
		// Above：显示上边框
		// Below：显示下边框
		// Lhs：显示左边框
		// Rhs：显示右边框
		// Hsides：显示上下边框
		// Vsides：显示左右边框
		// Void：不显示任何边框
		// --------------------------------------
		// Rules属性：控制表格内边线
		// All：显示所有分隔线
		// Groups：只显示组与组之间的分隔线
		// Rows：只显示行与行之间的分隔线
		// Cols：只显示列与列之间的分隔线
		// None：不显示任何分隔线
		StringBuffer sb = new StringBuffer();
		sb.append("<html><body><table border='1' bordercolor=black cellspacing=0 cellpadding=0 width='100%' ><tr bgcolor='#7FFFD4'><th>项目</th><th>行数</th><th>本月数</th><th>本年累计数</th></tr>");

		for (UserProfit up : userProfits) {
			sb.append("<tr><td>" + up.getIdesc() + "</td><td>" + up.getId()
					+ "</td><td>"
					+ (up.getSumP().equals("-1") ? "" : up.getSumP())
					+ "</td><td>"
					+ (up.getSumY().equals("-1") ? "" : up.getSumY())
					+ "</td></tr>");
		}
		sb.append("</table></body></html>");
		// loadData直接显示,失去表格属性;loadDataWithBaseURL则不会
		profit_webview.loadDataWithBaseURL(null, sb.toString(), null, "utf-8",
				null);

		// String dir = "/mnt/sdcard/buscache/html";//不要硬编码

		// String dir = Environment.getExternalStorageDirectory().getPath()
		// + "/buscache/html";// getPath为/mnt/sdcard最后没有/
		// String filename = et_year.getText() + "_" + et_month.getText()
		// + "_profit.html";
		// MyUtils.saveHTML(dir, filename, sb.toString());
		// String wv_htmlPath = "file://" + dir + "/" + filename;
		// profit_webview.loadUrl(wv_htmlPath);

	}

	private void initUI(String filePath) {
		profit_webview_ll.setVisibility(View.VISIBLE);
		profit_webview.loadUrl(filePath);
		Spanned title = Html.fromHtml("<font color='green'>" + fyear
				+ "</font>" + " 年" + "<font color='green'>" + fperiod
				+ "</font>" + " 月 <b><font color='green'>利润表</font></b>");
		((TextView) findViewById(R.id.title)).setText(title);
	}

	/**
	 * 向上移动视图
	 */
	private void up(View view) {
		int left = view.getLeft();
		int top = view.getTop();
		int right = view.getRight();
		int bottom = view.getBottom();
		int high = bottom - top;
		int change = (int) (high * 0.7f);
		// System.out.println("动画前：" + left + " " + top + " " + right + " "
		// + bottom);
		// layout_search.setVisibility(View.GONE);
		// 先释放空间。测试发现，gone后使得layout失去意义，再次显示会在原位
		view.layout(left, top - change, right, bottom - change);
		// System.out.println("动画后：" + left + " " + (top - change) + " " + right
		// + " " + (bottom - change));
		// layout_search.setVisibility(View.VISIBLE);
	}

	/**
	 * 向下移动视图
	 */
	private void down(View view) {
		int left = view.getLeft();
		int top = view.getTop();
		int right = view.getRight();
		int bottom = view.getBottom();
		int high = bottom - top;
		int change = (int) (high * 0.7f);
		// System.out.println("动画前：" + left + " " + top + " " + right + " "
		// + bottom);
		// layout_search.setVisibility(View.GONE);//
		// 先释放空间。测试发现，gone后使得layout失去意义，再次显示会在原位
		view.layout(left, top + change, right, bottom + change);
		// System.out.println("动画后：" + left + " " + (top + change) + " " + right
		// + " " + (bottom + change));
		// layout_search.setVisibility(View.VISIBLE);
	}

	AnimationListener animListener1 = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			layout_search.clearAnimation();
			// 上下左右的数据只能在这里(up()方法)获取，其它地方获取为0
			up(layout_search);
			btn_search.setText("重新查询");
		}

	};

	AnimationListener animListener2 = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			layout_search.clearAnimation();// 清除动画，避免绘图干扰
			// 上下左右的数据只能这里获取，其它地方获取为0
			down(layout_search);
			btn_search.setText("查询");
		}

	};

	class MyListener implements View.OnClickListener {
		// 记录原始位置 都是0
		// int left = layout_search.getLeft();
		// int top = layout_search.getTop();
		// int right = layout_search.getRight();
		// int bottom = layout_search.getBottom();
		// int high = bottom - top;
		// int change = (int) (high * 0.75f);

		@Override
		public void onClick(View v) {
			if (isShowing) {
				// 检查合法性
				String year = et_year.getText().toString().trim();
				String month = et_month.getText().toString().trim();
				if (!"".equals(year)
						&& !"".equals(month)
						&& Integer.valueOf(month) < 13
						&& Integer.valueOf(year) <= (Calendar.getInstance()
								.get(Calendar.YEAR))) {
					// 隐藏|先播放动画，后更新位置
					Animation animation = new TranslateAnimation(1, 0, 1, 0, 1,
							0, 1, -0.7f);// 向上3/4
					animation.setDuration(500);
					animation.setFillAfter(true);
					animation.setAnimationListener(animListener1);
					layout_search.startAnimation(animation);
					// searching
					new GetDetailsTask().execute();
					// 隐藏键盘
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(UserProfitActivity.this
									.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
				} else {
					Toast.makeText(UserProfitActivity.this, "请填写合理的年份和月份！", 0)
							.show();
					return;
				}

			} else {
				profit_webview_ll.setVisibility(View.INVISIBLE);
				// 显示|先播放动画，后更新位置
				Animation animation = new TranslateAnimation(1, 0, 1, 0, 1, 0,
						1, 0.7f);// 向下3/4
				animation.setDuration(500);
				animation.setFillAfter(true);
				animation.setAnimationListener(animListener2);
				layout_search.startAnimation(animation);
			}
			isShowing = !isShowing;

			// Toast.makeText(UserProfitActivity.this,profit_webview_ll.getTop()+" "+profit_webview_ll.getBottom()+"\n"+layout_search.getTop()+" "+layout_search.getBottom(),
			// 0).show();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Toast.makeText(this, "onStart", 0).show();
	}

	Handler myUIHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			checkUIAgain();
		}

		private void checkUIAgain() {
			// Display display = getWindowManager().getDefaultDisplay();
			// int width = display.getWidth();
			// int height = display.getHeight();
			// Toast.makeText(UserProfitActivity.this, width+" "+height,
			// 0).show();
			if (profit_webview_ll.getVisibility() == View.VISIBLE
					&& layout_search.getTop() > 0) {// 获得上边缘坐标
				checkUI();
				// Toast.makeText(UserProfitActivity.this, "再次更新了UI", 0).show();
			}
		};
	};

	@Override
	protected void onResume() {

		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		// Toast.makeText(this, "onResume", 0).show();
		// Toast.makeText(this, "" + layout_search.getTop(), 0).show();
		checkUI();
		if (UICheckThread == null) {
			UICheckThread = new Thread(new Runnable() {
				@Override
				public void run() {
					UICheck = true;
					while (UICheck) {
						myUIHandler.sendEmptyMessage(0);
						try {
							Thread.sleep(2000);// 2秒更新一次
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			UICheckThread.start();
		}
	}

	public void checkUI() {
		if (profit_webview_ll.getVisibility() == View.VISIBLE) {
			// Toast.makeText(this, "更新UI", 0).show();
			// 隐藏|先播放动画，后更新位置
			Animation animation = new TranslateAnimation(1, 0, 1, 0, 1, 0, 1,
					-0.7f);// 向上3/4
			animation.setDuration(500);
			animation.setFillAfter(true);
			animation.setAnimationListener(animListener1);
			layout_search.startAnimation(animation);
			// 直接更新位置 没用！为啥必须先来个动画再更新位置呢呢？
			// up(layout_search);
		}
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		// Toast.makeText(this, "onRestart", 0).show();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
		// Toast.makeText(this, "onPause", 0).show();
		// UICheckThread.stop();// 切换到其它标签后，强行停掉线程，但是该方法被废弃，不可使用。
		UICheck = false;
		UICheckThread = null;

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// Toast.makeText(this, "onStop", 0).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "onDestroy", 0).show();
		// 删除利润文件
		File profitFile = new File("/mnt/sdcard/buscache/html/profit.html");
		if (profitFile.exists() && profitFile.isFile()) {
			profitFile.delete();
		}
	}

	private class GetDetailsTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog dialog = new ProgressDialog(
				UserProfitActivity.this);
		private List<UserProfit> userProfits;
		View contentView = null;
		String filePath;
		File filecache;

		@Override
		protected void onPreExecute() {
			// this.dialog.setIndeterminateDrawable(getResources().getDrawable(
			// R.anim.loading));
			// this.dialog.setMessage("正在获取数据...");
			// this.dialog.show();
			if (contentView == null) {
				LayoutInflater inflater = getLayoutInflater();
				contentView = inflater.inflate(R.layout.customdialog, null);
			}
			// ImageView iView = (ImageView) contentView
			// .findViewById(R.id.imageView1);
			// AnimationDrawable ad = (AnimationDrawable) iView.getBackground();
			// if (ad != null)
			// ad.start();
			((TextView) contentView.findViewById(R.id.textView1))
					.setText("正在获取数据，请稍后...");
			this.dialog.show();
			this.dialog.setContentView(contentView);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// 其他线程，对于UI线程的控件，可以访问，不可修改。
			fperiod = Integer.valueOf(et_month.getText().toString());
			fyear = Integer.valueOf(et_year.getText().toString());
			filePath = "/mnt/sdcard/buscache/html/" + fyear + "_" + fperiod
					+ "_profit.html";
			filecache = new File(filePath);
			if (!filecache.exists()) {
				userProfits = WebServiceUtils.getProFit(fyear, fperiod);
				int i = 0;
				while (userProfits.size() == 0 && i < MyUtils.RETRY_TIMES) {
					userProfits = WebServiceUtils.getProFit(fyear, fperiod);
					i++;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			this.dialog.dismiss();
			if (!filecache.exists()) {
				// 创建文件并显示
				if (userProfits.size() == 0) {
					Toast.makeText(UserProfitActivity.this, "没有数据！", 0).show();
				} else {
					initUI(userProfits);
				}
			} else {
				initUI("file://" + filePath);
			}

		}

	}

}
