package com.franciscoreina.spring7.csv.service;

import com.franciscoreina.spring7.csv.dto.MilkCsvRecord;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class MilkCsvServiceImpl implements MilkCsvService {

    @Override
    public List<MilkCsvRecord> convertCSV(InputStream inputStream) {
        try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {

            return new CsvToBeanBuilder<MilkCsvRecord>(reader)
                    .withType(MilkCsvRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file", e);
        }
    }
}
