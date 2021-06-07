package com.adriann.opscmaps2;

public class Conversions {

    double kmsTD = 1.609;
    double miles;

    public double convertToKm()
    {
        double convertedDistKm;

        convertedDistKm = miles * kmsTD;
        return convertedDistKm;
    }

    public double convertToMiles()
    {
        double convertedDistMiles, km=0;

        convertedDistMiles = km/kmsTD;
         return convertedDistMiles;
    }
}
