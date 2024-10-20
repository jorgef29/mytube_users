package com.fiuni.mytube_users.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;


public class DateUtil {

    // Formato deseado: dd/MM/yyyy
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // metodo para formatear java.util.Date a String en formato dd/MM/yyyy
    public static String formatDate(Date date) {
        // Convertir Date a LocalDate
        if(date == null) {
            return null;
        }else{
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return localDate.format(formatter);
        }

    }

    // metodo para obtener la fecha actual en formato dd/MM/yyyy
    public static String getCurrentDateFormatted() {
        LocalDate currentDate = LocalDate.now();
        return currentDate.format(formatter);
    }


    // metodo para convertir una cadena de fecha en formato dd/MM/yyyy a java.util.Date
    public static Date parseToDate(String dateStr) {
        if(dateStr == null || dateStr.isEmpty()) {
            return null;
        }else{
            LocalDate localDate = LocalDate.parse(dateStr, formatter);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
    }

}

