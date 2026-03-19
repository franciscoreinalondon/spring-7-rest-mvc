package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dto.file.MilkCsvRecord;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

@Service
public class MilkCsvServiceImpl implements MilkCsvService {

    @Override
    public List<MilkCsvRecord> convertCSV(File csvFile) {

        try {
            return new CsvToBeanBuilder<MilkCsvRecord>(new FileReader(csvFile))
                    .withType(MilkCsvRecord.class)
                    .build().parse();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
