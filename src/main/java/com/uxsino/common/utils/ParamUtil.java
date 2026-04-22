package com.uxsino.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.uxsino.common.response.ResultInfo;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

/**
 * @className ParamUtil
 * @description TODO
 * @date 2021/1/22 11:36
 */
public class ParamUtil {
    /**
     * 参数校验
     *
     * @param bindingResult
     * @return
     */
    public static ResultInfo checkParams(BindingResult bindingResult) {
        if (bindingResult != null && bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            StringBuffer message = new StringBuffer();
            for (int i = 0; i < fieldErrors.size(); i++) {
                if (i == 0) {
                    message.append(fieldErrors.get(0).getField()).append(":")
                        .append(fieldErrors.get(0).getDefaultMessage());
                } else {
                    message.append(",").append(fieldErrors.get(i).getField()).append(":")
                        .append(fieldErrors.get(i).getDefaultMessage());
                }
            }
            return ResultInfo.validateFailed(message.toString());
        }
        return null;
    }

    /**
     * 解析post单个请求参数
     *
     * @param param
     * @param paramName
     * @return
     */
    public static Object parseSinglePostParam(String param, String paramName) {
        if (param == null || paramName == null) {
            return null;
        }
        JSONObject parse = null;
        try {
            parse = JSONObject.parseObject(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (parse == null) {
            return null;
        }
        return parse.get(paramName);
    }
}
