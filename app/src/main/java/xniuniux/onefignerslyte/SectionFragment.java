package xniuniux.onefignerslyte;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class SectionFragment extends Fragment {

    private String LOG_TAG = "pagerFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";

    public SectionFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SectionFragment newInstance(int sectionNumber) {
        SectionFragment fragment = new SectionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_section, container, false);
        new FetchWeather().execute();
        return rootView;
    }


    public class FetchWeather extends AsyncTask<Void, Void, Void> {

        String unit = "metric";
        String unitTempSymbol = "\u2109C";

        @Override
        protected Void doInBackground(Void...params){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;

            final String preUrl = "http://api.wunderground.com/api/";
            Uri builtUri = Uri.parse(preUrl).buildUpon()
                    .appendPath(BuildConfig.WUNDERGROUND_API_KEY)
                    .appendPath("hourly")
                    .appendPath("geolookup")
                    .appendPath("q")
                    .appendPath("Taiwan")
                    .appendPath("Taipei.json").build();

            String LOG_TAG = "Weather_Fragment";

            try {
                URL url = new URL(builtUri.toString());
                Log.d(LOG_TAG, "URL: " + url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }


                if (buffer.length() == 0) {
                    return null;
                }

                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);

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
                Log.d(LOG_TAG, forecastJsonStr);
                getWeatherDataFromJson(forecastJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;

        }

        private void getWeatherDataFromJson(String forecastJsonStr)
                throws JSONException {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            JSONArray forecastJsonArray = forecastJson.getJSONArray("hourly_forecast");
            JSONObject locationJson = forecastJson.getJSONObject("location");

            String city = locationJson.getString("city");

            int dataPointsNum = forecastJson.length();
            if (dataPointsNum == 0){ return; }

            String[] dates = new String[dataPointsNum];
            String[] forecasts = new String[dataPointsNum];
            Integer[] hours = new Integer[dataPointsNum];
            Integer[] temps = new Integer[dataPointsNum];
            Integer[] humidities = new Integer[dataPointsNum];
            Integer[] qpf = new Integer[dataPointsNum];
            Integer[] pop = new Integer[dataPointsNum];


            for (int i = 0; i < dataPointsNum ; i++){
                String hour, date, day, temperature, condition, humidity,
                       quantatitivePrecipitationForecast, probabilityOfPrecipitation;

                JSONObject hourlyForecastJson = forecastJsonArray.getJSONObject(i);
                JSONObject FCTTIME = hourlyForecastJson.getJSONObject("FCTTIME");
                date = FCTTIME.getString("mon_padded") + "/" + FCTTIME.getString("mday_padded");
                day = FCTTIME.getString("weekday_name_abbrev");
                hour = FCTTIME.getString("hour_padded");

                temperature = hourlyForecastJson.getJSONObject("temp").getString(unit);
                condition  = hourlyForecastJson.getString("condition");
                humidity = hourlyForecastJson.getString("humidity");
                quantatitivePrecipitationForecast = hourlyForecastJson.getJSONObject("qpf").getString(unit);
                probabilityOfPrecipitation = hourlyForecastJson.getString("pop");

                dates[i] = date + " (" + day + ") " + " " + hour + ":00 ";
                forecasts[i] = condition + ", " + temperature + unitTempSymbol;
                hours[i] = Integer.getInteger(hour);
                temps[i] = Integer.getInteger(temperature);
                humidities[i] = Integer.getInteger(humidity);
                qpf[i] = Integer.getInteger(quantatitivePrecipitationForecast);
                pop[i] = Integer.getInteger(probabilityOfPrecipitation);
            }

            ArrayList<Integer[]> forecastData = new ArrayList<>();
            forecastData.add(hours);
            forecastData.add(temps);
            forecastData.add(humidities);
            forecastData.add(qpf);
            forecastData.add(pop);

            Log.d(LOG_TAG, hours[10].toString());
            Log.d(LOG_TAG, temps[10].toString());
            Log.d(LOG_TAG, humidities[10].toString());
            Log.d(LOG_TAG, qpf[10].toString());
            Log.d(LOG_TAG, pop[10].toString());

            Log.d(LOG_TAG, dates[10].toString());
            Log.d(LOG_TAG, forecasts[10].toString());

        }

        @Override
        protected void onPostExecute(Void v) {
        }

    }
}
