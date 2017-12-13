package utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 对sp进行封装操作
 */
public class SpUtils {
	private SharedPreferences mSp;
	private Editor mEditor;

	public SpUtils(Context context) {
		mSp = context.getSharedPreferences("greenNet", Context.MODE_PRIVATE);
		mEditor = mSp.edit();
	}

	/**取出String*/
	public String getString(String key, String defValue) {
		return mSp.getString(key, defValue);
	}

	/**取出Int*/
	public int getInt(String key, int defValue) {
		return mSp.getInt(key, defValue);
	}

	/**取出Boolean*/
	public boolean getBoolean(String key, boolean defValue) {
		return mSp.getBoolean(key, defValue);
	}

	/**存入String*/
	public void putString(String key, String value) {
		mEditor.putString(key, value);
		mEditor.commit();
	}

	/**存入Int*/
	public void putInt(String key, int value) {
		mEditor.putInt(key, value);
		mEditor.commit();
	}

	/**存入Boolean*/
	public void putBoolean(String key, boolean value) {
		mEditor.putBoolean(key, value);
		mEditor.commit();
	}
}
