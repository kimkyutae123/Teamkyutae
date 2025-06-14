package com.example.project_1;

// 검색 추천 데이터 클래스
public class SearchSuggestion
{
    private String name;
    private String address;
    private double latitude;
    private double longitude;

    public SearchSuggestion(String name, String address, double latitude, double longitude)
    {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName()
    {
        return name;
    }

    public String getAddress()
    {
        return address;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }
} 