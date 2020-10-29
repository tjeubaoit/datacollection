package com.datacollection.core.tools.customer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rule {
    private  List<String> goodWords = new ArrayList<>();
    private  List<String> badWords = new ArrayList<>();

    public Rule loadRule(String pathFile){
            try (BufferedReader br = new BufferedReader(new FileReader(pathFile))){
                String line;
                while ((line = br.readLine())!=null){
                    if(line.startsWith("+")){
                        String[] lineSplit = line.split(":")[1].split(",");
                        goodWords.addAll(Arrays.asList(lineSplit));
                    }else {
                        String[] lineSplit = line.split(":")[1].split(",");
                        badWords.addAll(Arrays.asList(lineSplit));
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }

            return this;
    }

    public boolean containWords(String content){
        content = content.toLowerCase();
        for (String word: goodWords){
            if (content.contains(word)) return true;
        }
        return false;
    }
    public boolean notContainWords(String content){
        content = content.toLowerCase();
        for (String word: badWords){
            if (content.contains(word)) return false;
        }
        return true;
    }
}
