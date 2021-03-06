package com.kd.net;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.kd.callback.KdCallBack;
import com.kd.net.bean.RequestEnvironment;
import com.kd.net.engine.AbsCancelTask;
import com.kd.net.engine.INetEngine;
import com.kd.net.engine.NetEngineDefaultImpl;
import com.kd.net.param.BaseRequestParams;
import com.kd.net.provider.AbsReqBeanProviderImpl;
import com.kd.net.provider.IRequestConfigStrProvider;
import com.kd.net.provider.bean.ReqBean;
import com.kd.utils.string.PatternCheckUtils;
import com.kd.utils.sys.SPUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by developer on 16/7/7.
 */
public class KdRequest {

    private static KdRequest manager;
    public AbsReqBeanProviderImpl reqBeanProvider;
    public ReqBean.TaskItemBean currentTaskItem = null;
    public int EnvironmentSelectdeIndex = 0;
    private Context context;
    private ReqBean reqBean;
    private INetEngine netEngine;
    private IRequestConfigStrProvider strProvider;
    /**
     * 请求地址标头  优先级大于 配置文件
     */
    private String absoluteHeaderStr;
    /**
     * 请求环境列表
     */
    private List<RequestEnvironment> requestEnvironmentList;

    private KdRequest() {
    }

    /**
     * @param context
     * @param reqBeanProvider
     */
    public static void init(Context context, AbsReqBeanProviderImpl reqBeanProvider) {
        manager = new KdRequest();
        manager.context = context;
        manager.strProvider = reqBeanProvider.getStrProvider();
        manager.reqBeanProvider = reqBeanProvider;
    }

    /**
     * 默认使用IReqBeanProviderDefaultImp作为数据提供者
     * 默认使用xutils作为网络请求引擎
     *
     * @return
     */
    public synchronized static KdRequest getInstance() {
        if (manager == null) {
            manager = new KdRequest();
        }
        if (manager.reqBeanProvider == null) {
//            LogUtils.e("\"AbsReqBeanProviderImpl is null\"");
//            throw new Exception("AbsReqBeanProviderImpl is null");
        }
        return getInstance(manager.reqBeanProvider);
    }

