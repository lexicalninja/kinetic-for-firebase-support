package com.kinetic.fit.data.realm_objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kinetic.fit.util.RealmUtils;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 6/16/17.
 */

public class Subscription extends RealmObject {

    @PrimaryKey
    String transactionId;
    String type;
    long expiration;
    boolean trialing;
    boolean valid;

    public Subscription() {
    }

    public Subscription(JsonObject o) {
        if (isValid(o)) {
            this.transactionId = o.get("transactionId").getAsString();
            this.type = o.get("subscription").getAsString();
            this.expiration = RealmUtils.deserializeDateFromJson(o.getAsJsonObject("expiration"));
            this.trialing = o.get("trialing").getAsBoolean();
            valid = true;
        } else {
            this.transactionId = UUID.randomUUID().toString();
            this.expiration = new DateTime().getMillis();
            this.type = null;
            this.trialing = false;
            valid = false;
        }
    }

    private boolean isValid(JsonObject object) {
        return object.get("transactionId") != null && object.get("subscription") != null && object.get("expiration") != null && object.get("trialing") != null;
    }

    public boolean isCancelled(){
        DateTime dt = DateTime.now();
        return this.expiration < dt.getMillis();
    }
}
