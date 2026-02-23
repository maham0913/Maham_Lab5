package com.example.lab5_starter;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    // Firestore references
    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 🔥 Initialize Firestore
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        // Initialize views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // Initialize list + adapter
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // 🔥 Snapshot Listener (real-time updates)
        citiesRef.addSnapshotListener((value, error) -> {

            if (error != null) {
                error.printStackTrace();
                return;
            }

            if (value != null) {

                cityArrayList.clear();

                for (QueryDocumentSnapshot snapshot : value) {

                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    if (name != null && province != null) {
                        cityArrayList.add(new City(name, province));
                    }
                }

                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        // Add city button
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment fragment = new CityDialogFragment();
            fragment.show(getSupportFragmentManager(), "Add City");
        });

        // Click city to edit
        cityListView.setOnItemClickListener((adapterView, view, position, id) -> {
            City city = cityArrayAdapter.getItem(position);
            CityDialogFragment fragment = CityDialogFragment.newInstance(city);
            fragment.show(getSupportFragmentManager(), "City Details");
        });

        cityListView.setOnItemLongClickListener((parent, view, position, id) -> {

            City city = cityArrayAdapter.getItem(position);

            if (city != null) {
                deleteCity(city);
            }

            return true;  // for long click
        });
    }

    @Override
    public void updateCity(City city, String title, String province) {

        city.setName(title);
        city.setProvince(province);

        // Update Firestore
        citiesRef.document(title).set(city);
    }

    @Override
    public void addCity(City city) {

        // Add to Firestore
        citiesRef.document(city.getName()).set(city);
    }

    private void deleteCity(City city) {

        citiesRef.document(city.getName()).delete()
                .addOnSuccessListener(aVoid -> {
                    System.out.println("City successfully deleted");
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }
}