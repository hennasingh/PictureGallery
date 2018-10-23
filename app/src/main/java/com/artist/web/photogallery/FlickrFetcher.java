package com.artist.web.photogallery;

import android.net.Uri;
import android.util.Log;

import com.artist.web.photogallery.model.BaseResult;
import com.artist.web.photogallery.model.Photo;
import com.artist.web.photogallery.model.Photos;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetcher {

    private static final String TAG = FlickrFetcher.class.getSimpleName();

    public byte[] getUrlBytes(String urlSpec) throws IOException {

        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems(int offset) {

        List<GalleryItem> galleryItems = new ArrayList<>();
        try {
            String url = Uri.parse(Constants.BASE_URL)
                    .buildUpon()
                    .appendQueryParameter("method", Constants.METHOD)
                    .appendQueryParameter("api_key", Constants.API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .appendQueryParameter("page",String.valueOf(offset))
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            //JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(galleryItems, jsonString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return galleryItems;
    }

    private void parseItems(List<GalleryItem> items, String jsonBody)
            throws JSONException {
        //Old way
//        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
//        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
//        for (int i = 0; i < photoJsonArray.length(); i++) {
//            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
//            GalleryItem item = new GalleryItem();
//            item.setId(photoJsonObject.getString("id"));
//            item.setCaption(photoJsonObject.getString("title"));
//            if (!photoJsonObject.has("url_s")) {
//                continue;
//            }
//            item.setUrl(photoJsonObject.getString("url_s"));
//            items.add(item);
//        }
        //New way using Gson
        Gson gson = new Gson();
        BaseResult baseResult = gson.fromJson(jsonBody,BaseResult.class);
        Photos photos = baseResult.getPhotos();
        List<Photo> photo = photos.getPhoto();
        for(int i=0;i< photo.size();i++){
            GalleryItem item = new GalleryItem();
            item.setId(photo.get(i).getId());
            item.setCaption(photo.get(i).getTitle());
            if(photo.get(i).getUrl()==null){
              continue;
            }
            item.setUrl(photo.get(i).getUrl());
            items.add(item);
        }
    }
}
