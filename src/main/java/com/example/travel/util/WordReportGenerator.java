package com.example.travel.util;

import com.example.travel.model.TravelRecord;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for generating Word reports of planned travel records.
 */
public class WordReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Generates a Word document report containing all planned travel records.
     * 
     * @param filePath The path where the Word document will be saved
     * @throws IOException If there's an error writing the file
     */
    public static void generatePlannedVisitsReport(String filePath) throws IOException {
        try {
            // Create a new document
            XWPFDocument document = new XWPFDocument();

            // Add title
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("Planned Visits Report");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.addBreak();

            try {
                // Get all records where plan=true
                List<TravelRecord> plannedRecords = TravelRecord.findAll().stream()
                        .filter(record -> Boolean.TRUE.equals(record.getPlan()))
                        .toList();

                if (plannedRecords.isEmpty()) {
                    XWPFParagraph noPlansParagraph = document.createParagraph();
                    XWPFRun noPlanRun = noPlansParagraph.createRun();
                    noPlanRun.setText("No planned visits found.");
                    noPlanRun.setFontSize(12);
                } else {
                    // Add each planned record to the document
                    for (TravelRecord record : plannedRecords) {
                        addRecordToDocument(document, record);
                    }
                }

            } catch (Exception e) {
                XWPFParagraph errorParagraph = document.createParagraph();
                XWPFRun errorRun = errorParagraph.createRun();
                errorRun.setText("Error retrieving planned visits: " + e.getMessage());
                errorRun.setColor("FF0000");
            }

            // Write the document to file
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
            }

        } catch (Exception e) {
            throw new IOException("Failed to generate Word report: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a travel record to the document.
     * 
     * @param document The Word document
     * @param record The travel record to add
     */
    private static void addRecordToDocument(XWPFDocument document, TravelRecord record) {
        // Create a paragraph for the record title
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setStyle("Heading1");
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(record.getDescription());
        titleRun.setBold(true);
        titleRun.setFontSize(14);
        titleRun.addBreak();

        // Add location information
        XWPFParagraph locationParagraph = document.createParagraph();
        XWPFRun locationRun = locationParagraph.createRun();

        StringBuilder location = new StringBuilder();
        if (record.getAddress() != null && !record.getAddress().isEmpty()) {
            location.append(record.getAddress()).append(", ");
        }
        if (record.getCity() != null && !record.getCity().isEmpty()) {
            location.append(record.getCity()).append(", ");
        }
        if (record.getState() != null && !record.getState().isEmpty()) {
            location.append(record.getState()).append(" ");
        }
        if (record.getZip() != null && !record.getZip().isEmpty()) {
            location.append(record.getZip());
        }

        locationRun.setText("Location: " + location.toString().trim());
        locationRun.setFontSize(12);
        locationRun.addBreak();

        // Add URL if available
        if (record.getUrl() != null && !record.getUrl().isEmpty()) {
            XWPFRun urlRun = locationParagraph.createRun();
            urlRun.setText("URL: " + record.getUrl());
            urlRun.setFontSize(12);
            urlRun.addBreak();
        }

        // Add phone number if available
        if (record.getPhoneNumber() != null && !record.getPhoneNumber().isEmpty()) {
            XWPFRun phoneRun = locationParagraph.createRun();
            phoneRun.setText("Phone: " + record.getPhoneNumber());
            phoneRun.setFontSize(12);
            phoneRun.addBreak();
        }

        // Add geo information if available
        if (record.getGeo() != null && !record.getGeo().isEmpty()) {
            XWPFRun geoRun = locationParagraph.createRun();
            geoRun.setText("Geo: " + record.getGeo());
            geoRun.setFontSize(12);
            geoRun.addBreak();
        }

        // Add notes if available
        if (record.getNotes() != null && !record.getNotes().isEmpty()) {
            XWPFParagraph notesParagraph = document.createParagraph();
            XWPFRun notesLabelRun = notesParagraph.createRun();
            notesLabelRun.setText("Notes:");
            notesLabelRun.setBold(true);
            notesLabelRun.setFontSize(12);
            notesLabelRun.addBreak();

            XWPFRun notesContentRun = notesParagraph.createRun();
            notesContentRun.setText(record.getNotes());
            notesContentRun.setFontSize(12);
            notesContentRun.addBreak();
        }

        // Add image if available
        if (record.getPicture() != null && record.getPicture().length > 0) {
            try {
                XWPFParagraph imageParagraph = document.createParagraph();
                imageParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun imageRun = imageParagraph.createRun();

                // Add a label for the image
                XWPFParagraph imageLabelParagraph = document.createParagraph();
                imageLabelParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun imageLabelRun = imageLabelParagraph.createRun();
                imageLabelRun.setText("Image:");
                imageLabelRun.setBold(true);
                imageLabelRun.setFontSize(12);

                // Set reasonable width and height for the image
                // Using 300x200 points as a reasonable size that will fit well in the document
                // Add the image to the document
                ByteArrayInputStream imageStream = new ByteArrayInputStream(record.getPicture());
                imageRun.addPicture(imageStream, PictureType.JPEG, 
                    record.getPictureFileName() != null ? record.getPictureFileName() : "image.jpg", 
                    Units.toEMU(300), Units.toEMU(200)); // Width: 300 points, Height: 200 points
                imageRun.addBreak();
            } catch (Exception e) {
                // If there's an error adding the image, log it but continue with the report
                XWPFParagraph errorParagraph = document.createParagraph();
                XWPFRun errorRun = errorParagraph.createRun();
                errorRun.setText("Error adding image: " + e.getMessage());
                errorRun.setColor("FF0000");
                errorRun.addBreak();
            }
        }

        // Add a separator
        XWPFParagraph separatorParagraph = document.createParagraph();
        XWPFRun separatorRun = separatorParagraph.createRun();
        separatorRun.setText("------------------------------------------------------");
        separatorRun.addBreak();
    }
}
