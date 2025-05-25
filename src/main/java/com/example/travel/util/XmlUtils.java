package com.example.travel.util;

import com.example.travel.model.TravelRecord;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public static void exportToXml(List<TravelRecord> records, File file) throws JAXBException {
        XmlUtils wrapper = new XmlUtils();
        wrapper.setRecords(records);

        JAXBContext context = JAXBContext.newInstance(XmlUtils.class, TravelRecord.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(wrapper, file);
    }

    public static List<TravelRecord> importFromXml(File file) throws JAXBException, SQLException {
        JAXBContext context = JAXBContext.newInstance(XmlUtils.class, TravelRecord.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        XmlUtils wrapper = (XmlUtils) unmarshaller.unmarshal(file);
        
        List<TravelRecord> importedRecords = wrapper.getRecords();
        // Save each imported record to the database
        for (TravelRecord record : importedRecords) {
            record.setId(null); // Clear ID to ensure it's saved as a new record
            record.save();
        }
        
        return importedRecords;
    }
}
