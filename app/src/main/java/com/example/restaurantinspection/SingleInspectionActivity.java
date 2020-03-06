package com.example.restaurantinspection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;

public class SingleInspectionActivity extends AppCompatActivity {

    public static final String RESTAURANT_POS_TAG = "RESTAURANT POS TAG";
    public static final String INSPECTION_POS_TAG = "INSPECTION POS TAG";
    private RestaurantManager manager = RestaurantManager.getInstance();
    private RestaurantInspection inspection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_inspection);
        find_Data_to_Use();
    }

    private void find_Data_to_Use() {
        Intent intent = getIntent();
        int restaurant_pos = intent.getIntExtra(RESTAURANT_POS_TAG,0);
        int inspection_pos = intent.getIntExtra(INSPECTION_POS_TAG, 0);

        inspection = manager.getRestaurantList().get(restaurant_pos).getInspectionManager()
                            .getInspectionList().get(inspection_pos);
    }

    public static Intent makeIntent(Context c){
        return new Intent(c, SingleInspectionActivity.class);
    }

}
