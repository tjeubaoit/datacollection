package com.datacollection.extract.logging;

import com.datacollection.common.utils.Strings;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilsLog {

    public static List<String> getNameFolderOrFile(String url){
        List<String> listF = new ArrayList<>();

        File file = new File(url);
        File[] lstFile = file.listFiles();
        if(lstFile != null){
            for (File f: lstFile) {
                listF.add(f.getName());
            }
        }

        Collections.sort(listF);
        return listF;
    }

    public static String checkFileName(List<String> listFileName){
        String content = "";
        for (int i = 0; i < listFileName.size() - 1;i++){
            content += listFileName.get(i) + ",";
        }
        return content;
    }

    public static boolean addLogging(String folder, String fileName, String url,String getLine){
        if(!getLine.contains(fileName + "#")){
            if(!Strings.isNullOrStringEmpty(getLine)){
                if(getLine.split("\t")[1].equals("false")){
                    String s = getLine + fileName + "#";
                    UtilsLog.writerFile(s,url);
                }
            }else{
                String line = Time.getDefault(Time.SDFDATETIME) + "\tfalse\t" + folder + "\t" + fileName + "#" ;
                UtilsLog.writerFile(line,url);
            }
            return true;
        }
        return false;
    }

    public static boolean checkFileFinish(String getLine){
        if(!Strings.isNullOrStringEmpty(getLine) && getLine.split("\t")[1].equals("true")) return true;
        return false;
    }

    public static void closeLine(String nameFolder,String nameFileLog,String url){

        if(nameFileLog != null && !Time.getDefault(Time.SDFDATE).equals(nameFileLog)){
            String getLine = UtilsLog.readLastLine(url + "/" + nameFileLog);
            if(Strings.isNullOrStringEmpty(getLine)){
                UtilsLog.writerFile(getLine.replace("false","true"),url + "/" + nameFileLog);
            }
        }

        if(!Time.getDefault(Time.SDFDATE).equals(nameFolder)){
            String getLine = UtilsLog.readLastLine(url + "/" + nameFolder);
            if(getLine != null){
                UtilsLog.writerFile(getLine.replace("false","true"),url + "/" + nameFolder);
            }
        }
    }

    public static boolean checkRegex(String regex,String str){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    public static List<String> readFile(String path){
        List<String> lines = new ArrayList<>();

        BufferedReader br = null;
        FileReader fr = null;

        try {

            fr = new FileReader(path);
            br = new BufferedReader(fr);
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                lines.add(sCurrentLine);
            }

        } catch (IOException e) {
//            System.out.println("Error Read File");
        } finally {
            try {
                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();
            } catch (IOException ex) { }
        }

        return lines;
    }


    public static boolean writerFile(String line,String path){
        UtilsLog.checkFolder(path);
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(path);
            bw = new BufferedWriter(fw);
            bw.write(line);
            return true;
        } catch (IOException e) {
//            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {}
        }
        return false;
    }

    public static String readLastLine(String path){
        List<String> lines = readFile(path);
        return lines.size() > 0? lines.get(lines.size() - 1) : "";
    }


    public static void checkFolder(String path){
        String[] folder = path.split("/");
        String pathFile = "";
        for(int i = 1; i < folder.length - 1; i++){
            pathFile += "/"+ folder[i];
            File checkFolder = new File(pathFile);
            if(!checkFolder.isDirectory()){
                checkFolder.mkdir();
            }
        }

    }

}
