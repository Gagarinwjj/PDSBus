package wedo.oa.acitvity.workrep;

import wedo.oa.acitvity.R;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class WorkReportActivity extends Activity {
	ListView lv_workrep;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.workreport);
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

	private void initData(final String[] repTimes) {
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.replist, R.id.textView1, repTimes) {
			@Override
			// 重写getView，增加字体效果。
			public View getView(int position, View convertView, ViewGroup parent) {
				View contentView = View.inflate(WorkReportActivity.this,
						R.layout.replist, null);
				TextView textView = (TextView) contentView
						.findViewById(R.id.textView1);
				textView.setText(Html.fromHtml(repTimes[position]));
				return contentView;
			}
		};
		lv_workrep.setAdapter(adapter);
		lv_workrep.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 转到部门列表。repTime首次点击后，字符串被改变。以后点击，参数截取不对。需要去掉格式参数。巧了，刚测试发现Html可以做到这点。
				repTimes[position] = "<b><font color='RED'>"
						+ repTimes[position] + "</font></b>";
				adapter.notifyDataSetChanged();
				WorkReportActivity.this.startActivity(new Intent(
						WorkReportActivity.this, RepDepListActivity.class)
						.putExtra("repTime", Html.fromHtml(repTimes[position])
								.toString().subSequence(0, 10)));
			}
		});
		// lv_workrep.startLayoutAnimation();
	}

	private void initWidget() {
		// AnimationSet set = new AnimationSet(false);
		// Animation animation = new AlphaAnimation(0,1);
		// animation.setDuration(500);
		// set.addAnimation(animation);
		//
		// animation = new TranslateAnimation(1, 13, 10, 50);
		// animation.setDuration(300);
		// set.addAnimation(animation);
		//
		// animation = new RotateAnimation(30,10);
		// animation.setDuration(300);
		// set.addAnimation(animation);
		//
		// animation = new ScaleAnimation(5,0,2,0);
		// animation.setDuration(300);
		// set.addAnimation(animation);
		//
		// LayoutAnimationController controller = new
		// LayoutAnimationController(set, 1);

		lv_workrep = (ListView) this.findViewById(R.id.workreport);
		// lv_workrep.setLayoutAnimation(controller);
	}

	private class GetDetailsTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog dialog = new ProgressDialog(
				WorkReportActivity.this);
		View contentView = null;
		String[] repTimes;

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
			// if (ad != null) {
			// // 死循环导致ANR 浪费 2小时左右。。。。
			// // while (true) {
			// ad.start();
			// // }
			// }
			((TextView) contentView.findViewById(R.id.textView1))
					.setText("正在获取数据，请稍后...");
			this.dialog.show();
			this.dialog.setContentView(contentView);
		}

		@Override
		protected Void doInBackground(Void... params) {
			repTimes = WebServiceUtils.getRepList();
			int i = 0;
			while (repTimes.length == 0 && i <MyUtils.RETRY_TIMES) {
				repTimes = WebServiceUtils.getRepList();
				i++;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			this.dialog.dismiss();
			initData(repTimes);
		}

	}
}
