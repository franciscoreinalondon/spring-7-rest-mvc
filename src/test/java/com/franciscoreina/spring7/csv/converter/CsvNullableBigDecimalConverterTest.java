package com.franciscoreina.spring7.csv.converter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CsvNullableBigDecimalConverterTest {

    private final CsvNullableBigDecimalConverter converter = new CsvNullableBigDecimalConverter();

    @Test
    void convert_shouldReturnBigDecimal_whenValueIsValid() {
        // Act
        var result = converter.convert("0.0151");

        // Assert
        assertThat(result).isEqualTo("0.0151");
    }

    @Test
    void convert_shouldTrimValue_whenValueHasSpaces() {
        // Act
        var result = converter.convert(" 32.50 ");

        // Assert
        assertThat(result).isEqualTo("32.50");
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

    @Test
    void convert_shouldThrowException_whenValueIsInvalid() {
        // Act + Assert
        assertThatThrownBy(() -> converter.convert("abc"))
                .isInstanceOf(NumberFormatException.class);
    }
}
