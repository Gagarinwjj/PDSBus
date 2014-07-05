package wedo.oa.utils;

import wedo.oa.acitvity.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public abstract class CustomToast {
	private static Toast mToast;
	private static TextView toast_txt;

	public static void show(Context context, CharSequence text) {
		mToast = new Toast(context);
		View view = LayoutInflater.from(context).inflate(R.layout.custom_toast,
				null);
		toast_txt = (TextView) view.findViewById(R.id.toast_txt);
		toast_txt.setText(text);
		mToast.setView(view);
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.show();
	}
}