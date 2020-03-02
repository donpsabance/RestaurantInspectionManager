package com.example.restaurantinspection;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantManager;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private RestaurantManager restaurantManager = RestaurantManager.getInstance();

    private class CustomListAdapter extends ArrayAdapter<Restaurant>{
        public CustomListAdapter(){
            super(MainActivity.this, R.layout.restaurantlistlayout, restaurantManager.getRestaurantList());
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup){

            View itemView = view;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.restaurantlistlayout, viewGroup, false);
            }

            Restaurant restaurant = restaurantManager.getRestaurantList().get(position);

            ImageView imageView = findViewById(R.id.restaurantIcon);
            imageView.setImageResource(R.drawable.icon);

            TextView textView = findViewById(R.id.restaurantDescription);
            textView.setText(restaurant.toString());

            return itemView;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //temp
        ArrayAdapter<Restaurant> adapter = new CustomListAdapter();
        ListView listView = findViewById(R.id.restaurantListView);

        listView.setAdapter(adapter);

    }

    public void loadRestaurants(){


    }


}
