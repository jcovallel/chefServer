package com.chefserver.demo.repositories;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class SpecimenService implements ISpecimenService{

    @Override
    public void saveImage(MultipartFile imageFile, String empresa) throws Exception {
        //for local
        //String folder ="src/main/resources/Images/Menu.jpg";
        //for gcloud
        empresa = empresa.replaceAll(" ","-");
        String folder ="../src/main/resources/Images/"+empresa+"Menu.jpg";
        byte[] bytes = imageFile.getBytes();
        Path path = Paths.get(folder);
        Files.write(path,bytes);
    }
}
