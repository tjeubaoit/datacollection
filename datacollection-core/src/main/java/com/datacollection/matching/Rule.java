package com.datacollection.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kumin on 27/10/2017.
 */
public class Rule {
    public static final String PROFILE_TYPE = "fb.profile";
    public static final String EXCEL_TYPE = "excel";
    private static final String VIETID_TYPE = "fr.vietid";
    private static final String ECOMMERCE_TYPE = "fr.ecommerce";
    private static final String WEBLOG_TYPE = "fr.weblog";
    private static final String ZAMBA_TYPE = "fr.zb";

    private static final double THRESH_HOLD_NUMBER_OF_PHONE_LEVEL_1 = 3;
    private static final double THRESH_HOLD_NUMBER_OF_PHONE_LEVEL_2 = 5;
    private static final double THRESH_HOLD_NUMBER_OF_PHONE_LEVEL_3 = 10;
    private static final double THRESH_HOLD_NUMBER_OF_EMAIL_LEVEL_1 = 5;
    private static final double THRESH_HOLD_NUMBER_OF_EMAIL_LEVEL_2 = 10;

    public static double ruleByTypeHistoryAndFrequency(String type) {

        if (type.equals(PROFILE_TYPE)) return 500;
        if (type.equals(EXCEL_TYPE)) return 500;
        if (type.equals(VIETID_TYPE)) return 500;
        if (type.equals(ECOMMERCE_TYPE)) return 5;
        if (type.equals(WEBLOG_TYPE)) return 5;
        if (type.equals(ZAMBA_TYPE)) return 5;

        return 1;
    }

    public static double ruleByNumOfPhone(int size) {
        if (size > THRESH_HOLD_NUMBER_OF_PHONE_LEVEL_3) return -5;
        if (size > THRESH_HOLD_NUMBER_OF_PHONE_LEVEL_2) return -3;
        if (size > THRESH_HOLD_NUMBER_OF_PHONE_LEVEL_1) return -1;
        return 0;
    }

    public static double ruleByNumOfEmail(int size) {
        if (size > THRESH_HOLD_NUMBER_OF_EMAIL_LEVEL_2) return -3;
        if (size > THRESH_HOLD_NUMBER_OF_EMAIL_LEVEL_1) return -1;
        return 0;
    }

    public static List<String> getUidMaxScore(Map<String, Double> uidScore) {
        List<String> uidWithMaxScore = new ArrayList<>();
        double maxScore = -Double.MAX_VALUE;
        for (String key : uidScore.keySet()) {
            if (uidScore.get(key) > maxScore) {
                maxScore = uidScore.get(key);
                uidWithMaxScore.clear();
                uidWithMaxScore.add(key);
            } else if (uidScore.get(key) == maxScore) {
                uidWithMaxScore.add(key);
            }
        }
        return uidWithMaxScore;
    }
}
