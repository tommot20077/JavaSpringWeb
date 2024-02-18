package xyz.dowob.blogspring.functions;

import xyz.dowob.blogspring.config.WebConfig;

import java.io.File;

public class StorageMethod {
    public static String getRunningDirectory() {

        String path = WebConfig.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File jarFile = new File(path);
        System.out.println("jarFile: " + jarFile);
        String savePath;
        if(jarFile.isFile()) {
            savePath = jarFile.getParent();
            System.out.println("savePath: " + savePath);
        }else {
            if(System.getProperty("os.name").contains("dows")) {
                path = path.substring(1,path.length());
                System.out.println("path: " +path);
            }
            if(path.contains("jar")) {
                path = path.substring(0,path.lastIndexOf("."));
                path = path.substring(0,path.lastIndexOf("/"));
                System.out.println("path: " +path);
            }
            savePath = path.replace("target/classes/", "");
            savePath = savePath.replace("nested:/", "");
            savePath = savePath.replace("ested:/", "");

        }

        System.out.println("外連目錄 " + savePath);
        if (savePath != null && !savePath.trim().isEmpty()) {
            System.out.println("正常savePath: " +savePath);
            return savePath;
        }else {
            if(System.getProperty("os.name").contains("dows")) {
                System.out.println("強制savePath: " + "D:/end/workspace/java/");
                return "D:/end/workspace/java/";
            }
            else {
                System.out.println("強制savePath: " + "/root/JavaWeb/");
                return "";
            }
        }
    }
}
