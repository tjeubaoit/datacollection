package com.datacollection.extract.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.datacollection.common.config.Configuration;
import com.datacollection.core.MockMsgBrokerFactory;
import com.datacollection.extract.Extractor;
import com.datacollection.extract.model.GenericModel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VietIdExtractor extends Extractor {

    private static final String APP_ID = "8cfb3f1552dd1560f56a0b4a";
    private String lastID;
    private final ObjectMapper om = new ObjectMapper();

    public VietIdExtractor(Configuration config) {
        super("vietid", config);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> connectionApi() throws Exception {
        HttpURLConnection conn = null;
        List<Map<String, Object>> lstMap = null;

        try {
            URL url = new URL("https://api.vietid.net/1.0/vccorp/getUser?app_id=" + APP_ID
                    + "&last_id=" + lastID + "&checksum=" + getMD5(APP_ID + lastID));

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            String jsonSource;

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((jsonSource = br.readLine()) != null) {
                    HashMap result = om.readValue(jsonSource, HashMap.class);
                    if (result.get("message").equals("SUCCESS")) {
                        lstMap = (List<Map<String, Object>>) result.get("data");
                    }
                }
            }
        } finally {
            if (conn != null) conn.disconnect();
        }
        return lstMap;
    }

    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onLoop() throws Exception {
        while (isNotCanceled()) {
            lastID = (loadIndex() == null) ? "1" : loadIndex();
            List<Map<String, Object>> contentApi = connectionApi();
            if (contentApi != null) {
                for (Map<String, Object> row : contentApi) {
                    String id = row.get("id").toString();
                    row.put("url", "https://api.vietid.net/1.0/vccorp/getUser/" + id);
                    row.remove("created");

                    store(new GenericModel()
                            .setId(id)
                            .setType(GenericModel.TYPE_API_VIETID)
                            .setPost(row));
                }
            } else {
                break;
            }
        }
    }

    public static void main(String[] args) {
        VietIdExtractor extractor = new VietIdExtractor(new Configuration());
        extractor.setMsgBrokerFactory(new MockMsgBrokerFactory());
        extractor.start();
    }
}
