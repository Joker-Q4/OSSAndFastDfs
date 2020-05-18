package com.fitsoft.utils;

import java.util.HashMap;
import java.util.Map;

public class CommonResult {

    public static Map<String, Object> toResult(int code, String msg, int count, Object data){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("msg", msg);
        map.put("count", count);
        map.put("data", data);
        return map;
    }
}
