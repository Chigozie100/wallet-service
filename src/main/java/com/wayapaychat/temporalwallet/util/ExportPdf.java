/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wayapaychat.temporalwallet.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Olawale
 */
public class ExportPdf {

    public void export(HttpServletResponse response) throws DocumentException, IOException {
        PdfPTable tables = new PdfPTable(1);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        tables.setWidthPercentage(100f);
        tables.setWidths(new float[]{3.5f});
        tables.setSpacingBefore(10.5f);

        writeHeader(tables);
        document.add(tables);
        tables.setSpacingAfter(15.5f);

        Font font = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC);
        font.setSize(11);
        font.setColor(Color.red);
        font.isBold();
        
        document.add(new Paragraph("TEST ACCOUNT"));
        document.add(new Paragraph("Account Number: 90883993"));
        document.add(new Paragraph("Account Type:"));
        document.add(new Paragraph("Opening Bal:"));
        document.add(new Paragraph("Closing Bal:"));
        document.add(new Paragraph("Cleared Bal:"));
        
        
//        PdfPTable tablex = new PdfPTable(2);
//        tablex.setWidthPercentage(50);
//        addTableCell(tablex, "AccountType", Element.ALIGN_RIGHT, Font.NORMAL);
//
//        addTableCell(tablex, "Savings", Element.ALIGN_RIGHT, Font.NORMAL);
//        document.add(tablex);
    
        
        
        PdfPTable table2 = new PdfPTable(9);
        table2.setWidthPercentage(110f);
        table2.setWidths(new float[]{3.5f, 4.0f, 5.5f, 5.5f, 4.5f, 4.5f, 3.5f, 3.5f, 3.5f});
        table2.setSpacingBefore(15f);

        writeTableHeader(table2);
        document.add(table2);

        document.close();

    }
    
   

    private void writeHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setBackgroundColor(Color.RED);
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);
        font.setSize(10);
        font.isBold();

        cell.setPhrase(new Phrase("ACCOUNT STATEMENT:" + "test"));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);

        table.addCell(cell);

    }

    private void writeAccountTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setBackgroundColor(Color.red);
        cell.setPadding(10);
        

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);
        font.setSize(10);
        font.isBold();

      cell.setPhrase(new Phrase("ACCOUNT NUMBER:"));
      cell.setHorizontalAlignment(Element.ALIGN_LEFT);
      
//        

        table.addCell(cell);

    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setBackgroundColor(Color.red);
        cell.setPadding(15);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);
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

        cell.setPhrase(new Phrase("Balance", font));
        table.addCell(cell);

    }

    private void addTableCell(PdfPTable table, String text, int alignment, int fontStyle) {

        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 8, fontStyle, Color.BLACK)));

        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

    }
}