    /**
     * 获取requestManage操作实例
     *
     * @param provider 请求策略提供者接口实现
     * @return
     */
    public synchronized static KdRequest getInstance(AbsReqBeanProviderImpl provider) {
        if (manager == null) {
            try {
                throw new Exception("KdRequest did not register");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        manager.reqBeanProvider = provider;
        return manager;

    }

    /**
     * 设置绝对头部地址  优先级 大于 环境配置
     * 设置之后调用reload方法生效
     *
     * @param absoluteHeaderStr
     */
    public void setAbsoluteHeaderStr(String absoluteHeaderStr) {
        this.absoluteHeaderStr = absoluteHeaderStr;
        SPUtils.put(context, "absoluteHeaderStr", absoluteHeaderStr);
        SPUtils.remove(context, "RequestEnvironment");
        //清空保存条件
    }

    public List<RequestEnvironment> getRequestEnvironmentList() {
        return requestEnvironmentList;
    }

    /**
     * 设置环境列表(代码设置在)
     *
     * @param requestEnvironmentList
     */
    public void setRequestEnvironmentList(List<RequestEnvironment> requestEnvironmentList) {
        this.requestEnvironmentList = requestEnvironmentList;
    }

    /**
     * 系统调用之前需要先reload
     */
    public void reloadReqBean() {
        if (manager.netEngine == null) {
            manager.netEngine = new NetEngineDefaultImpl();
        }
        absoluteHeaderStr = (String) SPUtils.get(context, "absoluteHeaderStr", "");
        if (absoluteHeaderStr != null && absoluteHeaderStr.length() > 0) {
            LogUtils.e("绝对头部:" + absoluteHeaderStr);
            manager.reqBeanProvider.setAbsoluteHeaderStr(absoluteHeaderStr);
            manager.reqBean = manager.reqBeanProvider.getReqBean();
            return;
        }

        if (requestEnvironmentList != null && requestEnvironmentList.size() > 0) {
            String environName = (String) SPUtils.get(manager.context, "RequestEnvironment", "");//若存在配置
            LogUtils.e("环境配置" + environName);
            if (environName.length() > 0) {
                for (int i = 0; i < requestEnvironmentList.size(); i++) {
                    if (environName.equals(requestEnvironmentList.get(i).getName())) {
                        EnvironmentSelectdeIndex = i;
                        break;
                    }
                }
                manager.reqBeanProvider.setRequestEnvironment(requestEnvironmentList.get(EnvironmentSelectdeIndex));
                manager.reqBean = manager.reqBeanProvider.getReqBean();
                return;
            }

        }
        manager.reqBean = manager.reqBeanProvider.getReqBean();
    }

    public void setNetEngine(INetEngine netEngine) {
        this.netEngine = netEngine;
    }

    public String getRequestUrlByTaskId(String taskId) throws Exception {
        return getRequestUrl(taskId, null);
    }

    /**
     * 判断taskid 对应的必要参数时候缺失
     *
     * @param taskid
     * @param mapParam    待请求参数
     * @param busListener
     * @return
     * @throws Exception
     */
    public boolean isParamsNessaryValueLost(String taskid, Map<String, Object> mapParam, KdCallBack<String> busListener) throws Exception {
        return isParamsNessaryValueLost(taskid, BaseRequestParams.covertFormMap(mapParam), busListener);
    }

    public boolean isParamsNessaryValueLost(String taskid, BaseRequestParams params, KdCallBack<String> busListener) throws Exception {
        ReqBean.TaskItemBean itemBean = getItemBeanByTaskId(taskid, busListener);
        StringBuffer msg = new StringBuffer();
        if (itemBean.getParams() != null) {
            for (ReqBean.TaskItemBean.ParamsBean param : itemBean.getParams()) {//注意这种实现 是严格遵守配置文档的参数进行 param设置的 如果mapParam 存在配置中不存在的key值将会被忽略
                if (param.isIsNessary() && !params.hasKey(param.getKey())) {
                    msg.append("taskid: (" + taskid + ") 缺少key值为 " + param.getKey() + " 的必要参数\n");
                }
            }
            if (busListener != null && msg.toString().length() > 0) {

                busListener.onFailed(new Exception(msg.toString()));
                return true;
            }

        }
        return false;
    }

    private ReqBean.TaskItemBean getItemBeanByTaskId(String taskid, KdCallBack<String> busListener) {
        if (reqBean == null) {
            return null;
        }
        for (ReqBean.TaskItemBean itemBean : reqBean.getList()) {
            if (itemBean.getTaskId().equals(taskid)) {
                return itemBean;
            }
        }
        return null;
    }

    public String getRequestUrl(String taskId) throws Exception {
        return getRequestUrl(taskId, null);
    }

    /**
     * 根据taskid 获取对应 url
     *
     * @param taskId
     * @param busListener
     * @return
     */
    public String getRequestUrl(String taskId, KdCallBack<String> busListener) throws Exception {
        if (manager.reqBean == null) {
            reloadReqBean();
        }
        if (reqBean == null) {
            if (busListener != null) {
                busListener.onFailed(new Exception("无配置文件或文件解析出错"));
            } else {
                throw new Exception("无配置文件或文件解析出错");
            }
            return null;
        }
        currentTaskItem = null;
        currentTaskItem = getItemBeanByTaskId(taskId, busListener);
        if (null == currentTaskItem) {
            String msg = "不存在taskId:" + taskId + "映射的taskItem 请检查配置文件";
            if (busListener != null) {
                busListener.onFailed(new Exception(msg));
            } else {
                throw new Exception(msg);
            }
            return null;
        }
        if (currentTaskItem.getUrl() == null) {
            String msg = "taskid: " + taskId + " 映射的taskItem Url 为 null";
            if (busListener != null) {
                busListener.onFailed(new Exception(msg));
            } else {
                throw new Exception(msg);
            }
            return null;

        }
        String reqUrl;
        if (PatternCheckUtils.isUrl(currentTaskItem.getUrl()))//如果taskItem url 本身为完整路径 则不加载域名
        {
            reqUrl = currentTaskItem.getUrl();
        } else {
            if (reqBean.getDomainName() == null) {
                String msg = "DomainName为null";
                if (busListener != null) {
                    busListener.onFailed(new Exception(msg));
                } else {
                    throw new Exception(msg);
                }
                return null;
            }
            reqUrl = reqBean.getDomainName() + currentTaskItem.getUrl();
            if (!PatternCheckUtils.isUrl(reqUrl)) {
                String msg = "请求地址不合法，请检查domainName和taskItem URL 组合规则 domainName+taskItemUrl";
                if (busListener != null) {
                    busListener.onFailed(new Exception(msg));
                } else {
                    throw new Exception(msg);
                }
                return null;
            }
        }
        return reqUrl;
    }

    /**
     * 执行一个配置请求 默认网络优先
     *
     * @param TaskId      任务id
     * @param mapParam    参数对
     * @param busListener 回执
     * @throws Exception
     */
    public void doRequest(String TaskId, Map<String, Object> mapParam, KdCallBack<String> busListener) throws Exception {
        this.doRequest(TaskId, mapParam, busListener, false);
    }

    public void doRequest(String TaskId, BaseRequestParams mapParam, KdCallBack<String> busListener) throws Exception {
        this.doRequest(TaskId, mapParam, busListener, false);
    }

    /**
     * 执行一个配置请求
     *
     * @param TaskId        任务id
     * @param mapParam      参数对
     * @param busListener   回执
     * @param isCatcheFirst 是否缓存优先
     * @throws Exception
     */
    public void doRequest(String TaskId, Map<String, Object> mapParam, final KdCallBack<String> busListener, boolean isCatcheFirst) throws Exception {
        doRequestInControll(TaskId, mapParam, busListener, isCatcheFirst);
    }

    public void doRequest(String TaskId, BaseRequestParams mapParam, final KdCallBack<String> busListener, boolean isCatcheFirst) throws Exception {
        doRequestInControll(TaskId, mapParam, busListener, isCatcheFirst);
    }

    /**
     * 执行配置配置请求，返回请求任务控制句柄
     *
     * @param TaskId        任务id
     * @param mapParam      参数对
     * @param busListener   回执
     * @param isCatcheFirst 是否缓存优先
     * @return
     * @throws Exception
     */
    public AbsCancelTask doRequestInControll(String TaskId, Map<String, Object> mapParam, final KdCallBack<String> busListener, boolean isCatcheFirst) throws Exception {

        return doRequestInControll(TaskId, BaseRequestParams.covertFormMap(mapParam), busListener, isCatcheFirst);
    }

    public AbsCancelTask doRequestInControll(String TaskId, BaseRequestParams requestParam, final KdCallBack<String> busListener, boolean isCatcheFirst) throws Exception {
        String reqUrl = getRequestUrl(TaskId, busListener);
        if (null == reqUrl) {
            return null;
        }
        if (!isParamsNessaryValueLost(TaskId, requestParam, busListener)) {//判断是否缺少不要参数
            String httpMethod = "get";
            if (currentTaskItem.getRequestMethod() != null && !currentTaskItem.getRequestMethod().equals("")) {
                httpMethod = currentTaskItem.getRequestMethod().toLowerCase();
            }
            AbsCancelTask taskCancel = doCommonRequest(reqUrl, requestParam, httpMethod, currentTaskItem.isIsCache(), busListener);
            return taskCancel;
        }
        return null;
    }

    /**
     * 执行一个普通网络请求 返回请求任务回执
     *
     * @param url               请求地址
     * @param params            参数对
     * @param method            方法
     * @param isCacheFirst      缓存优先
     * @param commonBusListener 回执
     * @return
     */
    public AbsCancelTask doCommonRequest(String url, Map<String, Object> params, String method, boolean isCacheFirst, KdCallBack commonBusListener) {
        return doCommonRequest(url, BaseRequestParams.covertFormMap(params), method, isCacheFirst, commonBusListener);
    }

    /**
     * 执行一个普通网络请求 返回请求任务回执
     *
     * @param url               请求地址
     * @param params            参数对
     * @param method            方法
     * @param isCacheFirst      缓存优先
     * @param commonBusListener 回执
     * @return
     */
    public AbsCancelTask doCommonRequest(String url, BaseRequestParams params, String method, boolean isCacheFirst, KdCallBack commonBusListener) {
//        if (manager.reqBean == null) {
//            reloadReqBean();
//        }
        if (manager.netEngine == null) {
            manager.netEngine = new NetEngineDefaultImpl();
        }
        if (netEngine != null) {
            AbsCancelTask cancelTask = netEngine.doRequest(url, params, method, isCacheFirst, commonBusListener);
            return cancelTask;
        }
        return null;
    }

    /**
     * 执行简单的get方法
     *
     * @param url               请求地址
     * @param params            参数对
     * @param isCacheFirst      缓存优先
     * @param commonBusListener 回执
     */
    public void doGet(String url, Map<String, Object> params, boolean isCacheFirst, KdCallBack commonBusListener) {
        doCommonRequest(url, params, "get", isCacheFirst, commonBusListener);
    }

    public void doGet(String url, BaseRequestParams params, boolean isCacheFirst, KdCallBack commonBusListener) {
        doCommonRequest(url, params, "get", isCacheFirst, commonBusListener);
    }

    /**
     * 执行简单的post方法
     *
     * @param url               请求地址
     * @param params            参数对
     * @param isCacheFirst      缓存优先
     * @param commonBusListener 回执
     */
    public void doPost(String url, Map<String, Object> params, boolean isCacheFirst, KdCallBack commonBusListener) {
        doCommonRequest(url, params, "post", isCacheFirst, commonBusListener);
    }

    public void doPost(String url, BaseRequestParams params, boolean isCacheFirst, KdCallBack commonBusListener) {
        doCommonRequest(url, params, "post", isCacheFirst, commonBusListener);
    }

    public void showCustomHeaderEditDialog(Context mContext) {
        final EditText et = new EditText(mContext);
        et.setText("http://");
        new MaterialDialog.Builder(mContext).title("设置请求头")
                .customView(et, true)
                .positiveText("确定")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {


                        String content = et.getText().toString();
                        if (content.length() > 0 && content.contains("http")) {
                            setAbsoluteHeaderStr(content);
                            reloadReqBean();
                            ToastUtils.showLong("强制请求头" + content + "已配置");
                        }
                    }
                })
                .negativeText("取消")
                .show();
    }

