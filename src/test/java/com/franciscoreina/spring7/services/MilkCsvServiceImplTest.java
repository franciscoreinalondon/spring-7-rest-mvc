package com.franciscoreina.spring7.services;

import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

public class MilkCsvServiceImplTest {

    MilkCsvService milkCsvService = new MilkCsvServiceImpl();

    @Test
    void convertCSV() throws FileNotFoundException {

        var csvFile = ResourceUtils.getFile("classpath:csvdata/milk_dataset.csv");

        var recordList = milkCsvService.convertCSV(csvFile);

        System.out.println(recordList.size());

        assertThat(recordList.size()).isGreaterThan(0);
    }
}
