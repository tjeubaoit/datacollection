package com.datacollection.core.tools;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import java.io.*;
import java.util.Locale;

/**
 * Created by kumin on 19/04/2017.
 */
public class ExcelReader {

    public static void convertExcelToVsv(){
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/Security.csv"), "utf-8"));
            WorkbookSettings ws = new WorkbookSettings();
            ws.setLocale(new Locale("vn", "VN"));

            Workbook workbook = Workbook.getWorkbook(new File("data/Security.xls"));
            for (int i =0; i<workbook.getNumberOfSheets();i++){
                Sheet sheet = workbook.getSheet(i);
                System.out.println(sheet.getName());
                for(int j =0 ; j<sheet.getRows();j++){
                    Cell[] cells = sheet.getRow(j);
                    for(int k =0; k<cells.length;k++){
                        System.out.println(cells[k].getContents());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }finally {
            if(bw!=null) try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ExcelReader.convertExcelToVsv();
    }
}
