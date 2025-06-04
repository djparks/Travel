package com.example.travel.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.*;

/**
 * Model class for Tags table.
 */
@XmlRootElement(name = "tag")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tag {
    @XmlTransient // Don't serialize database connection info
    private static final String DB_URL = "jdbc:h2:file:./traveldb";
    @XmlTransient
    private static final String DB_USER = "sa";
    @XmlTransient
    private static final String DB_PASSWORD = "";

    @XmlElement
    private Long id;
    
    @XmlElement
    private String tag;

    public Tag() {
    }

    public Tag(String tag) {
        this.tag = tag;
    }

    // CRUD Operations
    public void save() throws SQLException {
        // Check if tag already exists
        if (this.id == null) { // Only check for new records
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String checkSql = "SELECT COUNT(*) FROM tags WHERE tag = ?";
                try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                    stmt.setString(1, this.tag);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("A tag with this name already exists.");
                        }
                    }
                }
            }
        }
        
        if (this.id == null) {
            // Create new tag
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO tags (tag) VALUES (?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, tag);
                    stmt.executeUpdate();

                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            this.id = generatedKeys.getLong(1);
                        }
                    }
                }
            }
        } else {
            // Update existing tag
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "UPDATE tags SET tag = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, tag);
                    stmt.setLong(2, id);
                    stmt.executeUpdate();
                }
            }
        }
    }

    public static Tag findById(Long id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM tags WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToTag(rs);
                    }
                }
            }
        }
        return null;
    }

    public static List<Tag> findAll() throws SQLException {
        List<Tag> tags = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM tags ORDER BY tag";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    tags.add(mapResultSetToTag(rs));
                }
            }
        }
        return tags;
    }

    public void delete() throws SQLException {
        if (this.id != null) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "DELETE FROM tags WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    stmt.executeUpdate();
                }
            }
        }
    }

    private static Tag mapResultSetToTag(ResultSet rs) throws SQLException {
        Tag tag = new Tag();
        tag.setId(rs.getLong("id"));
        tag.setTag(rs.getString("tag"));
        return tag;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    
    @Override
    public String toString() {
        return tag;
    }
}