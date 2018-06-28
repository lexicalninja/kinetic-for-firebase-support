package com.kinetic.fit.data.realm_objects;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 2/3/17.
 */

public class Tag extends RealmObject {


    @PrimaryKey
    private String name;

    private RealmList<Workout> workouts;

    private RealmList<Category> categories;

    public Tag() {
    }

    public Tag(String name) {
        this.name = name;
        workouts = new RealmList<>();
        categories = new RealmList<>();
    }

    public String getName() {
        return name;
    }

    public RealmList<Workout> getWorkouts() {
        return workouts;
    }

    public void addWorkout(Workout workout) {
        workouts.add(workout);
    }

    public RealmList<Category> getCategories() {
        return categories;
    }

    public void addCategory(Category category) {
        categories.add(category);
    }
}
