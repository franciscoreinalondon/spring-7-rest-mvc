package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dtos.milk.MilkCsvRecord;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MilkCsvServiceImplTest {

    MilkCsvService milkCsvService = new MilkCsvServiceImpl();

    @Test
    void convertCSV() throws FileNotFoundException {

        File csvFile = ResourceUtils.getFile("classpath:csvdata/milk_dataset.csv");

        List<MilkCsvRecord> recordList = milkCsvService.convertCSV(csvFile);

        System.out.println(recordList.size());

        assertThat(recordList.size()).isGreaterThan(0);
    }
}
