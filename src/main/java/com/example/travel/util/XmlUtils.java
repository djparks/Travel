package com.example.travel.util;

import com.example.travel.model.TravelRecord;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "travelRecords")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlUtils {
    @XmlElement(name = "record")
    private List<TravelRecord> records;

    public XmlUtils() {
        records = new ArrayList<>();
    }

    public void setRecords(List<TravelRecord> records) {
        this.records = records;
    }

    public List<TravelRecord> getRecords() {
        return records;
    }

    public static void exportToXml(List<TravelRecord> records, File file) throws JAXBException, IOException {
        XmlUtils wrapper = new XmlUtils();
        wrapper.setRecords(records);

        JAXBContext context = JAXBContext.newInstance(XmlUtils.class, TravelRecord.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        // Create pictures directory next to the XML file
        Path picturesDir = file.toPath().getParent().resolve(file.getName() + "_pictures");
        Files.createDirectories(picturesDir);

        // Save pictures to separate files and update XML references
        for (TravelRecord record : records) {
            byte[] picture = record.getPicture();
            if (picture != null && picture.length > 0) {
                String pictureFileName = UUID.randomUUID().toString() + ".jpg";
                File pictureFile = picturesDir.resolve(pictureFileName).toFile();
                try (FileOutputStream fos = new FileOutputStream(pictureFile)) {
                    fos.write(picture);
                }
                record.setPictureFileName(pictureFileName);
                record.setPicture(null); // Don't store binary data in XML
            }
        }

        marshaller.marshal(wrapper, file);

        // Restore picture data after XML export
        for (TravelRecord record : records) {
            if (record.getPictureFileName() != null) {
                File pictureFile = picturesDir.resolve(record.getPictureFileName()).toFile();
                record.setPicture(Files.readAllBytes(pictureFile.toPath()));
                record.setPictureFileName(null);
            }
        }
    }

    public static List<TravelRecord> importFromXml(File file) throws JAXBException, SQLException, IOException {
        JAXBContext context = JAXBContext.newInstance(XmlUtils.class, TravelRecord.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        XmlUtils wrapper = (XmlUtils) unmarshaller.unmarshal(file);
        
        List<TravelRecord> importedRecords = wrapper.getRecords();
        // Load pictures from the pictures directory
        Path picturesDir = file.toPath().getParent().resolve(file.getName() + "_pictures");
        if (Files.exists(picturesDir)) {
            for (TravelRecord record : importedRecords) {
                String pictureFileName = record.getPictureFileName();
                if (pictureFileName != null) {
                    File pictureFile = picturesDir.resolve(pictureFileName).toFile();
                    if (pictureFile.exists()) {
                        record.setPicture(Files.readAllBytes(pictureFile.toPath()));
                    }
                }
                record.setPictureFileName(null);
                record.setId(null); // Clear ID to ensure it's saved as a new record
                record.save();
            }
        }
        
        return importedRecords;
    }
}
