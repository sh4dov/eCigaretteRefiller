package com.sh4dov.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.sh4dov.common.Notificator;
import com.sh4dov.model.Refill;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


/**
 * Created by SYSTEM on 2014-12-06.
 */
public class DbHandler extends SQLiteOpenHelper implements RefillsRepository {
    public static final String DatabaseName = "ecigaretterefills.db";

    private Notificator notificator;
    private String[] columns = new String[] {"Id", "Date", "Size", "Name", "IsDeleted"};

    public DbHandler(Context context, Notificator notificator){
        super(context, DatabaseName, null, 1);
        this.notificator = notificator;
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
    public ArrayList<Refill> getRefills(){
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

    private void showInfo(String message){
        if(notificator!= null){
            notificator.showInfo(message);
        }
    }

    @Override
    public String exportToString(){
        StringBuffer buffer = new StringBuffer();
        String newLine = "\r\n";

        buffer.append(TextUtils.join(";", columns) + newLine);

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM Refills", null);
        if(!c.moveToFirst()){
            c.close();
            db.close();
            return buffer.toString();
        }

        do{
            for (int i = 0; i < c.getColumnCount(); i++) {
                buffer.append(c.getString(i));
                buffer.append(";");
            }
            buffer.append(newLine);
        }
        while(c.moveToNext());
        c.close();
        db.close();

        return buffer.toString();
    }

    @Override
    public boolean exportToCsv(File file) {
        if(!file.exists()){
            try {
                if(!file.createNewFile()){
                    return false;
                }
            } catch (IOException e) {
                showInfo(e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream stream = new FileOutputStream(file, false);
            String buffer = exportToString();
            stream.write(buffer.toString().getBytes());
            stream.close();
        } catch (IOException e) {
            showInfo(e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
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
                cv.put("Id", values[0]);
                cv.put("Date", values[1]);
                cv.put("Size", values[2]);
                cv.put("Name", values[3]);
                cv.put("IsDeleted", values[4]);


                int id = db.updateWithOnConflict("Refills", cv, "Id=?", new String[] {values[0]}, SQLiteDatabase.CONFLICT_REPLACE);
                if(id <= 0){
                    db.insert("Refills", null, cv);
                }
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
