package com.conlistech.sportsclubbookingengine.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Locale;

public class GetAddress {
    // Get Current Address
    public static List<Address> getAddress(Context ctx,
                                           Double latitude,
                                           Double longitude) {

        List<Address> addresses = null;

        try {
            Geocoder geocoder;

            geocoder = new Geocoder(ctx, Locale.getDefault());

            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return addresses;
    }


}
