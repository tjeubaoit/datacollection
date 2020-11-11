package com.datacollection.extract.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.datacollection.common.config.Configuration;
import com.datacollection.core.MockMsgBrokerFactory;
import com.datacollection.extract.Extractor;
import com.datacollection.extract.model.GenericModel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RbExtractor extends Extractor {
    public String adID;

    public RbExtractor(Configuration config) {
        super("rb", config);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> connectionApi() throws Exception {
        HttpURLConnection conn;
        List<Map<String, Object>> lstMap = null;

        URL url = new URL("https://rongbay.com/api/store/product_zamba.php?act=list_item&ad_id=" + adID + "&limit=22");
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        String jsonSource = "";
        if (conn.getResponseCode() == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((jsonSource = br.readLine()) != null) {
                HashMap result = new ObjectMapper().readValue(jsonSource, HashMap.class);
                if (result.get("status").equals("success")) {
                    lstMap = (List<Map<String, Object>>) result.get("data");
                }
            }
        }
        conn.disconnect();
        return lstMap;
    }


    @Override
    protected void onLoop() throws Exception {
        while (isNotCanceled()) {
            adID = (loadIndex() == null) ? "0" : loadIndex();
            List<Map<String, Object>> contentApi = connectionApi();
            if (contentApi != null) {
                for (Map<String, Object> row : contentApi) {
                    store(new GenericModel(row.get("ad_id").toString(), GenericModel.TYPE_API_RB, row));
                }
            } else {
                break;
            }
        }
    }

    public static void main(String[] args) {
        Extractor extractor = new RbExtractor(new Configuration());
        extractor.setMsgBrokerFactory(new MockMsgBrokerFactory());
        extractor.start();
    }
}
