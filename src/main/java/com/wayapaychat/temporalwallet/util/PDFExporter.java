package com.wayapaychat.temporalwallet.util;

import com.lowagie.text.BadElementException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.wayapaychat.temporalwallet.pojo.TransWallet;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.awt.Color;
public class PDFExporter {


    private List<TransWallet> trans;
    private String accountNo;
    private Date startDate;
    private Date endDate;

    public PDFExporter(List<TransWallet> trans, String accountNo, Date startDate, Date endDate) {
        this.trans = trans;
        this.accountNo = accountNo;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private void writeTableTitle(PdfPTable titleTable) throws IOException, BadElementException {

        titleTable.setWidthPercentage(100f);

        PdfPTable titleTable2 = new PdfPTable(2);
        //logo
        PdfPCell cell2 = new PdfPCell(Image.getInstance("src/main/resources/images/waya-bank.png"));
        cell2.setColspan(2);
        titleTable2.addCell(cell2);

        //title
        cell2 = new PdfPCell(new Phrase("\nTITLE TEXT", new Font(Font.HELVETICA, 16, Font.BOLD | Font.UNDERLINE)));
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setColspan(2);
        titleTable2.addCell(cell2);

        PdfPCell cell = new PdfPCell(titleTable2);
        titleTable.setHeaderRows(1);
        titleTable.addCell(cell);
        titleTable.addCell(new PdfPCell(new Phrase("")));



    }
    private void writeTableTitle2(PdfPTable titleTable) throws IOException, BadElementException {

        //logo
        PdfPCell cell2 = new PdfPCell(Image.getInstance("src/main/resources/others/waya_logo.svg"));
//        cell2.setColspan(2);
        titleTable.addCell(cell2);

        //title
        cell2 = new PdfPCell(new Phrase("\nTITLE TEXT", new Font(Font.HELVETICA, 16, Font.BOLD | Font.UNDERLINE)));
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        // cell2.setColspan(2);
        titleTable.addCell(cell2);


    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setBackgroundColor(Color.red);
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);
        font.setSize(9);
        font.isBold();
        cell.setPhrase(new Phrase("AcctNo", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("PaymentRef", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("TranAmount ", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("TranDate", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("TranNarrate", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("TranType", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("PartTranType", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("TranId", font));
        table.addCell(cell);


    }


    private void writeTableData(PdfPTable table) {
        String description = "Transaction Reference : " + "receipt.getReceipt().getReferenceNumber()";
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.BLACK);
        font.setSize(9);

        for (TransWallet data: trans){
            cell.setPhrase(new Phrase(data.getAcctNo(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getPaymentRef(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getTranAmount().toString(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getTranDate().toString(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getTranNarrate(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getTranType(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getPartTranType(), font));
            table.addCell(cell);

            cell.setPhrase(new Phrase(data.getTranId(), font));
            table.addCell(cell);


        }
    }

    private void writeTableData2(PdfPTable table2) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String staDate= formatter.format(startDate);
        String strDate= formatter.format(endDate);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setPhrase(new Phrase("DATE :" + staDate  + " TO " + strDate));
        table2.addCell(cell );

        cell.setPhrase(new Phrase("ACCOUNT NO:" +  accountNo ));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table2.addCell(cell);


    }

    public void export(HttpServletResponse response) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(18);
        font.setColor(Color.red);

        Paragraph p = new Paragraph("Account Statement", font);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);

//        PdfPTable titleTable = new PdfPTable(1);
//        writeTableTitle(titleTable);
//        document.add(titleTable);



//        Font title = FontFactory.getFont(FontFactory.HELVETICA);
//        title.setSize(11);
//
//        document.add(new Paragraph("Address : " + "Home Address",title ) );
//        document.add(new Paragraph("Phone Number : " + "Phone number",title));
//        document.add(new Paragraph("Email Address : " +  "Email Address",title));
        document.add(new Paragraph(""));
        document.add(new Paragraph("............................................................................................................................................................"));

        PdfPTable table2 = new PdfPTable(2);
        table2.setWidthPercentage(100f);
        table2.setWidths(new float[] {3.5f, 3.5f});
        table2.setSpacingBefore(10);

        writeTableData2(table2);
        document.add(table2);


        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {3.5f, 1.5f, 1.5f, 1.5f, 3.5f, 1.5f, 1.5f, 1.5f});
        table.setSpacingBefore(10);


        writeTableHeader(table);
        writeTableData(table);

        document.add(table);

        document.close();


    }
}
