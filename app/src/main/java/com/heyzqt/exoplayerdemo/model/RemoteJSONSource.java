package com.heyzqt.exoplayerdemo.model;

import android.support.v4.media.MediaMetadataCompat;

import com.heyzqt.exoplayerdemo.utils.LogHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 从服务器获取音乐数据类
 * Created by heyzqt on 2019-08-18.
 */
public class RemoteJSONSource implements MusicProviderSource {

    private static final String TAG = LogHelper.makeLogTag(RemoteJSONSource.class);

    protected static final String CATALOG_URL = "http://storage.googleapis.com/automotive-media/music.json";

    private static final String JSON_MUSIC = "music";
    private static final String JSON_TITLE = "title";
    private static final String JSON_ALBUM = "album";
    private static final String JSON_ARTIST = "artist";
    private static final String JSON_GENRE = "genre";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_IMAGE = "image";
    private static final String JSON_TRACK_NUMBER = "trackNumber";
    private static final String JSON_TOTAL_TRACK_COUNT = "totalTrackCount";
    private static final String JSON_DURATION = "duration";

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        try {
            int slashPos = CATALOG_URL.lastIndexOf('/');
            String path = CATALOG_URL.substring(0, slashPos + 1);

            LogHelper.e(TAG, "path = " + path);

            JSONObject jsonObject = fetchJSONFromUrl(CATALOG_URL);
            ArrayList<MediaMetadataCompat> tracks = new ArrayList<>();
            if (jsonObject != null) {
                JSONArray jsonArray = jsonObject.getJSONArray(JSON_MUSIC);
                if (jsonArray != null) {
                    for (int j = 0; j < jsonArray.length(); j++) {
                        tracks.add(buildFromJSON(jsonArray.getJSONObject(j), path));
                    }
                }
            }
            return tracks.iterator();
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not retrieve music list", e);
        }
    }

    private MediaMetadataCompat buildFromJSON(JSONObject json, String basePath) throws JSONException {
        String title = json.getString(JSON_TITLE);
        String album = json.getString(JSON_ALBUM);
        String artist = json.getString(JSON_ARTIST);
        String genre = json.getString(JSON_GENRE);
        String source = json.getString(JSON_SOURCE);
        String iconUrl = json.getString(JSON_IMAGE);
        int trackNumber = json.getInt(JSON_TRACK_NUMBER);
        int totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
        int duration = json.getInt(JSON_DURATION) * 1000; // ms

        if (!source.startsWith("http")) {
            source = basePath + source;
        }

        if (!iconUrl.startsWith("http")) {
            iconUrl = basePath + iconUrl;
        }

        //正常情况下，id应该从服务器获取
        String id = String.valueOf(source.hashCode());

        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, source).putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();

    }

    /**
     * Download a JSON file from a server, parse the content and return the JSON
     * object.
     *
     * @return result JSONObject containing the parsed representation.
     */
    private JSONObject fetchJSONFromUrl(String urlString) throws JSONException {
        BufferedReader reader = null;
        try {
            URLConnection urlConnection = new URL(urlString).openConnection();
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return new JSONObject(sb.toString());
        } catch (JSONException e) {
            throw e;
        } catch (Exception e) {
            LogHelper.e(TAG, "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
