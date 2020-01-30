package com.chefserver.demo.ExcelDB;

import com.chefserver.demo.model.DataModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelController {
    public void writeFile(DataModel datamodel) throws IOException {
        File myfile = new File("DatosExcel/Reservaciones.xlsx");
        FileInputStream fis = new FileInputStream(myfile);

        // Finds the workbook instance for XLSX file
        XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);

        // Return first sheet from the XLSX workbook
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);

        // Start in the first row empty available
        Row fila = mySheet.createRow(mySheet.getLastRowNum()+1);
        Cell celda = fila.createCell(0);
        celda.setCellValue(datamodel.getFecha().toString("dd/MM/yyyy"));
        celda = fila.createCell(1);
        celda.setCellValue(datamodel.getHora().toString("HH:mm"));
        celda = fila.createCell(2);
        celda.setCellValue(datamodel.getNombre());
        celda = fila.createCell(3);
        celda.setCellValue(datamodel.getCelular());
        celda = fila.createCell(4);
        celda.setCellValue(datamodel.getCorreo());
        celda = fila.createCell(5);
        celda.setCellValue(datamodel.getCargo());
        celda = fila.createCell(6);
        if(datamodel.isLunes()){
            celda.setCellValue("si");
        }
        celda = fila.createCell(7);
        if(datamodel.isMartes()){
            celda.setCellValue("si");
        }
        celda = fila.createCell(8);
        if(datamodel.isMiercoles()){
            celda.setCellValue("si");
        }
        celda = fila.createCell(9);
        if(datamodel.isJueves()){
            celda.setCellValue("si");
        }
        celda = fila.createCell(10);
        if(datamodel.isViernes()){
            celda.setCellValue("si");
        }
        celda = fila.createCell(11);
        celda.setCellValue(datamodel.getObservaciones());
        FileOutputStream salida = new FileOutputStream(myfile);
        myWorkBook.write(salida);
        myWorkBook.close();
    }
}
