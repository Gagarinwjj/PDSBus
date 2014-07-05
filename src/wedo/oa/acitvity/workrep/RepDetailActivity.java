package wedo.oa.acitvity.workrep;

import wedo.oa.acitvity.R;
import wedo.oa.utils.MyUtils;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class RepDetailActivity extends Activity {
	WebView wv_info_show;
	TextView tv_newsTitle;
	TextView tv_details;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		initWidget();
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
		// 自己的元素要 后加载 先释放
		wv_info_show.setVisibility(View.GONE);// 立即消失
		wv_info_show.destroy();// 释放webview内存
		super.onDestroy();
	};

	private void initWidget() {
		Bundle bundle = this.getIntent().getExtras();
		((TextView) findViewById(R.id.header_title)).setText("汇报内容");

		tv_newsTitle = (TextView) findViewById(R.id.news_detail_title);
		tv_newsTitle.setText(bundle.getCharSequence("title"));

		tv_details = (TextView) findViewById(R.id.news_detail_ds);
		tv_details.setText(bundle.getCharSequence("realTime"));

		wv_info_show = ((WebView) findViewById(R.id.news_webview));
		wv_info_show.getSettings().setBuiltInZoomControls(true);
		wv_info_show.getSettings().setDefaultTextEncodingName("utf-8");
		wv_info_show.loadDataWithBaseURL(null, bundle
				.getCharSequence("content").toString(), "text/html", "UTF-8",
				null);// 解决高版本上的乱码问题。
		// System.out.println("乱码"+
		// bundle.getCharSequence("content").toString());
		((Button) findViewById(R.id.titlebar_left))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (wv_info_show.canGoBack()) {
							wv_info_show.goBack();
						} else {
							RepDetailActivity.this.finish();
						}
					}
				});
	}
}
