package com.wayapaychat.temporalwallet.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wayapaychat.temporalwallet.dto.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import com.wayapaychat.temporalwallet.exception.CustomException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelHelper {

    private static DataFormatter dataFormatter = new DataFormatter();

    public static String[] TYPE = {"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel"};
    public static List<String> PRIVATE_USER_HEADERS = Arrays.asList("ACCOUNT", "PHONE_NUMBER", "EMAIL", "AMOUNT", "TRANTYPE", 
    		"TRANCRNCY", "TRANNARRATION", "PAYMENTREF", "OFFICEACCOUNT" );

    public static List<String> PRIVATE_TRANSFER_HEADERS = Arrays.asList("OFFICEACCOUNT", "EMAIL_PHONE_NUMBER", "FULLNAME", "AMOUNT","TRANCRNCY", "TRANNARRATION", "PAYMENTREF" );

    public static List<String> TRANSFER_HEADERS = Arrays.asList("CUSTOMER_ACCOUNT", "EMAIL_PHONE_NUMBER", "FULLNAME", "AMOUNT","TRANCRNCY", "TRANNARRATION", "PAYMENTREF" );

    static String SHEET_NON_WAYA = "NoneWayaTransfer";

    static String SHEET = "Transaction";
    static Pattern alphabetsPattern = Pattern.compile("^[a-zA-Z]*$");
    static Pattern numericPattern = Pattern.compile("^[0-9]*$");
    static Pattern emailPattern = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\." + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            + "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");

    public static boolean hasExcelFormat(MultipartFile file) {
        if (!Arrays.asList(TYPE).contains(file.getContentType())) {
            return false;
        }
        return true;
    }

    public static BulkTransactionExcelDTO excelToBulkTransactionPojo(InputStream is, String fileName){

        try(Workbook workbook = getWorkBook(is, fileName)) {
            if(workbook == null){
                throw new CustomException("Invalid Excel File Check Extension", HttpStatus.BAD_REQUEST);
            }
            Set<ExcelTransactionCreationDTO> models = new HashSet<>();

            Sheet sheet = workbook.getSheet(SHEET);
            if(sheet == null) throw new CustomException("Invalid Excel File Format Passed, Check Sheet Name", HttpStatus.BAD_REQUEST);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()){
                Row currentRow = rows.next();
                Iterator<Cell> cellsInRow = currentRow.iterator();
                ExcelTransactionCreationDTO pojo = new ExcelTransactionCreationDTO();

                // If First Cell is empty break from loop
                if (currentRow == null || isCellEmpty(currentRow.getCell(0))) {
                    break;
                }

                // Skip header After Check of Header Formats
                if (rowNumber == 0) {
                    List<String> excelColNames = new ArrayList<>();
                    int i = 0;
                    while (cellsInRow.hasNext()) {
                        Cell cell = cellsInRow.next();
                        String cellValue = dataFormatter.formatCellValue(cell).trim().toUpperCase();
                        excelColNames.add(cellValue);
                        i++;
                        if (i == PRIVATE_USER_HEADERS.size()) {
                            break;
                        }
                    }
                    boolean value = checkExcelFileValidity(PRIVATE_USER_HEADERS, excelColNames);
                    if (!value) {
                        String errorMessage = "Failure, Incorrect File Format";
                        throw new CustomException(errorMessage, HttpStatus.BAD_REQUEST);
                    }
                    rowNumber++;
                    continue;
                }

                int cellIdx = 0;
                while (cellsInRow.hasNext()){
                    Cell cell = cellsInRow.next();
                    String colName = CellReference.convertNumToColString(cell.getColumnIndex()).toUpperCase();
                    switch (colName) {
                        case "A":
                            pojo.setCustomerAccountNo(defaultStringCell(cell));
                            break;
                        case "B":
                            pojo.setPhoneNumber(validateStringNumericOnly(cell, cellIdx, rowNumber));
                            break;
                        case "C":
                            pojo.setEmail(validateStringIsEmail(cell, cellIdx, rowNumber));
                            break;
                        case "D":
                            pojo.setAmount(validateStringBigDecimalOnly(cell, cellIdx, rowNumber));
                            break;
                        case "E":
                        	pojo.setTranType(defaultStringCell(cell));
                            break;
                        case "F":
                        	pojo.setTranCrncy(defaultStringCell(cell));
                            break;
                        case "G":
                        	pojo.setTranNarration(defaultStringCell(cell));
                            break;
                        case "H":
                        	pojo.setPaymentReference(defaultStringCell(cell));
                            break;
                        case "I":
                        	pojo.setOfficeAccountNo(defaultStringCell(cell));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                models.add(pojo);
                rowNumber++;
            }
            return new BulkTransactionExcelDTO(models);
        }catch (Exception ex){
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    public static BulkNonWayaTransferExcelDTO excelToNoneWayaTransferPojo(InputStream is, String fileName){

        try(Workbook workbook = getWorkBook(is, fileName)) {
            if(workbook == null){
                throw new CustomException("Invalid Excel File Check Extension", HttpStatus.BAD_REQUEST);
            }
            Set<NonWayaPaymentMultipleOfficialDTO> models = new HashSet<>();

            Sheet sheet = workbook.getSheet(SHEET_NON_WAYA);
            if(sheet == null) throw new CustomException("Invalid Excel File Format Passed, Check Sheet Name", HttpStatus.BAD_REQUEST);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()){
                Row currentRow = rows.next();
                Iterator<Cell> cellsInRow = currentRow.iterator();
                NonWayaPaymentMultipleOfficialDTO pojo = new NonWayaPaymentMultipleOfficialDTO();

                // If First Cell is empty break from loop
                if (currentRow == null || isCellEmpty(currentRow.getCell(0))) {
                    break;
                }

                // Skip header After Check of Header Formats
                if (rowNumber == 0) {
                    List<String> excelColNames = new ArrayList<>();
                    int i = 0;
                    while (cellsInRow.hasNext()) {
                        Cell cell = cellsInRow.next();
                        String cellValue = dataFormatter.formatCellValue(cell).trim().toUpperCase();
                        excelColNames.add(cellValue);
                        i++;
                        if (i == PRIVATE_TRANSFER_HEADERS.size()) {
                            break;
                        }
                    }
                    boolean value = checkExcelFileValidity(PRIVATE_TRANSFER_HEADERS, excelColNames);
                    if (!value) {
                        String errorMessage = "Failure, Incorrect File Format";
                        throw new CustomException(errorMessage, HttpStatus.BAD_REQUEST);
                    }
                    rowNumber++;
                    continue;
                }

                int cellIdx = 0;
                while (cellsInRow.hasNext()){
                    Cell cell = cellsInRow.next();
                    String colName = CellReference.convertNumToColString(cell.getColumnIndex()).toUpperCase();
                    switch (colName) {
                        case "A":
                            pojo.setOfficialAccountNumber(defaultStringCell(cell));
                            break;
                        case "B":
                            pojo.setEmailOrPhoneNo(defaultStringCell(cell));
                            break;
                        case "C":
                            pojo.setFullName(defaultStringCell(cell));
                            break;
                        case "D":
                            pojo.setAmount(validateStringBigDecimalOnly(cell, cellIdx, rowNumber));
                            break;
                        case "E":
                            pojo.setTranCrncy(defaultStringCell(cell));
                            break;
                        case "F":
                            pojo.setTranNarration(defaultStringCell(cell));
                            break;
                        case "G":
                            pojo.setPaymentReference(defaultStringCell(cell));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                models.add(pojo);
                rowNumber++;
            }
            return new BulkNonWayaTransferExcelDTO(models);
        }catch (Exception ex){
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    public static NonWayaTransferExcelDTO excelToNoneWayaTransferAdmin(InputStream is, String fileName){

        try(Workbook workbook = getWorkBook(is, fileName)) {
            if(workbook == null){
                throw new CustomException("Invalid Excel File Check Extension", HttpStatus.BAD_REQUEST);
            }
            Set<NoneWayaPaymentRequest> models = new HashSet<>();

            Sheet sheet = workbook.getSheet(SHEET_NON_WAYA);
            if(sheet == null) throw new CustomException("Invalid Excel File Format Passed, Check Sheet Name", HttpStatus.BAD_REQUEST);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()){
                Row currentRow = rows.next();
                Iterator<Cell> cellsInRow = currentRow.iterator();
                NoneWayaPaymentRequest pojo = new NoneWayaPaymentRequest();

                // If First Cell is empty break from loop
                if (currentRow == null || isCellEmpty(currentRow.getCell(0))) {
                    break;
                }

                // Skip header After Check of Header Formats
                if (rowNumber == 0) {
                    List<String> excelColNames = new ArrayList<>();
                    int i = 0;
                    while (cellsInRow.hasNext()) {
                        Cell cell = cellsInRow.next();
                        String cellValue = dataFormatter.formatCellValue(cell).trim().toUpperCase();
                        excelColNames.add(cellValue);
                        i++;
                        if (i == TRANSFER_HEADERS.size()) {
                            break;
                        }
                    }
                    boolean value = checkExcelFileValidity(TRANSFER_HEADERS, excelColNames);
                    if (!value) {
                        String errorMessage = "Failure, Incorrect File Format";
                        throw new CustomException(errorMessage, HttpStatus.BAD_REQUEST);
                    }
                    rowNumber++;
                    continue;
                }

                int cellIdx = 0;
                while (cellsInRow.hasNext()){
                    Cell cell = cellsInRow.next();
                    String colName = CellReference.convertNumToColString(cell.getColumnIndex()).toUpperCase();
                    switch (colName) {
                        case "A":
                            pojo.setCustomerAccountNumber(defaultStringCell(cell));
                            break;
                        case "B":
                            pojo.setEmailOrPhoneNo(defaultStringCell(cell));
                            break;
                        case "C":
                            pojo.setFullName(defaultStringCell(cell));
                            break;
                        case "D":
                            pojo.setAmount(validateStringBigDecimalOnly(cell, cellIdx, rowNumber));
                            break;
                        case "E":
                            pojo.setTranCrncy(defaultStringCell(cell));
                            break;
                        case "F":
                            pojo.setTranNarration(defaultStringCell(cell));
                            break;
                        case "G":
                            pojo.setPaymentReference(defaultStringCell(cell));
                            break;
                        default:
                            break;

                    }
                    cellIdx++;
                }
                models.add(pojo);
                rowNumber++;
            }
            return new NonWayaTransferExcelDTO(models);
        }catch (Exception ex){
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static ByteArrayInputStream createExcelSheet(List<String> HEADERS){
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet(SHEET_NON_WAYA);
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS.get(col));
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
           throw new CustomException("Error in Forming Excel: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    private static Workbook getWorkBook(InputStream is, String fileName) {
        Workbook workbook = null;
        try {
            String extension = fileName.substring(fileName.lastIndexOf("."));
            if(extension.equalsIgnoreCase(".xls")){
                workbook = new HSSFWorkbook(is);
            }
            else if(extension.equalsIgnoreCase(".xlsx")){
                workbook = new XSSFWorkbook(is);
            }
        }
        catch(Exception ex) {
            log.error("An Error has Occurred while Getting WorkBook File: {}", ex.getMessage());
        }
        return workbook;
    }

    private static boolean isCellEmpty(final Cell cell) {
        String cellValue = dataFormatter.formatCellValue(cell).trim();
        return cellValue.isEmpty();
    }

    private static boolean checkExcelFileValidity(List<String> one, List<String> two) {
        if (one == null && two == null)
            return true;

        if ((one == null && two != null) || (one != null && two == null) || (one.size() != two.size())) {
            return false;
        }
        one = new ArrayList<>(one);
        two = new ArrayList<>(two);

        return one.equals(two);
    }

    private static String defaultStringCell(final Cell cell) {
        return dataFormatter.formatCellValue(cell).trim();
    }
    
    private static String validateStringNumericOnly(Cell cell, int cellNumber, int rowNumber) {
    	   String cellValue = null;
    	   try {
    	      double d = cell.getNumericCellValue();
    	      cellValue = String.format("%.0f", d);
    	   }catch(IllegalStateException | NumberFormatException ex) {
    	      cellValue = dataFormatter.formatCellValue(cell).trim();
    	   }
    	    boolean val = numericPattern.matcher(cellValue).find();
    	    if(!val) {
    	        String errorMessage = String.format("Invalid Numeric Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
    	        throw new CustomException(errorMessage, HttpStatus.EXPECTATION_FAILED);
    	    }
    	    return cellValue;
    	}

    @SuppressWarnings("unused")
	private static String validateAndPassStringValue(Cell cell, int cellNumber, int rowNumber){
        String cellValue =  dataFormatter.formatCellValue(cell).trim();
        boolean val = alphabetsPattern.matcher(cellValue).find();
        if(!cellValue.isBlank() && val && cellValue.length() >= 2){
            return cellValue;
        }
        String errorMessage = String.format("Invalid Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
        throw new CustomException(errorMessage, HttpStatus.EXPECTATION_FAILED);
    }

    private static String validateStringIsEmail(Cell cell, int cellNumber, int rowNumber) {
        String cellValue =  dataFormatter.formatCellValue(cell).trim();
        Matcher matcher = emailPattern.matcher(cellValue);
        if(!matcher.matches()){
            String errorMessage = String.format("Invalid Email Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
            throw new CustomException(errorMessage, HttpStatus.EXPECTATION_FAILED);
        }
        return cellValue;
    }
    
    private static BigDecimal validateStringBigDecimalOnly(Cell cell, int cellNumber, int rowNumber) {
        String cellValue =  dataFormatter.formatCellValue(cell).trim();
        boolean val = numericPattern.matcher(cellValue).find();
        if(!val) {
            String errorMessage = String.format("Invalid Numeric Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
            throw new CustomException(errorMessage, HttpStatus.EXPECTATION_FAILED);
        }
        return new BigDecimal(cellValue);
    }


}