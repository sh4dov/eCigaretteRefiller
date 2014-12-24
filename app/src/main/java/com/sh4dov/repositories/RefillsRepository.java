package com.sh4dov.repositories;

import com.sh4dov.common.ProgressPointer;
import com.sh4dov.model.Refill;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by sh4dov on 2014-12-14.
 */
public interface RefillsRepository {
    void Add(Refill refill);

    ArrayList<Refill> getRefills();

    void importFrom(File file, ProgressPointer progressPointer);

    void importFrom(String value, ProgressPointer progressPointer);

    Refill getLastRefill();

    void clear();

    void update(Refill refill);

    void delete(int id);

    String exportToString();

    boolean exportToCsv(File file);
}
