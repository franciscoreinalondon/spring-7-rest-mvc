package com.franciscoreina.spring7.csv.converter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvNullableStringConverterTest {

    private final CsvNullableStringConverter converter = new CsvNullableStringConverter();

    @Test
    void convert_shouldReturnTrimmedString_whenValueIsValid() {
        // Act
        var result = converter.convert(" Goat ");

        // Assert
        assertThat(result).isEqualTo("Goat");
    }

    @Test
    void convert_shouldReturnNull_whenValueIsNull() {
        // Act
        var result = converter.convert(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void convert_shouldReturnNull_whenValueIsBlank() {
        // Act
        var result = converter.convert(" ");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void convert_shouldReturnNull_whenValueIsNA() {
        // Act
        var result = converter.convert("NA");

        // Assert
        assertThat(result).isNull();
    }
}
