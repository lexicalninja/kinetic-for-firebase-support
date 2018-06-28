package com.kinetic.fit.data.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Created by Saxton on 5/8/17.
 */

public class ShippingAddress {
    JsonObject address;

    public ShippingAddress( ) {
        this.address = new JsonObject();
    }

    public void setAddress1(String address1){
        this.address.addProperty("line1", address1);
    }

    public void setAddress2(String address2){
        this.address.addProperty("line2", address2);
    }

    public void setState(String state){
        this.address.addProperty("state", state);
    }

    public void setCity(String city){
        this.address.addProperty("city", city);
    }

    public void setPostal(String postal){
        this.address.addProperty("postal_code", postal);
    }

    public void setCountry(String country){
        this.address.addProperty("country", country);
    }

    public boolean isValid(){
        return !(address.get("line1").isJsonNull() ||
                address.get("city").isJsonNull() ||
                address.get("state").isJsonNull() ||
                address.get("postal_code").isJsonNull() ||
                address.get("country").isJsonNull()
                );
    }

    public JsonObject getShippingAddress(){
        return this.address;
    }





}
