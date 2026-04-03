package com.franciscoreina.spring7.csv.dto;

import com.franciscoreina.spring7.domain.milk.MilkType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MilkCsvRecordTest {

    @Test
    void normalize_shouldTrimAndKeepValidFields() {
        // Arrange
        var record = new MilkCsvRecord(
                1,
                1,
                BigDecimal.valueOf(0.0151),
                null,
                1001,
                "  Milk Name  ",
                "  Semi Skimmed  ",
                121,
                BigDecimal.valueOf(16),
                null,
                121,
                "  Dairy  ",
                "  City  ",
                "  ST  ",
                "  Label  "
        );

        // Act
        record.normalize();

        // Assert
        assertThat(record.getMilkName()).isEqualTo("Milk Name");
        assertThat(record.getStyle()).isEqualTo("Semi Skimmed");
        assertThat(record.getDairyName()).isEqualTo("Dairy");
        assertThat(record.getCity()).isEqualTo("City");
        assertThat(record.getState()).isEqualTo("ST");
        assertThat(record.getLabel()).isEqualTo("Label");
    }

    @Test
    void normalize_shouldThrowException_whenRequiredFieldIsBlank() {
        // Arrange
        var record = new MilkCsvRecord(
                1,
                1,
                null,
                null,
                1001,
                " ",
                "Semi Skimmed",
                121,
                null,
                null,
                null,
                "Dairy",
                "City",
                "ST",
                "Label"
        );

        // Act + Assert
        assertThatThrownBy(record::normalize)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CSV field must not be blank");
    }

    @Test
    void toMilkType_shouldReturnCorrectEnum_forValidStyles() {
        // Arrange
        var record = baseRecord("Semi Skimmed");

        // Act
        var result = record.toMilkType();

        // Assert
        assertThat(result).isEqualTo(MilkType.SEMI_SKIMMED);
    }

    @Test
    void toMilkType_shouldTrimAndNormalizeInput() {
        // Arrange
        var record = baseRecord("  semi skimmed  ");

        // Act
        var result = record.toMilkType();

        // Assert
        assertThat(result).isEqualTo(MilkType.SEMI_SKIMMED);
    }

    @Test
    void toMilkType_shouldHandleDifferentStyles() {
        assertThat(baseRecord("A2").toMilkType()).isEqualTo(MilkType.A2);
        assertThat(baseRecord("Goat").toMilkType()).isEqualTo(MilkType.GOAT);
        assertThat(baseRecord("Lactose Free").toMilkType()).isEqualTo(MilkType.LACTOSE_FREE);
        assertThat(baseRecord("Skimmed").toMilkType()).isEqualTo(MilkType.SKIMMED);
        assertThat(baseRecord("Whole").toMilkType()).isEqualTo(MilkType.WHOLE);
    }

    @Test
    void toMilkType_shouldThrowException_whenStyleIsUnknown() {
        // Arrange
        var record = baseRecord("Unknown Style");

        // Act + Assert
        assertThatThrownBy(record::toMilkType)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported milk style: Unknown Style");
    }

    private MilkCsvRecord baseRecord(String style) {
        return new MilkCsvRecord(
                1,
                1,
                BigDecimal.ONE,
                null,
                1001,
                "Milk",
                style,
                121,
                BigDecimal.TEN,
                null,
                121,
                "Dairy",
                "City",
                "ST",
                "Label"
        );
    }
}
