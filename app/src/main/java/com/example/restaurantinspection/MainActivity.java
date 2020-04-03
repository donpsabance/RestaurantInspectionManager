package com.example.restaurantinspection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.restaurantinspection.model.DateManager;
import com.example.restaurantinspection.model.InspectionComparator;
import com.example.restaurantinspection.model.QueryPreferences;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantComparator;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String MAIN_ACTIVITY_TAG = "MyActivity";
    private static final int ACTIVITY_RESULT_FINISH = 101;
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();
    private ArrayAdapter<Restaurant> arrayAdapter;

    // Views Used for filtering
    private Spinner hazardSpinner;
    private SearchView restaurantFilter_SearchView;
    private CheckBox favoritesChecboxFilter;
    private EditText filter_maximumHazard_EditText;

    private List<Restaurant> hazardFilter;
    private List<Restaurant> maxViolationFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(this, MapsActivity.class));
        List<String> favourite_list = readFavouriteList();
        compareRestaurant(favourite_list);
        restaurantManager.CreateFullCopy();

        List<Restaurant> hazardFilter = restaurantManager.getFullRestaurantListCopy();
        List<Restaurant> maxViolationFilter = restaurantManager.getFullRestaurantListCopy();

        loadRestaurants();
        registerClickFeedback();
        setUpMapButton();


        //searchRestaurant();
        setUpSearchBar();
        setUpHazardsSpinner();
        setUpMaxCriticalViolationsSearch();
        setUpFavoritesFilter();
    }


    // search bar
    private void setUpSearchBar() {
        restaurantFilter_SearchView = findViewById(R.id.searchmain);
        restaurantFilter_SearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                QueryPreferences.setStoredQuery(MainActivity.this, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                arrayAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    //load restaurants based on hazard level
    private void filterRestaurants(String hazardLevel){

        List<Restaurant> result = new ArrayList<>();
        if(!hazardLevel.equalsIgnoreCase("none")){

            restaurantManager.getRestaurantList().clear();
            for(Restaurant restaurant : restaurantManager.getFullRestaurantListCopy()){

                //most recent inspection
                if(restaurant.getRestaurantInspectionList() != null &&
                        restaurant.getRestaurantInspectionList().size() > 0){
                    if(restaurant.getRestaurantInspectionList().get(0).getHazardRating().equalsIgnoreCase(hazardLevel)){

                        result.add(restaurant);

                    }
                }
            }
        } else if(hazardLevel.equalsIgnoreCase("none")){

            result = restaurantManager.getFullRestaurantListCopy();

        }

        hazardFilter = result;
    }

    private void setUpHazardsSpinner() {
        hazardSpinner = findViewById(R.id.spinnerHazard);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.hazards,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hazardSpinner.setAdapter(adapter);
        hazardSpinner.setOnItemSelectedListener(this);
    }
    // hazardSpinner click listener
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        arrayAdapter.getFilter().filter(restaurantFilter_SearchView.getQuery());
/*        //pos 0 -none, 1 - low, 2 -moderate, 3 - high
        switch (position){
            case 0:
                filterRestaurants("none");
                loadRestaurants();
                break;
            case 1:
                filterRestaurants("low");
                loadRestaurants();
                break;
            case 2:
                filterRestaurants("moderate");
                loadRestaurants();
                break;
            case 3:
                filterRestaurants("high");
                loadRestaurants();
                break;

        }*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void setUpFavoritesFilter() {
        favoritesChecboxFilter = findViewById(R.id.favoritescheckBox);
        favoritesChecboxFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Todo filter adapter
                arrayAdapter.getFilter().filter(restaurantFilter_SearchView.getQuery());

            }
        });
    }



    private void setUpMaxCriticalViolationsSearch() {
        filter_maximumHazard_EditText = findViewById(R.id.editText_maxCritical);
        filter_maximumHazard_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                arrayAdapter.getFilter().filter(restaurantFilter_SearchView.getQuery());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
/*        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                if(!s.toString().trim().equals("")){

                    List<Restaurant> result = new ArrayList<>();
                    int maxViolation = Integer.parseInt(s.toString());
                    for(Restaurant restaurant : restaurantManager.getFullRestaurantListCopy()){

                        if(restaurant.getRestaurantInspectionList()  != null &&
                                restaurant.getRestaurantInspectionList().size() > 0){

                            if(restaurant.getRestaurantInspectionList().get(0).getNumCritical() <= maxViolation){

                                result.add(restaurant);

                            }
                        }
                    }
                    maxViolationFilter = result;
                    loadRestaurants();
                } else {
                    maxViolationFilter = restaurantManager.getFullRestaurantListCopy();
                    loadRestaurants();
                }
            }
        };*/
    }

    private void compareRestaurant(List<String> list){
        for(Restaurant restaurant : restaurantManager.getRestaurantList()) {
            for (String TrackingNum : list) {
                String arr[]=TrackingNum.split("\\+");
                TrackingNum = arr[0];
                if (TrackingNum.equals(restaurant.getTrackingNumber())) {
                    restaurant.setFavourite(true);
                }
            }
        }

    }



    public void loadRestaurants() {

        if(hazardFilter != null & maxViolationFilter != null){

            restaurantManager.getRestaurantList().clear();
            for(Restaurant restaurant : hazardFilter){
                for(Restaurant restaurant1 : maxViolationFilter){

                    if(restaurant.getName().equalsIgnoreCase(restaurant1.getName())){
                        restaurantManager.add(restaurant);
                    }
                }
            }
        }

        arrayAdapter = new CustomListAdapter();
        ListView listView = findViewById(R.id.restaurantListView);
        listView.setAdapter(arrayAdapter);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    private void setUpMapButton() {

        //start MapActivity
        FloatingActionButton fab = findViewById(R.id.mapButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivityForResult(intent, ACTIVITY_RESULT_FINISH);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void determineHazardLevel(ProgressBar progressBar, String hazardLevel, int totalViolations) {

        switch (hazardLevel.toUpperCase()){
            case "LOW":
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(75, 194, 54)));
                break;
            case "MODERATE":
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(245, 158, 66)));
                break;
            case "HIGH":
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(245, 66, 66)));
                break;
        }
        progressBar.setMax(100);
        progressBar.setProgress(5 + 10 * totalViolations);
    }

    private int determineIcon(String restaurantName) {

        if(restaurantName.toLowerCase().contains("a&w")){
            return R.drawable.aw;
        } else if(restaurantName.toLowerCase().contains("burger king")){
            return R.drawable.bk;
        } else if(restaurantName.toLowerCase().contains("blenz")){
            return R.drawable.blenz;
        } else if (restaurantName.toLowerCase().contains("boston pizza")){
            return R.drawable.bp;
        } else if(restaurantName.toLowerCase().contains("dairy queen")){
            return R.drawable.dq;
        } else if (restaurantName.toLowerCase().contains("kfc")){
            return R.drawable.kfc;
        } else if (restaurantName.toLowerCase().contains("mcdonalds")){
            return R.drawable.mcd;
        } else if (restaurantName.toLowerCase().contains("7-eleven")){
            return R.drawable.seven11;
        } else if (restaurantName.toLowerCase().contains("starbucks")){
            return R.drawable.starbucks;
        } else if (restaurantName.toLowerCase().contains("subway")){
            return R.drawable.starbucks;
        } else if (restaurantName.toLowerCase().contains("tim hortons")){
            return R.drawable.timmys;
        } else if (restaurantName.toLowerCase().contains("pizza")) {
            return R.drawable.pizza;
        } else if (restaurantName.toLowerCase().contains("burger")){
            return R.drawable.burger;
        } else if (restaurantName.toLowerCase().contains("sushi")) {
            return R.drawable.sushi;
        } else if (restaurantName.toLowerCase().contains("sandwich")) {
            return R.drawable.sandwich;
        } else if (restaurantName.toLowerCase().contains("coffee")) {
            return R.drawable.coffee;
        } else if (restaurantName.toLowerCase().contains("chicken")) {
            return R.drawable.chicken;
        } else if (restaurantName.toLowerCase().contains("seafood")) {
            return R.drawable.lobster;
        } else if (restaurantName.toLowerCase().contains("taco")) {
            return R.drawable.taco;
        } else if (restaurantName.toLowerCase().contains("noodles") ||
                restaurantName.toLowerCase().contains("ramen") ||
                restaurantName.toLowerCase().contains("pho")) {
            return R.drawable.noodles;
        }

        //default
        return R.drawable.food;
    }

    private void registerClickFeedback() {

        ListView listView = findViewById(R.id.restaurantListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Restaurant restaurant = restaurantManager.getRestaurantList().get(position);
                Toast.makeText(MainActivity.this, "You clicked " + restaurant.getName(), Toast.LENGTH_SHORT).show();

                //start restaurant activity
                Intent intent = RestaurantActivity.makeIntent(MainActivity.this, position);
                startActivity(intent);
            }
        });
    }


    private class CustomListAdapter extends ArrayAdapter<Restaurant> implements Filterable {
        private List<Restaurant> exampleList;

        public CustomListAdapter() {
            super(MainActivity.this, R.layout.restaurantlistlayout, restaurantManager.getRestaurantList());
            this.exampleList = restaurantManager.getRestaurantList();
        }


        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            View itemView = view;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.restaurantlistlayout, viewGroup, false);
            }

            Restaurant restaurant = restaurantManager.getRestaurantList().get(position);

            ImageView imageView = itemView.findViewById(R.id.restaurantIcon);
            TextView addressText = itemView.findViewById(R.id.restaurantLocation);
            TextView descriptionText = itemView.findViewById(R.id.restaurantDescription);
            TextView reportText = itemView.findViewById(R.id.restaurantRecentReport);
            ProgressBar hazardRating = itemView.findViewById(R.id.hazardRatingBar);
            ImageView star = itemView.findViewById(R.id.favorited);

            imageView.setImageResource(R.drawable.food);
            addressText.setText(restaurant.getAddress());
            descriptionText.setText(restaurant.getName());
            //make sure they have an inspection report available

            if (restaurant.getRestaurantInspectionList().size() > 0) {

                RestaurantInspection restaurantInspection = restaurant.getRestaurantInspectionList().get(0);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                Date inspectionDate = null;

                try {
                    inspectionDate = simpleDateFormat.parse(restaurantInspection.getInspectionDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int issuesFound = restaurantInspection.getNumCritical() + restaurantInspection.getNumNonCritical();

                determineHazardLevel(hazardRating, restaurantInspection.getHazardRating(), issuesFound);

                String formattedInspectionDate = DateManager.formatDateInspection(inspectionDate);
                String reportMsg = "Most Recent Report: " + formattedInspectionDate + "\n";
                reportMsg += issuesFound + " issues found";

                int icon = determineIcon(restaurant.getName());

                imageView.setImageResource(icon);
                descriptionText.setText(restaurant.getName());
                reportText.setText(reportMsg);
                if(restaurant.getFavourite()){
                    star.setVisibility(View.VISIBLE);
                }else {
                    star.setVisibility(View.INVISIBLE);
                }


            } else {

                reportText.setText(R.string.noavailablereports);
                hazardRating.setProgress(5);
            }
            return itemView;
        }


        @NonNull
        @Override
        public Filter getFilter() {
            return exampleFilter;
        }
        private Filter exampleFilter = new Filter() {
            @Override
            // string match filter
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Restaurant> filteredList = new ArrayList<>();
                if(constraint == null || constraint.length() == 0){
                    filteredList.addAll(restaurantManager.getFullRestaurantListCopy());
                } else {
                    String filteredPattern = constraint.toString().toLowerCase().trim();
                    for (Restaurant restaurants : restaurantManager.getFullRestaurantListCopy()){
                        if (restaurants.getName().toLowerCase().contains(filteredPattern)){
                            filteredList.add(restaurants);
                        }
                    }
                }
                // THE FAVORITE CHECK BOX FILTER
                Iterator<Restaurant> iterator;
                if(favoritesChecboxFilter.isChecked()){
                    for(iterator = filteredList.iterator(); iterator.hasNext();){
                        Restaurant restaurant = iterator.next();
                        if(!restaurant.getFavourite()){
                            iterator.remove();
                        }
                    }
                }

                // THE SPINNER FILTER
                String selectedHazardLevel = hazardSpinner.getSelectedItem().toString();
                if(!selectedHazardLevel.equalsIgnoreCase("none")){
                    for (iterator = filteredList.iterator(); iterator.hasNext();){
                        Restaurant restaurant = iterator.next();
                        List<RestaurantInspection> inspectionList = restaurant.getRestaurantInspectionList();
                        if(inspectionList.size() > 0 ){
                            if((! inspectionList.get(0).getHazardRating().toLowerCase().equalsIgnoreCase(selectedHazardLevel))){
                                iterator.remove();
                            }
                        } else {
                            iterator.remove();
                        }
                    }
                }

                // THE MAX CRITICAL VIOLATIONS IN A YEAR FILTER
                if(filter_maximumHazard_EditText.getText().toString().length() != 0){
                    int maxConstraint = Integer.parseInt(filter_maximumHazard_EditText.getText().toString());
                    int count = 0;
                    for(iterator = filteredList.iterator(); iterator.hasNext();){
                        Restaurant restaurant = iterator.next();
                        count = restaurant.getTotalViolationsWithinYear();
                        if(count > maxConstraint){
                            iterator.remove();
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                exampleList.clear();
                exampleList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.search_menu);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                arrayAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    public List<String> readFavouriteList() {
        List<String> list = new ArrayList<>();
        SharedPreferences sp1 = getSharedPreferences("favourite_list", Context.MODE_PRIVATE);
        String favourite_jsonStr = sp1.getString("Favourite_list","");
        if(!favourite_jsonStr.equals("")){
            Gson gson = new Gson();
            list = gson.fromJson(favourite_jsonStr,new TypeToken<List<String>>(){}.getType());
        }
        return list;
    }


    public static Intent makeIntent(Context context){
        return new Intent(context,MainActivity.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        arrayAdapter.notifyDataSetChanged();
    }
}
