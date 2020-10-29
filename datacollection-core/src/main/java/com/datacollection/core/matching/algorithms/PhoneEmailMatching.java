package com.datacollection.core.matching.algorithms;

import com.datacollection.core.matching.ProfileLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kumin on 30/11/2017.
 */
public class PhoneEmailMatching implements MatchingAlgorithm {

    @Override
    public Map<String, Double> computeScore(Collection<ProfileLog> profileLogs, String value) {
        /*Naive Bayes Probabilistic
        profile u, entity e
        posterior P(u|e) = P(e|u)*P(u)/P(e) = (s/n)*(s/m)/P(e)
        Where: s is number of times u post e
               n is number of posts's u contain entity
               m is number of posts contain e
        Objective Function f = arg max(P(u|e))
        */
        Map<String, Double> scoreMatchingUids = new HashMap<>();
        int m = 0;
        Map<String, List<Integer>> frequencyMap = new HashMap<>();
        for (ProfileLog profileLog : profileLogs) {
            int s = profileLog.entityLogs.get(value);
            m += s;
            int n = 0;
            for (String key : profileLog.entityLogs.keySet()) {
                n += profileLog.entityLogs.get(key);
            }
            List<Integer> frequencyList = new ArrayList<>();
            frequencyList.add(s);
            frequencyList.add(n);

            frequencyMap.put(profileLog.uid, frequencyList);
        }
        for (String key : frequencyMap.keySet()) {
            List<Integer> frequencyList = frequencyMap.get(key);
            Double score = frequencyList.get(0) * frequencyList.get(0) / (double) (m * frequencyList.get(1));
            scoreMatchingUids.put(key, score);
        }
        return scoreMatchingUids;
    }

    @Override
    public List<String> getUidMaxScore(Map<String, Double> uidScore) {
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
