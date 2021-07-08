package ru.pfl.jvmtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.util.Set;

public class FileRwActivity extends AppCompatActivity {

    private static final String TAG = "MEDIA";
    private static final int RW_PERMISSION_MANAGE = 0;
    private static final int RW_PERMISSION_MEDIA = 1;


    private static final int MODE_FILE_MANAGE = 0;
    private static final int MODE_MEDIA_STORE = 1;
    private static int currentMode = 0;

    private TextInputEditText textInputEditText;
    private Button buttonWrite;
    private Button buttonRead;
    private Button buttonGetPermissions;
    private Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_rw);

        textInputEditText = (TextInputEditText) findViewById(R.id.textInputEditTextFileRw);
        buttonWrite = (Button) findViewById(R.id.buttonFileRwWrite);
        buttonRead = (Button) findViewById(R.id.buttonFileRwRead);
        buttonGetPermissions = (Button) findViewById(R.id.buttonFileRwGetPermissions);
        spinner = (Spinner) findViewById(R.id.spinnerFileRw);

        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToFile();
            }
        });
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readFromFile();
            }
        });
        buttonGetPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFileRwPermissions();
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentMode = MODE_FILE_MANAGE;
                        break;
                    case 1:
                        currentMode = MODE_MEDIA_STORE;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //checkRwPermissions(RW_PERMISSION_MEDIA);
        //checkRwPermissions(RW_PERMISSION_MANAGE);

        //checkExternalMedia();
        //writeToSDFile();
        //checkWorkingDirectory();
        //readRaw();
        //Log.i("FileRw", "OnCreateFunk");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void getFileRwPermissions() {
        switch (currentMode){
            case MODE_FILE_MANAGE:
                if(checkRwPermissions(RW_PERMISSION_MANAGE)){
                    Toast.makeText(getApplicationContext(), "Permission already granted!!!", Toast.LENGTH_SHORT).show();
                } else {
                    requestRwPermissions(RW_PERMISSION_MANAGE);
                }
                break;
            case MODE_MEDIA_STORE:
                if(checkRwPermissions(RW_PERMISSION_MEDIA)){
                    Toast.makeText(getApplicationContext(), "Permission already granted!!!", Toast.LENGTH_SHORT).show();
                } else {
                    requestRwPermissions(RW_PERMISSION_MEDIA);
                }

        }
    }

    private boolean checkRwPermissions(int type) {

        switch (type) {
            case RW_PERMISSION_MANAGE:
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R){
                    checkRwPermissions(RW_PERMISSION_MEDIA);
                } else {

                    if (Environment.isExternalStorageManager()) {
                        return true;
                    } else {
                        return false;
                    }
                }

            case RW_PERMISSION_MEDIA:
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    return true;
                } else {
                    return false;
                }
            default:
                return false;
        }
    }



    private void requestRwPermissions(int type){
        switch (type){
            case RW_PERMISSION_MANAGE:
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R){
                    requestRwPermissions(RW_PERMISSION_MEDIA);
                } else {
                    startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                }
                break;
            case RW_PERMISSION_MEDIA:
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                break;
        }
    }

    private void readFromFile(){
        if(currentMode == MODE_FILE_MANAGE){
            if(checkRwPermissions(RW_PERMISSION_MANAGE)){
                readFromFileManage();
            } else {
                Toast.makeText(getApplicationContext(), "Please allow permission!!!", Toast.LENGTH_SHORT).show();
            }
        } else {
            if(checkRwPermissions(RW_PERMISSION_MEDIA)){
                Uri uri = findMediaStoreFileOrNull();
                if(uri != null){
                    readFromMediaStoreFile(uri);
                } else {
                    Toast.makeText(getApplicationContext(), "File not exist!!!", Toast.LENGTH_SHORT).show();
                }
                //readFromMediaStoreFile();
            } else {
                Toast.makeText(getApplicationContext(), "Please allow permission!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void writeToFile(){
        if(currentMode == MODE_FILE_MANAGE){
            if(checkRwPermissions(RW_PERMISSION_MANAGE)){
                writeToFileManage();
            } else {
                Toast.makeText(getApplicationContext(), "Please allow permission!!!", Toast.LENGTH_SHORT).show();
            }
        } else {
            if(checkRwPermissions(RW_PERMISSION_MEDIA)){
                Uri uri = findMediaStoreFileOrNull();
                if(uri == null){
                    createMediaStoreFile();
                } else {
                    overwriteMediaStoreFile(uri);
                }
                //createMediaStoreFile();
            } else {
                Toast.makeText(getApplicationContext(), "Please allow permission!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void readFromFileManage(){
        File root = Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/JVM Test", "FileRw.txt");
        boolean bl = file.exists();


        if(file.exists()) {

            long fSize = file.length();
            if (fSize > 0 && fSize < 1024) {

                int size = Math.toIntExact(fSize);
                byte[] bytes = new byte[size];
                try {
                    //BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    //FileReader reader = new FileReader(file);
                    FileInputStream stream = new FileInputStream(file);
                    stream.read(bytes);
                    stream.close();

                    //stringBuilder.append(bytes);
                    //text = String.valueOf(bytes);
                    //CharsetDecoder.decode(bytes);
                    String text = new String(bytes);

                    textInputEditText.setText(text);

                    Toast.makeText(getApplicationContext(), "Readed from " + file, Toast.LENGTH_SHORT).show();

                    //reader.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "File too long!!!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Not exist " + file, Toast.LENGTH_SHORT).show();
        }
    }

    private void writeToFileManage(){
        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/JVM Test");
        dir.mkdirs();
        File file = new File(dir, "FileRw.txt");

        String text = textInputEditText.getText().toString();
        byte[] bytes = text.getBytes();//new byte[text.length()];
        //bytes = text.getBytes();
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(bytes);
            stream.close();
            Toast.makeText(getApplicationContext(), "Written to " + file, Toast.LENGTH_SHORT).show();
            //Log.i("FileRw", textInputEditText.getText().toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void createMediaStoreFile(){
        try {
            ContentValues values = new ContentValues();

            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "FileRwMediaStore");       //file name
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");        //file extension, will automatically add to file
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/JVM Test/");     //end "/" is not mandatory

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);      //important!

            OutputStream outputStream = getContentResolver().openOutputStream(uri);

            //outputStream.write("This is menu category data.".getBytes());
            outputStream.write(textInputEditText.getText().toString().getBytes());

            outputStream.close();

            Toast.makeText(getApplicationContext(), "File created successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Fail to create file", Toast.LENGTH_SHORT).show();
        }
    }

    private void readFromMediaStoreFile(Uri uri) {

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            int size = inputStream.available();
            byte[] bytes = new byte[size];
            inputStream.read(bytes);
            inputStream.close();
            String jsonString = new String(bytes, StandardCharsets.UTF_8);
            textInputEditText.setText(jsonString);
            /*AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setTitle("File Content");
            builder.setMessage(jsonString);
            builder.setPositiveButton("OK", null);
            builder.create().show();*/
            Toast.makeText(getApplicationContext(), "File read successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Fail to read file", Toast.LENGTH_SHORT).show();
        }
    }

    private void overwriteMediaStoreFile(Uri uri){
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri, "rwt");
            outputStream.write(textInputEditText.getText().toString().getBytes());
            outputStream.close();
            Toast.makeText(getApplicationContext(), "File written successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Fail to write file", Toast.LENGTH_SHORT).show();
        }
    }

    private void findAndOverwriteMediaStore(){
        Uri contentUri = MediaStore.Files.getContentUri("external");

        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";

        String[] selectionArgs = new String[]{Environment.DIRECTORY_DOCUMENTS + "/JVM Test/"};    //must include "/" in front and end

        Cursor cursor = getContentResolver().query(contentUri, null, selection, selectionArgs, null);

        Uri uri = null;

        if (cursor.getCount() == 0) {
            Toast.makeText(getApplicationContext(), "No file found in \"" + Environment.DIRECTORY_DOCUMENTS + "/JVM Test/\"", Toast.LENGTH_LONG).show();
        } else {
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));

                if (fileName.equals("FileRwMediaStore.txt")) {                          //must include extension
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));

                    uri = ContentUris.withAppendedId(contentUri, id);

                    break;
                }
            }

            if (uri == null) {
                Toast.makeText(getApplicationContext(), "\"FileRwMediaStore.txt\" not found", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri, "rwt");      //overwrite mode, see below

                    outputStream.write("This is overwritten data\n".getBytes());

                    outputStream.close();

                    Toast.makeText(getApplicationContext(), "File written successfully", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Fail to write file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private Uri findMediaStoreFileOrNull(){
        Uri contentUri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
        String[] selectionsArgs = new String[]{Environment.DIRECTORY_DOCUMENTS + "/JVM Test/"};
        Cursor cursor = getContentResolver().query(contentUri, null, selection, selectionsArgs, null);
        Uri uri = null;
        
        if(cursor.getCount() == 0){
            Toast.makeText(getApplicationContext(), "No file found in \"" + Environment.DIRECTORY_DOCUMENTS + "/JVM Test/\"", Toast.LENGTH_LONG).show();
        } else {
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                if(fileName.equals("FileRwMediaStore.txt")){
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    uri = ContentUris.withAppendedId(contentUri, id);
                    break;
                }
            }
        }
        return uri;
    }

    private void checkWorkingDirectory(){
        //String folder = "jvm";
        //File f = new File(getExternalFilesDir())
        //Log.i("FileRw", Environment.getStorageDirectory().toString());
        //Log.i("FileRw", getExternalMediaDirs().toString());
        Log.i("FileRw", "1: " + Environment.DIRECTORY_DOWNLOADS);
        Log.i("FileRw", "2: " + Environment.getExternalStorageDirectory().toString());
        Log.i("FileRw", "3: " + getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString());
        Log.i("FileRw", "4: " + getFilesDir().toString());
        //Log.i("FileRw", getExternalMediaDirs()[0].toString());
        for (File file :  getExternalMediaDirs()){
            Log.i("FileRw", "5: " + file.toString());
        }
        Log.i("FileRw", "6: " + Environment.getStorageDirectory().toString());
        Log.i("FileRw", "7: " + Environment.getStorageDirectory().getAbsolutePath());
        Log.i("FileRw", "8: " + Environment.getRootDirectory().toString());
        Log.i("FileRw", "9: " + Environment.getDownloadCacheDirectory().toString());
        Log.i("FileRw", "10: " + Environment.getDataDirectory().toString());
        Log.i("FileRw", "11: " + Environment.getExternalStorageState());

        Set<String> volumeNames = MediaStore.getExternalVolumeNames(getApplicationContext());
        for (String vol : volumeNames){
            Log.i("FileRw", "12: " + vol);
        }
        Uri uri = MediaStore.Files.getContentUri("external_primary");
        Log.i("FileRw", "13: " + uri.toString());

        Log.i("FileRw", "14: " + getRealPathFromURI(getApplicationContext(), uri));


        for (File file :  getExternalFilesDirs(Environment.DIRECTORY_PICTURES)){
            Log.i("FileRw", "15: " + file.toString());
        }
        Log.i("FileRw", "16: " + getExternalFilesDir(null).getAbsolutePath());




    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            //String[] proj = { MediaStore.Images.Media.DATA };
            String[] proj = { MediaStore.Files.FileColumns.DATA };

            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            //int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /** Method to check whether external media available and writable. This is adapted from
     http://developer.android.com/guide/topics/data/data-storage.html#filesExternal */
    private void checkExternalMedia(){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        //textInputEditText.append("\n\nExternal Media: readable="
        //        +mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
        Toast.makeText(getApplicationContext(), "External Media: readable=" + mExternalStorageAvailable + " writable=" + mExternalStorageWriteable, Toast.LENGTH_SHORT).show();
    }
    /** Method to write ascii text characters to file on SD card. Note that you must add a
     WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     a FileNotFound Exception because you won't have write permission. */
    private void writeToSDFile(){

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

        File root = android.os.Environment.getExternalStorageDirectory();
        textInputEditText.append("\nExternal file system root: " + root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        //File dir = new File (root.getAbsolutePath() + "/download");
        File dir = new File (root.getAbsolutePath() + "/jvm");
        dir.mkdirs();
        File file = new File(dir, "myData.txt");

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println("Hi , How are you");
            pw.println("Hello");
            pw.flush();
            pw.close();
            f.close();
            Log.i("FileRw", "Try success writing");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
            Log.i("FileRw", "Try - Exc File not found");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("FileRw", "Try - Exc IOError");
        }
        textInputEditText.append("\n\nFile written to "+file);
    }

    /** Method to read in a text file placed in the res/raw directory of the application. The
     method reads in all lines of the file sequentially. */

    /*private void readRaw(){
        textInputEditText.append("\nData read from res/raw/textfile.txt:");
        InputStream is = this.getResources().openRawResource(R.raw.textfile);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr, 8192);    // 2nd arg is buffer size

        // More efficient (less readable) implementation of above is the composite expression
    //BufferedReader br = new BufferedReader(new InputStreamReader(
            //this.getResources().openRawResource(R.raw.textfile)), 8192);

        try {
            String test;
            while (true){
                test = br.readLine();
                // readLine() returns null if no more lines in the file
                if(test == null) break;
                textInputEditText.append("\n"+"    "+test);
            }
            isr.close();
            is.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        textInputEditText.append("\n\nThat is all");
    }*/
}