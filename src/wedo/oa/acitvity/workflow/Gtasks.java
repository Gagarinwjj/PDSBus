package wedo.oa.acitvity.workflow;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import wedo.oa.acitvity.R;
import wedo.oa.beans.FormTypes;
import wedo.oa.beans.UserForm;
import wedo.oa.utils.CustomToast;
import wedo.oa.utils.MyUtils;
import wedo.oa.utils.PullToRefreshListView;
import wedo.oa.utils.PullToRefreshListView.OnAddMoreListener;
import wedo.oa.utils.PullToRefreshListView.OnRefreshListener;
import wedo.oa.utils.WebServiceUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class Gtasks extends Activity {
	Spinner spiner;
	RadioGroup rGroup;
	ArrayAdapter<String> spinnerAdapter;
	String forms[];
	List<UserForm> userForms = new ArrayList<UserForm>();
	MyLVAdapter adapter;
	PullToRefreshListView lv_userForm;
	int userid;
	int spinnerLastSelected;
	View headView;

	int allNum;
	int currentPage = 1;
	private boolean isRefreshing = false;
	private boolean isAddmorehing = false;

	private AsyncTask<String, Void, Void> currentAsyncTask;

	// 有待优化：
	// 1、检查是否正在加载，如果正在加载则 等待 或者 取消 当前加载
	// 2、结束activity时 同步结束AsyncTask
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gtasks);
		userid = getIntent().getIntExtra("userid", 0);// 存放类型要和取值类型一致！！！

		// System.out.println("Gtask userid=" +
		// userid);//打印结果为0。GtaskSpNext中明明已经传递了，为啥还取不到？因为类型不匹配，不自动转换！

		// if (userid == 0) {// 如果没取到，则再到Application中取值
		// userid = Integer.valueOf(((AppUserInfo) getApplication())
		// .getUserID());
		// }
		initViews();
		setListeners();

	}

	@Override
	protected void onResume() {

		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		// first parameter

		String[] params = getParams();
		if (params[1].equals("未办理")) {
			// 1、初始默认为未办理，加载数据。
			// 2、其他页面返回后，可能将未办理 办理， 所以要重新请求。
			new GetDetailsTask().execute(params[0], params[1]);
		}
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (currentAsyncTask != null)
			currentAsyncTask.cancel(true);
	}

	public String[] getParams() {
		String formtype = "";
		if (spinnerLastSelected != 0 && forms != null) {
			formtype = forms[spinnerLastSelected];
		}
		// second parameter
		String state = "";
		int checkedId = rGroup.getCheckedRadioButtonId();
		switch (checkedId) {
		case R.id.r1:// 未办理
			state = "未办理";
			break;

		case R.id.r2:// 已办理
			state = "已办理";
			break;

		case R.id.r3:// 已委托
			state = "已委托";
			break;
		}
		return new String[] { formtype, state };
	}

	private void initViews() {

		((Button) findViewById(R.id.titlebar_left))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Gtasks.this.finish();
					}
				});
		((TextView) findViewById(R.id.header_title)).setText("待办工作");
		((Button) findViewById(R.id.workButton)).setVisibility(View.INVISIBLE);
		spiner = (Spinner) findViewById(R.id.form_types);
		rGroup = (RadioGroup) findViewById(R.id.choices);
		lv_userForm = (PullToRefreshListView) findViewById(R.id.form_data);
		lv_userForm.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				// Do work to refresh the list here.
				isRefreshing = true;
				// 清空数据 | 不清空也行
				// userForms.clear();
				// adapter.notifyDataSetChanged();
				String[] params = getParams();
				new GetDetailsTask().execute(params[0], params[1]);
			}
		});

		lv_userForm.setOnAddMoreListener(new OnAddMoreListener() {

			@Override
			public void onAddMore() {
				isAddmorehing = true;
				String[] params = getParams();
				new GetDetailsTask().execute(params[0], params[1]);
			}
		});
	}

	private void setListeners() {
		// 监听，触发查询，获得两个参数，并区别查询类型: 所有类型/具体类型
		spiner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position != spinnerLastSelected) {

					// get new info
					currentPage = 1;// 发起新的请求了，肯定需要重置为第一页
					spinnerLastSelected = position;// 先更新位置
					String[] params = getParams();// 再根据位置取值。否则取到的值就是最后选中值
					new GetDetailsTask().execute(params[0], params[1]);

				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
		rGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				String state = "";
				switch (checkedId) {
				case R.id.r1:// 未办理
					state = "未办理";
					break;

				case R.id.r2:// 已办理
					state = "已办理";// 除等待办理和委托之外的，都是已办理
					break;

				case R.id.r3:// 已委托
					state = "已委托";// 这个关键字不知道对否
					break;
				}

				currentPage = 1;// 发起新的请求了，肯定需要重置为第一页
				// 所有类型
				if (spinnerLastSelected == 0) {
					new GetDetailsTask().execute("", state);
				} else {
					new GetDetailsTask().execute(forms[spinnerLastSelected],
							state);
				}
			}
		});
	}

	private void initSpinnerAdapter() {
		spinnerAdapter = new ArrayAdapter<String>(Gtasks.this,
				R.layout.spinner_item, forms);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spiner.setAdapter(spinnerAdapter);
	}

	public void showData() {
		// 初始加载
		if (lv_userForm.getAdapter() == null) {
			headView = LayoutInflater.from(Gtasks.this).inflate(
					R.layout.formitem, null);
			lv_userForm.addHeaderView(headView);
			adapter = new MyLVAdapter(this);
			lv_userForm.setAdapter(adapter);

			lv_userForm.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						final int position, long id) {
					// Toast.makeText(Gtasks.this, "item点击了position:" +
					// position,
					// 0).show();
					// 点击headview ,固定的，判断显示
					if (position == 1) {
						LinearLayout ll = ((LinearLayout) view
								.findViewById(R.id.operate));
						if (ll.getVisibility() == View.GONE) {
							// 判断在那种state下点击的HeadView
							// Toast.makeText(Gtasks.this,
							// "headview item clicked", 0).show();
							showHeadViewByStateId(ll);
						}
						// else {
						// 显示则不显示，没必要
						// ll.setVisibility(View.GONE);
						// }

						// 点击HeadView后，焦点移交给view，同时，事件处理逻辑也要移交
						view.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								LinearLayout ll = ((LinearLayout) v
										.findViewById(R.id.operate));
								if (ll.getVisibility() == View.GONE) {
									showHeadViewByStateId(ll);// 根据状态，显示视图相应部分
								} else {
									// 显示则隐藏，没必要
									ll.setVisibility(View.GONE);
									// 显示不对，需要刷新
									// showHeadViewByStateId(ll);
								}
								// Toast.makeText(Gtasks.this,
								// "headview clicked", 0)
								// .show();
							}
						});

					} else if (position > 1) {
						// 点击Item,动态生成的，隐藏和显示即可
						LinearLayout ll = ((LinearLayout) view
								.findViewById(R.id.operate));
						if (ll.getVisibility() == View.GONE) {
							ll.setVisibility(View.VISIBLE);
						}
						// 点击后，veiw将获取焦点，再次点击时，将触发 view.setOnClickListener
						// 而不是 onItemClick
						// final int p = position;
						view.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// position不准确，具体不明
								// Toast.makeText(Gtasks.this, "view点击了" + p, 0)
								// .show();
								LinearLayout linearLayout = ((LinearLayout) v
										.findViewById(R.id.operate));
								if (linearLayout.getVisibility() == View.VISIBLE) {
									linearLayout.setVisibility(View.GONE);
								} else {
									linearLayout.setVisibility(View.VISIBLE);
								}
							}
						});
					}
					// 如果业务逻辑写在这里，要注意以下点
					// 第0个下拉刷新 第1个 header 最后一个加载更多 忽略。其他的position-2
					// 0、1、2->0、3->1、4->2、5
					// 好在业务逻辑在设置适配器的时候就绑定了

					// 核心业务逻辑处理 这里的button 肯定已经初始化好了
					// 没有view.，会有意外！
					// view.findViewById(R.id.transaction).setOnClickListener(
					// new View.OnClickListener() {
					// @Override
					// public void onClick(View v) {
					// Toast.makeText(Gtasks.this,
					// "transaction点击了" + position, 0)
					// .show();
					// if (position > 0) {
					// startActivity(new Intent(Gtasks.this,
					// GtaskTransaction.class)
					// .putExtra("detail", userForms
					// .get(position - 1)
					// .getFileContent()));
					// }
					//
					// }
					// });

				}
			});

		} else {// 切换到其他标签加载
			// 参考: http://hi.baidu.com/su350380433/item/3e70f16022b0471b7ddeccd8
			// ((MyLVAdapter) lv_userForm.getAdapter()).notifyDataSetChanged();
			// ((LinearLayout) headView.findViewById(R.id.operate))
			// .setVisibility(View.VISIBLE);// 设置可见，触发单击事件，会设置成不可见，但是不对哎
			headView.performClick();// 模拟点击，以调用getView，刷新出委托人。焦点移交给view，可能导致点击无效
			((LinearLayout) headView.findViewById(R.id.operate))
					.setVisibility(View.GONE);// 放在后面，OK
			HeaderViewListAdapter ha = (HeaderViewListAdapter) lv_userForm
					.getAdapter();
			((MyLVAdapter) ha.getWrappedAdapter()).notifyDataSetChanged();
		}

	}

	/**
	 * 判断在哪种状态下(未办理，已办理，已委托) 点击的HeadView 以显示不同样式
	 * 
	 * @param ll
	 *            需要显示的view，显示的部分根据状态确定
	 */
	public void showHeadViewByStateId(View ll) {
		switch (rGroup.getCheckedRadioButtonId()) {
		case R.id.r1:// 未办理
			ll.setVisibility(View.VISIBLE);
			ll.findViewById(R.id.entrust).setVisibility(View.VISIBLE);
			((Button) ll.findViewById(R.id.transaction)).setText("办理");
			headView.findViewById(R.id.who).setVisibility(View.GONE);
			break;
		case R.id.r2:// 已办理
			ll.setVisibility(View.VISIBLE);
			ll.findViewById(R.id.entrust).setVisibility(View.GONE);
			((Button) ll.findViewById(R.id.transaction)).setText("查看");
			headView.findViewById(R.id.who).setVisibility(View.GONE);
			break;
		case R.id.r3:// 已委托

			ll.setVisibility(View.VISIBLE);
			ll.findViewById(R.id.entrust).setVisibility(View.GONE);
			((Button) ll.findViewById(R.id.transaction)).setText("查看");
			headView.findViewById(R.id.who).setVisibility(View.VISIBLE);
			break;
		}
	}

	private class GetDetailsTask extends AsyncTask<String, Void, Void> {

		private final ProgressDialog dialog = new ProgressDialog(Gtasks.this);
		View contentView = null;

		// String forms[];放在这里，过一会就被释放，导致forms为null，所以定义为全局。

		@Override
		protected void onPreExecute() {
			// 可以取消或者等待原线程 这里采取取消的方式
			if (currentAsyncTask != null) {
				// System.out.println("原线程：" + currentAsyncTask.hashCode()
				// + currentAsyncTask.getStatus());
				currentAsyncTask.cancel(true);
				// System.out.println("原线程取消后：" + currentAsyncTask.hashCode()
				// + currentAsyncTask.getStatus());
			}
			currentAsyncTask = this;
			if (contentView == null) {
				LayoutInflater inflater = getLayoutInflater();
				contentView = inflater.inflate(R.layout.customdialog, null);
			}
			((TextView) contentView.findViewById(R.id.textView1))
					.setText("正在获取数据，请稍后...");
			this.dialog.show();
			this.dialog.setContentView(contentView);
			System.out.println("GetDetailsTask 开始");
		}

		@Override
		protected Void doInBackground(String... params) {
			// 综合逻辑
			if (forms == null || forms.length == 0)
				forms = WebServiceUtils.getWFForm();

			List<UserForm> tempUserForms = null;
			// 如果未传递参数，那么params相当于 new String[]{} 。但是不为空，此时params.length == 0
			if (isAddmorehing) {
				// 新增

				int i = 0;// 一共5次重试机会 不额外机会
				while ((tempUserForms == null || tempUserForms.size() == 0)
						&& i < MyUtils.RETRY_TIMES) {
					System.out.println("请求次数：" + (i + 1));
					tempUserForms = WebServiceUtils.getUserForm(userid + "",
							params[0], params[1], ++currentPage,
							MyUtils.PAGE_SIZE);
					i++;
					// 一共5次重试机会 不是额外机会 所以 if
					// else判断必须放在whlie循环中，需要currentPage--还原到当前页
					// 如果获取0条数据，即没有获取到。导致如下情形：
					// 假设共有100条数据，可以加载10次到10页，但是由于某2次获取0条数据，
					// 导致加载10次到第10页时只有80条数据。此时80<100,满足加载更多的条件。
					// 再次加载更多，第11页，0条数据，此时，80<100
					// 再次加载更多，第12页，0条数据，此时，80<100
					// 再次加载更多....永远没有结束了
					if (tempUserForms.size() > 0) {
						tempUserForms.remove(tempUserForms.size() - 1);
					} else {
						// 所以这里加上else判断
						// 异常导致没有获取到下一页数据，currentPage还原为当前页。则现在情形如下：
						// 假设共有100条数据，加载了8次有了80页，但是由于第9次获取第9页 0条数据，
						// 则还原为当前第8页，即如下过程
						// 再次加载更多，第9页，0条数据，else分支，还原第8页，此时，80<100
						// 再次加载更多，第9页，0条数据，else分支，还原第8页，此时，80<100
						// 再次加载更多，第9页，0条数据，else分支，还原第8页，此时，80<100
						// 再次加载更多，第9页，10条数据，if分支，当前为第9页，此时，90<100，
						// 再次加载更多，第10页，10条数据，if分支，当前为第10页，此时，100=100，结束
						currentPage--;
					}
				}

				System.out.println("新增数据（不包括最后的页码）：" + tempUserForms.size());

				userForms.addAll(tempUserForms);
			} else {
				// 第一页或刷新
				// 一开始就已经获取了数据，所以spinner的position=0时，不需要触发选择事件，再次执行查询。
				// spinnerLastSelected的初始值为0，刚好不满足触发事件的条件，不查询。
				// 如果是-1，则导致首次查询2次。

				int i = 0;// 一共5次重试机会 不额外机会
				while ((tempUserForms == null || tempUserForms.size() == 0)
						&& i < MyUtils.RETRY_TIMES) {
					System.out.println("请求次数：" + (i + 1));
					tempUserForms = WebServiceUtils.getUserForm(userid + "",
							params[0], params[1], 1, MyUtils.PAGE_SIZE);
					i++;
				}

				// if else判断放在while里外都行
				if (tempUserForms.size() > 0) {
					UserForm lastUserForm = tempUserForms.remove(tempUserForms
							.size() - 1);
					allNum = Integer.parseInt(lastUserForm.getId());
				}
				System.out.println("第一页或刷新数据有（不包括最后的页码）:"
						+ tempUserForms.size());// 至少有一条页码数据，如果是0，则有异常
				userForms.clear();// 清空原始数据
				userForms.addAll(tempUserForms);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			System.out.println("GetDetailsTask 结束");
			if (spinnerAdapter == null && forms != null) {
				initSpinnerAdapter();
			}

			this.dialog.dismiss();
			if (isAddmorehing) {
				adapter.notifyDataSetChanged();
				isAddmorehing = false;
				lv_userForm.setSelection(currentPage * MyUtils.PAGE_SIZE
						- MyUtils.PAGE_SIZE + 2);// 加2是因为 加载更多、表头各占了一个位置

			} else if (isRefreshing) {

				// 如果不指定，那么可能情况：
				// 假设共有4页，加载到第2页。刷新到第1页。
				// 再次加载为第3页 和 第4页的数据。接着加载第5、6、7...页，没有数据返回。
				// 共有1、3、4页的数据，使得data.size()<allNum恒满足
				// 所以一直可以加载更多，就是没有数据
				currentPage = 1;// 刷新也是新的请求，重置第一页
				adapter.notifyDataSetChanged();
				isRefreshing = false;
				// 设置刷新时间
				lv_userForm.onRefreshComplete(new Date(System
						.currentTimeMillis()).toLocaleString());
			} else {// 第一页
				showData();
				// 设置刷新时间
				lv_userForm.onRefreshComplete(new Date(System
						.currentTimeMillis()).toLocaleString());
			}

			// 检查数据是否获取完毕（每次均执行）
			if (userForms.size() < allNum) {
				lv_userForm.onAddmoreComplete(false, userForms.size(), allNum);
			} else {
				lv_userForm.onAddmoreComplete(true, userForms.size(), allNum);
				CustomToast.show(Gtasks.this, "已经加载全部数据！");
			}
			// 有的时候数据有 就是不显示ListView 怎么回事呢？？？
			// 再次检查ListView是否加载成功---提供一个刷新按钮？
			// if (lv_userForm.getAdapter() == null) {
			// showData();
			// }
			// CustomToast.show(
			// Gtasks.this,
			// lv_userForm == null ? "lv_userForm is null" : lv_userForm
			// .toString());
			// CustomToast.show(Gtasks.this, "数据有:" + userForms.size());
			// CustomToast.show(Gtasks.this, "适配器:" + lv_userForm.getAdapter());
		}

	}

	class MyLVAdapter extends BaseAdapter {
		private Context context;

		public MyLVAdapter(Context c) {
			context = c;
			// 避免空指针异常
			if (userForms == null) {
				userForms = new ArrayList<UserForm>();
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return userForms.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.formitem, null);
				// 阻塞子控件抢夺焦点，listview的onitemclick就能被正确触发，
				// 同时对item内部的button等控件也没有影响, 他们在被点击时照样可以触发自身的点击事件。
				((LinearLayout) convertView.findViewById(R.id.formitem))
						.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
				viewHolder.no = (TextView) convertView.findViewById(R.id.no);
				viewHolder.sequence = (TextView) convertView
						.findViewById(R.id.sequence);
				viewHolder.originator = (TextView) convertView
						.findViewById(R.id.originator);
				viewHolder.state = (TextView) convertView
						.findViewById(R.id.state);
				viewHolder.formtype = (TextView) convertView
						.findViewById(R.id.formtype);
				viewHolder.workname = (TextView) convertView
						.findViewById(R.id.workname);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);
				// System.out.println("viewHolder.time " + viewHolder.time);
				// Button 没有初始化?需要多次调用!
				// 一定要convertView.findViewById 而不能直接
				// findViewById(即this.findViewById)，否则有nullpointException。即:前缀没有，会有意外！
				viewHolder.transaction = (Button) convertView
						.findViewById(R.id.transaction);
				// System.out.println("viewHolder.transaction "
				// + viewHolder.transaction);
				viewHolder.entrust = (Button) convertView
						.findViewById(R.id.entrust);
				viewHolder.who = (LinearLayout) convertView
						.findViewById(R.id.who);
				// System.out.println("viewHolder.entrust " +
				// viewHolder.entrust);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
				// System.out.println("重用 viewholder");
			}
			final UserForm userForm = userForms.get(position);
			viewHolder.no.setText(position + 1 + "");
			viewHolder.sequence.setText(userForm.getSequence());
			viewHolder.originator.setText(userForm.getFqUserName());
			viewHolder.state.setText(userForm.getState());
			viewHolder.formtype.setText(userForm.getFormName());
			viewHolder.workname.setText(userForm.getName());
			viewHolder.time.setText(userForm.getNowTimes());
			// 核心业务逻辑处理 写在这里
			// if (viewHolder.transaction != null)

			// 判断当前的待办类型:未办理，已办理，已委托 以决定处理界面
			switch (rGroup.getCheckedRadioButtonId()) {
			case R.id.r1:// 未办理
				viewHolder.entrust.setVisibility(View.VISIBLE);
				viewHolder.who.setVisibility(View.GONE);
				viewHolder.transaction.setText("办理");
				// 上面两句，解决界面显示混乱异常
				viewHolder.transaction
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// Toast.makeText(Gtasks.this,
								// "未办理 transaction点击了" + position, 0).show();
								startActivity(new Intent(Gtasks.this,
										GtaskTransaction.class).putExtra(
										"detail", userForm).putExtra("type",
										FormTypes.UNHANDLED));

							}
						});
				viewHolder.entrust
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								startActivity(new Intent(Gtasks.this,
										GtaskWT.class).putExtra("detail",
										userForm));
							}
						});
				break;

			case R.id.r2:// 已办理

				viewHolder.entrust.setVisibility(View.GONE);
				viewHolder.who.setVisibility(View.GONE);
				// viewHolder.transaction.setGravity(Gravity.CENTER);
				viewHolder.transaction.setText("查看");
				viewHolder.transaction
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// Toast.makeText(Gtasks.this,
								// "已办理transaction点击了" + position, 0).show();
								startActivity(new Intent(Gtasks.this,
										GtaskTransaction.class).putExtra(
										"detail", userForm).putExtra("type",
										FormTypes.HANDLED));
							}
						});
				break;
			case R.id.r3:// 已委托
				viewHolder.entrust.setVisibility(View.GONE);
				// viewHolder.transaction.setGravity(Gravity.CENTER);
				viewHolder.transaction.setText("查看");
				viewHolder.who.setVisibility(View.VISIBLE);
				((TextView) viewHolder.who.findViewById(R.id.towho))
						.setText(userForm.getWtUsername());
				viewHolder.transaction
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// Toast.makeText(Gtasks.this,
								// "已办理transaction点击了" + position, 0).show();
								startActivity(new Intent(Gtasks.this,
										GtaskTransaction.class).putExtra(
										"detail", userForm).putExtra("type",
										FormTypes.DELEGATED));
							}
						});
				break;
			}

			return convertView;
		}

		class ViewHolder {
			TextView no;
			TextView sequence;
			TextView originator;
			TextView state;
			TextView formtype;
			TextView workname;
			TextView time;
			Button transaction;
			Button entrust;
			LinearLayout who;
		}
	}
}
