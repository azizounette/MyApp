package com.example.aziza.myapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import static android.graphics.Color.valueOf;

public class MainActivity extends AppCompatActivity {

    private Bitmap originalBmp;
    private Bitmap bmp;
    private Button resetButton;


    /* Used to reset the picture */
    private View.OnClickListener resetButtonListener = new View.OnClickListener() {
            public void onClick(View v) {
                ImageView iv = findViewById(R.id.viewLenna);
                iv.setImageBitmap(originalBmp);
                bmp = originalBmp.copy(originalBmp.getConfig(), true);
            }
    };


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

    /* Linear Extension of Dynamics for contrasts */
    private void linExtension(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int min = 255;
        int max = 0;

        // Finding the minimum and the maximum
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++) {
                int pix = bmp.getPixel(x, y);
                if (Color.red(pix) < min)
                    min = Color.red(pix);
                if (Color.red(pix) > max)
                    max = Color.red(pix);
            }

        // Changing the luminosity of each pixel
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++) {
                int pix = bmp.getPixel(x, y);
                int pixColor = Color.red(pix);
                int c = 255/(max - min) * (pixColor - min);
                int finalColor = Color.rgb(c, c, c);
                bmp.setPixel(x, y, finalColor);
            }

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

    /* Convolution methods*/

    // Calculates gaussian coefficients
    private float gauss (int whatMask, int size) {
        int mu = 0;
        double sigma = 1/(size*Math.sqrt(2*Math.PI));
        return (float) (1/(sigma*Math.sqrt(2*Math.PI))*Math.exp(-(whatMask-mu)*(whatMask-mu)/(2*sigma*sigma)));
    }

    // Sets the coefficients of the matrix used as a mask
    // whatMask: 0 = Second matrix of Contouring  1 = Average Blur    2 = Gaussian Blur    3 = Contouring  4 = Sharpening Contours
    private int[] setMatrixConvo(int whatMask , int size) {
        int[] res = new int[size*size];
        if (whatMask == 3) {
            res[0] = -1;res[1] = -1;res[2] = -1;res[6] = 1;res[7] = 1;res[8] = 1;
        }
        else if ( whatMask == 2) {
            int aoe = (size - 1)/2;
            for (int m = 0; m < size; m++) {
                for (int n = 0; n < size; n++) {
                    res[m + size * n] = (int) gauss((Math.abs(aoe-m)+Math.abs(aoe-n))/(size*size), size);
                }
            }
        }
        else if (whatMask == 0) {
            res[0] = -1;res[2] = 1;res[3] = -1;res[5] = 1;res[6] = -1;res[8] = 1;
        }
        else if (whatMask == 1) {
            for (int i = 0; i < size*size; i++){
                res[i] = 1;
            }
        }
        else if (whatMask == 4) {
            res[1] = -1; res[3] = -1; res[4] = 5; res[5] = -1; res[7] = -1;
        }
        return res;
    }
    
    // Applies the mask to the bitmap
    public void convolution (Bitmap bmp, int whatMask, int area) {
        int outh = bmp.getHeight();
        int outw = bmp.getWidth();
        int[] pixels = new  int [outw*outh];
        int[] pixelsf = new int [outw*outh];
        bmp.getPixels(pixels, 0, outw,  0, 0, outw, outh);
        bmp.getPixels(pixelsf, 0, outw,  0, 0, outw, outh);
        int matrixWidth = 2 * area + 1;
        int[] matrixConvo = setMatrixConvo(whatMask, matrixWidth);
        int matrixLen = matrixConvo.length;
        int[] matrixConvo1 = new int[matrixLen];
        if (whatMask == 3) {
            matrixConvo1 = setMatrixConvo(0, matrixWidth);
            toGray(bmp);
        }
        int[] currentPixel = new int [matrixLen];
        for (int k = area; k < outw - area; k++) {
            for (int l = area; l < outh - area; l++) {
                float param1 = 0;
                float param2 = 0;
                float param3 = 0;
                int weight = 0;
                for (int m = 0; m < matrixWidth; m++) {
                    for (int o = 0; o < matrixWidth; o++) {
                        currentPixel[matrixWidth * m + o] = pixels[k - area + o + (l + m - area) * outw];
                    }
                }
                // Case: Contouring
                if (whatMask == 3) {
                    for (int n = 0; n < 9; n++) {
                        param1 += Color.red(currentPixel[n]) * matrixConvo[n];
                        param2 += Color.red(currentPixel[n]) * matrixConvo1[n];
                    }
                    int norm = (int) Math.min(Math.sqrt(param1 * param1 + param2 * param2), 255);
                    pixelsf[k + l * outw] = Color.rgb( norm, norm, norm);
                }
                // Other whatMask
                else {
                    for (int n = 0; n < matrixLen; n++) {
                        param1 += Color.red(currentPixel[n]) * matrixConvo[n];
                        param2 += Color.green(currentPixel[n]) * matrixConvo[n];
                        param3 += Color.blue(currentPixel[n]) * matrixConvo[n];
                        weight += matrixConvo[n];
                    }
                    pixelsf[k + l * outw] = Color.rgb((int) param1/weight, (int) param2/weight, (int) param3/weight);
                }

            }
        }
        bmp.setPixels(pixelsf, 0, outw,  0, 0, outw, outh);
    }

    /* End of convolution methods */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView iv = findViewById(R.id.viewLenna);
        resetButton = findViewById(R.id.resetButton);
        originalBmp = BitmapFactory.decodeResource(getResources(), R.drawable.lenna);

        // Copy that is used in order to apply changes to the original picture
        bmp = originalBmp.copy(originalBmp.getConfig(), true);
        iv.setImageBitmap(bmp);

        resetButton.setOnClickListener(resetButtonListener);
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
            case R.id.averageBlur:
                convolution(bmp, 1, 3);
                iv.setImageBitmap(bmp);
                Toast.makeText(MainActivity.this, "clicked on average blur", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.gaussBlur:
                convolution(bmp, 2, 3);
                iv.setImageBitmap(bmp);
                Toast.makeText(MainActivity.this, "clicked on gaussian blur", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.contouring:
                convolution(bmp, 3, 3);
                iv.setImageBitmap(bmp);
                Toast.makeText(MainActivity.this, "clicked on contouring", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.sharpenContours:
                convolution(bmp, 4, 3);
                iv.setImageBitmap(bmp);
                Toast.makeText(MainActivity.this, "clicked on sharpen contours", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
    
}
