package com.franciscoreina.spring7.csv.converter;

import com.opencsv.bean.AbstractBeanField;

public class CsvNullableIntegerConverter extends AbstractBeanField<Integer, String> {

    @Override
    protected Integer convert(String value) {
        if (value == null || value.isBlank() || "NA".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return Integer.valueOf(value.trim());
    }
}
