package com.scb.rider.util;

public class AccountMaskUtils {
    private AccountMaskUtils(){}
    public static final String NATIONAL_ID_FORMAT = "X-XXXX-XXXX#-##-#";
    public static final String ACCOUNT_NO_FORMAT = "XXX-XXX###-#";

    public static String maskAccountDetails(String account, String mask) {
        int index = 0;
        StringBuilder maskedNumber = new StringBuilder();
        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);
            if (c == '#') {
                maskedNumber.append(account.charAt(index));
                index++;
            } else if (c == 'x') {
                maskedNumber.append(c);
                index++;
            } else {
                maskedNumber.append(c);
            }
        }
        return maskedNumber.toString();
    }
}
