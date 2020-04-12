package com.example.SilentAppInstaller;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button open, btnOpenAccess, btnAccess, btnSelectApk;

    TextView tvTitle;


    private static final int FILE_SELECT = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate");

        setContentView(R.layout.activity_main);

        open = (Button) findViewById(R.id.btn_open);
        btnOpenAccess = (Button) findViewById(R.id.btn_openaccess);
        btnAccess = (Button) findViewById(R.id.btn_access);
        btnSelectApk = (Button) findViewById(R.id.btn_selectapk);


        tvTitle = (TextView) findViewById(R.id.txt_view);


        btnOpenAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });
        btnAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accessInstall();
            }
        });
        btnSelectApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectApk();
            }
        });

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                startActivity(intent2);
            }
        });

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

    }


    private void accessInstall() {
        final String strPath = tvTitle.getText().toString();
        Log.d("Path : ",strPath);

        if (TextUtils.isEmpty(strPath)) {
            return;
        }
        //Uri uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", new File(strPath));
        Uri uri = Uri.fromFile(new File(strPath));
        Intent localIntent = new Intent(Intent.ACTION_VIEW);
        localIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        localIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(localIntent);
    }
    private void selectApk() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Choose an APK"), FILE_SELECT);
        }catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Can't find file browser", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == FILE_SELECT && resultCode == RESULT_OK) {

            Uri uri = data.getData();

            // path providing null result I need to figure it out

            String path = getPath(this,uri);
            tvTitle.setText(path);

        }
//        super.onActivityResult(requestCode, resultCode, data);
    }


    public static String getPath(Context context, Uri uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }



}
