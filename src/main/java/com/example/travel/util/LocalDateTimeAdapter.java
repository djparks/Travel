package com.example.travel.util;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    @Override
    public LocalDateTime unmarshal(String v) {
        return v != null ? LocalDateTime.parse(v) : null;
    }

    @Override
    public String marshal(LocalDateTime v) {
        return v != null ? v.toString() : null;
    }
}
