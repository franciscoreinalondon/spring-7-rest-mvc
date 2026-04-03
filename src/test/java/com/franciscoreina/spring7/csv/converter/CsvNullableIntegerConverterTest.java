package com.franciscoreina.spring7.csv.converter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvNullableIntegerConverterTest {

    private final CsvNullableIntegerConverter converter = new CsvNullableIntegerConverter();

    @Test
    void convert_shouldReturnInteger_whenValueIsValid() {
        // Act
        var result = converter.convert("121");

        // Assert
        assertThat(result).isEqualTo(121);
    }

    @Test
    void convert_shouldTrimValue_whenValueHasSpaces() {
        // Act
        var result = converter.convert(" 42 ");

        // Assert
        assertThat(result).isEqualTo(42);
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
        assertThatThrownBy(() -> converter.convert("12.5"))
                .isInstanceOf(NumberFormatException.class);
    }
}
