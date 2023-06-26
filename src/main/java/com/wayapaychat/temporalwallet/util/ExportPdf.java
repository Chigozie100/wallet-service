/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wayapaychat.temporalwallet.util;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.wayapaychat.temporalwallet.dto.AccountStatement;
import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Olawale
 */
public class ExportPdf {

    public static final String BASE_PATH = "/images";
    private List<AccountStatement> trans;
    private String accountNo;
    private String accountName;
    private Date startDate;
    private Date endDate;
    private String openBal;
    private String closeBal;

    public ExportPdf(List<AccountStatement> trans, String accountNo, Date startDate, Date endDate, String accountName, String openingBal,
            String closeBal) {
        this.trans = trans;
        this.accountNo = accountNo;
        this.startDate = startDate;
        this.endDate = endDate;
        this.accountName = accountName;
        this.openBal = openingBal;
        this.closeBal = closeBal;
    }

    public void export(HttpServletResponse response) throws DocumentException, IOException {
        PdfPTable tables = new PdfPTable(1);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        tables.setWidthPercentage(100f);
        tables.setWidths(new float[]{3.5f});
        tables.setSpacingBefore(10.5f);
        tables.setSpacingAfter(15.5f);
        writeHeader(tables);
        document.add(tables);

        Font font = FontFactory.getFont(FontFactory.TIMES_ITALIC);
        font.setSize(20);
        font.setColor(BaseColor.BLACK);
        font.isBold();

        Paragraph p = new Paragraph(accountName.toUpperCase(), font);
        p.setAlignment(Paragraph.ALIGN_LEFT);
        p.setSpacingAfter(9f);
        p.setSpacingBefore(15.5f);
        document.add(p);

        // add account detils
        PdfPTable accountDetail = new PdfPTable(2);
        PdfPTable accountNum = new PdfPTable(1);
        accountNum.setWidthPercentage(40);
        accountNum.setSpacingBefore(10f);
        accountNum.setWidths(new float[]{10.5f});
        accountNum.setHorizontalAlignment(Element.ALIGN_LEFT);
        writeAccountTable(accountNum);

        accountDetail.setWidthPercentage(50);
        accountDetail.setSpacingAfter(90f);
        accountDetail.setWidths(new float[]{10.5f, 10.5f});
        accountDetail.setHorizontalAlignment(Element.ALIGN_RIGHT);
        addAccountDetailTableCell(accountDetail, "AccountType:", Element.ALIGN_LEFT, Font.NORMAL);
        addAccountDetailTableCell(accountDetail, "Savings", Element.ALIGN_RIGHT, Font.NORMAL);
        addAccountDetailTableCell(accountDetail, "Currency", Element.ALIGN_LEFT, Font.NORMAL);
        addAccountDetailTableCell(accountDetail, "NGN", Element.ALIGN_RIGHT, Font.NORMAL);
        addAccountDetailTableCell(accountDetail, "Opening Balance", Element.ALIGN_LEFT, Font.NORMAL);
        addAccountDetailTableCell(accountDetail, openBal, Element.ALIGN_RIGHT, Font.NORMAL);
        addAccountDetailTableCell(accountDetail, "Closing Balance", Element.ALIGN_LEFT, Font.NORMAL);
        addAccountDetailTableCell(accountDetail, closeBal, Element.ALIGN_RIGHT, Font.NORMAL);

        document.add(accountNum);
        document.add(accountDetail);

        // add fradulent image
        Image img = Image.getInstance("https://s3.eu-west-3.amazonaws.com/waya-2.0-file-resources/others/1/app1_Fraudimage.png");
        float documentWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
        float documentHeight = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
        img.scaleToFit(documentWidth, documentHeight);
        img.setSpacingBefore(90f);
        img.setSpacingAfter(100f);
        document.add(img);

        //add contact table
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100f);
        footerTable.setWidths(new float[]{3.5f});
        footerTable.setSpacingBefore(100f);
        onEndPage(footerTable);
        document.add(footerTable);

        // add transaction history table
        PdfPTable table2 = new PdfPTable(8);
        table2.setWidthPercentage(113f);
        table2.setWidths(new float[]{3.5f, 4.0f, 5.5f, 5.5f, 4.5f, 4.5f, 3.5f, 3.5f});
        table2.setSpacingBefore(10f);
         
        writeTableHeader(table2);
        
        
        document.newPage();
        document.add(table2);

        document.close();

    }

    private void writeHeader(PdfPTable table) throws IOException, BadElementException {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String staDate = formatter.format(startDate);
        String strDate = formatter.format(endDate);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setBackgroundColor(new BaseColor(255, 68, 0));
        cell.setPadding(15);

        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN);
        font.setColor(BaseColor.WHITE);
        font.setSize(9);
        font.isBold();

        cell.setPhrase(new Phrase("ACCOUNT STATEMENT: " + staDate + " TO " + strDate, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);

    }

    private void writeAccountTable(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setBackgroundColor(new BaseColor(255, 68, 0));
        cell.setPadding(9);

        Font font = FontFactory.getFont(FontFactory.COURIER);
        font.setColor(BaseColor.WHITE);
        font.setSize(10);
        font.isBold();
        cell.setPhrase(new Phrase("ACCOUNT NUMBER", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell.setPhrase(new Phrase(accountNo, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setBackgroundColor(new BaseColor(255, 68, 0));
        cell.setPadding(10);

        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN);
        font.setColor(BaseColor.WHITE);
        font.setSize(7);
        font.isBold();
        cell.setPhrase(new Phrase("Narration", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Reference", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Sender's Name ", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Receiver's Name", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Trans Date", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Value Date", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Debit", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Credit", font));
        table.addCell(cell);
        
        for (AccountStatement data : trans) {
            cell.setPhrase(new Phrase(data.getDescription(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getRef(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getSender(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getReceiver(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getDate(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getValueDate(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getDeposits(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getWithdrawals(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getBalance().toString(), font));
            table.addCell(cell);

        }

    }

    private void addAccountDetailTableCell(PdfPTable table, String text, int alignment, int fontStyle) {

        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 10, fontStyle, BaseColor.BLACK)));

        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);

    }

    public void onEndPage(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setBackgroundColor(new BaseColor(255, 68, 0));
        cell.setPadding(15);

        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN);
        font.setColor(BaseColor.WHITE);
        font.setSize(8);

        cell.setPhrase(new Phrase("CALL WAYABANK(23413300359)-www.wayabank.ng", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

//    private void writeTableData(PdfPTable table) throws DocumentException {       
//        PdfPCell cell = new PdfPCell();
//        cell.setBorder(0);
//        Font font = FontFactory.getFont(FontFactory.HELVETICA);
//        font.setColor(BaseColor.BLACK);
//        font.setSize(9);
//
//        
//    }

}
