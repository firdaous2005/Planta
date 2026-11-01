package com.example.planta;

public class PlantItem {
    private final int id;
    private final String name;
    private final String imagePath;
    private final String dateAdded;

    public PlantItem(int id, String name, String imagePath, String dateAdded) {
        this.id = id;
        this.name = name;
        this.imagePath = imagePath;
        this.dateAdded = dateAdded;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getImagePath() { return imagePath; }
    public String getDateAdded() { return dateAdded; }
}