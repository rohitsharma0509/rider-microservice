package com.scb.rider.service;

import com.scb.rider.model.document.AppConfig;
import com.scb.rider.repository.AppConfigRepository;
import com.scb.rider.service.document.AppConfigService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppConfigServiceTest {

    private static final int APP_VERSION = 1;

    @InjectMocks
    private AppConfigService appConfigService;

    @Mock
    private AppConfigRepository appConfigRepository;

    @Test
    void shouldGetNullWhenConfigNotAvailableInDb() {
        when(appConfigRepository.findAll()).thenReturn(new ArrayList<>());
        AppConfig result = appConfigService.getAppConfig();
        Assertions.assertNull(result);
    }

    @Test
    void shouldGetAppConfigWhenConfigAvailableInDb() {
        List<AppConfig> appConfigs = new ArrayList<>();
        AppConfig appConfig = AppConfig.builder().version(APP_VERSION).build();
        appConfigs.add(appConfig);
        when(appConfigRepository.findAll()).thenReturn(appConfigs);
        AppConfig result = appConfigService.getAppConfig();
        Assertions.assertEquals(APP_VERSION, result.getVersion());
    }
}
