package com.example.testandroid;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import wseemann.media.FFmpegMediaMetadataRetriever;

import com.example.testandroid.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    int PICK_AUDIO_REQUEST = 0;


//    private HashMap<String, Object> getFileMetadata(File file) throws IOException {
//        HashMap<String, Object> res = new HashMap<>();
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(this, Uri.fromFile(file));
//        int duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))/1000;
//        res.put("duration", duration);
//        res.put("title", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
//        res.put("artist", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
//        byte[] albumArt = retriever.getEmbeddedPicture();
//        if (albumArt != null) {
//            String filename = file.getName();
//            String artName = filename.substring(0, filename.lastIndexOf(".")) + ".webp";
//            res.put("art", saveWebp(BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length), artName).toURI().toURL().toString());
//        }
//        retriever.release();
//        return res;
//    }

    private void getFileMetadata(File audioFile) {
        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        try {
            mmr.setDataSource(audioFile.getAbsolutePath());

            String title = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
            byte[] artwork = mmr.getEmbeddedPicture();

            Log.d("MainActivity", "Title: " + title);
            Log.d("MainActivity", "Artist: " + artist);
            if (artwork != null) {
                Log.d("MainActivity", "Artwork found");
            } else {
                Log.d("MainActivity", "No artwork found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mmr.release();
        }
    }



    File saveWebp(Bitmap bitmap, String fname) {
        File new_file = new File(getCacheDir(), fname);
        if (new_file.exists()) new_file.delete();
        try {
            FileOutputStream out = new FileOutputStream(new_file);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new_file;
    }

    private void pickAudioFile() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri audioUri = data.getData();
            String filePath = getRealPathFromURI(audioUri);

            if (filePath != null) {
                File audioFile = new File(filePath);

                    getFileMetadata(audioFile);
//                    Log.d("MainActivity dddddddddddd", getFileMetadata(audioFile));

                Log.d("MainActivity dddddddddddd 222", "Audio File Path: " + audioFile.getAbsolutePath());
            }
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickAudioFile();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}