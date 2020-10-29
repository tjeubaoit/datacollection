package com.datacollection.core.extract.raw;

import com.datacollection.common.config.Configuration;
import com.datacollection.core.platform.elastic.ElasticBulkInsert;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.bulk.BulkResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by duc on 27/04/2017.
 */
@SuppressWarnings({"deprecation", "MismatchedQueryAndUpdateOfCollection"})
public class ReadExcel {
    static Workbook getWorkbook(String type, FileInputStream fis) throws IOException {
        if (type.equals("xls"))
            return new HSSFWorkbook(fis);
        else if (type.equals("xlsx"))
            return new XSSFWorkbook(fis);

        return null;
    }

    public static void main(String[] args) throws IOException {
        ElasticBulkInsert elasticBulkInsert = new ElasticBulkInsert(new Configuration().toSubProperties("raw"));

        String dirpath = "/home/duc/Desktop/Xu Ly Database";
        Scanner scanner = new Scanner(dirpath);

        String files = null;
        File folder = new File(dirpath);
        File[] listofFiles = folder.listFiles();
        while (true) {

            dirpath = scanner.nextLine();
            File file1 = new File(dirpath);
            if (file1.canRead()) break;
             System.out.println("Error: Directory does not exitsts");
        }
        try {
            for (int i = 0; i < listofFiles.length; i++) {
                if (listofFiles[i].isFile()) {
                    files = listofFiles[i].getAbsolutePath();
                    if (files.endsWith(".xlsx") || files.endsWith(".xls")) {
                        System.out.println(files);
                        FileInputStream fis = new FileInputStream(files);

                        Workbook myWorkBook;
                        if (files.endsWith(".xlsx"))
                            myWorkBook = getWorkbook("xlsx", fis);
                        else
                            myWorkBook = getWorkbook("xls", fis);

                        int numberOfAllSheet = myWorkBook.getNumberOfSheets();
                        // System.out.println(numberOfAllSheet);
                        for (int numberOfSheet = 0; numberOfSheet < numberOfAllSheet; numberOfSheet++) {
                            Sheet sheet = myWorkBook.getSheetAt(numberOfSheet);


                            int totalRows = sheet.getPhysicalNumberOfRows();
                            Map<String, Integer> mapHeader = new HashMap<>();

                            Row row = sheet.getRow(0);

                            int minColumIndex = row.getFirstCellNum();
                            int maxColumIndex = row.getLastCellNum();

                            for (int columIndex = minColumIndex; columIndex < maxColumIndex; columIndex++) {
                                Cell cell = row.getCell(columIndex);
                                mapHeader.put(cell.getStringCellValue(), cell.getColumnIndex());
                                //System.out.println(mapHeader);
                            }

                            String id = null;
                            for (int x = 1; x < totalRows; x++) {
                                Map<String, Object> mapRow = new HashMap<>();
                                Row dataRow = sheet.getRow(x);

                                List<String> phones = new ArrayList<>();

                                for (String key : mapHeader.keySet()) {
                                    int index = mapHeader.get(key);
                                    //System.out.println(totalRows);
                                    //System.out.println(x);
                                    Cell cell = dataRow.getCell(index, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                                    Object value = null;

                                    switch (cell.getCellType()) {
                                        case Cell.CELL_TYPE_NUMERIC:
                                            cell.setCellType(Cell.CELL_TYPE_STRING);
                                            value = cell.getStringCellValue() + " ";
                                            //      mapRow.put(key, value);
                                            break;
                                        case Cell.CELL_TYPE_STRING:
                                            value = cell.getStringCellValue();
                                            if (key.equals("phone")|| key.equals("office_phone") ) {
                                                value = value.toString().replace("Fax","-");
                                                value = value.toString().replace(" ","");
                                                value = value.toString().replace(".","");
                                                value = value.toString().replace(",","");
                                                value = value.toString().replace("'","");
                                                value = value.toString().replace("/","-");
                                                //  value = value.toString().replace("-","");
                                                value = value.toString().replace("(","");
                                                value = value.toString().replace(")","");

                                                // System.out.println(value);
                                                if (value.toString().startsWith("0"))
                                                    break;
                                                value = "0" + value;
                                                //mapRow.put(key, value);
                                            } else //mapRow.put(key, value);
                                                break;
                                    }
                                    if (key.equals("phone") && value!= null){
                                        id = value.toString();
                                        //  System.out.println(id);
                                    } else if (key.equals("office_phone") && value!= null){
                                        id = value.toString();
                                    }
                                    if (value !=null && (key.equals("phone") || key.equals("office_phone"))) {
                                        phones.add(value.toString());
                                        mapRow.put("phone", phones);
                                    }else
                                    if (value != null)
                                        mapRow.put(key, value);
                                }
                                // listDataHolders.add(mapRow);
                                // System.out.println(x+":"+mapRow);
                                //System.out.println(id);
                                elasticBulkInsert.addRequest("excel", id,mapRow );
                                if (elasticBulkInsert.bulkSize() > 1000 ){
                                    BulkResponse response = elasticBulkInsert.submitBulk();
                                    System.out.println(response.buildFailureMessage());
                                    System.out.println(!response.hasFailures());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Excel files doesn't exit or files have error");
        }
    }
}
