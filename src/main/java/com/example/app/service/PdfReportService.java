// src/main/java/com/example/app/service/PdfReportService.java
package com.example.app.service;

import com.example.app.dto.ChildReportDto;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReportService {

    public byte[] createPdf(ChildReportDto report) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font titleFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font subtitleFont= FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
            Font headerFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font cellFont    = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("Raport Elev", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Subtitle: Child’s full name
            Paragraph childLine = new Paragraph("Elev: " + report.getChildName(), subtitleFont);
            childLine.setAlignment(Element.ALIGN_CENTER);
            childLine.setSpacingBefore(4);
            document.add(childLine);

            // Date line
            String formattedDate = report.getDate().format(DateTimeFormatter.ISO_DATE);
            Paragraph datePara = new Paragraph("Data: " + formattedDate, subtitleFont);
            datePara.setAlignment(Element.ALIGN_CENTER);
            datePara.setSpacingAfter(12);
            document.add(datePara);

            // Separator
            LineSeparator separator = new LineSeparator();
            separator.setOffset(-2);
            separator.setLineColor(Color.LIGHT_GRAY);
            document.add(new Chunk(separator));
            document.add(Chunk.NEWLINE);

            if (report.getAnswers().isEmpty()) {
                Paragraph emptyPara = new Paragraph(
                        "Nu există răspunsuri înregistrate pentru această dată.",
                        FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, Color.GRAY)
                );
                emptyPara.setAlignment(Element.ALIGN_CENTER);
                emptyPara.setSpacingBefore(20);
                document.add(emptyPara);
            } else {
                PdfPTable table = new PdfPTable(new float[] {2f, 6f, 3f});
                table.setWidthPercentage(100);
                table.setSpacingBefore(8f);

                Color headerBg = new Color(60, 60, 60); // dark gray
                PdfPCell cell;

                cell = new PdfPCell(new Phrase("Categorie", headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase("Întrebare", headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase("Răspuns", headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);

                Color rowBg1 = Color.WHITE;
                Color rowBg2 = new Color(240, 240, 240);
                boolean oddRow = true;

                for (var ans : report.getAnswers()) {
                    Color bg = oddRow ? rowBg1 : rowBg2;

                    cell = new PdfPCell(new Phrase(ans.getCategory(), cellFont));
                    cell.setBackgroundColor(bg);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(6);
                    table.addCell(cell);

                    cell = new PdfPCell(new Phrase(ans.getQuestionPrompt(), cellFont));
                    cell.setBackgroundColor(bg);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(6);
                    table.addCell(cell);

                    cell = new PdfPCell(new Phrase(ans.getAnswer(), cellFont));
                    cell.setBackgroundColor(bg);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(6);
                    table.addCell(cell);

                    oddRow = !oddRow;
                }

                document.add(table);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}