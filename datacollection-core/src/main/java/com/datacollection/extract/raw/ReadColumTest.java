package com.datacollection.extract.raw;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by duc on 20/04/2017.
 */
@SuppressWarnings("deprecation")
public class ReadColumTest {

    static class DataHolder {
        public String data[];
        DataHolder(int totalAttribute) {
            data = new String[totalAttribute];
        }
    }

    public static void main(String[] args) throws IOException {
        String input_path = "/home/duc/IdeaProjects/DataCollection/src/main/resources/247_XEM_DS 30.000 CHUC VU HA NOI.xls";
        String output_path = " ";

        File input = new File(input_path);
        FileInputStream fis = new FileInputStream(input);

        HSSFWorkbook myWorkBook = new HSSFWorkbook(fis);

        HSSFSheet sheet = myWorkBook.getSheetAt(0);
        int totalRows = sheet.getPhysicalNumberOfRows();

        Iterator<Row> rowIterator = sheet.iterator();

        Map<String, Integer> map = new HashMap<>();

        HSSFRow row = sheet.getRow(0);

        int minColumIndex = row.getFirstCellNum();
        int maxColumIndex = row.getLastCellNum();

        for (int columIndex = minColumIndex; columIndex < maxColumIndex; columIndex++) {
            HSSFCell cell = row.getCell(columIndex);
            map.put(cell.getStringCellValue(), cell.getColumnIndex());
            //System.out.println(map);
        }


        List<DataHolder> listDataHolders = new LinkedList<>();
        //ArrayList<ReportRow> listOfData = new ArrayList<ReportRow>();
        for (int x = 1; x < totalRows; x++) {
           // ReportRow rr = new ReportRow();
            HSSFRow dataRow = sheet.getRow(x);
            DataHolder dataHolder = new DataHolder(map.size());

            for (String key : map.keySet()) {
                int index = map.get(key);
                HSSFCell cell = dataRow.getCell(index, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_NUMERIC:
                        int value = (int) cell.getNumericCellValue();
                        dataHolder.data[index] = value + "";
                        break;
                    case Cell.CELL_TYPE_STRING:
                        String valueStr = cell.getStringCellValue();
                        dataHolder.data[index] = valueStr;
                        break;
                }
            }
            listDataHolders.add(dataHolder);
        }

        for (int i = 0; i < listDataHolders.size(); i ++) {
            DataHolder read = listDataHolders.get(i);
            System.out.println(Arrays.asList(read.data));
        }

    }
}