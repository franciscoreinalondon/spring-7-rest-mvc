package com.franciscoreina.spring7.csv.converter;

import com.opencsv.bean.AbstractBeanField;

import java.math.BigDecimal;

public class CsvNullableBigDecimalConverter extends AbstractBeanField<BigDecimal, String> {

    @Override
    protected BigDecimal convert(String value) {
        if (value == null || value.isBlank() || "NA".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return new BigDecimal(value.trim());
    }
}
