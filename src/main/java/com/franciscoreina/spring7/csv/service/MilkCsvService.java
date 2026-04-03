package com.franciscoreina.spring7.csv.service;

import com.franciscoreina.spring7.csv.dto.MilkCsvRecord;

import java.io.InputStream;
import java.util.List;

public interface MilkCsvService {

    List<MilkCsvRecord> convertCSV(InputStream inputStream);
}
