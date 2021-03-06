package com.example.voyage.travelcompanionapp.model;



import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.voyage.api.api.data.TheoricPlace;
import com.example.voyage.api.api.data.TheoricUser;
import com.example.voyage.api.api.ia.IAManager;
import com.example.voyage.api.common.type.TypeSafeMemory;
import com.example.voyage.api.common.type.TypeConfiguration;
import com.example.voyage.api.common.type.PlaceType;
import com.example.voyage.api.tools.math.CoordinatesDouble;
import com.example.voyage.api.tools.math.compare.CompareUnitDouble;
import com.example.voyage.api.tools.parse.StringParser;
import com.example.voyage.travelcompanionapp.callwebservice.RecupMonument;
import com.example.voyage.travelcompanionapp.conversionapi.TheoricPlaceConvertionApli;
import com.example.voyage.travelcompanionapp.conversionapi.TheoricUserConvertionApli;

public class ApliUser {

    public ApliUser(){}
    private String username;
    private String email;
    private String id;
    private String pass;


    private CoordinatesDouble position;
    private HashMap<PlaceType, Double> preferences = new HashMap<PlaceType, Double>();
    private ArrayList<ApliUser> friends = new ArrayList<ApliUser>();

    public ApliUser(int id, String userName, HashMap<PlaceType, Double> pref) {
        this.id = ""+id;
        this.username = userName;
        this.preferences =pref;
    }

    public void setPosition(CoordinatesDouble position) {
        this.position = position;
    }

    public void setPreferences(HashMap<PlaceType, Double> preferences) {
        this.preferences = preferences;
    }
    public ApliUser(String username, String pass){
        this.username=username;
        this.pass=pass;
    }

    public void setFriends(ArrayList<ApliUser> friends) {
        this.friends = friends;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void setId(String id) {
        this.id = id;
    }

    public ApliUser(String id, CoordinatesDouble position) {
        this.id = id;
        this.position = position;

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public ApliUser(String id, CoordinatesDouble position, HashMap<PlaceType, Double> preferences) {
        TypeConfiguration.getConfig();
        this.id = id;
        this.position = position;
        this.preferences = preferences;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public ApliUser(String id, CoordinatesDouble position, String pref) {
        TypeConfiguration.getConfig();
        this.id = id;
        this.position = position;
        ArrayList<String> lines = StringParser.sliceLine(pref,',');
        for(int i = 0; i < lines.size();i++)
            preferences.put(TypeConfiguration.get(i),Double.parseDouble(lines.get(i)));
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setPreferences(String pref) {
        TypeConfiguration.getConfig(new TypeSafeMemory());
        ArrayList<String> lines = StringParser.sliceLine(pref,',');
        for(int i = 0; i < lines.size();i++)preferences.put(TypeConfiguration.get(i),Double.parseDouble(lines.get(i)));

    }

    public String getId() {
        return id;
    }

    public CoordinatesDouble getPosition() {
        return position;
    }

    public HashMap<PlaceType, Double> getPreferences() {
        return preferences;
    }

    public ArrayList<ApliUser> getFriends() {
        return friends;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public  static   ArrayList<ApliMonument> requestSuggest(ApliUser apliUser, ArrayList<ApliMonument> monuments){
        TypeConfiguration.getConfig(new TypeSafeMemory());
        TheoricUserConvertionApli userConvertionApli = new TheoricUserConvertionApli();
        TheoricPlaceConvertionApli placeConvertionApli = new TheoricPlaceConvertionApli();

        TheoricUser theoricUser = userConvertionApli.convertFrom(apliUser);
        ArrayList<TheoricPlace> places = new ArrayList<TheoricPlace>();
        for(ApliMonument apliMonument : monuments)
            places.add(placeConvertionApli.convertFrom(apliMonument));

        ArrayList<CompareUnitDouble<TheoricPlace>> result = IAManager.choosePlaces(theoricUser, places);
        ArrayList<ApliMonument> apliMonuments = new ArrayList<ApliMonument>();

        for(CompareUnitDouble<TheoricPlace> cud : result)
            apliMonuments.add(placeConvertionApli.convertTo(cud.getElement()));
        for(ApliMonument monument : apliMonuments) {
            monument.setDescription(RecupMonument.monumentHashMap.get(monument.getId()).getDescription());
            monument.setGeoloc(RecupMonument.monumentHashMap.get(monument.getId()).getGeoloc());
        }
        return apliMonuments;
    }

}

