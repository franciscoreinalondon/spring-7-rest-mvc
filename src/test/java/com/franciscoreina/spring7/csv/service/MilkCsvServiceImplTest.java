package com.franciscoreina.spring7.csv.service;

import com.franciscoreina.spring7.csv.dto.MilkCsvRecord;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MilkCsvServiceImplTest {

    private final MilkCsvService milkCsvService = new MilkCsvServiceImpl();

    @Test
    void convertCSV_shouldReturnRecords_whenCsvIsValid() throws Exception {
        // Arrange
        var resource = new ClassPathResource("csvdata/milk_dataset.csv");

        // Act
        List<MilkCsvRecord> records;
        try (var inputStream = resource.getInputStream()) {
            records = milkCsvService.convertCSV(inputStream);
        }

        // Assert
        assertThat(records).isNotNull();
        assertThat(records).hasSize(500);

        var first = records.getFirst();
        assertThat(first.getRow()).isEqualTo(1);
        assertThat(first.getProductCount()).isEqualTo(1);
        assertThat(first.getMilkName()).isEqualTo("Select Semi Skimmed 1.5% [0Y6D-1]");
        assertThat(first.getStyle()).isEqualTo("Semi Skimmed");
        assertThat(first.getExternalId()).isEqualTo(1001);
        assertThat(first.getDairyId()).isEqualTo(121);
        assertThat(first.getOunces()).isEqualByComparingTo("16");
        assertThat(first.getDairyName()).isEqualTo("Midtown Creamery");
        assertThat(first.getCity()).isEqualTo("Chicago");
        assertThat(first.getState()).isEqualTo("IL");
        assertThat(first.getLabel()).isEqualTo("Select Semi Skimmed 1.5% [0Y6D-1] (Midtown Creamery)");
    }

    @Test
    void convertCSV_shouldMapNullableFieldsAsNull_whenCsvContainsNA() throws Exception {
        // Arrange
        var resource = new ClassPathResource("csvdata/milk_dataset.csv");

        // Act
        List<MilkCsvRecord> records;
        try (var inputStream = resource.getInputStream()) {
            records = milkCsvService.convertCSV(inputStream);
        }

        // Assert
        var first = records.getFirst();
        assertThat(first.getLactoseContent()).isNull();
        assertThat(first.getSecondaryStyle()).isNull();
    }

    @Test
    void convertCSV_shouldParseNumericFields_whenCsvIsValid() throws Exception {
        // Arrange
        var resource = new ClassPathResource("csvdata/milk_dataset.csv");

        // Act
        List<MilkCsvRecord> records;
        try (var inputStream = resource.getInputStream()) {
            records = milkCsvService.convertCSV(inputStream);
        }

        // Assert
        var second = records.get(1);
        assertThat(second.getMilkFatPercentage()).isEqualByComparingTo("0.0266");
        assertThat(second.getLactoseContent()).isEqualByComparingTo("0.01");
        assertThat(second.getOunces()).isEqualByComparingTo("32");
        assertThat(second.getDairyCount()).isEqualTo(107);
    }
}
