package com.zen.ui.view;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.zen.ui.R;

/**
 * @author Allen
 * @date 2019/9/28.
 * @description
 */
public abstract class BaseDialogFragment extends DialogFragment {


    /**
     * 点击返回键是否消失
     */
    public static final String DIALOG_BACK = "dialog_back";
    /**
     * 是否取消
     */
    public static final String DIALOG_CANCELABLE = "dialog_cancelable";
    /**
     * 点击外部是否消失
     */
    public static final String DIALOG_CANCELABLE_TOUCH_OUT_SIDE = "dialog_cancelable_touch_out_side";

    protected boolean isCancelableTouchOutSide, isCancelable, isBack;

    protected Bundle bundle;
    protected Activity activity;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    /**
     * 返回当前DialogFragment的实例，使用Bundle传递参数
     *
     * @param clazz 当前DialogFragment.class
     * @param args  携带参数的Bundle对象
     * @param <T>   返回类型参数控制，必须是BaseDialogFragment的子类
     * @return 当前Fragment的实例
     */
    public static <T extends BaseDialogFragment> T newInstance(Class clazz, Bundle args) {
        T fragment = null;
        try {
            fragment = (T) clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        }
        //给fragment注入bundle
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取出bundle
        bundle = getArguments();
        //根据传过来的bundle参数初始化isBack,isCancelable,isCancelableTouchOutSide  默认全是false
        isBack = bundle.getBoolean(DIALOG_BACK, false);
        isCancelable = bundle.getBoolean(DIALOG_CANCELABLE, false);
        isCancelableTouchOutSide = bundle.getBoolean(DIALOG_CANCELABLE_TOUCH_OUT_SIDE, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //获取dialog布局view
        View rootView = inflater.inflate(getLayoutResId(), container, false);
        //对dialog布局初始化
        initView(rootView);
        //设置视图
        setSubView();
        //监听返回键
        onBackPressed();
        //初始化事件监听
        initEvent();

        return rootView;
    }

    /**
     * 初始化对话框视图
     *
     * @param view
     */
    protected abstract void initView(View view);

    /**
     * @return LayoutId
     */
    protected abstract int getLayoutResId();

    /**
     * 设置视图
     */
    protected abstract void setSubView();

    /**
     * 初始化事件监听
     */
    protected abstract void initEvent();

    /**
     * 当对话框消失时的监听事件
     */
    protected abstract void onCancel();

    /**
     * 监听返回键
     */
    private void onBackPressed() {
        //设置窗口以对话框样式显示
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog);
        //设置对话框背景色，否则有虚框
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().setCancelable(isCancelable);
        getDialog().setCanceledOnTouchOutside(isCancelableTouchOutSide);

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (isBack) {
                        onCancel();
                    }
                    return !isBack;
                }

                return false;
            }
        });
    }
}
