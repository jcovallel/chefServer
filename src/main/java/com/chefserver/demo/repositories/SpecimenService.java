package com.chefserver.demo.repositories;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.StringUtils;

@Component
public class SpecimenService implements ISpecimenService{

    @Override
    public void saveImage(MultipartFile imageFile, String empresa) throws Exception {
        empresa = StringUtils.stripAccents(empresa).replaceAll(" ","-");
        String i = empresa.substring(empresa.indexOf("?")+1);
        empresa = empresa.substring(0,empresa.indexOf("?"));
        //for local
        //String folder ="src/main/resources/Images/"+empresa+"/"+"/Menu"+i+".jpg";
        //for gcloud
        String folder ="../src/main/resources/Images/"+empresa+"/"+"/Menu"+i+".jpg";
        byte[] bytes = imageFile.getBytes();
        Path path = Paths.get(folder);
        if(i.equals("0")){
            //for local
            //FileUtils.deleteDirectory(new File("src/main/resources/Images/"+empresa));
            //for gcloud
            FileUtils.deleteDirectory(new File("../src/main/resources/Images/"+empresa));
        }
        Files.createDirectories(path.getParent());
        if( !Files.exists(path))
            Files.createFile(path);
        Files.write(path,bytes);
    }
}
