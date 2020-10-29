package com.datacollection.core.extract.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.extract.Extractor;
import okhttp3.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatBotExtractor extends Extractor {

    private final String URL_PAGE = "https://chatapi.bizfly.vn/chatbox/api.php?mod=social&cmd=loadListAllFanpage";

    OkHttpClient client = new OkHttpClient();

    public ChatBotExtractor(Configuration config) {
        super("chatbot.api", config);
    }

    private String run (String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private List<Map<String,Object>> getListFanpage() throws IOException {
        String res = run(URL_PAGE);
        HashMap mapPage = new ObjectMapper().readValue(res, HashMap.class);
        return (List<Map<String, Object>>) mapPage.get("data");
    }


    private String getDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(new Date());
    }

    @Override
    protected void onLoop() throws Exception {
        List<Map<String,Object>> listPage = getListFanpage();
        for (Map<String,Object> map : listPage){
            if(!Strings.isNullOrStringEmpty(map.get("FanPageID"))
                    && !Strings.isNullOrStringEmpty(map.get("project"))){
                String url = "https://rdapip.zamba.vn/facebook/contact_of_user?project=" + map.get("project") + "&fanpage_id=" + map.get("FanPageID") + "&start_time=" + getDate() + "&end_time=" + getDate();
                String res = run(url);
                HashMap mapPage = new ObjectMapper().readValue(res, HashMap.class);
                Map<String,Object> mapData = (Map<String, Object>) mapPage.get("data");
            }
        }
    }

}
