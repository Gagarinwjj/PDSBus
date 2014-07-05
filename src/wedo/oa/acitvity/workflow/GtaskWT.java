package wedo.oa.acitvity.workflow;

import java.util.ArrayList;

import wedo.oa.acitvity.AppUserInfo;
import wedo.oa.acitvity.R;
import wedo.oa.beans.DepNode;
import wedo.oa.beans.People;
import wedo.oa.beans.ProcessResult;
import wedo.oa.beans.UserForm;
import wedo.oa.utils.WebServiceUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class GtaskWT extends Activity {
	TextView tv_bz;
	TextView tv_dbr;
	Button btn_choose;
	UserForm userForm;
	private long childIDLastSelected = -1;
	private LayoutInflater inflater;
	ArrayList<ArrayList<DepNode>> depItems;
	String[] depNames = { "总公司机关", "营运单位", "二级单位" };
	ArrayList<People> depPeopleArrayList = new ArrayList<People>();
	private People selectedPeople;
	AppUserInfo aui;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wt);
		initView();
		initListener();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void initView() {
		inflater = LayoutInflater.from(this);
		// 返回
		((Button) findViewById(R.id.titlebar_left))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						GtaskWT.this.finish();
					}
				});
		((TextView) findViewById(R.id.header_title)).setText("委托办理");
		tv_bz = (TextView) findViewById(R.id.bz);
		tv_dbr = (TextView) findViewById(R.id.dbr);
		btn_choose = (Button) findViewById(R.id.btn_choose);
		userForm = (UserForm) this.getIntent().getSerializableExtra("detail");
		tv_bz.setText(userForm.getUpNodeName());
		aui = (AppUserInfo) getApplication();
	}

	private void initListener() {
		tv_dbr.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (depItems == null || depItems.size() == 0) {
					new DepItemsTask().execute();
				} else {
					// 定义弹窗

					final AlertDialog dialog = new AlertDialog.Builder(
							GtaskWT.this).create();

					// 1、构建人事选择View
					View peopleView = inflater.inflate(
							R.layout.check_dep_people_single, null);
					// 2、初始化界面元素
					TextView ptv = (TextView) peopleView
							.findViewById(R.id.title);
					ptv.setText("选择审批人员");

					// 3、按钮事件处理
					Button conformBtn = (Button) peopleView
							.findViewById(R.id.btn_conform);// id写错，找不到控件，null异常
					conformBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// 找到被选中的人员
							for (People p : depPeopleArrayList) {

								if (p.isChecked()) {
									selectedPeople = p;
									break;
								}
							}
							if (selectedPeople != null) {
								tv_dbr.setText(selectedPeople.getUserName());
							}
							dialog.dismiss();// pgv.getAdapter()会被重置为null
						}
					});

					Button cancleBtn = (Button) peopleView
							.findViewById(R.id.btn_cancel);
					cancleBtn.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();// pgv.getAdapter()会被重置为null
						}
					});

					// 4、GridView初始化
					final GridView pgv = (GridView) peopleView
							.findViewById(R.id.peoples);
					pgv.setAdapter(new PeopleAdapter(depPeopleArrayList));// 避免空指针异常，depPeopleArrayList要初始化一个空的

					// 5、部门 ExpandListView
					final ExpandableListView depsExListView = (ExpandableListView) peopleView
							.findViewById(R.id.depslist);
					depsExListView.setAdapter(new DepsAdapter(GtaskWT.this));
					depsExListView
							.setOnChildClickListener(new OnChildClickListener() {

								@Override
								public boolean onChildClick(
										ExpandableListView parent, View v,
										int groupPosition, int childPosition,
										long id) {

									// System.out.println(groupPosition +
									// "-"
									// + childPosition + "-" + id);
									if (id != childIDLastSelected) {
										// 获取部门对应的人员数据
										new PeoplesTask(pgv).execute("deps", id
												+ "");
										childIDLastSelected = id;
										// 刷新视图数据，会调用getChildView，以将当前选中设为蓝色
										// //java.lang.ClassCastException:
										// android.widget.ExpandableListConnector
										// ((BaseExpandableListAdapter)
										// depsExListView
										// .getAdapter())
										// .notifyDataSetChanged();
										// 手工收缩一次，发现会重绘子视图 or 查询
										// SDK，发现收缩一次groupView
										// 会引起子视图childView重绘
										// 所以，解决方法：收缩一次，以调用getChildView刷新ChildView视图
										depsExListView
												.collapseGroup(groupPosition);
										depsExListView
												.expandGroup(groupPosition);

									}
									return false;
								}
							});

					dialog.show();// 一定要先show 否则有错
					dialog.setContentView(peopleView);
				}
			}
		});

		btn_choose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (selectedPeople != null) {
					new WTTask().execute();
				} else {
					Toast.makeText(GtaskWT.this, "请选择委托人", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});
	}

	private void showResult(ProcessResult pr) {
		Toast.makeText(this, pr.getMsg(), Toast.LENGTH_SHORT).show();
		if (pr.getResult() == 1) {
			this.finish();
		}
	}

	private class PeoplesTask extends AsyncTask<String, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(GtaskWT.this);
		View contentView = null;
		GridView gridView;

		public PeoplesTask(GridView gridView) {
			this.gridView = gridView;
		}

		@Override
		protected void onPreExecute() {
			if (contentView == null) {
				LayoutInflater inflater = getLayoutInflater();
				contentView = inflater.inflate(R.layout.customdialog, null);
			}
			((TextView) contentView.findViewById(R.id.textView1))
					.setText("正在获取审批人员数据，请稍后...");
			this.dialog.show();
			this.dialog.setContentView(contentView);

		}

		@Override
		protected Void doInBackground(String... params) {

			// 这样做，才会真正改变数据库的数据源
			// depPeopleArrayList.clear();
			// 访问网络需要耗费比较长的时间，在这段时间内，快速点击选项，或导致系统调用getView刷新视图，由于已经clear,会报告错误:Index
			// 0 Size 0
			ArrayList<People> peoples = (ArrayList<People>) WebServiceUtils
					.execSql("getDepPeoples @depid=" + params[1], People.class);
			depPeopleArrayList.clear();// 放到下面有效避免 Index 0，Size 0 报错
			depPeopleArrayList.addAll(peoples);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
			this.dialog.dismiss();
		}
	}

	private class PeopleAdapter extends BaseAdapter {

		// 数组的方法比较少，用ArrayList方便
		// private People[] peoples;

		ArrayList<People> peoples;

		public PeopleAdapter(ArrayList<People> peoples) {
			this.peoples = peoples;// 构造函数中赋值为全局 depPeopleArrayList

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return peoples.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return peoples.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return Long.valueOf(peoples.get(position).getUserID());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// System.out.println("GtaskWT getView"+position);
			if (peoples.size() == 0)
				return null;
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(
						R.layout.checkpeople_item_single, null);
				holder.cb = (RadioButton) convertView.findViewById(R.id.cb);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (peoples.size() > 0) {// 对于易报错的健壮性判断。估计是peoples被清空后，没来得及填充数据源，系统就调用getView了。
				final People people = peoples.get(position);// 易报错：Index
															// 0，Size 0即
															// 没有任何数据，请求第一个。
				holder.cb.setText(people.getUserName());
				holder.cb.setChecked(people.isChecked());
				holder.cb.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// RadioButton cb = (RadioButton) v;
						// cb.setChecked(true);//刷新视图时会根据People状态设置选中与否。这里可以不用设置了。
						// 当前对应人员选中
						people.setChecked(true);
						// 其他均不选中
						for (People p : peoples) {
							if (p != people) {
								p.setChecked(false);
							}
						}
						// 刷新视图
						PeopleAdapter.this.notifyDataSetChanged();
					}
				});
			}
			return convertView;
		}

		class ViewHolder {
			RadioButton cb;
		}
	}

	// 初始化伸缩ListView的部门列表
	private class DepItemsTask extends AsyncTask<Void, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(GtaskWT.this);
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
			depItems = new ArrayList<ArrayList<DepNode>>();
			depItems.add((ArrayList<DepNode>) WebServiceUtils
					.execSql(
							"select DepID,DepName from Bas_DepInfo where ParentNodesID=1",
							DepNode.class));
			depItems.add((ArrayList<DepNode>) WebServiceUtils
					.execSql(
							"select DepID,DepName from Bas_DepInfo where ParentNodesID=2",
							DepNode.class));
			depItems.add((ArrayList<DepNode>) WebServiceUtils
					.execSql(
							"select DepID,DepName from Bas_DepInfo where ParentNodesID=3",
							DepNode.class));

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			tv_dbr.performClick();
			this.dialog.dismiss();
		}

	}

	private class DepsAdapter extends BaseExpandableListAdapter {
		private Context context;

		public DepsAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getGroupCount() {
			// 1 总公司机关
			// 2 营运单位
			// 3 二级单位
			return depNames.length;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return depItems.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return depItems.size();
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return depItems.get(groupPosition).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return Long.valueOf(depItems.get(groupPosition).get(childPosition)
					.getDepID());
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// System.out.println("groupPosition" + groupPosition);
			// if (convertView == null) {
			// System.out.println(groupPosition + "is null");
			TextView tv = new TextView(context);
			tv.setText(depNames[groupPosition]);
			tv.setHeight(48);
			tv.setTextSize(18);
			tv.setTextColor(Color.BLACK);
			tv.setPadding(60, 0, 0, 0);
			convertView = tv;
			// }
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// if (convertView == null) {
			TextView tv = new TextView(context);
			tv.setText(depItems.get(groupPosition).get(childPosition)
					.getDepName());
			tv.setHeight(36);
			tv.setTextSize(16);
			tv.setTextColor(Color.BLACK);
			tv.setPadding(65, 0, 0, 0);
			if (childIDLastSelected == getChildId(groupPosition, childPosition)) {
				tv.setTextColor(Color.BLUE);// 当前选中为蓝色，嘿嘿 ，好聪明
			}

			convertView = tv;
			// }
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}

	}

	private class WTTask extends AsyncTask<Void, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(GtaskWT.this);
		View contentView = null;
		ProcessResult pr;

		@Override
		protected void onPreExecute() {
			if (contentView == null) {
				LayoutInflater inflater = getLayoutInflater();
				contentView = inflater.inflate(R.layout.customdialog, null);
			}
			((TextView) contentView.findViewById(R.id.textView1))
					.setText("正在办理委托，请稍后...");
			this.dialog.show();
			this.dialog.setContentView(contentView);

		}

		@Override
		protected Void doInBackground(Void... params) {
			pr = WebServiceUtils.getWTProcessResult(userForm.getId(),
					aui.getUserID(), aui.getUserName(),
					selectedPeople.getUserID(), selectedPeople.getUserName());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (pr != null) {
				showResult(pr);
			}
			this.dialog.dismiss();
		}

	}
}
