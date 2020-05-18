package com.fitsoft.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertyUtil {

    @Value("${fdfs.visit.url}")
    private String URL;

    @Value("${fdfs.visit.port}")
    private String PORT;

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getPORT() {
        return PORT;
    }

    public void setPORT(String PORT) {
        this.PORT = PORT;
    }
}
