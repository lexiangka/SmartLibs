package com.wedo.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


/**
 * 系统状态栏与底部导航栏相关工具类
 * <p>仅在 SDK >= 4.4 时有效</p>
 * <p>本工具类共分两种模式：着色模式{@link #setDisplayOption} 和 全透明模式{@link #fullTransparentBar}</p>
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public final class SystemBarUtil {
    private static final String TAG_STATUS_BAR = "StatusBar";
    private static final String TAG_NAVIGATION_BAR = "NavigationBar";
    /* 无效的颜色值 */
    private static final int INVALID_VAL = -1;
    /* 默认的状态栏颜色 */
    private static final int DEFAULT_STATUS_COLOR = 0x10000000;
    /* 默认的底部导航栏颜色 */
    private static final int DEFAULT_NAVIGATION_COLOR = Color.TRANSPARENT;

    private SystemBarUtil() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /***********************************************************************************************
     ****  沉浸式系统栏（状态栏与导航栏），着色模式，不可全透明（sdk >= 4.4）
     **********************************************************************************************/
    /**
     * 设置状态栏和底部导航栏的显示方式
     *
     * @param activity        activity
     * @param fitSystemWindow {@code true}: 内容不会显示到状态栏和导航栏上<br>{@code false}: 内容显示到状态栏和导航栏上
     * @param clipToPadding   {@code true}: 裁剪 padding 区域，padding 区域不可使用<br>{@code false}: 不裁剪 padding 区域，padding 区域可使用
     */
    public static void setDisplayOption(@NonNull Activity activity, boolean fitSystemWindow, boolean clipToPadding) {
        // 获取根布局
        ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        rootView.setClipToPadding(clipToPadding);
        rootView.setFitsSystemWindows(fitSystemWindow);
    }

    /**
     * 设置顶部状态栏
     *
     * @param activity
     * @param color    设置顶部状态栏的颜色
     */
    public static void setupStatusBar(@NonNull Activity activity, @ColorInt int color) {
        int statusHeight = ScreenUtil.getStatusBarHeight();
        int statusColor = DEFAULT_STATUS_COLOR;
        if (color != INVALID_VAL) {
            statusColor = color;
        }
        // 设置状态栏透明
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        // 防止重复添加 statusBarView
        removeStatusBarView(decorView);
        // 绘制一个和状态栏一样高的矩形 View
        View statusBarView = createStatusBarView(activity, statusHeight, statusColor);
        // 添加 statusBarView 到整个Window的最顶层布局中,这里的 statusBarView 只是作为状态栏的背景，
        // 它的 visible 不能影响到状态栏的 visible
        decorView.addView(statusBarView);
    }

    /**
     * 绘制一个和状态栏一样高的矩形View
     *
     * @param activity
     * @param statusHeight 绘制的矩形的高度
     * @param statusColor  绘制的矩形的颜色
     * @return 绘制的矩形 View
     */
    private static View createStatusBarView(@NonNull Activity activity, int statusHeight, @ColorInt int statusColor) {
        View statusBarView = new View(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, statusHeight);
        params.gravity = Gravity.TOP;
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(statusColor);
        statusBarView.setTag(TAG_STATUS_BAR);
        return statusBarView;
    }

    /**
     * 移除已经存在的 statusBarView
     *
     * @param decorView
     */
    private static void removeStatusBarView(@NonNull ViewGroup decorView) {
        View statusBarView = decorView.findViewWithTag(TAG_STATUS_BAR);
        if (statusBarView != null) {
            decorView.removeView(statusBarView);
        }
    }

    /**
     * 设置状态栏的透明度
     *
     * @param activity
     * @param alpha    透明度（0-1）
     */
    public static void setStatusBarAlpha(@NonNull Activity activity, @FloatRange(from = 0, to = 1) float alpha) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View statusView = decorView.findViewWithTag(TAG_STATUS_BAR);
        if (statusView != null) {
            statusView.setAlpha(alpha);
        }
    }

    /**
     * 设置状态栏的显示和隐藏
     *
     * @param activity
     * @param isShow   {@code true}: 显示<br>{@code false}: 隐藏
     */
    public static void showStatusBar(@NonNull Activity activity, boolean isShow) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View statusView = decorView.findViewWithTag(TAG_STATUS_BAR);
        if (isShow) {
            if (statusView != null) {
                statusView.setVisibility(View.VISIBLE);
            }
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            if (statusView != null) {
                statusView.setVisibility(View.GONE);
            }
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 设置底部导航栏
     *
     * @param activity
     * @param color    底部导航栏的颜色
     */
    public static void setupNavBar(@NonNull Activity activity, @ColorInt int color) {
        if (ScreenUtil.hasNavigationBar()) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            int navHeight = ScreenUtil.getNavBarHeight();
            int navColor = DEFAULT_NAVIGATION_COLOR;
            if (color != INVALID_VAL) {
                navColor = color;
            }
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            removeNavBarView(decorView);
            View navBarView = createNavBarView(activity, navHeight, navColor);
            decorView.addView(navBarView);
        }
    }

    /**
     * 绘制一个和底部导航栏一样高的矩形 View
     *
     * @param activity
     * @param navHeight 绘制的矩形的高度
     * @param navColor  绘制的矩形的颜色
     * @return 绘制的矩形 View
     */
    private static View createNavBarView(@NonNull Activity activity, int navHeight, @ColorInt int navColor) {
        View navBarView = new View(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, navHeight);
        params.gravity = Gravity.BOTTOM;
        navBarView.setLayoutParams(params);
        navBarView.setBackgroundColor(navColor);
        navBarView.setTag(TAG_NAVIGATION_BAR);
        return navBarView;
    }

    /**
     * 移除已经存在的 navBarView
     *
     * @param decorView
     */
    private static void removeNavBarView(@NonNull ViewGroup decorView) {
        View navBarView = decorView.findViewWithTag(TAG_NAVIGATION_BAR);
        if (navBarView != null) {
            decorView.removeView(navBarView);
        }
    }

    /**
     * 设置底部导航栏的透明度
     *
     * @param activity
     * @param alpha    透明度（0-1）
     */
    public static void setNavBarAlpha(@NonNull Activity activity, @FloatRange(from = 0, to = 1) float alpha) {
        if (ScreenUtil.hasNavigationBar()) {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            View navBarView = decorView.findViewWithTag(TAG_NAVIGATION_BAR);
            if (navBarView != null) {
                navBarView.setAlpha(alpha);
            }
        }
    }

    /**
     * 设置底部导航栏的显示和隐藏
     *
     * @param activity activity
     * @param isShow   {@code true}: 显示<br>{@code false}: 隐藏
     */
    public static void showNavBar(@NonNull Activity activity, boolean isShow) {
        if (ScreenUtil.hasNavigationBar()) {
            View decorView = activity.getWindow().getDecorView();
            if (isShow) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            } else {
                decorView.setSystemUiVisibility(View.INVISIBLE);
            }
        }
    }

    /***********************************************************************************************
     ****  沉浸式系统栏（状态栏与导航栏），全透明模式，（sdk >= 5.0）
     **********************************************************************************************/
    /**
     * 设置系统栏全透明
     *
     * @param activity
     * @param statusBar 状态栏是否全透明
     * @param navBar    导航栏是否全透明
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void fullTransparentBar(Activity activity, boolean statusBar, boolean navBar) {
        Window window = activity.getWindow();
        window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        if (statusBar && navBar) {
            window.getDecorView().setSystemUiVisibility(
                    // 全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity 顶端布局部分会被状态遮住
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // 隐藏导航栏
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            // 防止系统栏隐藏时内容区域大小发生变化
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (statusBar && !navBar) {
            window.getDecorView().setSystemUiVisibility(
                    // 全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity 顶端布局部分会被状态遮住
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // 防止系统栏隐藏时内容区域大小发生变化
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (!statusBar && navBar) {
            window.getDecorView().setSystemUiVisibility(
                    // 全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity 顶端布局部分会被状态遮住
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // 隐藏导航栏
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            // 防止系统栏隐藏时内容区域大小发生变化
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }
}