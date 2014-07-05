package wedo.oa.acitvity.workrep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wedo.oa.acitvity.R;
import wedo.oa.beans.RepDep;
import wedo.oa.utils.MyUtils;
import wedo.oa.utils.WebServiceUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class RepDepListActivity extends Activity {
	String repTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.repdep);
		repTime = getIntent().getStringExtra("repTime");
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

	ListView lv_repdeps;

	private void initWidget() {
		((TextView) findViewById(R.id.header_title)).setText("汇报部门");
		((Button) findViewById(R.id.titlebar_left))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 不想通过finish来返回到上一个界面。
						RepDepListActivity.this.finish();
						// startActivity(new
						// Intent(RepDepListActivity.this,WorkReportActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
					}
				});
		lv_repdeps = (ListView) this.findViewById(R.id.repdeps);
	}

	private void initUI(final List<RepDep> repDeps) {
		final List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();// final
																							// 引用不变，内容可变。
		for (RepDep repDep : repDeps) {
			HashMap<String, String> item = new HashMap<String, String>();
			//System.out.println(repDep.getRealTime());
			item.put("title", repDep.getRealTime().substring(0, 10) + " "
					+ repDep.getRepDep() + "工作汇报");
			data.add(item);
		}
		final SimpleAdapter adapter = new SimpleAdapter(
				RepDepListActivity.this, data, R.layout.replist,
				new String[] { "title" }, new int[] { R.id.textView1 });
		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if (view instanceof TextView) {
					((TextView) view).setText(Html.fromHtml(textRepresentation));
					return true;// 已经处理完，不再交予系统处理
				}
				return false;// 没有处理，交予系统处理
			}
		});
		lv_repdeps.setAdapter(adapter);
		lv_repdeps.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String html = "<b><font color='RED'>"
						+ data.get(position).get("title") + "</font></b>";// 重复点击会照成标签重复嵌套，不过没事。
				data.get(position).put("title", html);
				adapter.notifyDataSetChanged();
				Bundle b = new Bundle();
				b.putCharSequence("title",
						Html.fromHtml(data.get(position).get("title"))
								.toString());
				String realTime = repDeps.get(position).getRealTime();

				b.putCharSequence(
						"realTime",
						"发表时间: " + realTime.subSequence(0, 10) + " "
								+ realTime.substring(11, 19));// 服务器没处理，则客户端处理
				b.putCharSequence("content", repDeps.get(position).getRepCont());
				startActivity(new Intent(RepDepListActivity.this,
						RepDetailActivity.class).putExtras(b));
			}
		});
	}

	private class GetDetailsTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog dialog = new ProgressDialog(
				RepDepListActivity.this);
		private List<RepDep> repDeps;
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
			repDeps = WebServiceUtils.getRepDep(repTime);
			int i = 0;
			while (repDeps.size() == 0 && i < MyUtils.RETRY_TIMES) {
				repDeps = WebServiceUtils.getRepDep(repTime);
				i++;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			this.dialog.dismiss();
			initUI(repDeps);
		}

	}
}