    public void showEnvirmentListDilog(final Context mContext) {
        if (requestEnvironmentList == null) {
            return;
        }
        List<String> itemList = new ArrayList<String>();
        if (requestEnvironmentList != null) {
            for (int i = 0; i < requestEnvironmentList.size(); i++) {
                RequestEnvironment en = requestEnvironmentList.get(i);
                itemList.add(en.getName());
            }
        }
        if (reqBeanProvider.getRequestEnvironment() != null) {
            for (int i = 0; i < itemList.size(); i++) {
                if (reqBeanProvider.getRequestEnvironment().getName().equals(itemList.get(i))) {
                    EnvironmentSelectdeIndex = i;
                    break;
                }
            }
        }
        String content = "";
        if (absoluteHeaderStr != null && absoluteHeaderStr.length() > 0) {
            content = "自定义地址：" + absoluteHeaderStr;
            EnvironmentSelectdeIndex = -1;
        } else if (reqBeanProvider.getRequestEnvironment() != null) {
            content = "环境参数：";
            for (RequestEnvironment.VarsBean varsBean : reqBeanProvider.getRequestEnvironment().getVars()) {
                content = content + "\n" + "key: " + varsBean.getName() + "\n value:" + varsBean.getValue();
            }

        }

        new MaterialDialog.Builder(mContext)

                .title("环境选择")
                .items(itemList)
                .content(content)
                .itemsCallbackSingleChoice(EnvironmentSelectdeIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        EnvironmentSelectdeIndex = which;
                        if (EnvironmentSelectdeIndex >= 0) {
                            RequestEnvironment rq = requestEnvironmentList.get(EnvironmentSelectdeIndex);
                            reqBeanProvider.setRequestEnvironment(rq);
                            reqBeanProvider.setAbsoluteHeaderStr(null);//绝对头部制空
                            SPUtils.remove(mContext, "absoluteHeaderStr");
                            reqBean = reqBeanProvider.getReqBean();
                            ToastUtils.showLong(rq.getName() + "环境已配置");
                            SPUtils.put(mContext, "RequestEnvironment", rq.getName());

                        }
                        return true;
                    }
                })
                .positiveText("确认")
                .negativeText("设置自定义")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        LogUtils.e("222");
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showCustomHeaderEditDialog(mContext);
                    }
                })
                .show();

    }

    /**
     * 获取请求前缀
     * 自定义前缀 优先级 大于 环境配置
     *
     * @param key
     * @return
     */
    public String getDomainStr(String key) {
        if (absoluteHeaderStr != null && absoluteHeaderStr.length() > 0) {
            return absoluteHeaderStr;
        } else {
            for (RequestEnvironment.VarsBean varsBean : reqBeanProvider.getRequestEnvironment().getVars()) {
//                content = content + "\n" + "key: " + varsBean.getName() + "\n value:" + varsBean.getValue();
                if (varsBean.getName().equals(key)) {
                    return varsBean.getValue();
                }
            }
        }
        return "defaule url is not setted or not find";
    }
}
