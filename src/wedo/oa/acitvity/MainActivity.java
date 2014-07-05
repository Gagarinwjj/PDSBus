package wedo.oa.acitvity;

import wedo.oa.acitvity.news.NewsListActivity;
import wedo.oa.acitvity.userprofit.UserProfitActivity;
import wedo.oa.acitvity.workflow.Gtasks;
import wedo.oa.acitvity.workrep.WorkReportActivity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class MainActivity extends TabActivity implements OnTabChangeListener,
		OnCheckedChangeListener {

	private TabHost mTabHost;
	private TabWidget mTabWidget;
	LinearLayout title_bar;
	private boolean isMenuShowing = true;
	private int isMenuShowingCount = 0;
	private boolean isTitleBarShowing = true;
	AlertDialog exitDialog;
	int userid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initViews();
	}

	// 使用TabHost、TabActivity、ActivityGroup时 只要在自子类中添加即可
	// public void onResume() {
	// super.onResume();
	// MobclickAgent.onResume(this);
	// }
	//
	// public void onPause() {
	// super.onPause();
	// MobclickAgent.onPause(this);
	// }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		MobclickAgent.onKillProcess(this);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private void initViews() {
		((Button) findViewById(R.id.titlebar_left))
				.setVisibility(View.INVISIBLE);
		((TextView) findViewById(R.id.header_title)).setText("新闻中心");// 默认第一个新闻
		((Button) findViewById(R.id.workButton)).setVisibility(View.VISIBLE);// 显示代办
		((Button) findViewById(R.id.workButton))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						startActivity(new Intent(MainActivity.this,
								Gtasks.class).putExtra("userid", userid));
					}
				});
		title_bar = (LinearLayout) findViewById(R.id.include_title_bar);
		title_bar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				Animation animation = new TranslateAnimation(1, 0, 1, 0, 1, 0,
						1, -1f);// 向上
				animation.setDuration(100);
				// animation.setFillAfter(true);//设置这个会出现黑色背景。
				animation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						v.setVisibility(View.GONE);
					}
				});
				v.startAnimation(animation);
				isTitleBarShowing = false;
			}
		});
		initTabs();
		initRadioButtons();
		// 特殊用户
		if (getIntent().getIntExtra("auth", 0) == 3) {
			findViewById(R.id.radio_button3).setVisibility(View.VISIBLE);
		}
		userid = getIntent().getIntExtra("userid", 0);
	}

	private void initTabs() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		// 或者
		// mTabHost=getTabHost();
		mTabWidget = (TabWidget) findViewById(android.R.id.tabs);
		mTabHost.setOnTabChangedListener(this);
		setIndicator(R.drawable.selector_tab_recommend, 0, new Intent(
				MainActivity.this, NewsListActivity.class).putExtra("ftype", 1));// 新闻中心
		setIndicator(R.drawable.selector_tab_channel, 1, new Intent(
				MainActivity.this, NewsListActivity.class).putExtra("ftype", 3));// 公交一天
		setIndicator(0, 2,
				new Intent(MainActivity.this, NewsListActivity.class).putExtra(
						"ftype", 2));// 通知公告
		setIndicator(0, 3, new Intent(MainActivity.this,
				UserProfitActivity.class));// 财务查询
		setIndicator(0, 4, new Intent(MainActivity.this,
				WorkReportActivity.class));// 工作汇报
	}

	// icon 指定drawable的xml文件，这里没用到
	private void setIndicator(int icon, int tabid, Intent intent) {

		// View localView = LayoutInflater.from(this.mTabHost.getContext())
		// .inflate(R.layout.widget_tab, null);
		// ((ImageView) localView.findViewById(R.id.main_activity_tab_image))
		// .setBackgroundResource(icon);
		// TabHost.TabSpec localTabSpec = mTabHost
		// .newTabSpec(String.valueOf(tabid)).setIndicator(localView)
		// .setContent(intent);
		// mTabHost.addTab(localTabSpec);

		mTabHost.addTab(mTabHost.newTabSpec(String.valueOf(tabid))
				.setIndicator(String.valueOf(tabid))
				.setContent(new Intent(intent)));
	}

	private RadioGroup group;

	private void initRadioButtons() {
		group = (RadioGroup) findViewById(R.id.main_radio);
		group.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radio_button0:
			mTabHost.setCurrentTabByTag(String.valueOf(0));
			((TextView) findViewById(R.id.header_title)).setText("新闻中心");

			break;
		case R.id.radio_button1:
			mTabHost.setCurrentTabByTag(String.valueOf(1));
			((TextView) findViewById(R.id.header_title)).setText("公交一天");
			break;
		case R.id.radio_button2:
			mTabHost.setCurrentTabByTag(String.valueOf(2));
			((TextView) findViewById(R.id.header_title)).setText("通知公告");
			break;
		case R.id.radio_button3:
			mTabHost.setCurrentTabByTag(String.valueOf(3));
			((TextView) findViewById(R.id.header_title)).setText("财务查询");
			break;
		case R.id.radio_button4:
			mTabHost.setCurrentTabByTag(String.valueOf(4));
			((TextView) findViewById(R.id.header_title)).setText("工作汇报");
			break;
		default:
			break;
		}
	}

	@Override
	public void onTabChanged(String tabId) {
		int tabID = Integer.valueOf(tabId);

		for (int i = 0; i < mTabWidget.getChildCount(); i++) {
			if (i == tabID) {
				mTabWidget.getChildAt(Integer.valueOf(i))
						.setBackgroundResource(R.drawable.selector_tabs);
			} else {
				mTabWidget.getChildAt(Integer.valueOf(i))
						.setBackgroundResource(R.drawable.selector_tabs);
			}
		}
	}

	// TabActivity中onKeyDown无效是因为冲突,于是在上一层方法处理。
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

			// new AlertDialog.Builder(this).setTitle("提醒")
			// .setIcon(R.drawable.icon_exit).setMessage("您确认退出客户端？")
			// .setPositiveButton("确定", new OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// dialog.dismiss();
			// // 还是不能完全退出
			// android.os.Process.killProcess(android.os.Process
			// .myPid());
			// // 或者
			// // System.exit(0);
			// }
			// }).setNegativeButton("取消", new OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// dialog.dismiss();
			// }
			// }).create().show();
			// 有的机器上弹出一个 有的机器上弹出两个。 解决方法：dialog变为全局变量，加一个NULL判断
			if (exitDialog == null) {
				exitDialog = new AlertDialog.Builder(this).create();
				LayoutInflater inflater = LayoutInflater
						.from(MainActivity.this);
				View dialog_view = inflater.inflate(R.layout.widget_dialog,
						null);
				dialog_view.findViewById(R.id.btn_confirm).setOnClickListener(
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								MainActivity.this.finish();
							}
						});
				dialog_view.findViewById(R.id.btn_cancel).setOnClickListener(
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								exitDialog.dismiss();
								exitDialog = null;// 确保下一次if(exitDialog==null)判断为true，弹出提示框。
							}
						});
				exitDialog.show();
				exitDialog.setContentView(dialog_view);
			} else {
				exitDialog.show();
			}
			return true;
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			// 自己调用异常，系统又会调用一次，于是要做的就是让系统调用的那一次失效，加入一个isMenuShowingCount变量
			// 即 自己：系统 --> 0:1 2:3 4:5 ..... 可以放在一个大if里，只处理偶数。这里没有放到大if，放在了每次判断。
			if (isMenuShowing && isMenuShowingCount % 2 == 0) {// 显示中&&自己点击
				group.setVisibility(View.GONE);
				AnimationSet animationSet = new AnimationSet(false);
				Animation translateAnimation = new TranslateAnimation(1, 0, 1,
						-1, 1, 0, 1, 0);
				translateAnimation.setDuration(1000);
				Animation alphaAnimation = new AlphaAnimation(1, 0);
				alphaAnimation.setDuration(1000);
				animationSet.addAnimation(translateAnimation);
				animationSet.addAnimation(alphaAnimation);

				group.startAnimation(animationSet);

				isMenuShowing = !isMenuShowing;// 有效才变化，即if else条件满足之一才会变
			} else if (isMenuShowingCount % 2 == 0) {// 不显示&&自己点击
				// group.setVisibility(View.VISIBLE);
				AnimationSet animationSet = new AnimationSet(false);
				Animation translateAnimation = new TranslateAnimation(1, -1, 1,
						0, 1, 0, 1, 0);
				translateAnimation.setDuration(1000);
				Animation alphaAnimation = new AlphaAnimation(0, 1);
				alphaAnimation.setDuration(1000);
				animationSet.addAnimation(translateAnimation);
				animationSet.addAnimation(alphaAnimation);
				animationSet.setFillAfter(true);
				animationSet.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						group.setVisibility(View.VISIBLE);
					}
				});
				group.startAnimation(animationSet);// gone状态下可以播放动画？实测可以。

				// 如果标题没有显示则顺带显示
				if (!isTitleBarShowing) {
					title_bar.setVisibility(View.VISIBLE);
					isTitleBarShowing = true;
				}
				isMenuShowing = !isMenuShowing;// 有效才变化，即if else条件满足之一才会变
			}
			isMenuShowingCount++;// 每次变化
			return true;
		} else {
			// TODO Auto-generated method stub
			return super.dispatchKeyEvent(event);
		}
	}

}
