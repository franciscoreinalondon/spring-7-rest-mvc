package com.franciscoreina.spring7.dto.file;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilkCsvRecord {

    @CsvBindByName
    private Integer row;

    @CsvBindByName(column = "count.x")
    private Integer count;      // Counter in the original dataset

    @CsvBindByName
    private String mfp;         // Milk fat percentage content

    @CsvBindByName
    private String lc;          // Lactose content (maybe NA for lactose-free milk)

    @CsvBindByName
    private Integer id;         // Unique identifier of the milk product

    @CsvBindByName
    private String milk;        // Name of the milk product

    @CsvBindByName
    private String style;       // Type of milk (e.g. Whole, Skimmed, A2, Goat)

    @CsvBindByName
    private Integer dairy_id;   // Identifier of the dairy producer

    @CsvBindByName
    private Float ounces;       // Package size in fluid ounces

    @CsvBindByName
    private String style2;      // Secondary milk classification (optional)

    @CsvBindByName(column = "count.y")
    private String count_y;     // Counter associated with the dairy producer

    @CsvBindByName
    private String dairy;       // Name of the dairy producer

    @CsvBindByName
    private String city;        // City where the dairy is located

    @CsvBindByName
    private String state;       // State where the dairy is located

    @CsvBindByName
    private String label;       // Full product label (milk name + dairy)

}

/*
* private Integer count;              // Counter in the original dataset
private BigDecimal milkFatPercentage; // Percentage of milk fat content
private BigDecimal lactoseContent;    // Lactose content (may be NA for lactose-free milk)
private Integer id;                 // Unique identifier of the milk product
private String milk;                // Name of the milk product
private String style;               // Type of milk (e.g. Whole, Skimmed, A2, Goat)
private Integer dairyId;            // Identifier of the dairy producer
private Integer ounces;             // Package size in fluid ounces
private String style2;              // Secondary milk classification (optional)
private Integer dairyCount;         // Counter associated with the dairy producer
private String dairy;               // Name of the dairy producer
private String city;                // City where the dairy is located
private String state;               // State where the dairy is located
private String label;               // Full product label (milk name + dairy)
*
* */
