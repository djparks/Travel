package com.example.travel.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.*;
import com.example.travel.util.LocalDateTimeAdapter;

@XmlRootElement(name = "travelRecord")
@XmlAccessorType(XmlAccessType.FIELD)
public class TravelRecord {
    @XmlTransient // Don't serialize database connection info
    private static final String DB_URL = "jdbc:h2:file:./traveldb";
    @XmlTransient
    private static final String DB_USER = "sa";
    @XmlTransient
    private static final String DB_PASSWORD = "";


    @XmlElement
    private Long id;
    @XmlElement
    private String description;
    @XmlElement
    private String url;
    @XmlElement
    private String state;
    @XmlElement
    private String city;
    @XmlElement
    private String address;
    @XmlElement
    private String zip;
    @XmlElement
    private String geo;
    @XmlElement
    private String pictures;
    @XmlElement
    private String notes;
    @XmlElement
    @XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
    private LocalDateTime dateCreated;
    @XmlElement
    @XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
    private LocalDateTime dateUpdated;

    public TravelRecord() {
        this.dateCreated = LocalDateTime.now();
        this.dateUpdated = this.dateCreated;
    }

    // CRUD Operations
    public void save() throws SQLException {
        // Check if description already exists
        if (this.id == null) { // Only check for new records
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String checkSql = "SELECT COUNT(*) FROM travel_records WHERE description = ?";
                try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                    stmt.setString(1, this.description);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("A record with this description already exists.");
                        }
                    }
                }
            }
        }
        if (this.id == null) {
            // Create new record
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO travel_records (description, url, state, city, address, zip, geo, pictures, notes, date_created, date_updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, description);
                    stmt.setString(2, url);
                    stmt.setString(3, state);
                    stmt.setString(4, city);
                    stmt.setString(5, address);
                    stmt.setString(6, zip);
                    stmt.setString(7, geo);
                    stmt.setString(8, pictures);
                    stmt.setString(9, notes);
                    stmt.setTimestamp(10, Timestamp.valueOf(dateCreated));
                    stmt.setTimestamp(11, Timestamp.valueOf(dateUpdated));
                    stmt.executeUpdate();

                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            this.id = generatedKeys.getLong(1);
                        }
                    }
                }
            }
        } else {
            // Update existing record
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "UPDATE travel_records SET description=?, url=?, state=?, city=?, address=?, zip=?, geo=?, pictures=?, notes=?, date_updated=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, description);
                    stmt.setString(2, url);
                    stmt.setString(3, state);
                    stmt.setString(4, city);
                    stmt.setString(5, address);
                    stmt.setString(6, zip);
                    stmt.setString(7, geo);
                    stmt.setString(8, pictures);
                    stmt.setString(9, notes);
                    setDateUpdated(LocalDateTime.now());
                    stmt.setTimestamp(10, Timestamp.valueOf(dateUpdated));
                    stmt.setLong(11, id);
                    stmt.executeUpdate();
                }
            }
        }
    }

    public static TravelRecord findById(Long id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM travel_records WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToRecord(rs);
                    }
                }
            }
        }
        return null;
    }

    public static List<TravelRecord> findAll() throws SQLException {
        List<TravelRecord> records = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM travel_records ORDER BY date_created DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    records.add(mapResultSetToRecord(rs));
                }
            }
        }
        return records;
    }

    public void delete() throws SQLException {
        if (this.id != null) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "DELETE FROM travel_records WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    stmt.executeUpdate();
                }
            }
        }
    }

    private static TravelRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        TravelRecord record = new TravelRecord();
        record.setId(rs.getLong("id"));
        record.setDescription(rs.getString("description"));
        record.setUrl(rs.getString("url"));
        record.setState(rs.getString("state"));
        record.setGeo(rs.getString("geo"));
        record.setCity(rs.getString("city"));
        record.setAddress(rs.getString("address"));
        record.setZip(rs.getString("zip"));
        record.setPictures(rs.getString("pictures"));
        record.setNotes(rs.getString("notes"));
        record.dateCreated = rs.getTimestamp("date_created").toLocalDateTime();
        record.dateUpdated = rs.getTimestamp("date_updated").toLocalDateTime();
        return record;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPictures() {
        return pictures;
    }

    public void setPictures(String pictures) {
        this.pictures = pictures;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
}
