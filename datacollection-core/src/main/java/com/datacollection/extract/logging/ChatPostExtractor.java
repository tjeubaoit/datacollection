package com.datacollection.extract.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.utils.Strings;
import com.datacollection.extract.Extractor;
import com.datacollection.extract.model.GenericModel;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatPostExtractor extends Extractor {

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private boolean runFirstTime = false;
    private final String REGEXFOLDER = "^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}$";
    private final String REGEXFILE = "^adv[0-9]+\\.dat$";
    private String indexPath;
    private String indexData;

    public ChatPostExtractor(Configuration config) {
        super("chatpost", config);
        indexPath = props.getProperty("datalogging.path") + "/extract/" + getClass().getSimpleName();
        indexData = props.getProperty("chatpost.path");
    }

    public void readFileLog(String nameFolder) throws IOException {
        List<String> listNameFileLog = UtilsLog.getNameFolderOrFile(indexPath);

        if(!UtilsLog.checkFileName(listNameFileLog).contains(nameFolder)){

            List<String> listFile = UtilsLog.getNameFolderOrFile(indexData + "/" + nameFolder);
            
            String urlFileLog = indexPath + "/" + nameFolder;
            String contentFile = UtilsLog.readLastLine(urlFileLog);

            if(!UtilsLog.checkFileFinish(contentFile)){
                for (String nameFile : listFile){
                    if (UtilsLog.checkRegex(REGEXFILE,nameFile)){

                        contentFile = UtilsLog.readLastLine(urlFileLog);

                        if(UtilsLog.addLogging(nameFolder,nameFile,urlFileLog,contentFile)){
                            insertKafka(indexData + "/" + nameFolder + "/" + nameFile);
                        }
                    }
                }

                if(listNameFileLog.size() > 0){
                    UtilsLog.closeLine(nameFolder,listNameFileLog.get(listNameFileLog.size() - 1),indexPath);
                }else{
                    UtilsLog.closeLine(nameFolder,null,indexPath);
                }

            }

        }
    }

    private void insertKafka(String url) throws IOException {
        List<String> getLines = UtilsLog.readFile(url);
        for (String line : getLines){
            String[] arrLine = line.split("\t");
            if(!arrLine[1].equals("-1")){
                String contentDecode = URLDecoder.decode(arrLine[1], "UTF-8");

                HashMap<String, Object> result = new ObjectMapper().readValue(contentDecode, HashMap.class);
                List<Map<String,Object>> row = (List<Map<String, Object>>) result.get("entry");
                Map<String ,Object> entry =  row.get(0);

                if(!Strings.isNullOrStringEmpty(entry.get("time")) &&
                        !Strings.isNullOrStringEmpty(entry.get("id"))){
                    store(new GenericModel( entry.get("time") + "_" + entry.get("id"), GenericModel.TYPE_ADCHATBOT, result));
                }

            }
        }
    }

    @Override
    protected void onLoop() throws Exception {
        while (isNotCanceled()) {
            if(!runFirstTime){
                List<String> listFolder = UtilsLog.getNameFolderOrFile(indexData);

                for(String nameFolder : listFolder){
                    if(UtilsLog.checkRegex(REGEXFOLDER,nameFolder)){
                        readFileLog(nameFolder);
                    }
                }
                runFirstTime = true;
            }else {
                readFileLog(Time.getDefault(Time.SDFDATE));
            }
        }
    }

}
