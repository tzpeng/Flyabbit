package com.dhc.library.base;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dhc.library.R;
import com.dhc.library.framework.IDaggerListener;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import me.yokeyword.fragmentation.SupportActivity;

/**
 * 创建者：邓浩宸
 * 时间 ：2016/11/15 16:08
 * 描述 ：无MVP的activity基类
 */
public abstract class BaseActivity extends SupportActivity implements LifecycleProvider<ActivityEvent> ,IDaggerListener {
    protected Context mContext;
    private static final String TAG = BaseActivity.class.getSimpleName();
    private final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();//重写RxLife控制生命周期

    protected <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initInject(savedInstanceState);
        super.onCreate(savedInstanceState);
        lifecycleSubject.onNext(ActivityEvent.CREATE);
        if (getLayout() > 0) {
            setContentView(getLayout());
        }
        mContext = this;
        Log.i(TAG,"activity: " + getClass().getSimpleName() + " onCreate()");
        initEventAndData(savedInstanceState);
    }

    protected void setToolBar(Toolbar toolbar, String title) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressedSupport();
            }
        });
    }


    @Override
    protected void onDestroy() {
        lifecycleSubject.onNext(ActivityEvent.DESTROY);
        super.onDestroy();
        Log.i(TAG,"activity: " + getClass().getSimpleName() + " onDestroy()");
    }

    /**
     * 重新加载本页面
     */
    public final void reload() {
        reload(false);
    }

    /**
     * 重新加载
     *
     * @param isNeedAnim  是否是需要动画
     */
    public final void reload(boolean isNeedAnim) {
        if (isNeedAnim) {
            getWindow().setWindowAnimations(R.style.WindowAnimationFadeInOut);
            recreate();
        } else {
            Intent intent = getIntent();
            overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(R.anim.fade_in,0);
            startActivity(intent);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (-1 != getMenuId())
            getMenuInflater().inflate(getMenuId(), menu);
        return true;
    }

    public int getMenuId() {
        return -1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * 该方法回调时机为,Activity回退栈内Fragment的数量 小于等于1 时,默认finish Activity
     * 请尽量复写该方法,避免复写onBackPress(),以保证SupportFragment内的onBackPressedSupport()回退事件正常执行
     */
    @Override
    public void onBackPressedSupport() {
        super.onBackPressedSupport();
    }

    protected boolean isCompatible(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }

    protected abstract int getLayout();

    protected abstract void initEventAndData(Bundle savedInstanceState);



    /**------------------------             Rxlife用于管理Rxjava的生命周期的                ------------------------*/
    /**
     * ------------------------             start               ------------------------
     */

    @Override
    @NonNull
    @CheckResult
    public final Observable<ActivityEvent> lifecycle() {
        return lifecycleSubject.hide();
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull ActivityEvent event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycleAndroid.bindActivity(lifecycleSubject);
    }


    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();
        lifecycleSubject.onNext(ActivityEvent.START);
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        lifecycleSubject.onNext(ActivityEvent.RESUME);
    }

    @Override
    @CallSuper
    protected void onPause() {
        lifecycleSubject.onNext(ActivityEvent.PAUSE);
        super.onPause();
    }

    @Override
    @CallSuper
    protected void onStop() {
        lifecycleSubject.onNext(ActivityEvent.STOP);
        super.onStop();
    }


    /**------------------------             Rxlife用于管理Rxjava的生命周期的                ------------------------*/
    /**------------------------                            end              ------------------------*/
}
