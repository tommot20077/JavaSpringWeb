package xyz.dowob.blogspring.functions;

import xyz.dowob.blogspring.config.WebConfig;

import java.io.File;

public class StorageMethod {
    public static String getRunningDirectory() {
        if(System.getProperty("os.name").contains("dows")) {
            String path = WebConfig.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File jarFile = new File(path);
            String savePath;
            if (jarFile.isFile()) {
                savePath = jarFile.getParent();
                if (savePath != null && !savePath.trim().isEmpty()) {
                    return savePath;
                }else {
                    return "./";
                }
            } else {
                if (System.getProperty("os.name").contains("dows")) {
                    path = path.substring(1, path.length());
                }
                if (path.contains("jar")) {
                    path = path.substring(0, path.lastIndexOf("."));
                    path = path.substring(0, path.lastIndexOf("/"));
                }
                savePath = path.replace("target/classes/", "");
                savePath = savePath.replace("nested:/", "");
                savePath = savePath.replace("ested:/", "");


                if (!savePath.trim().isEmpty()) {
                    return savePath;
                }else {
                    return "./";
                }
            }
        }else {
            return "";
        }

    }
}

