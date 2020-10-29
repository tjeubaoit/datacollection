package com.datacollection.core.extract.raw.xlsx;

import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.extract.raw.xlsx.model.KeyValueModel;
import com.datacollection.core.platform.elastic.ElasticBulkInsert;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.elasticsearch.action.bulk.BulkResponse;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadExcelAuto {

    private final String path = "/home/tailetuan/storage_file";
    private final long timeAutoRunService = 300000;
    private final String index_Es = "raw_excel";
    private final String tyle_Es = "excel";
    private ProfileRegexHelper profileRegexHelper;
    private ElasticBulkInsert bulkInsert1;

    public ReadExcelAuto() {
        profileRegexHelper = ProfileRegexHelper.getDefault();
        bulkInsert1 = new ElasticBulkInsert(new Configuration().toSubProperties(index_Es));
    }

    public void readFileExcel(String fileName){
        if(!Strings.isNullOrEmpty(fileName)){

            FileInputStream inputStream = null;
            final String id_Es = fileName.toLowerCase();
            try {
                inputStream = new FileInputStream(new File(path + "/" + fileName).getAbsoluteFile());
                Workbook workbook =  WorkbookFactory.create(inputStream);
                for (int n = 0; n < workbook.getNumberOfSheets();n++){
                    Sheet sheet = workbook.getSheetAt(n);
                    Row row;
                    List<String> lstNameExcel = new ArrayList<>();
                    int cnt = 0;
                    int lastRowNum = sheet.getLastRowNum() + 1;

                    for (int i = 0; i < lastRowNum; i++) {
                        Map<String,Object> map = new HashMap<>();
                        row = sheet.getRow(i);
                        int cntRow = 0;
                        if(row != null){

                            List<KeyValueModel> valueModels = null;
                            if(i < 1){
                                for (int j = 0; j < row.getLastCellNum(); j++){
                                    String value = row.getCell(j).toString().toLowerCase();
                                    if(!Strings.isNullOrStringEmpty(value)){
                                        lstNameExcel.add(row.getCell(j).toString().toLowerCase());
                                    }
                                }
                            }else {
                                for(int t = 0; t < lstNameExcel.size();t++){
                                    if(row.getCell(t) != null && !Strings.isNullOrEmpty(row.getCell(t).toString())){
                                        if(!Strings.isNullOrEmpty(lstNameExcel.get(t))){
                                            Cell cell = row.getCell(t);
                                            cell.setCellType(CellType.STRING);

                                            String strType = lstNameExcel.get(t).trim().toLowerCase();
                                            String strContent = cell.getStringCellValue().trim().toLowerCase();

                                            if(strType.equals("name")){
                                                if(checkCompany(strContent)){
                                                    strType = "company";
                                                }
                                            }

                                            valueModels = setValue(valueModels,strType,strContent);
                                        }
                                        cnt = 0;
                                    }else {
                                        cntRow++;
                                    }
                                }
                            }

                            if(valueModels != null){
                                for(KeyValueModel keyValueModel : valueModels){
                                    map.put(keyValueModel.getKey(),keyValueModel.getValue());
                                }
                            }

                            if(cntRow == row.getLastCellNum()){
                                cnt++;
                            }

                        }else{
                            cnt++;
                        }
                        if(i > 0 && map.toString().length() > 2){
                            System.out.println(map);
                            bulkInsert1.addRequest(tyle_Es, id_Es + "_" + n + "_" + i, map);
                        }

                        if((bulkInsert1.bulkSize() > 500 || (i + 1 == sheet.getLastRowNum()) || cnt > 5) && bulkInsert1.bulkSize() > 0){
                            BulkResponse response = bulkInsert1.submitBulk();
                            System.out.print(response.buildFailureMessage());
                            System.out.println(!response.hasFailures());
                        }

                        if(cnt > 5){
                            break;
                        }
                    }

                    if(n + 1 == workbook.getNumberOfSheets()){

                        System.out.print(fileName + " :");
                        System.out.println(moveFileInBackup(fileName));
                    }
                }

            } catch (InvalidFormatException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(inputStream != null){
                        inputStream.close();
                    }
                } catch (IOException e) { }
            }
        }
    }

    private List<KeyValueModel> setValue(List<KeyValueModel> keyValueModels, String key, String value){
        if(keyValueModels == null){
            keyValueModels = new ArrayList<>();
        }

        int count = 0;
        for (KeyValueModel keyValue : keyValueModels){
            if(keyValue.getKey().toLowerCase().equals(key.toLowerCase())){
                List<String> lStr = keyValue.getValue();
                if(keyValue.getKey().toLowerCase().equals("phone")){
                    lStr.addAll(checkPhone(value));
                }else if(keyValue.getKey().toLowerCase().equals("email")){
                    lStr.addAll(checkEmail(value));
                }else {
                    lStr.add(value);
                }
                keyValue.setValue(lStr);
                keyValueModels.set(count,keyValue);
                return keyValueModels;
            }
            count++;
        }

        List<String> str = new ArrayList<>();
        if(key.toLowerCase().equals("phone")){
            str.addAll(checkPhone(value));
        }else if(key.toLowerCase().equals("email")){
            str.addAll(checkEmail(value));
        }else {
            str.add(value);
        }
        keyValueModels.add(new KeyValueModel(key,str));

        return keyValueModels;
    }

    private List<String> checkPhone(String v){
        List<String> lstPhone = new ArrayList<>();
        for (String s : v.split(",")){
            if(!checkRegexPhone(s)){
                s = 0 + s;
            }
            if(profileRegexHelper.isPhone(s)){
                lstPhone.add(s);
            }
        }
        return lstPhone;
    }

    private List<String> checkEmail(String v){
        List<String> lstEmail = new ArrayList<>();
        for (String s : v.split(",")){
            if(profileRegexHelper.isEmail(s)){
                lstEmail.add(s);
            }
        }
        return lstEmail;
    }

    private boolean checkRegexPhone(String line){
        String pattern = "^0[0-9]+$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        return m.find();
    }


    public boolean moveFileInBackup(String nameFile){
        File checkFolder = new File(path + "/backup");
        if(!checkFolder.isDirectory()){
            checkFolder.mkdir();
            System.out.println("New Folder backup");
        }

        File file = new File(path + "/" + nameFile);
        if(file.exists()){
            return file.renameTo(new File(path + "/backup/" + file.getName()));
        }
        return false;
    }

    public String renameFile(String fileName){
        File file = new File(path + "/" + fileName);
        if(file.exists()){
            String strName = new Date().getTime() + "_" + file.getName().trim().replace(" ","_");
            if(file.renameTo(new File(path + "/" + strName))){
                return strName;
            }
        }
        return null;
    }

    public void readFileName(){
        try{

            File file = new File(path);
            File[] lstFile = file.listFiles();

            for (File f: lstFile) {
                String key_file = f.getName().toLowerCase();
                if(key_file.endsWith(".xlsx") || key_file.endsWith(".xls")){
                    readFileExcel(renameFile(f.getName()));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("folder don't exist");
        }
    }

    public void autoRunService(){
        try{
            while (true){
                Date d = new Date();
                readFileName();
                System.out.println("Run: " + d);
                Thread.sleep(timeAutoRunService);
            }
        }catch (Exception e){
            System.out.println("Error auto run service");
        }
    }

    private boolean checkCompany (String s){
        s = s.trim().toLowerCase();
        if(s.indexOf("công ty") > -1 || s.indexOf("cty") > -1 || s.indexOf("cong ty") > -1){
            return true;
        }
        return false;
    }


    public static void main (String [] args){
        ReadExcelAuto readExcelAuto = new ReadExcelAuto();
        readExcelAuto.autoRunService();
    }
}
