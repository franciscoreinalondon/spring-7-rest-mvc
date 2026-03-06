package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dtos.milk.MilkCsvRecord;

import java.io.File;
import java.util.List;

public interface MilkCsvService {

    List<MilkCsvRecord> convertCSV(File csvFile);

}
