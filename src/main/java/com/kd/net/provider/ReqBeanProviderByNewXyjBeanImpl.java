package com.kd.net.provider;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kd.net.bean.NewXiaoYaoJiRequestItem;
import com.kd.net.bean.NewXiaoyaojiBean;
import com.kd.net.bean.RequestEnvironment;
import com.kd.net.provider.bean.ReqBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 小幺鸡新规范 解析实现类
 * Created by xu on 2017/8/29.
 */
public class ReqBeanProviderByNewXyjBeanImpl extends AbsReqBeanProviderImpl {
    List<ReqBean.TaskItemBean> taskItemList = new ArrayList<>();//接口列表
    private NewXiaoyaojiBean newXiaoyaojiBean;
    private ReqBean reqBean;

    public ReqBeanProviderByNewXyjBeanImpl(IRequestConfigStrProvider strProvider, Context context) {
        super(strProvider, context);
    }

    public NewXiaoyaojiBean createNewXiaoyaojiBean() throws Exception {
        if (newXiaoyaojiBean == null) {
            newXiaoyaojiBean = new Gson().fromJson(getConfigStr(), NewXiaoyaojiBean.class);
            if (newXiaoyaojiBean == null) {
                throw new Exception("request配置文件解析失败");
            }
        }
        return newXiaoyaojiBean;
    }

    @Override
    public ReqBean getReqBean() {


        try {
            if (createNewXiaoyaojiBean() != null) {
                reqBean = new ReqBean();
                reqBean.setVersion("");
                reqBean.setMatchAppCode("");
                reqBean.setName(newXiaoyaojiBean.getName());
                if (requestEnvironment == null) {//环境未设置 获取获取配置的第一个环境进行配置
                    List<RequestEnvironment> requestEnvironmentList = new Gson().fromJson(newXiaoyaojiBean.getGlobal().getEnvironment(), new TypeToken<List<RequestEnvironment>>() {
                    }.getType());
                    if (requestEnvironmentList != null && requestEnvironmentList.size() > 0) {
                        requestEnvironment = requestEnvironmentList.get(0);
                    }
                }

                taskItemList.clear();
                for (NewXiaoyaojiBean.ChildrenEntity entity : newXiaoyaojiBean.getDocs()) {
                    travserseChirenEntity(entity);
                }
                //便利完成后  taskItemList已经填充
                reqBean.setList(taskItemList);

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return reqBean;
    }

    private void travserseChirenEntity(NewXiaoyaojiBean.ChildrenEntity entity) {

//        LogUtils.e(entity.getType() + entity.getName());
        if (entity.getType().contains("http")) {//过滤接口类型的数据
            if (entity.getContent() != null) {

                NewXiaoYaoJiRequestItem requestItem = new Gson().fromJson(entity.getContent(), NewXiaoYaoJiRequestItem.class);
                if (requestItem != null) {
                    ReqBean.TaskItemBean taskItemBean = new ReqBean.TaskItemBean();
//                    1:设置请求地址
                    String temUrl = requestItem.getUrl();
                    if (requestEnvironment != null) {
//                        LogUtils.e("当前环境为============>" + requestEnvironment.getName());
                        if (requestEnvironment.getVars() != null) {
                            for (RequestEnvironment.VarsBean varBean : requestEnvironment.getVars()) {
                                String tag = "$" + varBean.getName() + "$";
                                String value = varBean.getValue();
                                if (temUrl.contains(tag)) {
                                    if (AbsoluteHeaderStr != null && AbsoluteHeaderStr.length() > 0) {//存在绝对头部时优先设置头部
                                        temUrl = temUrl.replace(tag, AbsoluteHeaderStr);
                                    } else {
                                        temUrl = temUrl.replace(tag, value);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    taskItemBean.setUrl(temUrl);
                    taskItemBean.setIsCache(false);
                    taskItemBean.setRequestMethod(requestItem.getRequestMethod());
                    taskItemBean.setTaskId(entity.getName());
                    List<ReqBean.TaskItemBean.ParamsBean> paramsBeanList = new ArrayList<>();
                    for (NewXiaoYaoJiRequestItem.RequestArgsEntity requestArg : requestItem.getRequestArgs()
                    ) {
                        ReqBean.TaskItemBean.ParamsBean paramBean = new ReqBean.TaskItemBean.ParamsBean();
                        paramBean.setKey(requestArg.getName());
                        paramBean.setDesc(requestArg.getDescription());
                        paramBean.setIsNessary(requestArg.getRequire().toLowerCase().equals("true") ? true : false);
                        paramsBeanList.add(paramBean);
                    }
                    taskItemBean.setParams(paramsBeanList);
                    taskItemList.add(taskItemBean);
                }

            }
        }
        if (entity.getChildren() != null && entity.getChildren().size() > 0) {
            for (NewXiaoyaojiBean.ChildrenEntity itemEntity : entity.getChildren()) {
                travserseChirenEntity(itemEntity);
            }
        }

    }
}
