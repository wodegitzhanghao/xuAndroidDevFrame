package com.xudev.ReqUrlManage;

import android.content.Context;

import com.xudev.ReqUrlManage.Engine.AbsCancelTask;
import com.xudev.ReqUrlManage.Engine.INetEngine;
import com.xudev.ReqUrlManage.Engine.INetEngineDefaultImp;
import com.xudev.ReqUrlManage.ReqBeanProvider.IBaseReqBeanProImp;
import com.xudev.ReqUrlManage.ReqBeanProvider.IReqBeanProvider;
import com.xudev.ReqUrlManage.ReqBeanProvider.IRequestConfigStrProvider;
import com.xudev.ReqUrlManage.ReqBeanProvider.ReqBean;
import com.xudev.iface.OnCommonBusListener;
import com.xudev.utils.PatternCheckUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by developer on 16/7/7.
 */
public class RequestManager {

    private static RequestManager manager;
    private Context context;
    private static ReqBean reqBean;
    private static INetEngine netEngine;
    private IRequestConfigStrProvider strProvider;
    private IReqBeanProvider reqBeanProvider;

    private RequestManager() {
    }

    /**
     * @param context
     * @param reqBeanProvider
     */
    public static void register(Context context, IBaseReqBeanProImp reqBeanProvider) {
        manager = new RequestManager();
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
    public synchronized static RequestManager getInstance() {
        if (manager == null) {
            try {
                throw new Exception("RequestManager did not register");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getInstance(manager.reqBeanProvider);
    }

    public synchronized static RequestManager getInstance(IReqBeanProvider provider) {
        if (manager == null) {
            try {
                throw new Exception("RequestManager did not register");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (manager.reqBean == null) {
            manager.reqBean = provider.getReqBean();
        }
        manager.setNetEngine(new INetEngineDefaultImp());
        return manager;

    }

    /**
     * 获取请求参数配置对象
     * @param iReqBeanProvider
     */
    public void getReqBean(IReqBeanProvider iReqBeanProvider) {
        reqBean = iReqBeanProvider.getReqBean();
    }

    public void setNetEngine(INetEngine netEngine) {
        this.netEngine = netEngine;
    }

    ReqBean.TaskItemBean currentTaskItem = null;

    /**
     * 根据taskid 获取对应 url
     *
     * @param taskId
     * @param busListener
     * @return
     */
    public String getRequestUrl(String taskId, OnCommonBusListener<String> busListener) {
        currentTaskItem = null;
        if (reqBean == null) {
            if (busListener != null) {
                busListener.onFailed("无配置文件或文件解析出错");
            }

            return null;
        }
        for (ReqBean.TaskItemBean itemBean : reqBean.getList()) {
            if (itemBean.getTaskId().equals(taskId)) {
                currentTaskItem = itemBean;
                break;
            }
        }

        if (null == currentTaskItem) {
            if (busListener != null) {
                busListener.onFailed("不存在taskId映射的taskItem 请检查配置文件");
            }
            return null;
        }

        if (currentTaskItem.getUrl() == null) {
            if (busListener != null) {
                busListener.onFailed("taskItem Url 为 null");
            }
            return null;

        }

        String reqUrl;

        if (PatternCheckUtils.isUrl(currentTaskItem.getUrl()))//如果taskItem url 本身为完整路径 则不加载域名
        {
            reqUrl = currentTaskItem.getUrl();
        } else {
            if (reqBean.getDomainName() == null) {
                if (busListener != null) {
                    busListener.onFailed("DomainName为null");
                }
                return null;
            }
            reqUrl = reqBean.getDomainName() + currentTaskItem.getUrl();
            if (!PatternCheckUtils.isUrl(reqUrl)) {
                if (busListener != null) {
                    busListener.onFailed("请求地址不合法，请检查domainName和taskItem URL 组合规则 domainName+taskItemUrl");
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
    public void doRequest(String TaskId, Map<String, Object> mapParam, OnCommonBusListener<String> busListener) throws Exception {
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
    public void doRequest(String TaskId, Map<String, Object> mapParam, final OnCommonBusListener<String> busListener, boolean isCatcheFirst) throws Exception {
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
    public AbsCancelTask doRequestInControll(String TaskId, Map<String, Object> mapParam, final OnCommonBusListener<String> busListener, boolean isCatcheFirst) throws Exception {

        String reqUrl = getRequestUrl(TaskId, busListener);
        if (null == reqUrl) {
            return null;
        }

        if (currentTaskItem.getParams() != null) {
            for (ReqBean.TaskItemBean.ParamsBean param : currentTaskItem.getParams()) {//注意这种实现 是严格遵守配置文档的参数进行 param设置的 如果mapParam 存在配置中不存在的key值将会被忽略
                if (param.getKey() == null) {
                    continue;
                }
                if (param.getKey().equals("")) {
                    continue;
                }
                if (param.isIsNessary() && !mapParam.containsKey(param.getKey())) {
                    if (busListener!=null){
                        busListener.onFailed("缺少key值为" + param.getKey() + "的必要参数");
                    }
                    return null;
                }
                if (!mapParam.containsKey(param.getKey())) {
                    continue;
                }
            }

            String httpMethod = "get";
            if (currentTaskItem.getRequestMethod() != null && !currentTaskItem.getRequestMethod().equals("")) {
                httpMethod = currentTaskItem.getRequestMethod().toLowerCase();
            }
            AbsCancelTask taskCancel = doCommonRequest(reqUrl, mapParam, httpMethod, currentTaskItem.isIsCache(), busListener);
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
    public AbsCancelTask doCommonRequest(String url, Map<String, Object> params, String method, boolean isCacheFirst, OnCommonBusListener commonBusListener) {
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
    public void doGet(String url, Map<String, Object> params, boolean isCacheFirst, OnCommonBusListener commonBusListener) {
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
    public void doPost(String url, Map<String, Object> params, boolean isCacheFirst, OnCommonBusListener commonBusListener) {

        doCommonRequest(url, params, "post", isCacheFirst, commonBusListener);
    }

    @Deprecated
    /***
     * 判断返回信息token是否有效
     *
     * @param result
     * @return
     * @throws JSONException
     */
    public boolean isAccessTokenEffected(String result) throws JSONException {
        JSONObject jsonObject = new JSONObject(result);
        if (jsonObject.has("succeed")) {
            String succeed = jsonObject.getString("succeed");
            if (succeed.equals("-5")) {
                return false;
            }
        }
        return true;
    }
}
