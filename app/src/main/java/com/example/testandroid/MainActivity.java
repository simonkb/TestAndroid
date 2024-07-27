package com.example.testandroid;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import com.example.testandroid.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    int PICK_AUDIO_REQUEST = 0;
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
            HashMap<String, Object> res =  getAudioMetaData(audioUri);
            TextView titleTextView = findViewById(R.id.title);
            TextView artistTextView = findViewById(R.id.artist);
            TextView durationTextView = findViewById(R.id.duration);
            ImageView artImageView = findViewById(R.id.image_art);

            titleTextView.setText((String) res.get("title"));
            artistTextView.setText((String) res.get("artist"));
            durationTextView.setText(""+res.get("duration"));

            File artFile = (File) res.get("art");
            if (artFile != null && artFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(artFile.getAbsolutePath());
                artImageView.setImageBitmap(bitmap);
            } else {
                artImageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }
    private HashMap<String, Object> getAudioMetaData(Uri uri){
        HashMap<String, Object> res = new HashMap<>();
        String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM_ID};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            res.put("title" , cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
            res.put("artist" , cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
            res.put("duration" , cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
            long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
            Uri artworkUri = ContentUris.withAppendedId(albumArtUri, albumId);
            try (InputStream inputStream = getContentResolver().openInputStream(artworkUri)){
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    res.put("art", saveWebp(bitmap, System.currentTimeMillis() + ".webp"));
                }
            } catch (Exception e){
                Log.d("error", e.toString());
            }
            cursor.close();
        }
        return res;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.fab.setOnClickListener(view -> pickAudioFile());
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


}