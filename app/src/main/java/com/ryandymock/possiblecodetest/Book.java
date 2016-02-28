package com.ryandymock.possiblecodetest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ryand on 2/27/2016.
 */
public class Book {
    private String title;
    private String imageURL;
    private String author;

    public String getAuthor() {
        return author;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getTitle() {
        return title;
    }

    public Book(JSONObject bookData){


        try {
            this.title = bookData.getString("title");
        }
        catch (JSONException e){
            this.title = "";
        }

        try {
            this.imageURL = bookData.getString("imageURL");
        }
        catch (JSONException e){
            this.imageURL = "";
        }

        try {
            this.author = bookData.getString("author");
        }
        catch (JSONException e){
            this.author = "";
        }
    }
}
