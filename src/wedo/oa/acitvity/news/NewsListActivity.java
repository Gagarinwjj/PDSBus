package wedo.oa.acitvity.news;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wedo.oa.acitvity.R;
import wedo.oa.beans.News;
import wedo.oa.utils.CustomToast;
import wedo.oa.utils.MyUtils;
import wedo.oa.utils.PullToRefreshListView;
import wedo.oa.utils.PullToRefreshListView.OnAddMoreListener;
import wedo.oa.utils.PullToRefreshListView.OnRefreshListener;
import wedo.oa.utils.WebServiceUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class NewsListActivity extends Activity {
	PullToRefreshListView newsListview;
	int ftype;
	SimpleAdapter adapter;
	List<HashMap<String, String>> data;
	int allNum;
	int currentPage = 1;
	private boolean isRefreshing = false;
	private boolean isAddmorehing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newslist);
		ftype = getIntent().getIntExtra("ftype", 1);// 列表类型
		data = new ArrayList<HashMap<String, String>>();
		newsListview = (PullToRefreshListView) findViewById(R.id.newsinfolist);
		newsListview.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				// Do work to refresh the list here.
				isRefreshing = true;
				// 清空数据 | 不清空也行
				// data.clear();
				// adapter.notifyDataSetChanged();
				new GetDetailsTask().execute();
			}
		});

		newsListview.setOnAddMoreListener(new OnAddMoreListener() {

			@Override
			public void onAddMore() {
				isAddmorehing = true;
				new GetDetailsTask().execute();
			}
		});
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

	public void setHotColor(int clickCount, ImageView iv) {
		if (clickCount < 50) {
			iv.setBackgroundColor(getResources().getColor(R.color.hot1));
		} else if (clickCount < 100) {
			iv.setBackgroundColor(getResources().getColor(R.color.hot2));
		} else if (clickCount < 150) {
			iv.setBackgroundColor(getResources().getColor(R.color.hot3));
		} else if (clickCount < 200) {
			iv.setBackgroundColor(getResources().getColor(R.color.hot4));
		} else if (clickCount < 250) {
			iv.setBackgroundColor(getResources().getColor(R.color.hot5));
		} else if (clickCount < 300) {
			iv.setBackgroundColor(getResources().getColor(R.color.hot6));
		} else if (clickCount < 350) {
			iv.setBackgroundColor(getResources().getColor(R.color.hot7));
		} else if (clickCount < 400) {
			iv.setBackgroundColor(getResources().getColor(R.color.hot8));
		} else {
			iv.setBackgroundColor(getResources().getColor(R.color.hot9));
		}
	}

	private void initData(List<News> newsList) {

		// Animation animation = AnimationUtils.loadAnimation(
		// NewsListActivity.this, R.anim.push_right_in);
		// LayoutAnimationController lac = new
		// LayoutAnimationController(animation);
		// lac.setDelay(0.5f);
		// newsListview.setLayoutAnimation(lac);
		newsListview.setFocusableInTouchMode(true);
		newsListview.setFocusable(true);
		addItemToData(newsList);
		// newsList可以清空了
		newsList.clear(); 
		// 简单适配器只能适配字符串给TextView，ImageView不好适配吗？img的字符串的id就不能自动转为int的id吗？哎！
		// 这里，"img"字段给ImageView 被忽略，clickCount没有值，也被忽略。
		adapter = new SimpleAdapter(this, data, R.layout.list_adapter,
				new String[] { "title", "sendTime", "sendUsername",
						"clickCount", "clickCount", "clickCount" }, new int[] {
						R.id.news_title, R.id.news_date, R.id.news_person,
						R.id.news_clickcount, R.id.news_hotcolor,
						R.id.news_image });

		final int imgid = (ftype == 1) ? R.drawable.news
				: (ftype == 2) ? R.drawable.notice
						: (ftype == 3) ? R.drawable.bus_day
								: R.drawable.finance;
		adapter.setViewBinder(new ViewBinder() {
			// 技巧：data采用HashMap<String, Object>作为容器元素，就不需要ViewBinder了
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if (view instanceof ImageView) {
					ImageView imageView = ((ImageView) view);
					switch (imageView.getId()) {
					case R.id.news_image:// 匹配列表中必须有，才能检测到
						imageView.setImageResource(imgid);
						break;
					case R.id.news_hotcolor:// 匹配列表中必须有，才能检测到
						setHotColor(Integer.parseInt(data.toString()),
								imageView);
						break;
					}
					return true;
				}
				return false;// 绑定器会在“对应绘图”前调用，true，则不再继续处理，false，则继续处理。
			}
		});

		newsListview.setAdapter(adapter);
		newsListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 头刷新，尾加载更多忽略不计
				if (position > 0 && position < adapter.getCount() + 1) {
					HashMap<String, String> newsItem = data.get(position - 1);// 错位一个
					// 界面点击数加1,以便低版本（低于4.1）中立即刷新一个+1
					newsItem.put("clickCount",
							Integer.valueOf(newsItem.get("clickCount")) + 1
									+ "");
					Intent intent = new Intent(NewsListActivity.this,
							NewsDetailActivity.class)
							.putExtra("ftype", ftype)
							.putExtra("position", position - 1)
							.putExtra("articleId",
									Integer.valueOf(newsItem.get("articleId")));// 存放int
																				// 才能getIntExtra
					// startActivity(intent);//无法取得点击值（刷新多次才取得值，点击也会触发多次）
					startActivityForResult(intent, 0);// 为了取得点击值
				}
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intentData) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intentData);
		int position = intentData.getIntExtra("position", -1);
		int clickCount = intentData.getIntExtra("clickCount", -1);
		if (position != -1 && clickCount != -1) {
			HashMap<String, String> newsItem = data.get(position);
			// 标准点击数（NewsDetailActivity中可能刷新多次才取得值，所以点击也会触发多次）
			newsItem.put("clickCount", clickCount + "");// 返回的点击数
			adapter.notifyDataSetChanged();// 刷新
			System.out.println("刷新位置:" + position + ",标准点击数为:" + clickCount);
		}
	}

	private void addItemToData(List<News> newsList) {
		for (News news : newsList) {
			HashMap<String, String> newsItem = new HashMap<String, String>();
			// newsItem.put("img", ((ftype == 1) ?
			// String.valueOf(R.drawable.news)
			// : String.valueOf(R.drawable.notice)));
			newsItem.put("title", news.getTitle());
			newsItem.put("sendUsername", "发布人:" + news.getSendUsername()
					+ "   点击数:");
			newsItem.put("clickCount", news.getClickCount());
			newsItem.put("sendTime", "发布时间:" + news.getSendTime());
			newsItem.put("articleId", news.getArticleID());
			data.add(newsItem);
		}
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		// 估计是onRestart=>onAcitvityResult=>onResume
		// onAcitvityResult有了刷新 onRestart中就不用刷新了
		// adapter.notifyDataSetChanged();//
		// 为了刷新加1后的点击数|点击后，低版本（低于4.1）会自动刷新该元素，高版本不会，所以还是加上这句话，兼容高版本。
	}

	private class GetDetailsTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog dialog = new ProgressDialog(
				NewsListActivity.this);
		private List<News> newsList;
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
			if (isAddmorehing) {
				// 新增
				newsList = WebServiceUtils.getNewsInfo(ftype, ++currentPage,
						MyUtils.PAGE_SIZE);
				int i = 0;// 额外尝试5次
				while (newsList.size() == 0 && i < MyUtils.RETRY_TIMES) {
					System.out.println("额外尝试：" + (i + 1));
					newsList = WebServiceUtils.getNewsInfo(ftype, currentPage,
							MyUtils.PAGE_SIZE);
					i++;
				}
				// 额外的尝试 if else 必须放外边 因为whlie循环中，不用currentPage--还原到当前页
				if (newsList.size() > 0) {
					newsList.remove(newsList.size() - 1);
				} else {
					currentPage--;
				}
			} else {
				// 第一页或刷新
				newsList = WebServiceUtils.getNewsInfo(ftype, 1,
						MyUtils.PAGE_SIZE);
				int i = 0;// 额外尝试次数
				while (newsList.size() == 0 && i < MyUtils.RETRY_TIMES) {
					System.out.println("额外尝试：" + (i + 1));
					newsList = WebServiceUtils.getNewsInfo(ftype, 1,
							MyUtils.PAGE_SIZE);
					i++;
				}
				// if else判断放在while里外都行
				if (newsList.size() > 0) {// 一般来说，newsList至少有一条数据，就是最后一条的页码数据。但是如果有异常，也可能没有数据。判断是必须的。
					News lastNews = newsList.remove(newsList.size() - 1);
					allNum = Integer.parseInt(lastNews.getArticleID());
				}

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			this.dialog.dismiss();
			if (isAddmorehing) {
				addItemToData(newsList);
				adapter.notifyDataSetChanged();
				isAddmorehing = false;
				newsListview.setSelection(currentPage * MyUtils.PAGE_SIZE
						- MyUtils.PAGE_SIZE + 1);// 加1是因为 表头占了一个位置

			} else if (isRefreshing) {
				data.clear();
				// 如果不指定，那么可能情况：
				// 假设共有4页，加载到第2页。刷新到第1页。
				// 再次加载为第3页 和 第4页的数据。接着加载第5、6、7...页，没有数据返回。
				// 共有1、3、4页的数据，使得data.size()<allNum恒满足
				// 所以一直可以加载更多，就是没有数据
				currentPage = 1;
				addItemToData(newsList);
				adapter.notifyDataSetChanged();

				isRefreshing = false;
				// 设置刷新时间
				newsListview.onRefreshComplete(new Date(System
						.currentTimeMillis()).toLocaleString());
			} else {// 第一页
				initData(newsList);
				// 设置刷新时间
				newsListview.onRefreshComplete(new Date(System
						.currentTimeMillis()).toLocaleString());
			}

			// 检查数据是否获取完毕（每次均执行）
			if (data.size() < allNum) {
				newsListview.onAddmoreComplete(false, data.size(), allNum);
			} else {
				newsListview.onAddmoreComplete(true, data.size(), allNum);
				CustomToast.show(NewsListActivity.this, "已经加载全部数据！");
			}
		}

	}
	// 服务器端处理了，客户端就不用处理了
	// private String fromatDate(String date) {
	// // 2012-04-21T16:09:54.6+08:00
	// return date.substring(0, 10) + " " + date.substring(11, 19);
	// }
}
