package com.scb.rider.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class PropertyUtils {

    @Autowired
    MessageSource messageSource;

    public String getProperty(String key){
        Locale locale = LocaleContextHolder.getLocale();
        return getProperty(key, locale);
    }

    public String getProperty(String key, Object[] objects){
        Locale locale = LocaleContextHolder.getLocale();
        return getProperty(key, objects, locale);
    }
    public String getProperty(String key, Object[] objects, Locale locale){
        return messageSource.getMessage(key, objects, locale);
    }

    public String getProperty(String key, Locale locale){
        return messageSource.getMessage(key, null, locale);
    }
}
