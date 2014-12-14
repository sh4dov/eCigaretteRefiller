package com.sh4dov.ecigaretterefiller;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by sh4dov on 2014-12-14.
 */
public interface RefillsRepository {
    void Add(Refill refill);

    ArrayList<Refill> GetRefills();

    void importFromCsv(File file);

    Refill getLastRefill();

    void clear();

    void update(Refill refill);

    void delete(int id);
}
