package com.zen.ui.utils;

import android.content.Context;

import java.lang.reflect.Field;


/**
 * 屏幕显示工具类
 * 
 */
public class DisplayUtil {

	/**
	 * 获取通知栏高度.
	 * 
	 * @param context
	 *            上下文环境.
	 * @return 通知栏高度.
	 * 
	 * @version 1.0
	 * @createTime 2013-9-15,下午2:29:29
	 * @updateTime 2013-9-15,下午2:29:29
	 * @createAuthor paladin
	 * @updateAuthor paladin
	 * @updateInfo
	 */
	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return statusBarHeight;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 * 
	 * @author julyzeng
	 * @date 2013-04-02
	 * @version 1.0
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 * 
	 * @author julyzeng
	 * @date 2013-04-02
	 * @version 1.0
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int dp2PxInt(Context context, float dp) {
		return (int) (dip2px(context, dp) + 0.5f);
	}


	/**
	 * 将px值转换为sp值，保证文字大小不变
	 * 
	 * @param pxValue
	 * @param
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 * 
	 * @param spValue
	 * @param
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	
	
	/**
	 * 描述：获取屏幕宽度
	 * 
	 * @version 1.0
	 * @createTime 2014-4-16 下午3:16:41
	 * @createAuthor
	 * 
	 * @updateTime 2014-4-16 下午3:16:41
	 * @updateAuthor
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenWidthPx(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}
	
	/**
	 * 描述：获取屏幕高度
	 *
	 * @version 1.0
	 * @createTime 2015-1-15,下午4:46:02
	 * @updateTime 2015-1-15,下午4:46:02
	 * @createAuthor
	 * @updateAuthor
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 *
	 * @param context
	 * @return
	 */
	public static int getScreenHightPx(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	
}
