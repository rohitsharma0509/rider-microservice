package com.scb.rider.validator;

import com.scb.rider.exception.AppUpgradeRequiredException;
import com.scb.rider.model.document.AppConfig;
import com.scb.rider.service.document.AppConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppConfigValidatorTest {

    private static final int CURRENT_APP_VERSION = 1;
    private static final int NEW_APP_VERSION = 2;

    @InjectMocks
    private AppConfigValidator appConfigValidator;

    @Mock
    private AppConfigService appConfigService;

    @Test
    void validateAppVersionShouldDoNothingWhenAppConfigIsNull() {
        when(appConfigService.getAppConfig()).thenReturn(null);
        appConfigValidator.validateAppVersion(CURRENT_APP_VERSION);
    }

    @Test
    void validateAppVersionShouldThrowExceptionWhenNewVersionAvailable() {
        AppConfig appConfig = AppConfig.builder().forceUpdate(Boolean.TRUE).version(NEW_APP_VERSION).build();
        when(appConfigService.getAppConfig()).thenReturn(appConfig);
        assertThrows(AppUpgradeRequiredException.class, () -> appConfigValidator.validateAppVersion(CURRENT_APP_VERSION));
    }

    @Test
    void validateAppVersionShouldDoNothingWhenNewVersionAvailableButNotForcingToUpdate() {
        AppConfig appConfig = AppConfig.builder().forceUpdate(Boolean.FALSE).version(NEW_APP_VERSION).build();
        when(appConfigService.getAppConfig()).thenReturn(appConfig);
        appConfigValidator.validateAppVersion(CURRENT_APP_VERSION);
    }

    @Test
    void validateAppVersionShouldDoNothingWhenAppVersionsAreSame() {
        AppConfig appConfig = AppConfig.builder().forceUpdate(Boolean.TRUE).version(CURRENT_APP_VERSION).build();
        when(appConfigService.getAppConfig()).thenReturn(appConfig);
        appConfigValidator.validateAppVersion(CURRENT_APP_VERSION);
    }
}
