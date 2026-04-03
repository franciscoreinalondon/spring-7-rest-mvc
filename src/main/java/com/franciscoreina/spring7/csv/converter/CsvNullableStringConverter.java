package com.franciscoreina.spring7.csv.converter;

import com.opencsv.bean.AbstractBeanField;

public class CsvNullableStringConverter extends AbstractBeanField<String, String> {

    @Override
    protected String convert(String value) {
        if (value == null || value.isBlank() || "NA".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }
}
