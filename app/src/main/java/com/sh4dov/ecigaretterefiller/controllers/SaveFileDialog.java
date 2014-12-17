package com.sh4dov.ecigaretterefiller.controllers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.sh4dov.ecigaretterefiller.ListenerList;
import com.sh4dov.ecigaretterefiller.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by sh4dov on 2014-12-15.
 */
public class SaveFileDialog extends DialogFragment {
    private final String PARENT ="..";
    private ArrayList<String> items = new ArrayList<String>();
    private File path;
    private View view;
    private String fileName = "filename.txt";

    public interface FileListener{
        void fileSelected(File file);
    }

    private ListenerList<FileListener> fileListeners = new ListenerList<FileListener>();

    public SaveFileDialog(){
        path = Environment.getDataDirectory();
        listDirectory(path);
    }

    public void addFileListener(FileListener listener) {
        fileListeners.add(listener);
    }

    public void setFileName(String name){
        fileName = name;
    }

    private void listDirectory(File path) {
        items.clear();
        if(path.getParentFile() != null){
            items.add(PARENT);
        }
        String[] list = path.list();
        if(list == null){
            return;
        }

        for(String item: list){
            items.add(item);
        }
    }

    private File getSelected(int i){
        String item = items.get(i);
        if(item == PARENT){
            return path.getParentFile();
        }

        return new File(path, item);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        view = inflater.inflate(R.layout.save_file_dialog, null);
        builder.setView(SaveFileDialog.this.view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File file = new File(path, getFileName(SaveFileDialog.this.view));
                        fireFileSelectedEvent(file);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SaveFileDialog.this.getDialog().cancel();
                    }
                });

        final ListView listView = (ListView) SaveFileDialog.this.view.findViewById(R.id.file_list);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                File selected = getSelected(i);
                if(selected.isFile()){
                    EditText editText = (EditText) SaveFileDialog.this.view.findViewById(R.id.file_name);
                    editText.setText(selected.getName());
                }else{
                    path = selected;
                    listDirectory(path);
                    adapter.notifyDataSetChanged();
                    listView.setSelectionAfterHeaderView();
                }

                displayPath(SaveFileDialog.this.view);
            }
        });
        displayPath(SaveFileDialog.this.view);
        setFileName(SaveFileDialog.this.view);

        return builder.create();
    }

    private void fireFileSelectedEvent(final File file) {
        fileListeners.fireEvent(new ListenerList.FireHandler<FileListener>() {
            public void fireEvent(FileListener listener) {
                listener.fileSelected(file);
            }
        });
    }

    private String getFileName(View view) {
        TextView textView = (TextView) view.findViewById(R.id.file_name);
        return textView.getText().toString();
    }

    private void setFileName(View view) {
        TextView textView = (TextView) view.findViewById(R.id.file_name);
        textView.setText(fileName);
    }

    private void displayPath(View view){
        TextView textView = (TextView) view.findViewById(R.id.file_path);
        textView.setText(path.getAbsolutePath());
    }
}
