package com.franciscoreina.spring7.csv.dto;

import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.csv.converter.CsvNullableBigDecimalConverter;
import com.franciscoreina.spring7.csv.converter.CsvNullableIntegerConverter;
import com.franciscoreina.spring7.csv.converter.CsvNullableStringConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Locale;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilkCsvRecord {

    @CsvBindByName(column = "row")
    private Integer row;

    @CsvBindByName(column = "count.x")
    private Integer count;

    @CsvCustomBindByName(column = "mfp", converter = CsvNullableBigDecimalConverter.class)
    private String milkFatPercentage;

    @CsvCustomBindByName(column = "lc", converter = CsvNullableBigDecimalConverter.class)
    private String lactoseContent;

    @CsvBindByName(column = "id")
    private Integer externalId;

    @CsvBindByName(column = "milk")
    private String milkName;

    @CsvBindByName(column = "style")
    private String style;

    @CsvBindByName(column = "dairy_id")
    private Integer dairyId;

    @CsvCustomBindByName(column = "ounces", converter = CsvNullableBigDecimalConverter.class)
    private BigDecimal ounces;

    @CsvCustomBindByName(column = "style2", converter = CsvNullableStringConverter.class)
    private String secondaryStyle;

    @CsvCustomBindByName(column = "count.y", converter = CsvNullableIntegerConverter.class)
    private Integer dairyCount;

    @CsvBindByName(column = "dairy")
    private String dairyName;

    @CsvBindByName(column = "city")
    private String city;

    @CsvBindByName(column = "state")
    private String state;

    @CsvBindByName(column = "label")
    private String label;

    public void normalize() {
        milkName = normalizeText(milkName);
        style = normalizeText(style);
        secondaryStyle = normalizeNullableText(secondaryStyle);
        dairyName = normalizeText(dairyName);
        city = normalizeText(city);
        state = normalizeText(state);
        label = normalizeText(label);
    }

    public MilkType toMilkType() {
        var normalizedStyle = normalizeText(style);

        return switch (normalizedStyle.toUpperCase(Locale.ROOT)) {
            case "A2" -> MilkType.A2;
            case "GOAT" -> MilkType.GOAT;
            case "HIGH PROTEIN" -> MilkType.HIGH_PROTEIN;
            case "LACTOSE FREE" -> MilkType.LACTOSE_FREE;
            case "ORGANIC WHOLE" -> MilkType.ORGANIC_WHOLE;
            case "SEMI SKIMMED" -> MilkType.SEMI_SKIMMED;
            case "SKIMMED" -> MilkType.SKIMMED;
            case "WHOLE" -> MilkType.WHOLE;
            default -> throw new IllegalArgumentException("Unsupported milk style: " + style);
        };
    }

    private static String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CSV field must not be blank");
        }

        return value.trim();
    }

    private static String normalizeNullableText(String value) {
        if (value == null || value.isBlank() || "NA".equalsIgnoreCase(value.trim())) {
            return null;
        }

        return value.trim();
    }
}
