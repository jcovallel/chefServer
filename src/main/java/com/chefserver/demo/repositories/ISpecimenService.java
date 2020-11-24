package com.chefserver.demo.repositories;

import org.springframework.web.multipart.MultipartFile;


public interface ISpecimenService {
    void saveImage(MultipartFile imageFile, String empresa) throws Exception;
}
