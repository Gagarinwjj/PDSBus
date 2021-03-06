package wedo.oa.acitvity.workflow;

import java.util.ArrayList;

import wedo.oa.acitvity.AppUserInfo;
import wedo.oa.acitvity.R;
import wedo.oa.beans.DepNode;
import wedo.oa.beans.NextStep;
import wedo.oa.beans.People;
import wedo.oa.beans.ProcessResult;
import wedo.oa.beans.UserForm;
import wedo.oa.beans.WorkFlowNode;
import wedo.oa.utils.MyUtils;
import wedo.oa.utils.WebServiceUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class GtaskSpBhNext extends Activity {
	private EditText jbrEditText;
	private EditText jbbmEditText;
	private EditText jbjsEditText;
	private EditText spjsryEditText;

	Button jbrButton;
	Button jbbmButton;
	Button jbjsButton;
	Button spjsButton;
	Button rejectButton;// 公用布局中的 提交 改为 驳回

	AppUserInfo aui;
	private TextView titleTextView;
	private Spinner spinner;
	private ArrayAdapter<NextStep> spinnerAdapter;
	private ArrayList<NextStep> spinnerData;// 不用HashMap<String, String>
											// 用JavaBean也行
	private ArrayList<WorkFlowNode> workFlowNodes;
	UserForm userForm;
	private int spinnerLastSelected = -1;// 初始化为-1，确保position=0的初始状态，自动查询。
	private long childIDLastSelected = -1;// 初始化为-1，确保position=0的初始状态，自动查询。
	private WorkFlowNode currentNode;
	private WorkFlowNode selectedNode;// jbr，jbbm，jbjs参数由此初始化

	private LayoutInflater inflater;
	ArrayList<People> jbrArrayList;
	ArrayList<People> jbbmArrayList;
	ArrayList<People> jbjsArrayList;
	String[] depNames = { "总公司机关", "营运单位", "二级单位" };
	ArrayList<ArrayList<DepNode>> depItems;
	ArrayList<People> depPeopleArrayList = new ArrayList<People>();

	ArrayList<People> spjsArrayList = new ArrayList<People>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spnext);//公用‘审批通过’的布局
		initView();
		aui = (AppUserInfo) getApplication();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		// 返回
		((Button) findViewById(R.id.titlebar_left))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						GtaskSpBhNext.this.finish();
					}
				});
		((TextView) findViewById(R.id.header_title)).setText("审批驳回");
		// 经办人
		jbrEditText = (EditText) this.findViewById(R.id.jbrNames);
		jbrEditText.setKeyListener(null);// 设置只读
		jbrEditText.setCursorVisible(false);// 光标不可见
		// 经办部门
		jbbmEditText = (EditText) this.findViewById(R.id.jbbmNames);
		jbbmEditText.setKeyListener(null);// 设置只读
		jbbmEditText.setCursorVisible(false);// 光标不可见
		// 经办角色
		jbjsEditText = (EditText) this.findViewById(R.id.jbjsNames);
		jbjsEditText.setKeyListener(null);// 设置只读
		jbjsEditText.setCursorVisible(false);// 光标不可见
		// 审批接受人员
		spjsryEditText = (EditText) this.findViewById(R.id.spjsryNames);
		spjsryEditText.setKeyListener(null);// 设置只读
		spjsryEditText.setCursorVisible(false);// 光标不可见
		// 标题
		titleTextView = (TextView) this.findViewById(R.id.title);
		userForm = (UserForm) getIntent().getSerializableExtra("detail");
		titleTextView.setText(userForm.getName() + "\n流水号:"
				+ userForm.getSequence() + "   当前步骤:"
				+ userForm.getUpNodeName());
		// 下拉框
		spinner = (Spinner) this.findViewById(R.id.nextstep);
		// 按钮
		jbrButton = (Button) this.findViewById(R.id.jbrBtn);
		jbbmButton = (Button) this.findViewById(R.id.jbbmBtn);
		jbjsButton = (Button) this.findViewById(R.id.jbjsBtn);
		spjsButton = (Button) this.findViewById(R.id.spjsBtn);
		((LinearLayout) findViewById(R.id.smsll)).setVisibility(View.GONE);// 隐藏短信选择视图，不需要

		inflater = LayoutInflater.from(this);
		// step1 currentNode
		// new WorkFlowNodeTask().execute(userForm.getUpNodeId(),
		// "initUI");//对于驳回而言，不需要判断是否结束，直接 step3。下方所有关于initUI均可忽略
		new SpinnerDataTask().execute(userForm.getId());
		rejectButton = (Button) findViewById(R.id.submit);
		rejectButton.setText("驳回");
		rejectButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (spinnerLastSelected > 0 && spjsArrayList.size() <= 0) {
					Toast.makeText(GtaskSpBhNext.this, "请选择审批接受人员",
							Toast.LENGTH_SHORT).show();
				} else {
					// 驳回给发起人（spinnerLastSelected=0） 或者
					// 驳回给指定人（spjsArrayList.size()>0）
					// 由于直接step3来获取下拉数据，所以spinnerLastSelected=-1 不可能
					new RejectTask().execute();
				}

			}
		});
	}

	/**
	 * 初始化界面(对于驳回而言，不需要判断是否结束，直接 step3) initUI忽略
	 */
	private void initUI(WorkFlowNode currentNode) {
		if (currentNode.getNodeSite().equals("结束")) {
			this.findViewById(R.id.panel).setVisibility(View.GONE);

			TextView textViewHint = ((TextView) this
					.findViewById(R.id.itemhint));
			textViewHint.setVisibility(View.VISIBLE);
			textViewHint.setText("当前步骤为结束");
		} else {
			// step3 进一步初始化界面> 获取下拉框数据 selectedNode
			new SpinnerDataTask().execute(userForm.getId());
			// 审批按钮的响应事件 (移到selectedNode 获取后再初始化)
			// initSpBtns(currentNode);
		}
	}

	/**
	 * 审批按钮的初始化
	 * 
	 * @param currentNode
	 */
	private void initSpBtns() {

		if (selectedNode != null) {
			jbrButton.setText("[选择审批人员]");
			jbbmButton.setText("[选择审批人员]");
			jbjsButton.setText("[选择审批人员]");
			spjsButton.setText("[选择审批人员]");

			jbrButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// selectedNode 容易被释放==>其实是自己释放了
					// System.out.println("selectedNode:" + selectedNode);

					if ("0".equals(selectedNode.getJBRObjectId())
							|| "未设置".equals(selectedNode.getJBRObjectName())) {
						Toast.makeText(GtaskSpBhNext.this, "未设置",
								Toast.LENGTH_SHORT).show();
					} else {

						// 定义弹窗
						final AlertDialog dialog = new AlertDialog.Builder(
								GtaskSpBhNext.this).create();

						// 1、构建人事选择View
						View peopleView = inflater.inflate(
								R.layout.check_people, null);
						// 2、初始化界面元素
						TextView ptv = (TextView) peopleView
								.findViewById(R.id.title);
						ptv.setText("从经办人中选择");

						// 3、按钮事件处理
						Button conformBtn = (Button) peopleView
								.findViewById(R.id.btn_conform);// id写错，找不到控件，null异常
						conformBtn
								.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										// 勾选：不存在则添加
										// 未勾选：存在则删除
										for (People people : jbrArrayList) {
											// 勾选
											if (people.isChecked()) {
												// 审批接受人员不存在则添加
												if (!spjsArrayList
														.contains(people)) {
													spjsArrayList.add(people);
												}
												// 其他审批人同步勾选状态
												syncIsChecked(jbrArrayList,
														people, true);
											} else {
												// 未勾选
												// 审批接受人员存在则删除
												if (spjsArrayList
														.contains(people)) {
													spjsArrayList
															.remove(people);
												}
												// 其他审批人同步未勾选状态
												syncIsChecked(jbrArrayList,
														people, false);

											}
										}
										// 设置审批人员文本显示
										spjsryEditText.setText(MyUtils
												.getIdsAndNames(spjsArrayList)[1]);

										dialog.dismiss();// pgv.getAdapter()会被重置为null
									}
								});

						Button cancleBtn = (Button) peopleView
								.findViewById(R.id.btn_cancel);
						cancleBtn
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {
										dialog.dismiss();// pgv.getAdapter()会被重置为null
									}
								});

						// 4、GridView初始化
						GridView pgv = (GridView) peopleView
								.findViewById(R.id.peoples);
						// dismiss后pgv.getAdapter()就是null，这里恒满足
						// 导致jbrArrayList每次都是重新初始化的，
						// 所以加上null判断，确保只初始化一次
						// if (pgv.getAdapter() == null) {
						// System.out.println("gridview 适配器为null，重新加载");
						if (jbrArrayList == null) {// 确保只初始化一次
							jbrArrayList = MyUtils.getPeoples(
									selectedNode.getJBRObjectName(),
									selectedNode.getJBRObjectId());
							initWithChecked(jbrArrayList);
						}
						pgv.setAdapter(new PeopleAdapter(jbrArrayList));// 设置适配器，每次都是new的
						pgv.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								// 此处无法监听到事件，被子控件的事件响应抢去了，解决方案：
								// 1、在子控件的响应函数中处理业务逻辑
								// 2、让子控件放弃响应，以便这里接受到响应做业务处理
								System.out.println(position + "clicked");
							}
						});
						// }

						dialog.show();// 一定要先show 否则有错
						dialog.setContentView(peopleView);

					}
				}
			});

			jbbmButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if ("0".equals(selectedNode.getJBBMObjectId())
							|| "未设置".equals(selectedNode.getJBBMObjectName())) {
						Toast.makeText(GtaskSpBhNext.this, "未设置",
								Toast.LENGTH_SHORT).show();
					} else {
						if (jbbmArrayList == null) {// 确保只初始化一次
							new PeoplesTask().execute("jbbm",
									selectedNode.getJBBMObjectId());
						} else {
							// 定义弹窗

							final AlertDialog dialog = new AlertDialog.Builder(
									GtaskSpBhNext.this).create();

							// 1、构建人事选择View
							View peopleView = inflater.inflate(
									R.layout.check_people, null);
							// 2、初始化界面元素
							TextView ptv = (TextView) peopleView
									.findViewById(R.id.title);
							ptv.setText("从经办部门中选择");

							// 3、按钮事件处理
							Button conformBtn = (Button) peopleView
									.findViewById(R.id.btn_conform);// id写错，找不到控件，null异常
							conformBtn
									.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											// 勾选：不存在则添加
											// 未勾选：存在则删除
											for (People people : jbbmArrayList) {
												// 勾选
												if (people.isChecked()) {
													// 审批接受人员不存在则添加
													if (!spjsArrayList
															.contains(people)) {
														spjsArrayList
																.add(people);
													}
													// 其他审批人同步勾选状态
													syncIsChecked(
															jbbmArrayList,
															people, true);
												} else {
													// 未勾选
													// 审批接受人员存在则删除
													if (spjsArrayList
															.contains(people)) {
														spjsArrayList
																.remove(people);
													}
													// 其他审批人同步未勾选状态
													syncIsChecked(
															jbbmArrayList,
															people, false);

												}
											}
											// 设置审批人员文本显示
											spjsryEditText.setText(MyUtils
													.getIdsAndNames(spjsArrayList)[1]);

											dialog.dismiss();// pgv.getAdapter()会被重置为null
										}
									});

							Button cancleBtn = (Button) peopleView
									.findViewById(R.id.btn_cancel);
							cancleBtn
									.setOnClickListener(new View.OnClickListener() {

										@Override
										public void onClick(View v) {
											dialog.dismiss();// pgv.getAdapter()会被重置为null
										}
									});

							// 4、GridView初始化
							GridView pgv = (GridView) peopleView
									.findViewById(R.id.peoples);
							pgv.setAdapter(new PeopleAdapter(jbbmArrayList));// 设置适配器，每次都是new的
							dialog.show();// 一定要先show 否则有错
							dialog.setContentView(peopleView);

						}
					}
				}
			});

			jbjsButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					if ("0".equals(selectedNode.getJBJSObjectId())
							|| "未设置".equals(selectedNode.getJBJSObjectName())) {
						Toast.makeText(GtaskSpBhNext.this, "未设置",
								Toast.LENGTH_SHORT).show();
					} else {
						if (jbjsArrayList == null) {// 确保只初始化一次
							new PeoplesTask().execute("jbjs",
									selectedNode.getJBJSObjectId());
						} else {
							// 定义弹窗

							final AlertDialog dialog = new AlertDialog.Builder(
									GtaskSpBhNext.this).create();

							// 1、构建人事选择View
							View peopleView = inflater.inflate(
									R.layout.check_people, null);
							// 2、初始化界面元素
							TextView ptv = (TextView) peopleView
									.findViewById(R.id.title);
							ptv.setText("从经办角色中选择");

							// 3、按钮事件处理
							Button conformBtn = (Button) peopleView
									.findViewById(R.id.btn_conform);// id写错，找不到控件，null异常
							conformBtn
									.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											// 勾选：不存在则添加
											// 未勾选：存在则删除
											for (People people : jbjsArrayList) {
												// 勾选
												if (people.isChecked()) {
													// 审批接受人员不存在则添加
													if (!spjsArrayList
															.contains(people)) {
														spjsArrayList
																.add(people);
													}
													// 其他审批人同步勾选状态
													syncIsChecked(
															jbjsArrayList,
															people, true);
												} else {
													// 未勾选
													// 审批接受人员存在则删除
													if (spjsArrayList
															.contains(people)) {
														spjsArrayList
																.remove(people);
													}
													// 其他审批人同步未勾选状态
													syncIsChecked(
															jbjsArrayList,
															people, false);

												}
											}
											// 设置审批人员文本显示
											spjsryEditText.setText(MyUtils
													.getIdsAndNames(spjsArrayList)[1]);

											dialog.dismiss();// pgv.getAdapter()会被重置为null
										}
									});

							Button cancleBtn = (Button) peopleView
									.findViewById(R.id.btn_cancel);
							cancleBtn
									.setOnClickListener(new View.OnClickListener() {

										@Override
										public void onClick(View v) {
											dialog.dismiss();// pgv.getAdapter()会被重置为null
										}
									});

							// 4、GridView初始化
							GridView pgv = (GridView) peopleView
									.findViewById(R.id.peoples);
							pgv.setAdapter(new PeopleAdapter(jbjsArrayList));// 设置适配器，每次都是new的
							dialog.show();// 一定要先show 否则有错
							dialog.setContentView(peopleView);

						}
					}

				}
			});

			// 实测发现，该按钮的点击，消耗很多资源，会将selectedNode置null释放，导致空指针异常
			// 为了避免selectedNode 被释放，设为static
			// 晕，原来是自己置空了
			spjsButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (depItems == null || depItems.size() == 0) {
						new DepItemsTask().execute();
					} else {
						// 定义弹窗

						final AlertDialog dialog = new AlertDialog.Builder(
								GtaskSpBhNext.this).create();

						// 1、构建人事选择View
						View peopleView = inflater.inflate(
								R.layout.check_dep_people, null);
						// 2、初始化界面元素
						TextView ptv = (TextView) peopleView
								.findViewById(R.id.title);
						ptv.setText("选择审批人员");

						// 3、按钮事件处理
						Button conformBtn = (Button) peopleView
								.findViewById(R.id.btn_conform);// id写错，找不到控件，null异常
						conformBtn
								.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										// 勾选：不存在则添加
										// 未勾选：存在则删除
										for (People people : depPeopleArrayList) {
											// 勾选
											if (people.isChecked()) {
												// 审批接受人员不存在则添加
												if (!spjsArrayList
														.contains(people)) {
													spjsArrayList.add(people);
												}

											} else {
												// 未勾选
												// 审批接受人员存在则删除
												if (spjsArrayList
														.contains(people)) {
													spjsArrayList
															.remove(people);
												}

											}
										}
										// 设置审批人员文本显示
										spjsryEditText.setText(MyUtils
												.getIdsAndNames(spjsArrayList)[1]);

										dialog.dismiss();// pgv.getAdapter()会被重置为null
									}
								});

						Button cancleBtn = (Button) peopleView
								.findViewById(R.id.btn_cancel);
						cancleBtn
								.setOnClickListener(new View.OnClickListener() {

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
						depsExListView.setAdapter(new DepsAdapter(
								GtaskSpBhNext.this));
						depsExListView
								.setOnChildClickListener(new OnChildClickListener() {

									@Override
									public boolean onChildClick(
											ExpandableListView parent, View v,
											int groupPosition,
											int childPosition, long id) {

										// System.out.println(groupPosition +
										// "-"
										// + childPosition + "-" + id);
										if (id != childIDLastSelected) {
											// 获取部门对应的人员数据
											new PeoplesTask(pgv).execute(
													"deps", id + "");
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
		} else {
			// 选择了开始步骤
			jbrButton.setText("[驳回给发起人]");
			jbrButton.setOnClickListener(null);

			jbbmButton.setText("[驳回给发起人]");
			jbbmButton.setOnClickListener(null);

			jbjsButton.setText("[驳回给发起人]");
			jbjsButton.setOnClickListener(null);

			spjsButton.setText("[驳回给发起人]");
			spjsButton.setOnClickListener(null);
		}
	}

	/**
	 * 初始化审批人员界面
	 */
	private void initPeople(WorkFlowNode node) {
		jbrEditText.setText(node.getJBRObjectName());
		jbbmEditText.setText(node.getJBBMObjectName());
		jbjsEditText.setText(node.getJBJSObjectName());
	}

	private void initSpinnerAdapter() {

		spinnerAdapter = new ArrayAdapter<NextStep>(GtaskSpBhNext.this,
				R.layout.spinner_item, spinnerData);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);
		// java.lang.RuntimeException: setOnItemClickListener cannot be used
		// with a spinner.

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (spinnerLastSelected != position) {

					// 清空原来的人员选择数据
					if (jbrArrayList != null) {
						jbrArrayList.clear();
						jbrArrayList = null;// 重置为null，以便满足==null的条件，重新查询赋值
					}
					if (jbbmArrayList != null) {
						jbbmArrayList.clear();
						jbbmArrayList = null;
					}
					if (jbjsArrayList != null) {
						jbjsArrayList.clear();

						jbjsArrayList = null;
					}
					spjsArrayList.clear();

					spjsryEditText.setText("");
					jbrEditText.setText("");
					jbbmEditText.setText("");
					jbjsEditText.setText("");
					// 部门人员还在，但都还原为未选中状态
					for (People p : depPeopleArrayList) {
						p.setChecked(false);
					}

					if (position != 0) {
						// 查询数据后再init
						new WorkFlowNodeTask().execute(spinnerData
								.get(position).getID(), "initPeople");
					} else {
						// 开始步奏，不用查询数据，直接init
						selectedNode = null;
						GtaskSpBhNext.this.initSpBtns();
					}

					spinnerLastSelected = position;
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
	}

	/**
	 * 同步审批人勾选状态:将已经勾选或未勾选的人，在其他列表中也同步起来
	 * 
	 * @param soruce
	 *            照成影响的那个ArrayList
	 */
	private void syncIsChecked(ArrayList<People> soruce, People people,
			boolean isChecked) {
		if (jbrArrayList != null && jbrArrayList != soruce) {
			if (jbrArrayList.contains(people)) {
				int index = jbrArrayList.indexOf(people);
				jbrArrayList.get(index).setChecked(isChecked);
			}
		}
		if (jbbmArrayList != null && jbbmArrayList != soruce) {
			if (jbbmArrayList.contains(people)) {
				int index = jbbmArrayList.indexOf(people);
				jbbmArrayList.get(index).setChecked(isChecked);
			}
		}
		if (jbjsArrayList != null && jbjsArrayList != soruce) {
			if (jbjsArrayList.contains(people)) {
				int index = jbjsArrayList.indexOf(people);
				jbjsArrayList.get(index).setChecked(isChecked);
			}
		}
	}

	/**
	 * 对于初次加载的人员数据，同步已经勾选的人
	 * 
	 * @param firstArrayList
	 *            初次加载的人员数据
	 */
	private void initWithChecked(ArrayList<People> firstArrayList) {
		if (spjsArrayList.size() > 0) {
			for (People people : spjsArrayList) {
				if (firstArrayList.contains(people)) {
					int index = firstArrayList.indexOf(people);
					firstArrayList.get(index).setChecked(true);
				}
			}
		}
	}

	// 人事适配器
	private class PeopleAdapter extends BaseAdapter {

		// 数组的方法比较少，用ArrayList方便
		// private People[] peoples;

		ArrayList<People> peoples;

		public PeopleAdapter(ArrayList<People> peoples) {
			this.peoples = peoples;
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
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.checkpeople_item, null);
				holder.cb = (CheckBox) convertView.findViewById(R.id.cb);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final People people = peoples.get(position);
			// System.out.println("getView" + people.getName()+
			// people.isChecked());
			holder.cb.setText(people.getUserName());
			holder.cb.setChecked(people.isChecked());
			// CheckedChanged被同步触发
			// holder.cb.setOnCheckedChangeListener(new
			// OnCheckedChangeListener() {
			//
			// @Override
			// public void onCheckedChanged(CompoundButton buttonView,
			// boolean isChecked) {
			// // 勾选状态true->移出可视区域，销毁，勾选重置为false->
			// // 状态改变，触发事件，people的isChecked又设置为false
			// // 即勾选状态被还原不勾选
			// // people.setChecked(isChecked);
			// System.out.println(people.getName() + " （勾选|销毁）状态改变为 "
			// + isChecked);
			// }
			// });

			// ClickListener被同步触发
			holder.cb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					// people.setChecked(!cb.isChecked());//cb.isChecked与ui矛盾
					people.setChecked(!people.isChecked());
					cb.setChecked(people.isChecked());
					// System.out.println(people.getUserName() +
					// "clicked set to "
					// + people.isChecked());
				}
			});
			return convertView;
		}

		class ViewHolder {
			CheckBox cb;
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

	private class SpinnerDataTask extends AsyncTask<String, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(
				GtaskSpBhNext.this);
		View contentView = null;

		// String forms[];放在这里，过一会就被释放，导致forms为null，所以定义为全局。

		@Override
		protected void onPreExecute() {
			if (contentView == null) {
				LayoutInflater inflater = getLayoutInflater();
				contentView = inflater.inflate(R.layout.customdialog, null);
			}
			((TextView) contentView.findViewById(R.id.textView1))
					.setText("正在获取步骤，请稍后...");
			this.dialog.show();
			this.dialog.setContentView(contentView);
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			// spinnerData = WebServiceUtils.getNextStep(userForm.getId());
			spinnerData = (ArrayList<NextStep>) WebServiceUtils.execSql(
					"proc_transaction_getBhNextStep @id=" + params[0],
					NextStep.class);
			spinnerData.add(0, new NextStep("0", "开始"));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (spinnerAdapter == null && spinnerData != null) {
				// step4
				initSpinnerAdapter();
			}

			this.dialog.dismiss();
		}
	}

	private class WorkFlowNodeTask extends AsyncTask<String, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(
				GtaskSpBhNext.this);
		View contentView = null;
		String how;

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

			selectedNode = null;// 清空原选择的节点数据
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			// spinnerData = WebServiceUtils.getNextStep(userForm.getId());
			workFlowNodes = (ArrayList<WorkFlowNode>) WebServiceUtils.execSql(
					"getWorkFlowNode  @id=" + params[0], WorkFlowNode.class);
			how = params[1];
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// step2
			if (how.equals("initUI") && workFlowNodes != null
					&& workFlowNodes.size() > 0) { //initUI忽略
				currentNode = workFlowNodes.get(0);
				initUI(currentNode);
			}// step5
			else if (how.equals("initPeople") && workFlowNodes != null
					&& workFlowNodes.size() > 0) {
				selectedNode = workFlowNodes.get(0);
				initPeople(selectedNode);
				// 审批按钮的响应事件 (移到selectedNode 获取后再初始化)
				initSpBtns();
			}
			this.dialog.dismiss();
		}
	}

	private class PeoplesTask extends AsyncTask<String, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(
				GtaskSpBhNext.this);
		View contentView = null;
		GridView gridView;
		String how;

		public PeoplesTask() {

		}

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

			how = params[0];
			if (how.equals("jbbm")) {
				// jbbmArrayList会被重新赋值，适配器也会重新初始化为基于新数据源jbbmArrayList的数据，所以可以直接赋值
				jbbmArrayList = (ArrayList<People>) WebServiceUtils.execSql(
						"getJBBMPeoples @depids='" + params[1] + "'",
						People.class);

				// 完善1、初次取得数据，同步已经勾选的人
				initWithChecked(jbbmArrayList);

			} else if (how.equals("jbjs")) {
				jbjsArrayList = (ArrayList<People>) WebServiceUtils.execSql(
						"getJBJSPeoples @roleids='" + params[1] + "'",
						People.class);
				initWithChecked(jbjsArrayList);
			} else if (how.equals("deps")) {
				// 1、直接赋值改变了depPeopleArrayList的指向
				// 2、适配器会重新初始化为基于新depPeopleArrayList指向的数据
				// 3、适配器的数据源变化了
				// 4、但是对于重新初始化的新的数据源数据，DataSetChanged没有变化，
				// 5、notifyDataSetChanged 没有反应的
				// depPeopleArrayList = (ArrayList<People>) WebServiceUtils
				// .execSql("getDepPeoples @depid=" + params[1],
				// People.class);

				// 1、 这样做，即使适配器重新初始化，但是数据源没变。
				// 2、由于改变了数据源数据，notifyDataSetChanged有效。
				ArrayList<People> peoples = (ArrayList<People>) WebServiceUtils
						.execSql("getDepPeoples @depid=" + params[1],
								People.class);
				initWithChecked(peoples);
				depPeopleArrayList.clear();
				depPeopleArrayList.addAll(peoples);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (how.equals("jbbm")) {
				jbbmButton.performClick();
			} else if (how.equals("jbjs")) {
				jbjsButton.performClick();
			} else if (how.equals("deps")) {
				((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();// 数据源没变，数据源内数据改变，有效触发。
				// 如果直接赋值给depPeopleArrayList，那么：数据源改变，数据源数据没变（初始加载的，当然没变），无效触发。
			}
			this.dialog.dismiss();
		}
	}

	private class DepItemsTask extends AsyncTask<Void, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(
				GtaskSpBhNext.this);
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

			// selectedNode = null;// 清空原选择的节点数据
			// 我擦！查了半天原来是这里置空引起空指针异常。
			// 在SpNext逻辑中，如果用spjs选择，那么原来的选择节点数据用不到，可以置空。
			// 但是这里的逻辑是需要它的，不可以置空。否则按钮点击报告空指针异常
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
			spjsButton.performClick();
			this.dialog.dismiss();
		}

	}

	private class RejectTask extends AsyncTask<Void, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(
				GtaskSpBhNext.this);
		View contentView = null;
		ProcessResult pr;

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
			String[] idsAndNames = MyUtils.getIdsAndNames(spjsArrayList);

			pr = WebServiceUtils.getRejectProcessResult(userForm.getId(), aui
					.getUserID(), aui.getUserName(), idsAndNames[0],
					idsAndNames[1], spinnerData.get(spinnerLastSelected)
							.getID());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			this.dialog.dismiss();
			if (pr != null) {// 健壮性判断
				Toast.makeText(GtaskSpBhNext.this, pr.getMsg(),
						Toast.LENGTH_SHORT).show();
				if (pr.getResult() == 1) {
					Intent intent = new Intent(GtaskSpBhNext.this, Gtasks.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// Single_Task
					intent.putExtra("userid", Integer.parseInt(aui.getUserID()));
					startActivity(intent);
				}
			}
		}

	}

}
