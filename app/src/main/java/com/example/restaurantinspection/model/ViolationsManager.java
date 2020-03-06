package com.example.restaurantinspection.model;

import androidx.annotation.NonNull;

import com.example.restaurantinspection.model.Violation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ViolationsManager implements Iterable<Violation> {
    private List<Violation> violationList = new ArrayList<>();

    public void add(Violation violation){
        violationList.add(violation);
    }

    public List<Violation> getViolationList() {
        return violationList;
    }

    @NonNull
    @Override
    public Iterator<Violation> iterator() {
        return violationList.iterator();
    }
}
