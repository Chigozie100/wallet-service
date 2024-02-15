package com.wayapaychat.temporalwallet.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.wayapaychat.temporalwallet.dto.AccountStatement;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
public class ExportPdf2 {

    private List<AccountStatement> trans;
    private String accountNo;
    private String accountName;
    private Date startDate;
    private Date endDate;
    private BigDecimal blockedAmount;

    public ExportPdf2(List<AccountStatement> trans, String accountNo, Date startDate, Date endDate, String accountName, BigDecimal blockedAmount) {
        this.trans = trans;
        this.accountNo = accountNo;
        this.startDate = startDate;
        this.endDate = endDate;
        this.accountName = accountName;
        this.blockedAmount = blockedAmount;
    }

    public void export(HttpServletResponse response) {
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            addContent(document);
            document.close();
        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage());
        }
    }

    private void addContent(Document document) throws DocumentException {
        PdfPTable headerTable = createHeaderTable();
        PdfPTable accountDetailsTable = createAccountDetailsTable();
        PdfPTable transactionTable = createTransactionTable();

        document.add(headerTable);
        document.add(accountDetailsTable);
        document.add(transactionTable);
    }

    private PdfPTable createHeaderTable() throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 3});

        PdfPCell cell = new PdfPCell(new Phrase("ACCOUNT STATEMENT: " + formatDate(startDate) + " TO " + formatDate(endDate)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTable.addCell(cell);

        Image img = null;
        try {
            img = Image.getInstance("https://s3.eu-west-3.amazonaws.com/waya-2.0-file-resources/others/1/app1_wayawhitelogo.png");
        } catch (IOException e) {
            log.error("Error loading image: {}", e.getMessage());
        }
        if (img != null) {
            img.scaleAbsolute(100, 50);
            PdfPCell imgCell = new PdfPCell(img);
            imgCell.setBorder(Rectangle.NO_BORDER);
            imgCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            headerTable.addCell(imgCell);
        }

        return headerTable;
    }

    private PdfPTable createAccountDetailsTable() throws DocumentException {
        PdfPTable accountDetailsTable = new PdfPTable(2);
        accountDetailsTable.setWidthPercentage(100);
        accountDetailsTable.setWidths(new float[]{1, 3});

        accountDetailsTable.addCell(createCell("Account Name:", accountName));
        accountDetailsTable.addCell(createCell("Account Number:", accountNo));
        accountDetailsTable.addCell(createCell("Opening Balance:", String.valueOf(calculateOpeningBalance())));
        accountDetailsTable.addCell(createCell("Closing Balance:", String.valueOf(calculateClosingBalance())));

        return accountDetailsTable;
    }

    private PdfPTable createTransactionTable() throws DocumentException {
        PdfPTable transactionTable = new PdfPTable(5);
        transactionTable.setWidthPercentage(100);
        transactionTable.setWidths(new float[]{2, 4, 3, 2, 2});

        transactionTable.addCell(createHeaderCell("Date"));
        transactionTable.addCell(createHeaderCell("Description"));
        transactionTable.addCell(createHeaderCell("Reference"));
        transactionTable.addCell(createHeaderCell("Debit"));
        transactionTable.addCell(createHeaderCell("Credit"));

        for (AccountStatement statement : trans) {
            transactionTable.addCell(createCell(statement.getDate()));
            transactionTable.addCell(createCell(statement.getDescription()));
            transactionTable.addCell(createCell(statement.getRef()));
            transactionTable.addCell(createCell(statement.getWithdrawals()));
            transactionTable.addCell(createCell(statement.getDeposits()));
        }

        return transactionTable;
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell createCell(String text) {
        return new PdfPCell(new Phrase(text, new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL)));
    }

    private PdfPCell createCell(String label, String value) {
        Font labelFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
        Font valueFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);

        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + ": ", labelFont));
        phrase.add(new Chunk(value, valueFont));

        cell.addElement(phrase);
        return cell;
    }

    private String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    private BigDecimal calculateOpeningBalance() {
        if (!trans.isEmpty()) {
            return trans.get(0).getBalance().add(blockedAmount);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateClosingBalance() {
        if (!trans.isEmpty()) {
            BigDecimal lastBalance = trans.get(trans.size() - 1).getBalance();
            return lastBalance.add(blockedAmount);
        }
        return BigDecimal.ZERO;
    }

}
