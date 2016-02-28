package com.ryandymock.possiblecodetest;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    ListView mListView;
    BookAdapter mAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootView.findViewById(R.id.bookListView);

        mAdapter = new BookAdapter(getActivity(),new ArrayList<Book>());
        mListView.setAdapter(mAdapter);

        FetchBookDataTask task = new FetchBookDataTask();
        task.execute();
        return rootView;
    }

    public class FetchBookDataTask extends AsyncTask<Void, Void, ArrayList<Book>> {

        private final String LOG_TAG = FetchBookDataTask.class.getSimpleName();

        private ArrayList<Book> getBookDataFromJSONString(String bookJSONString)
                throws JSONException {

            ArrayList<Book> resultsList = new ArrayList<>();
            JSONArray array;

            try{
                array = new JSONArray(bookJSONString);
            } catch (JSONException e) {
                Log.d("Rcd","JSON parse error");
                array = new JSONArray("");

            }

            for(int i =0; i < array.length(); i++){
                JSONObject bookData = array.getJSONObject(i);
                resultsList.add(i, new Book(bookData));
             }

            Log.d("RCD","Length of array is " + String.valueOf(resultsList.size()));
            return resultsList;

        }

        @Override
        protected ArrayList<Book> doInBackground(Void... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String bookJsonString = null;

            String format = "json";

            try {

                final String BOOK_BASE_URL = "http://de-coding-test.s3.amazonaws.com/books.json";

                URL url = new URL(BOOK_BASE_URL);


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                bookJsonString = buffer.toString();

                Log.v(LOG_TAG, "Book string: " + bookJsonString);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getBookDataFromJSONString(bookJsonString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Book> result) {
            if (result != null) {
                mAdapter.clear();
                for(Book book : result) {

                    mAdapter.add(book);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }

    public class BookAdapter extends ArrayAdapter<Book> {
        Context mContext;

        public BookAdapter(Context context, ArrayList<Book> books){

            super(context, 0 ,books);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Book book  = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.book_layout, parent, false);
            }

            TextView textViewTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
            TextView textViewAuthor = (TextView) convertView.findViewById(R.id.textViewAuthor);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);

            textViewTitle.setText(book.getTitle());
            textViewAuthor.setText(book.getAuthor());

            Picasso.with(mContext).setLoggingEnabled(true);
            Picasso.with(mContext).load(book.getImageURL()).error(R.drawable.ic_info_black_24dp).into(imageView);

            return convertView;
        }
    }
}
