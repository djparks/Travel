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
            // Handle picture 1
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

            // Handle picture 2
            byte[] picture2 = record.getPicture2();
            if (picture2 != null && picture2.length > 0) {
                String pictureFileName2 = UUID.randomUUID().toString() + ".jpg";
                File pictureFile2 = picturesDir.resolve(pictureFileName2).toFile();
                try (FileOutputStream fos = new FileOutputStream(pictureFile2)) {
                    fos.write(picture2);
                }
                record.setPictureFileName2(pictureFileName2);
                record.setPicture2(null); // Don't store binary data in XML
            }

            // Handle picture 3
            byte[] picture3 = record.getPicture3();
            if (picture3 != null && picture3.length > 0) {
                String pictureFileName3 = UUID.randomUUID().toString() + ".jpg";
                File pictureFile3 = picturesDir.resolve(pictureFileName3).toFile();
                try (FileOutputStream fos = new FileOutputStream(pictureFile3)) {
                    fos.write(picture3);
                }
                record.setPictureFileName3(pictureFileName3);
                record.setPicture3(null); // Don't store binary data in XML
            }
        }

        marshaller.marshal(wrapper, file);

        // Restore picture data after XML export
        for (TravelRecord record : records) {
            // Restore picture 1
            if (record.getPictureFileName() != null) {
                File pictureFile = picturesDir.resolve(record.getPictureFileName()).toFile();
                record.setPicture(Files.readAllBytes(pictureFile.toPath()));
                record.setPictureFileName(null);
            }

            // Restore picture 2
            if (record.getPictureFileName2() != null) {
                File pictureFile2 = picturesDir.resolve(record.getPictureFileName2()).toFile();
                record.setPicture2(Files.readAllBytes(pictureFile2.toPath()));
                record.setPictureFileName2(null);
            }

            // Restore picture 3
            if (record.getPictureFileName3() != null) {
                File pictureFile3 = picturesDir.resolve(record.getPictureFileName3()).toFile();
                record.setPicture3(Files.readAllBytes(pictureFile3.toPath()));
                record.setPictureFileName3(null);
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
                // Import picture 1
                String pictureFileName = record.getPictureFileName();
                if (pictureFileName != null) {
                    File pictureFile = picturesDir.resolve(pictureFileName).toFile();
                    if (pictureFile.exists()) {
                        record.setPicture(Files.readAllBytes(pictureFile.toPath()));
                    }
                }
                record.setPictureFileName(null);

                // Import picture 2
                String pictureFileName2 = record.getPictureFileName2();
                if (pictureFileName2 != null) {
                    File pictureFile2 = picturesDir.resolve(pictureFileName2).toFile();
                    if (pictureFile2.exists()) {
                        record.setPicture2(Files.readAllBytes(pictureFile2.toPath()));
                    }
                }
                record.setPictureFileName2(null);

                // Import picture 3
                String pictureFileName3 = record.getPictureFileName3();
                if (pictureFileName3 != null) {
                    File pictureFile3 = picturesDir.resolve(pictureFileName3).toFile();
                    if (pictureFile3.exists()) {
                        record.setPicture3(Files.readAllBytes(pictureFile3.toPath()));
                    }
                }
                record.setPictureFileName3(null);

                record.setId(null); // Clear ID to ensure it's saved as a new record
                record.save();
            }
        }

        return importedRecords;
    }
}
