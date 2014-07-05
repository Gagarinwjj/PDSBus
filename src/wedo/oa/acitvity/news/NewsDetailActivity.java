package wedo.oa.acitvity.news;

import java.lang.ref.WeakReference;

import wedo.oa.acitvity.R;
import wedo.oa.beans.NewsDetails;
import wedo.oa.utils.MyUtils;
import wedo.oa.utils.WebServiceUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class NewsDetailActivity extends Activity {
	WebView wv_info_show;
	TextView tv_newsTitle;
	TextView tv_details;
	NewsDetails newsDetails;
	ProgressBar progressBar;
	int articleId;
	LinearLayout root;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		initWidget();
		new GetDetailsTask().execute();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	protected void onDestroy() {
		// Activity wedo.oa.acitvity.news.NewsDetailActivity has leaked window
		// android.widget.ZoomButtonsController$Container@421e0e08 that was
		// originally added here
		// 原因是: WebView中包含一个ZoomButtonsController，
		// 当使用web.getSettings().setBuiltInZoomControls(true);
		// 启用后，用户一旦触摸屏幕，就会出现缩放控制图标。
		// 这个图标过上几秒会自动消失，但在3.X系统上，如果图标自动消失前退出当前Activity的话，
		// 就会报上面的这些异常。2.X上没有这种情况
		wv_info_show.removeAllViews();// 这种方式更好
		// wv_info_show.setVisibility(View.GONE);// 立即消失。报错channel '41e52dd8
		// Panel:wedo.oa.acitvity/wedo.oa.acitvity.news.NewsDetailActivity
		// (client)' ~ Publisher closed
		// input channel or an error
		// occurred. events=0x8
		// 虽然不会崩溃，但会闪屏。如果缩放图标 没有显示 或 消失了 则没有报错。

		// wv_info_show.getZoomControls().setVisibility(View.GONE);//空指针异常
		// 延迟消失
		// long timeout = ViewConfiguration.getZoomControlsTimeout();
		// new Timer().schedule(new TimerTask() {
		// @Override
		// public void run() {
		// wv_info_show.destroy();
		// }
		// }, timeout);
		// }

		// Activity wedo.oa.acitvity.news.NewsDetailActivity has leaked
		// IntentReceiver
		// com.android.qualcomm.browsermanagement.BrowserManagement$1@4219b9e8
		// that was originally registered here. Are you missing a call to
		// unregisterReceiver()?
		// 当发生这种异常时，很有可能是Activity使用了WebView，而退出Activity时没有把WebView的对象销毁。
		// 正确做法是：在Activity中OnDestory()方法中调用WebView.destory()。这样可以避免此类异常的抛出。
		root.removeAllViews();
		wv_info_show.destroy();// 如果一加载就立即退出，导致未附加成功就销毁，报错 Error:
								// WebView.destroy() called while still
								// attached! 所以需要 先root.removeAllViews();

		super.onDestroy();
	};

	// HandlerLeak警告处理方法 MyHandler持有外部类的弱引用
	Handler handler = new MyHandler(this);

	static class MyHandler extends Handler {
		WeakReference<NewsDetailActivity> activityReference;

		public MyHandler(NewsDetailActivity activity) {
			activityReference = new WeakReference<NewsDetailActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			NewsDetailActivity activity = activityReference.get();
			if (activity != null)
				activity.wv_info_show.getSettings().setLayoutAlgorithm(
						LayoutAlgorithm.NORMAL);
		}
	}

	private void initWidget() {

		wv_info_show = ((WebView) findViewById(R.id.news_webview));
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		root = (LinearLayout) this.findViewById(R.id.details);
		WebSettings ws = wv_info_show.getSettings();
		// 设置可以支持缩放
		ws.setSupportZoom(true);
		// 设置出现缩放工具 ，如果不设置，则没有缩放功能，会闪屏
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
		ws.setDefaultZoom(zoomDensity);

		ws.setDefaultTextEncodingName("utf-8");
		// 开启下载功能
		wv_info_show.setDownloadListener(new DownloadListener() {

			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		wv_info_show.setWebViewClient(new WebViewClient() {

			String flag = null;

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				if (progressBar.getVisibility() == View.VISIBLE) {
					progressBar.setVisibility(View.GONE);
				}
			}

			// 正常访问页-->错误页（null处理；需要重置为"",避免死循环）-->404
			// 正常访问页<--错误页（""处理；需要重置为null，避免死循环）<--404返回

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				if (flag == null) {// null处理；需要重置为"",避免死循环
					flag = "";
					view.loadUrl("file:///android_asset/404.html");
					// view.getSettings().setLayoutAlgorithm(
					// LayoutAlgorithm.SINGLE_COLUMN);// 重绘，占满屏幕宽度，1列

				} else {
					// ""处理；需要重置为null，避免死循环
					flag = null;
					// 结论：文件加载，
					// 只要1view.goBack()就行；loadDataWithBaseURL加载，2、3必有，1可有可无
					// view.goBack();// 1、回到历史空白页（可有可无）|如果文件加载，则回到上一页。

					// 如果是文件加载页面，那么会自动记住历史文件url，不用下面的再次加载及清除历史，直接goBack就行
					// 完整路径加载链接
					view.loadDataWithBaseURL(MyUtils.APP_URL, MyUtils
							.modifyImgAsFullUrls(newsDetails.getArticlCont()),
							null, "utf-8", null);// 2、重载页面
					view.clearHistory();// 3、清除历史，即设置为起点，避免死循环

					// view.getSettings().setLayoutAlgorithm(
					// LayoutAlgorithm.NORMAL);// 重绘,可能没有立即生效

					// 通过handler延迟重绘,实测还是不行
					// handler.sendMessageDelayed(new Message(), 100);

					// 最终解决：redesign 404.html with css
				}

			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				view.loadUrl(url);// 当前webview打开。这里可以检查后缀名，启用下载服务。
				return true;
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
		tv_newsTitle.requestFocus();
		tv_details = (TextView) findViewById(R.id.news_detail_ds);

		Intent intent = this.getIntent();// 只能调用一次，以后调用没有数据
		int ftype = intent.getIntExtra("ftype", 1);// 列表类型
		articleId = intent.getIntExtra("articleId", 1309);

		if (ftype == 1) {
			((TextView) findViewById(R.id.header_title)).setText("新闻中心");
		} else if (ftype == 2) {
			((TextView) findViewById(R.id.header_title)).setText("通知公告");
		} else if (ftype == 3) {
			((TextView) findViewById(R.id.header_title)).setText("公交一天");
		} else {
			((TextView) findViewById(R.id.header_title)).setText("新闻");
		}

		((Button) findViewById(R.id.titlebar_left))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						System.out.println("当前 canGoBack="
								+ wv_info_show.canGoBack());
						// canGoBack判断不准确，if判断来弥补
						if (wv_info_show.getUrl().equals("about:blank")) {// 起始页返回
							NewsDetailActivity.this.finish();
							return;// 以下语句均忽略
						}
						// System.out.println("finish 后 可以执行语句？");//
						// 实测可以继续执行，所以需要return
						if (wv_info_show.canGoBack()) {
							System.out.println("可以返回，当前url（即标记的historyUrl）为："
									+ wv_info_show.getUrl());
							// 一、结论：1、goBack 2、加载到内容页面，并且设置起点 3、清除历史
							// 1、文件方式的加载，自动记住历史url， 只要1 就行；
							// 2、loadDataWithBaseURL加载到内容页面，并且设置起点，2、3必有，1可有可无
							// wv_info_show.goBack();
							// wv_info_show.clearHistory();
							// 3、loadDataWithBaseURL前清除历史，只留下当前页，但是加载下一页后，当前页就成为历史，再次canGoBack，造成死循环
							// 4、如果可以返回，则直接返回到内容页，并设置起点，有点不妥。
							// 5、loadDataWithBaseURL后清除历史，只留下当前页，即设置为起点，避免死循环
							wv_info_show.goBack();// 不确保立即执行完毕.如果要有事件通知最好了
							System.out.println("可以返回，返回后url（即标记的historyUrl）为："
									+ wv_info_show.getUrl());// 可能还是上一个页面的url，最好延迟一下
							// 延迟200ms后发起特殊判断
							new Handler().postDelayed(new Runnable() {

								@Override
								public void run() {
									System.out.println("延迟判断！");
									if (wv_info_show.getUrl().equals(
											"about:blank")) {// 可能还是上一个页面的url,导致判断不成立，不会执行以下"重新加载页面，并设置起点"操作。加载的NewsDetailAcitvity页面不存在。接下来，当前页和返回页可能都是NewsDetailActivity.
										System.out.println("重新加载内容页！");
										// 完整路径重新加载内容页
										wv_info_show.loadDataWithBaseURL(
												MyUtils.APP_URL,
												MyUtils.modifyImgAsFullUrls(newsDetails
														.getArticlCont()),
												null, "utf-8", null);// 内容链接为绝对路径方式加载
										// System.out.println("当前URL为"
										// + wv_info_show.getUrl());
										wv_info_show.clearHistory();// 必须清空，，让canGoBack=false，否则死循环。
										// System.out.println("清除 历史后 canGoBack="+
										// wv_info_show.canGoBack());//
										// canGoBack=false。但是再次点击时，canGoBack又变成了true？？测试好久发现，webview的所有方法估计都是异步的。这里的异常估计由goBack没来得及执行完有关
									}

								}
							}, 200);

						} else {
							NewsDetailActivity.this.finish();
						}
					}
				});
		Button refreshButton = ((Button) findViewById(R.id.workButton));
		refreshButton.setVisibility(View.VISIBLE);
		refreshButton.setText("刷新");
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new GetDetailsTask().execute();
			}
		});
	}

	private void initUI() {
		// 每次都会从网络获取而不缓存起来，是为了获得"点击数"的最新值
		tv_newsTitle.setText(newsDetails.getTitle());
		String comString = "发布时间:" + fromatDate(newsDetails.getSendTime())
				+ " 发布人:" + newsDetails.getSendUserName() + " 点击数:"
				+ newsDetails.getHits();
		tv_details.setText(comString);

		// 打开本包内asset目录下的index.html文件
		// wView.loadUrl(" file:///android_asset/index.html ");
		// 打开本地sd卡内的index.html文件
		// wView.loadUrl("file:///mnt/sdcard/index.html");
		// 打开指定URL的html文件
		// wView.loadUrl("http://m.oschina.net");

		// html显示：1，fromHtml 2，自己构造 3，存为文件

		// 1，fromHtml
		// if (newsDetails.getArticlCont() != null) {
		// // wv_info_show.loadData(
		// // Html.fromHtml(
		// // MyUtils.modifyImgUrls(newsDetails.getArticlCont()))
		// // .toString(), null, "utf-8");// loadData不能用，报告异常ActivityNotFound...
		//
		// wv_info_show.loadDataWithBaseURL(
		// MyUtils.APP_URL,
		// Html.fromHtml(
		// MyUtils.modifyImgUrls(newsDetails.getArticlCont()))
		// .toString(), null, "utf-8", null);// 可以，丢失样式
		// }

		// 2，自己构造
		// 2.1newsDetails.getArticlCont() 不行
		// 2.2"<html><body>" + newsDetails.getArticlCont()+"</body></html>"
		// 不行
		// 2.3 URLEncoder.encode("<html><body>" +
		// newsDetails.getArticlCont()+"</body></html>" ) 可以，但失去表格属性

		// wv_info_show.loadData(
		// URLEncoder.encode(
		// "<html><body>" + newsDetails.getArticlCont()
		// + "</body></html>", "utf-8"), "text/html",
		// "utf-8");

		// 3，存为文件再读取（存为文件可以正确显示）|添加缓存功能|方便goBack
		// String dir = Environment.getExternalStorageDirectory().getPath()
		// + "/buscache/html";
		// String filename = newsDetails.getAtricleID() + ".html";
		// MyUtils.saveHTML(dir, filename,
		// MyUtils.modifyImgAsFullUrls(newsDetails.getArticlCont()));
		//
		// String wv_htmlPath = "file://" + dir + "/" + filename;
		// wv_info_show.loadUrl(wv_htmlPath);

		// 直接加载
		if (newsDetails.getArticlCont() != null)
		// 如果使用了绝对位置，相对位置可有可无了

		{
			// 完整路径加载链接
			// wv_info_show.loadDataWithBaseURL(MyUtils.APP_URL,
			// MyUtils.modifyImgAsFullUrls(newsDetails.getArticlCont()), null,
			// "utf-8", null);// 设置当前加载页 历史标记为 空白页，即从下一个页面返回时，回到空白页。
			// ios中然支持.xls预览。最新测试发现，指定了BaseURL后，加载内容无需处理，根本没必要去除相对路径的"/"打头。如果不显示，说明服务器把该图片丢了。

			// 相对路径加载链接
			wv_info_show.loadDataWithBaseURL(MyUtils.APP_URL, newsDetails
					.getArticlCont().replaceAll("(?i)/WebUI/", "")// 去除"/WebUI/"打头
																	// 可以不处理
																	// 不保存到服务器，无所谓
					.replaceAll("(?i)/ewebeditor", "ewebeditor"), null,// 去除"/"打头，可以不处理
																		// 不保存到服务器，无所谓
					"utf-8", null);// 设置当前加载页 历史页null代表空白页
									// about:blank
									// 如果标记为其他页面，如"NewsDetailActivity"，会提示地址错误，不友好。一个空白页面
									// 友好点。
			// （一般设为当前url地址），即从下一个页面返回时，回到该historyUrl。
			// System.out.println("当前URL为" +
			// wv_info_show.getUrl());//about:blank 或 没来得及 初始化 该字段 则为 null
			wv_info_show.clearHistory();// 清除浏览历史，设为起点页面。否则按照titlebar_left按钮的处理逻辑，刷新后要点击2次（加载当前页，并设置起点）|刷新N次要返回N+1次（直接goBack）才能返回
			// System.out.println("清除 历史后 canGoBack=" +
			// wv_info_show.canGoBack());//false

		}
	}

	private class GetDetailsTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog dialog = new ProgressDialog(
				NewsDetailActivity.this);

		View contentView = null;

		@Override
		protected void onPreExecute() {

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
			// int articleId = getIntent().getIntExtra("articleID", 1467);
			// 不缓存是为了获取最新的点击数
			// 如果缓存，只能本次操作中缓存，关闭程序后及时删除文件，以便下次获取最新数据。
			newsDetails = WebServiceUtils.getDetailInfo(articleId);
			int i = 0;
			while (newsDetails.getAtricleID() == null
					&& i < MyUtils.RETRY_TIMES) {
				newsDetails = WebServiceUtils.getDetailInfo(articleId);
				i++;
				System.out.println("获取新闻详情失败，有" + MyUtils.RETRY_TIMES
						+ "次重试，重试第： " + i + " 次！");
			}

			// 如果网络请求不通时，返回的默认值不是默认实例，而是null，那么经过1+3即4次请求后
			// newsDetais可能仍然为null，此时需要赋值默认实例，避免空指针异常。
			// if(newsDetails==null){
			// newsDetails=new NewsDetails();
			// }
			// 修改请求的返回值为默认实例，if判断可省略
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			this.dialog.dismiss();
			Intent clickIntent = new Intent();
			clickIntent.putExtra("clickCount",
					Integer.parseInt(newsDetails.getHits()));// 标准点击数。反射类型，存String只恩取String
																// 存int只能取int
			clickIntent.putExtra("position",
					getIntent().getIntExtra("position", 0));// 获取点击位置
															// 并放在clickIntent中
			setResult(0, clickIntent);// 在该activity finish时 发送
			initUI();
		}
	}

	private String fromatDate(String date) {
		// 2012-04-21T16:09:54.6+08:00
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// return sdf.format(new Date(date)).toString();
		String returnString = "";
		try {
			returnString = date.substring(0, 10) + " " + date.substring(11, 19);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnString;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && wv_info_show.canGoBack()) {
			if (wv_info_show.getUrl().equals("about:blank")) {// 起始页返回
				NewsDetailActivity.this.finish();
				return true;// 以下语句均忽略
			}
			wv_info_show.goBack();// 不确保立即执行完毕.如果要有事件通知最好了
			// 延迟200ms后发起特殊判断
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					if (wv_info_show.getUrl().equals("about:blank")) {
						// 完整路径加载链接
						wv_info_show.loadDataWithBaseURL(MyUtils.APP_URL,
								MyUtils.modifyImgAsFullUrls(newsDetails
										.getArticlCont()), null, "utf-8", null);// 内容链接为绝对路径方式加载
						wv_info_show.clearHistory();// 必须清空，，让canGoBack=false，否则死循环。不可靠，canGoBack可能被重置true
					}
				}
			}, 200);
			return true;
		} else {
			// 确保本activity消费掉 “返回” 按钮事件。否则会继续传递到MainActivity，从而弹出退出框。
			return super.onKeyDown(keyCode, event);// 高版本可以确保，低版本不行

			// NewsDetailActivity.this.finish();//由于finish了，所以无论高版本、低版本均不能确保消费掉，导致弹出退出框
			// return true;
		}
	}

}
