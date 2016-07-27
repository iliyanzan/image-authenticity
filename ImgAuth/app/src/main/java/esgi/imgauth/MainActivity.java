package esgi.imgauth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    /**
     * Distance between dots in small mesh in pixels.
     */
    private static int SMALL_MESH_GAP_SIZE = 32;

    /**
     * Distance between dots in big mesh in pixels.
     */
    private static int BIG_MESH_GAP_SIZE = 128;

    /**
     * Generate mask for watermark bits,
     *
     * @param width Image width.
     * @param height Image height.
     *
     * @return Mask for the bits.
     */
    private int[] watermarkBitsMaskGeneration(int width, int height) {
        int mask[] = new int[width*height];

        //TODO Check which is column and which is row.
        for(int j=0, k=0; j<height; j++) {
            for(int i=0; i<width; i++, k++) {
                mask[k] = 0xFFFFFF;

                if(i%SMALL_MESH_GAP_SIZE==0 && j%SMALL_MESH_GAP_SIZE==0) {
                    mask[k] &= 0xFEFEFE;
                } else if(i%BIG_MESH_GAP_SIZE==0 && j%BIG_MESH_GAP_SIZE==0) {
                    mask[k] &= 0xEFEFEF;
                }
            }
        }

        return mask;
    }

    /**
     * Put zeros in each bit which will be used during watarmarking process.
     *
     * @param pixels Array with RGB image pixels.
     * @param width Width of the image.
     * @param height Height of the image.
     */
    private void zeroWatarmarkBits(int pixels[], int width, int height) {
        if(pixels==null || pixels.length != width*height) {
            //TODO Exception handling.
        }

        int[] mask = watermarkBitsMaskGeneration(width, height);

        for(int k=0; k<mask.length && k<pixels.length; k++) {
            pixels[k] &= mask[k];
        }
    }


    /**
     * Singal to noise ratio calculation.
     *
     * @param original The origial image as RGB pixesl array.
     * @param watermarked The watermarked image as RGB pixels array.
     *
     * @return Three values SNR for each channel (red, green, blue).
     */
    private double[] calculateSignalToNoiceRatio(int original[], int watermarked[]) {
        if(original==null || watermarked==null || original.length!=watermarked.length) {
            //TODO Rise an exception.
        }

        double rgb[] = {0, 0, 0};

        int value;
        for(int k=0; k<original.length && k<watermarked.length; k++) {
            /*
             * Red channel.
             */
            value = (original[k]>>16)&0xFF - (watermarked[k]>>16)&0xFF;
            rgb[0] += value*value;

            /*
             * Green channel.
             */
            value = (original[k]>>8)&0xFF - (watermarked[k]>>8)&0xFF;
            rgb[1] += value*value;

            /*
             * Blue channel.
             */
            value = (original[k]>>0)&0xFF - (watermarked[k]>>0)&0xFF;
            rgb[2] += value*value;
        }

        /*
         * Normalize sum as mean square error.
         */
        rgb[0] /= (double)original.length;
        rgb[1] /= (double)original.length;
        rgb[2] /= (double)original.length;

        final int MAX_INTENSITY_VALUE = 255;

        /*
         * Logarithm transform in order to get dB.
         */
        rgb[0] = 10D * Math.log10(MAX_INTENSITY_VALUE*MAX_INTENSITY_VALUE / rgb[0]);
        rgb[1] = 10D * Math.log10(MAX_INTENSITY_VALUE*MAX_INTENSITY_VALUE / rgb[1]);
        rgb[2] = 10D * Math.log10(MAX_INTENSITY_VALUE*MAX_INTENSITY_VALUE / rgb[2]);

        return rgb;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void takePhoto(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ((ImageView)findViewById(R.id.imageView)).setImageBitmap(bitmap);

            //TODO Do all time consuming calculations in separate thread.

            /*
             * Image information as array of RGB pixels.
             */
            int pixels[] = new int[bitmap.getWidth()*bitmap.getHeight()];

            /*
             * Obtain image pixels as bytes array.
             */
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            /*
             * Put zeros all bits which will be used in the watermarking process.
             */
            zeroWatarmarkBits(pixels, bitmap.getWidth(), bitmap.getHeight());

            //TODO RSA digital stamp generation.

            //TODO CRC codes generation.

            //TODO Watermarking with digital stamp and CRC codes.

            //TODO Meta data createion.

            //TODO SNR calculation.

            //TODO Save bitmap image file.
        }
    }
}
