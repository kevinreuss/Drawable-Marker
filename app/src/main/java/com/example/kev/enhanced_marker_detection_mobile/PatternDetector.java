package com.example.kev.enhanced_marker_detection_mobile;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kev on 09.05.2017.
 */

public class PatternDetector {


    double COLOR_TRESHOLD = 300.0;
    double PERCENTAGE_TRESHOLD = 0.4;
    double PERCENTAGE_TRESHOLD_CALCULATED = 0.2;

    public PatternDetector() {}

    int min(List<Pair<Integer, Integer>> my_list, int max_value) {
        int value = max_value;
        for (Pair<Integer, Integer> i : my_list) {
            if (i.first <= value) value = i.first;
        }
        return value;
    }

    int max(List<Pair<Integer, Integer>> my_list, int min_value) {
        int value = min_value;
        for (Pair<Integer, Integer> i : my_list) {
            if (i.first >= value) value = i.first;
        }
        return value;
    }

    double mean(List<Pair<Integer, Integer>> my_list) {
        int value_sum = 0;
        for (Pair<Integer, Integer> i : my_list) {
            value_sum += i.second;
        }
        return (double) value_sum / (double) my_list.size();
    }

    double variance(List<Pair<Integer, Integer>> my_list, double mean) {
        double variance = 0;
        for (Pair<Integer, Integer> i : my_list) {
            variance += Math.pow(((float) (i.second) - mean), 2);
            variance /= (double) my_list.size();
        }
        return variance;
    }

    int sum(int color) {
        return Color.red(color) + Color.green(color) + Color.blue(color);
    }

    List<Pair<Integer, Integer>> filter(List<Pair<Integer, Integer>> my_list) {
        double standard_deviation = Math.sqrt(variance(my_list, mean(my_list)));
        double counter = 0;
        for (Pair<Integer, Integer> i : my_list) {
            if (Math.abs(i.second - standard_deviation) > standard_deviation) {
                my_list.remove(counter);
                counter += 1;
            }
        }
        return my_list;
    }


    public ArrayList<ArrayList<String>> detect_pattern(Bitmap input_image) {




        int height = input_image.getHeight();
        int width = input_image.getWidth();

        // DETERMINE MIN & MAX VALUES //
        float min_value = 3 * 255;
        float max_value = 0;

        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                int value = sum(input_image.getPixel(column, row));
                if (value <= min_value) min_value = value;
                if (value >= max_value) max_value = value;
            }
        }

        COLOR_TRESHOLD = min_value + ((max_value - min_value) / 2);



        // CREATE FILTERED IMAGE //
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                int value = sum(input_image.getPixel(column, row));
                if (value <= COLOR_TRESHOLD) input_image.setPixel(column, row, Color.rgb(0, 0, 0));
                else input_image.setPixel(column, row, Color.rgb(255, 255, 255));
            }
        }



        // DETECT RECTANGLE //
        List<Pair<Integer, Integer>> from_left = new ArrayList<Pair<Integer, Integer>>();
        List<Pair<Integer, Integer>> from_right = new ArrayList<Pair<Integer, Integer>>();

        // FROM LEFT //
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                if (sum(input_image.getPixel(column, row)) == 0) {
                    from_left.add(new Pair<Integer, Integer>(row, column));
                    break;
                }
            }
        }



        from_left = filter(from_left);
        int x_coord = (int) mean(from_left);
        int y_start = min(from_left, height);
        int y_end = max(from_left, 0);




        // FROM RIGHT //
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                if (sum(input_image.getPixel(width - column - 1, row)) == 0){
                    from_right.add(new Pair<Integer, Integer>(row, width - column));
                    break;
                }
            }
        }


        from_right = filter(from_right);
        int x_coord_two = (int) mean(from_right);
        int y_start_two = (int) min(from_right, height);
        int y_end_two = (int) max(from_right, 0);

        y_start = (y_start + y_start_two) / 2;
        y_end = (y_end + y_end_two) / 2;





        int grid_width = (int)(2 + (x_coord_two - x_coord) / 3);
        int grid_height = (int)((y_end - y_start) / 3);



        // DETECT PATTERN //
        ArrayList<Integer> x_fields = new ArrayList<Integer>();
        x_fields.add(x_coord);
        x_fields.add(x_coord + grid_width);
        x_fields.add(x_coord + 2 * grid_width);
        x_fields.add(x_coord_two);

        ArrayList<Integer> y_fields = new ArrayList<Integer>();
        y_fields.add(y_start);
        y_fields.add(y_start + grid_height);
        y_fields.add(y_start + 2 * grid_height);
        y_fields.add(y_end);


        ArrayList<ArrayList<Double>> fields_percentage = new ArrayList<ArrayList<Double>>();

        for (int y = 0; y < y_fields.size() - 1; y++) {
            ArrayList<Double> tmp_percentage = new ArrayList<Double>();
            for (int x = 0; x < x_fields.size() - 1; x++) {
                int x_start = x_fields.get(x);
                int y_start_t = y_fields.get(y);
                int x_end = x_fields.get(x + 1);
                int y_end_t = y_fields.get(y + 1);

                int value = 0;
                for (int x_c = x_start; x_c < x_end; x_c++) {
                    for (int y_c = y_start_t; y_c < y_end_t; y_c++) {
                        if (sum(input_image.getPixel(x_c, y_c)) == 0) value += 1;
                    }
                }
                int amount = (x_end - x_start) * (y_end_t - y_start_t);
                double percentage = (double) value / (double) amount;
                tmp_percentage.add(percentage);
            }
            fields_percentage.add(tmp_percentage);
        }

        ArrayList<ArrayList<String>> fields = new ArrayList<ArrayList<String>>();

        for (ArrayList<Double> i : fields_percentage) {
            ArrayList<String> tmp = new ArrayList<String>();
            for (double j : i) {
                if (j >= PERCENTAGE_TRESHOLD) tmp.add("#");
                else tmp.add(" ");
            }
            fields.add(tmp);
        }

        // RETURN DETECTED MARKER //
        return fields;

    }
















}















// PRINT RECTANGLE //
            /*
            for (int row = y_start - 1; row < y_end + 1; row++) {
                input_image.putpixel((x_coord, row), (255, 0, 0))
                for row in range(y_start - 1, y_end + 1):
            input_image.putpixel((x_coord_two, row), (255, 0, 0))

            for column in range(x_coord, x_coord_two):
            input_image.putpixel((column, y_start - 1), (255, 0, 0))
            for column in range(x_coord, x_coord_two):
            input_image.putpixel((column, y_end), (255, 0, 0))
            */
// CALCULATE GRID  //







// PRINT GRID //
            /*
            for row in range(y_start - 1, y_end + 1):
            input_image.putpixel((x_coord + grid_height, row), (255, 0, 0))
            input_image.putpixel((x_coord + 2 * grid_height, row), (255, 0, 0))
            for column in range(x_coord, x_coord_two):
            input_image.putpixel((column, y_start + grid_width), (255, 0, 0))
            input_image.putpixel((column, y_start + 2 * grid_width), (255, 0, 0))

            input_image.save("new_2.bmp")
            */