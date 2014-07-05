package wedo.oa.utils;

import wedo.oa.acitvity.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public abstract class CustomProgressDialog {
	// mDialog 可以是ProgressDialog，也可以是其父类 AlertDialog
	// public static AlertDialog mDialog;
	public static ProgressDialog mDialog;
	private static TextView info_txt;

	public static void show(Context context, CharSequence text) {
		// context可能不同，每次都要new
		// mDialog = new AlertDialog.Builder(context).create();
		mDialog = new ProgressDialog(context);
		View contentView = LayoutInflater.from(context).inflate(
				R.layout.customdialog, null);
		info_txt = ((TextView) contentView.findViewById(R.id.textView1));
		mDialog.show();
		mDialog.setContentView(contentView);
		info_txt.setText(text);
	}
}