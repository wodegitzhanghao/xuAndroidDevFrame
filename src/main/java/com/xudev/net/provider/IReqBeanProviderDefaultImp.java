package com.xudev.net.provider;

import android.content.Context;

import com.google.gson.Gson;
import com.xudev.utils.net.json.JsonValidator;

import java.io.IOException;

/**
 * Created by developer on 16/7/8.
 */
public class IReqBeanProviderDefaultImp extends IBaseReqBeanProImp {


    public IReqBeanProviderDefaultImp(IRequestConfigStrProvider strProvider, Context context) {
        super(strProvider, context);
    }

    @Override
    public ReqBean getReqBean() {
        ReqBean reqBean = null;
        try {
            String resultStr = getConfigStr();
            reqBean = new Gson().fromJson(resultStr, ReqBean.class);
            JsonValidator jsonValidator = new JsonValidator();
            reqBean = new Gson().fromJson(resultStr, ReqBean.class);

            if (reqBean == null) {
                throw new Exception("request配置文件解析失败");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reqBean;
    }


}
