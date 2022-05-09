package com.scb.rider.validator;

import com.scb.rider.exception.AppUpgradeRequiredException;
import com.scb.rider.model.document.AppConfig;
import com.scb.rider.service.document.AppConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
@Slf4j
public class AppConfigValidator {

    private static final int[] mulDigit = new int[] { 4, 3, 2, 7, 6, 5, 4, 3, 2, 0 };

    @Autowired
    private AppConfigService appConfigService;

    public void validateAppVersion(Integer currentAppVersion) {
        AppConfig appConfig = appConfigService.getAppConfig();
        if (Objects.nonNull(appConfig)) {
            log.info("header appVersion: {}, database appVersion: {}, forceUpdate: {}", currentAppVersion, appConfig.getVersion(), appConfig.isForceUpdate());
            if (appConfig.isForceUpdate() && appConfig.getVersion() > currentAppVersion) {
                throw new AppUpgradeRequiredException("There is a new app version available for download");
            }
        }
    }

    public boolean validateCreditAccountNumber(String accountNumber) {
        boolean flag = false;
        if(StringUtils.isNotBlank(accountNumber) && isNumeric(accountNumber) && accountNumber.length() == 10) {
            flag = checkDigitSCBAccount(accountNumber, 9);
        }
        return flag;
    }

    public static boolean isNumeric(String _number) {
        try {
            new BigDecimal(_number.trim());
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean checkDigitSCBAccount(String _account, int checkPosition) throws NumberFormatException {
        int digitSum = 0;
        int chkDigit = 0;
        boolean isFCD2Account = false;

        if (_account.length() == 13) {
            isFCD2Account = true;
            _account = _account.substring(0, 10);
        }
        for (int i = 0; i < mulDigit.length; i++) {
            digitSum += Integer.parseInt(_account.substring(i, i + 1)) * mulDigit[i];
        }

        digitSum %= 10;

        if (digitSum != 0) {
            chkDigit = 10 - digitSum;
        } else {
            chkDigit = digitSum;
        }

        if (isFCD2Account) {
            chkDigit++;
            if (chkDigit == 10) {
                chkDigit = 0;
            }
        }

        return chkDigit == Integer.parseInt(_account.substring(checkPosition, checkPosition + 1));
    }

}
