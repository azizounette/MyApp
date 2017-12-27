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


    /* Sets picture to black and white */
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

    /* Histogram equalizer for contrasts in black and white */
    private int[] histogram(Bitmap bmp) {
        int[] res = new int[256];
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Bitmap bmCopy = bmp.copy(bmp.getConfig(), true);
        toGray(bmCopy);

        int pixels[] = new int[w*h];
        bmCopy.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < w*h; i++)
            res[Color.red(pixels[i])]++;
        return res;
    }

    private int[] cumulatedHist(int[] hist) {
        int[] res = new int[256];
        res[0] = hist[0];
        for (int i = 1; i < 256; i++)
            res[i] = res[i - 1] + hist[i];
        return res;
    }

    private void equalizer(Bitmap bmp, int[] cumulatedH) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int n = w * h;

        int pixels[] = new int[n];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < w*h; i++) {
            int r = (cumulatedH[Color.red(pixels[i])] * 255 / n);
            pixels[i] = Color.rgb(r, r, r);
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h);
    }
    /*End of histogram equalizer in black and white */


    /* Histogram equalizer for colored pictures */
    private void equalizeColors(Bitmap bmp, int[] cumulatedH) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int n = w * h;

        int pixels[] = new int[n];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < w*h; i++) {
            int r = (cumulatedH[Color.red(pixels[i])] * 255 / n);
            int g = (cumulatedH[Color.green(pixels[i])] * 255 / n);
            int b = (cumulatedH[Color.blue(pixels[i])] * 255 / n);
            pixels[i] = Color.rgb(r, g, b);
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    /* Overexposes a picture */
    private void overexposure(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int[] pixels = new int[w*h];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < w*h; i++) {
            float[] hsv = new float[3];
            Color.RGBToHSV(Color.red(pixels[i]), Color.green(pixels[i]), Color.blue(pixels[i]), hsv);
            hsv[2] *= 1.5;
            pixels[i] = Color.HSVToColor(hsv);
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h);
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
                Toast.makeText(MainActivity.this, "clicked on toGray", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.equalColors:
                equalizeColors(bmp,cumulatedHist(histogram(bmp)));
                iv.setImageBitmap(bmp);
                Toast.makeText(MainActivity.this, "clicked on equalColors", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.overexp:
                overexposure(bmp);
                iv.setImageBitmap(bmp);
                Toast.makeText(MainActivity.this, "clicked on overexposure", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
    
}
