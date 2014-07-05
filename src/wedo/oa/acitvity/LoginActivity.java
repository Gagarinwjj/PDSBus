package wedo.oa.acitvity;

import wedo.oa.beans.LoginState;
import wedo.oa.utils.WebServiceUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class LoginActivity extends Activity {
	String username;
	String password;
	CheckBox rememberpass;
	CheckBox autologin;
	int auth;
	LoginState loginState;
	TextView tv_launchApp;
	LoginTask lt;
	boolean isCancled = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		tv_launchApp = (TextView) findViewById(R.id.launch_app);
		Typeface typeface = Typeface.createFromAsset(this.getAssets(),
				"fonts/some.ttf");// 2.57M;华文行楷3.83M太大，native typeface cannot be
									// made。估计要3M以下

		tv_launchApp.setTypeface(typeface);
		ImageView ihead = (ImageView) findViewById(R.id.head);
		AlphaAnimation animation = new AlphaAnimation(0.0f, (float) 1.0);
		animation.setDuration(2500);
		ihead.startAnimation(animation);
		rememberpass = (CheckBox) this.findViewById(R.id.rememberPassword);
		autologin = (CheckBox) this.findViewById(R.id.autoLogin);
		initCheckBoxState();
		Button loginbtn = (Button) findViewById(R.id.login_bt);

		loginbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				username = ((EditText) findViewById(R.id.username)).getText()
						.toString();
				password = ((EditText) findViewById(R.id.password)).getText()
						.toString();
				if (username.trim().equals("") || password.trim().equals("")) {
					Toast.makeText(LoginActivity.this, "请输入用户名和密码",
							Toast.LENGTH_SHORT).show();
					return;
				}
				isCancled = false;
				lt = new LoginTask();
				lt.execute();

			}

		});

	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void initCheckBoxState() {
		SharedPreferences sp = getSharedPreferences("account", MODE_PRIVATE);
		rememberpass.setChecked(sp.getBoolean("rememberpass", false));
		autologin.setChecked(sp.getBoolean("autologin", false));

		String sp_username = sp.getString("username", "");
		String sp_password = sp.getString("password", "");
		if (rememberpass.isChecked()) {
			((EditText) findViewById(R.id.username)).setText(sp_username);
			((EditText) findViewById(R.id.password)).setText(sp_password);
		}
		if (autologin.isChecked()) {
			if (!isCancled) {
				username = sp_username;
				password = sp_password;
				lt = new LoginTask();
				lt.execute();
			}
		}

	}

	/**
	 * 检查密码状态
	 */
	private void checkRememberPassState() {
		if (rememberpass.isChecked()) {
			SharedPreferences sp = getSharedPreferences("account", MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putString("username", username);
			editor.putString("password", password);
			editor.commit();
		}

	}

	private class LoginTask extends AsyncTask<Void, Void, Void> {

		// AlertDialog.Builder builder = new AlertDialog.Builder(
		// LoginActivity.this);
		// AlertDialog dialog;

		private final ProgressDialog dialog = new ProgressDialog(
				LoginActivity.this);

		@Override
		protected void onPreExecute() {
			// dialog��ʽ����֮setContentView
			LayoutInflater inflater = getLayoutInflater();
			View contentView = inflater.inflate(R.layout.customdialog, null);
			// ImageView iView = (ImageView) contentView
			// .findViewById(R.id.imageView1);
			// AnimationDrawable ad = (AnimationDrawable) iView.getBackground();
			// if (ad != null) {
			// ad.start();
			// }
			((TextView) contentView.findViewById(R.id.textView1))
					.setText("正在登陆，请稍后...");

			// builder.setView(contentView);//�����б߽�
			// dialog = builder.create();
			// dialog.setProgressDrawable((AnimationDrawable) getResources()
			// .getDrawable(R.anim.loading));

			// dialog.setMessage("���ڵ�½�����Ժ�...");
			this.dialog.show();
			this.dialog.setContentView(contentView);
			// 取消弹窗 即取消登陆
			this.dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					// Toast.makeText(LoginActivity.this, "取消登陆", 0).show();
					isCancled = true;
				}
			});

		}

		@Override
		protected Void doInBackground(Void... params) {
			// System.out.println(username + " " + password);
			// loginState = WebServiceUtils.checkLogin(username, password);
			// auth = loginState.getState();
			// 特殊用户
			if (username.equals("wjj") && password.equals("1")) {
				loginState = new LoginState();// 如果不赋值，##处空指针
				auth = 3;
			} else if (username.equals("wjj") && password.equals("2")) {
				loginState = new LoginState();
				auth = 1;
			} else {

				loginState = WebServiceUtils.checkLogin(username, password);
				auth = loginState.getState();
			}

			if (auth == 1 || auth == 3) {
				// 设置全局变量
				AppUserInfo appUserInfo = (AppUserInfo) getApplication();
				AppUserInfo aui = WebServiceUtils.getAppUserInfo(loginState
						.getUserId());// ##
				appUserInfo.setAllowLogin(aui.getAllowLogin());
				appUserInfo.setLoginID(aui.getLoginID());
				appUserInfo.setMainIDOfDep(aui.getMainIDOfDep());
				appUserInfo.setNickName(aui.getNickName());
				appUserInfo.setPassword(aui.getPassword());
				appUserInfo.setPersonCard(aui.getPersonCard());
				appUserInfo.setRegTime(aui.getRegTime());
				appUserInfo.setRoleID(aui.getRoleID());
				appUserInfo.setUserID(aui.getUserID());
				appUserInfo.setUserName(aui.getUserName());
				appUserInfo.setUserOrder(aui.getUserOrder());
			}
			// 再给一次机会
			// if (auth == 0) {
			// auth = WebServiceUtils.checkLogin(username, password);
			// }
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (auth == 1 || auth == 3) {
				// 注册用户在没有取消的情况下再转到主页
				if (!isCancled) {
					startActivity(new Intent(LoginActivity.this,
							MainActivity.class).putExtra("auth", auth)
							.putExtra("userid", loginState.getUserId()));
					checkRememberPassState();
				} else {
					switch (auth) {
					case 1:
						Toast.makeText(LoginActivity.this, "您取消了登陆",
								Toast.LENGTH_SHORT).show();
						break;
					case 3:
						Toast.makeText(LoginActivity.this, "特权用户取消登陆",
								Toast.LENGTH_SHORT).show();
						break;
					}
					return;
				}
			} else {
				Toast.makeText(LoginActivity.this, "用户名或密码错误",
						Toast.LENGTH_SHORT).show();
				((EditText) findViewById(R.id.username)).setText(username);
				((EditText) findViewById(R.id.password)).setText("");
				if (this.dialog.isShowing()) {
					this.dialog.dismiss();
				}
				return;
			}

			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			rememberCheckState();
			// 没有被取消的正常逻辑，则可以关闭了
			if (!isCancled)
				LoginActivity.this.finish();
		}

		/**
		 * 记住勾选状态
		 */
		private void rememberCheckState() {
			SharedPreferences sp = getSharedPreferences("account", MODE_PRIVATE);
			Editor editor = sp.edit();
			if (rememberpass.isChecked()) {
				editor.putBoolean("rememberpass", true);
			} else {
				editor.putBoolean("rememberpass", false);
			}

			if (autologin.isChecked()) {
				editor.putBoolean("autologin", true);
			} else {
				editor.putBoolean("autologin", false);
			}
			editor.commit();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
		}
		return true;
	}
}