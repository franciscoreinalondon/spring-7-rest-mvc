package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dto.file.MilkCsvRecord;

import java.io.File;
import java.util.List;

public interface MilkCsvService {

    List<MilkCsvRecord> convertCSV(File csvFile);

}
