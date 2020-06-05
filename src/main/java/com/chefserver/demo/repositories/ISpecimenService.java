package com.chefserver.demo.repositories;

import com.chefserver.demo.model.DisponibilidadModel;
import org.springframework.web.multipart.MultipartFile;


public interface ISpecimenService {
    void saveImage(MultipartFile imageFile, String empresa) throws Exception;
}
