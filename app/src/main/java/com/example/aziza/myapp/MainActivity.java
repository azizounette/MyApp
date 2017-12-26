package com.example.aziza.myapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import static android.graphics.Color.valueOf;

public class MainActivity extends AppCompatActivity {

    private Bitmap bmp;


    private void toGray(Bitmap bmp) {
        int outH = bmp.getHeight();
        int outW = bmp.getWidth();

        int pixels[] = new int[outW*outH];
        bmp.getPixels(pixels, 0, outW, 0, 0, outW, outH);
        for (int i = 0; i < outW*outH; i++) {
            int gray = (int) (0.11 * Color.blue(pixels[i]) + 0.3 * Color.red(pixels[i]) + 0.59 * Color.green(pixels[i]));
            pixels[i] = Color.rgb(gray, gray, gray);
        }
        bmp.setPixels(pixels, 0, outW, 0, 0, outW, outH);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView iv = findViewById(R.id.viewLenna);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.lenna);
        bmp = bm.copy(bm.getConfig(), true);
        iv.setImageBitmap(bmp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ImageView iv = findViewById(R.id.viewLenna);
        switch(item.getItemId()) {
            case R.id.toGray:
                toGray(bmp);
                iv.setImageBitmap(bmp);
                Toast.makeText(MainActivity.this, "clicked toGray", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
    
}
