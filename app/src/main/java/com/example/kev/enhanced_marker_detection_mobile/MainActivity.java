package com.example.kev.enhanced_marker_detection_mobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Bitmap marker_image;
    ImageView photo_preview;
    RelativeLayout first_screen, second_screen;
    Button take_photo_button;
    PatternDetector pattern_detector = new PatternDetector();

    ArrayList<ImageView> pattern_viz = new ArrayList<ImageView>();

    static final int IMAGE_SIZE = 200;
    static final int TAKE_PHOTO_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        pattern_viz.add((ImageView) findViewById(R.id.p1));
        pattern_viz.add((ImageView) findViewById(R.id.p2));
        pattern_viz.add((ImageView) findViewById(R.id.p3));
        pattern_viz.add((ImageView) findViewById(R.id.p4));
        pattern_viz.add((ImageView) findViewById(R.id.p5));
        pattern_viz.add((ImageView) findViewById(R.id.p6));
        pattern_viz.add((ImageView) findViewById(R.id.p7));
        pattern_viz.add((ImageView) findViewById(R.id.p8));
        pattern_viz.add((ImageView) findViewById(R.id.p9));

        first_screen = (RelativeLayout) findViewById(R.id.first_screen);
        second_screen = (RelativeLayout) findViewById(R.id.second_screen);
        take_photo_button = (Button) findViewById(R.id.take_photo);
        photo_preview = (ImageView) findViewById(R.id.photo_preview);

        take_photo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //first_screen.setVisibility(View.INVISIBLE);
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, TAKE_PHOTO_CODE);
                }
            }
        });
    }

    public static Bitmap scale_down(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            Uri tmp_media_uri;

            switch (requestCode) {
                case TAKE_PHOTO_CODE:
                    tmp_media_uri = data.getData();
                    try {
                        marker_image = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), tmp_media_uri);
                    } catch (IOException e) {}

                    marker_image = scale_down(marker_image, IMAGE_SIZE, false);

                    ArrayList<ArrayList<String>> pattern = pattern_detector.detect_pattern(marker_image);
                    int counter = 0;

                    for (ArrayList<String> x : pattern) {
                        for (String y : x) {
                            if (y.equals("#")) {
                                pattern_viz.get(counter).setImageDrawable(new ColorDrawable(Color.BLACK));
                            }
                            else {
                                pattern_viz.get(counter).setImageDrawable(new ColorDrawable(Color.BLUE));
                            }
                            counter += 1;
                        }
                    }

                    photo_preview.setImageBitmap(marker_image);
                    //second_screen.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
