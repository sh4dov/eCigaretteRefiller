package com.sh4dov.ecigaretterefiller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by SYSTEM on 2014-12-06.
 */
public class DbHandler extends SQLiteOpenHelper implements RefillsRepository {
    public static final String DatabaseName = "ecigaretterefills.db";

    public DbHandler(Context context){
        super(context, DatabaseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Refills (Id INTEGER PRIMARY KEY, Date DATE NOT NULL, Size FLOAT NOT NULL, Name VARCHAR(100) NOT NULL, IsDeleted BOOLEAN DEFAULT FALSE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void Add(Refill refill){
        ContentValues values = new ContentValues();
        values.put("Date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(refill.date));
        values.put("Size", refill.size);
        values.put("Name", refill.name);

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert("Refills", null, values);
        db.close();
    }

    @Override
    public ArrayList<Refill> GetRefills(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM Refills WHERE NOT IsDeleted ORDER BY Date DESC", null);
        ArrayList<Refill> result = new ArrayList<Refill>();

        if(!c.moveToFirst()){
            c.close();
            return result;
        }

        do{
            Refill r = getRefill(c);
            result.add(r);
        }
        while(c.moveToNext());
        c.close();
        db.close();

        return result;
    }

    private Refill getRefill(Cursor c) {
        Refill r = new Refill();
        r.id = c.getInt(0);
        try {
            r.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(c.getString(1));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        r.size = c.getFloat(2);
        r.name = c.getString(3);
        return r;
    }

    @Override
    public void importFromCsv(File file)  {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");

                ContentValues cv = new ContentValues();
                cv.put("Date", values[1]);
                cv.put("Size", values[2]);
                cv.put("Name", values[3]);
                cv.put("IsDeleted", values[4]);


                db.insert("Refills", null, cv);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        db.close();
    }

    @Override
    public Refill getLastRefill(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM Refills WHERE NOT IsDeleted ORDER BY Date DESC LIMIT 1", null);

        if(!c.moveToFirst()){
            c.close();
            return null;
        }

        Refill refill = getRefill(c);
        c.close();
        db.close();

        return refill;
    }

    @Override
    public void clear() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM Refills");

        db.close();
    }

    @Override
    public void update(Refill refill) {
        ContentValues values = new ContentValues();
        values.put("Date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(refill.date));
        values.put("Size", refill.size);
        values.put("Name", refill.name);

        SQLiteDatabase db = this.getWritableDatabase();

        db.update("Refills", values, "Id = " + Integer.toString(refill.id), null);
        db.close();
    }

    @Override
    public void delete(int id) {
        ContentValues values = new ContentValues();
        values.put("IsDeleted", true);

        SQLiteDatabase db = this.getWritableDatabase();

        db.update("Refills", values, "Id = " + Integer.toString(id), null);
        db.close();
    }
}
