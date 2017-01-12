package com.xudev.Ui.Mvp;

import android.os.Bundle;

import com.xudev.Ui.activity.XuBaseActivity;

/**
 * Created by developer on 16/6/29.
 */
public abstract class BaseMvpActivity<T extends BasePresenter,V extends IBaseUiAction> extends XuBaseActivity implements IBaseUiAction {
    public T mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initPresenter();
        mPresenter.attachView((V)this);
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    public abstract void initPresenter();

    @Override
    public void showToast(String msg) {
        super.showToast(msg);
    }

    @Override
    public void showLoadingDialog() {
        super.showProgressDialog(null, "正在加载");
    }
}