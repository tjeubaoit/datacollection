package com.datacollection.core.extract.logging;

import com.datacollection.common.config.Configuration;
import com.datacollection.common.mb.MockMsgBrokerFactory;
import com.datacollection.core.extract.Extractor;
import com.datacollection.core.extract.model.GenericModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackFormExtractor extends Extractor {

    private boolean runFirstTime = false;
    private final String REGEXFOLDER = "^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}$";
    private final String REGEXFILE = "^tkf-[0-9]+\\.dat$";
    private String indexPath;
    private String indexData;

    public TrackFormExtractor(Configuration config) {
        super("trackform", config);
        indexPath = props.getProperty("datalogging.path") + "/extract/" + getClass().getSimpleName();
        indexData = props.getProperty("trackform.path");

    }

    private void insertKafka(String url) throws IOException {
        List<String> getLines = UtilsLog.readFile(url);
        String nameFile = url.split("/")[url.split("/").length -1].replace(".dat","");
        int number = 0;
        for (String line : getLines){
            Map<String ,Object> result = new HashMap<>();
            result.put("logging",line);
            store(new GenericModel( number + nameFile, GenericModel.TYPE_TRACKFORM, result));
            number++;
        }
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
