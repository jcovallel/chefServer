package com.chefserver.demo.ExcelDB;

import com.chefserver.demo.model.ComentModel;
import com.chefserver.demo.model.DataModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelController {
    public void writeFile(List<DataModel> datamodel) throws IOException {
        //for local
        //File myfile = new File("DatosExcel/Reservaciones.xlsx");
        //for gcloud
        File myfile = new File("../DatosExcel/Reservaciones.xlsx");
        FileInputStream fis = new FileInputStream(myfile);

        // Finds the workbook instance for XLSX file
        XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);

        // Return first sheet from the XLSX workbook
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);

        // Start in the first row empty available
        Row fila = mySheet.createRow(0);
        Cell celda = fila.createCell(0);
        celda.setCellValue("Fecha");
        celda = fila.createCell(1);
        celda.setCellValue("Hora");
        celda = fila.createCell(2);
        celda.setCellValue("Nombre");
        celda = fila.createCell(3);
        celda.setCellValue("Celular");
        celda = fila.createCell(4);
        celda.setCellValue("Correo");
        celda = fila.createCell(5);
        celda.setCellValue("Cargo");
        celda = fila.createCell(6);
        celda.setCellValue("Tipo menú");
        celda = fila.createCell(7);
        celda.setCellValue("Reserva menú Lunes");
        celda = fila.createCell(8);
        celda.setCellValue("Reserva menú Martes");
        celda = fila.createCell(9);
        celda.setCellValue("Reserva menú Miercoles");
        celda = fila.createCell(10);
        celda.setCellValue("Reserva menú Jueves");
        celda = fila.createCell(11);
        celda.setCellValue("Reserva menú Viernes");
        celda = fila.createCell(12);
        celda.setCellValue("Entrega");
        celda = fila.createCell(13);
        celda.setCellValue("Hora entrega");
        celda = fila.createCell(14);
        celda.setCellValue("Dirección");
        celda = fila.createCell(15);
        celda.setCellValue("Observaciones");

        for(int i =0; i<datamodel.size(); i++){
            fila = mySheet.createRow(i+1);
            celda = fila.createCell(0);
            celda.setCellValue(datamodel.get(0).getFecha());
            celda = fila.createCell(1);
            celda.setCellValue(datamodel.get(1).getHora());
            celda = fila.createCell(2);
            celda.setCellValue(datamodel.get(2).getNombre());
            celda = fila.createCell(3);
            celda.setCellValue(datamodel.get(3).getCelular());
            celda = fila.createCell(4);
            celda.setCellValue(datamodel.get(4).getCorreo());
            celda = fila.createCell(5);
            celda.setCellValue(datamodel.get(5).getCargo());
            celda = fila.createCell(6);
            celda.setCellValue(datamodel.get(6).getTipomenu());
            celda = fila.createCell(7);
            if(datamodel.get(7).isLunes()){
                celda.setCellValue("si");
            }else {
                celda.setCellValue("no");
            }
            celda = fila.createCell(8);
            if(datamodel.get(8).isMartes()){
                celda.setCellValue("si");
            }else {
                celda.setCellValue("no");
            }
            celda = fila.createCell(9);
            if(datamodel.get(9).isMiercoles()){
                celda.setCellValue("si");
            }else {
                celda.setCellValue("no");
            }
            celda = fila.createCell(10);
            if(datamodel.get(10).isJueves()){
                celda.setCellValue("si");
            }else {
                celda.setCellValue("no");
            }
            celda = fila.createCell(11);
            if(datamodel.get(11).isViernes()){
                celda.setCellValue("si");
            }else {
                celda.setCellValue("no");
            }
            celda = fila.createCell(12);
            celda.setCellValue(datamodel.get(12).getEntrega());
            celda = fila.createCell(13);
            celda.setCellValue(datamodel.get(13).getHoraentrega());
            celda = fila.createCell(14);
            celda.setCellValue(datamodel.get(14).getDireccion());
            celda = fila.createCell(15);
            celda.setCellValue(datamodel.get(15).getObservaciones());
        }
        FileOutputStream salida = new FileOutputStream(myfile);
        myWorkBook.write(salida);
        myWorkBook.close();
    }

    public void comentwriteFile(List<ComentModel> comentmodel) throws IOException {
        //for local
        //File myfile = new File("DatosExcel/Reservaciones.xlsx");
        //for gcloud
        File myfile = new File("../DatosExcel/Comentarios.xlsx");
        FileInputStream fis = new FileInputStream(myfile);

        // Finds the workbook instance for XLSX file
        XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);

        // Return first sheet from the XLSX workbook
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);

        // Start in the first row empty available
        Row fila = mySheet.createRow(0);
        Cell celda = fila.createCell(0);
        celda.setCellValue("Empresa");
        celda = fila.createCell(1);
        celda.setCellValue("Estrellas");
        celda = fila.createCell(2);
        celda.setCellValue("Comentarios");
        celda = fila.createCell(3);
        celda.setCellValue("Nombre");
        celda = fila.createCell(4);
        celda.setCellValue("Celular");
        celda = fila.createCell(5);
        celda.setCellValue("Correo");

        for(int i =0; i<comentmodel.size(); i++) {
            fila = mySheet.createRow(i + 1);
            celda = fila.createCell(0);
            celda.setCellValue(comentmodel.get(0).getEmpresa());
            celda = fila.createCell(1);
            celda.setCellValue(comentmodel.get(1).getEstrellas());
            celda = fila.createCell(2);
            celda.setCellValue(comentmodel.get(2).getComentario());
            celda = fila.createCell(3);
            celda.setCellValue(comentmodel.get(3).getNombre());
            celda = fila.createCell(4);
            celda.setCellValue(comentmodel.get(4).getCelular());
            celda = fila.createCell(5);
            celda.setCellValue(comentmodel.get(5).getCorreo());
        }

        FileOutputStream salida = new FileOutputStream(myfile);
        myWorkBook.write(salida);
        myWorkBook.close();
    }
}
