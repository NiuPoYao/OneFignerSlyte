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
import java.util.Arrays;


public class PagerFragmentWeatherForecast extends Fragment {

    private String LOG_TAG = "pagerFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_UNIT = "unit";

    private ArrayList<Object> forecastData;
    private String mLocation;
    private int mUnit;
    private int mSectionNumber;

    private LineAndBarChartView weatherChart;

    public PagerFragmentWeatherForecast() {
    }

    public static PagerFragmentWeatherForecast newInstance(int sectionNumber) {
        PagerFragmentWeatherForecast fragment = new PagerFragmentWeatherForecast();
        Bundle args = new Bundle();
        /*args.putString(ARG_LOCATION, location);
        args.putInt(ARG_UNIT, unit);*/
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLocation = getArguments().getString(ARG_LOCATION);
            mUnit = getArguments().getInt(ARG_UNIT);
            mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_section, container, false);
        ViewGroup bodyContainer =  (ViewGroup) rootView.findViewById(R.id.pager_body);
        View bodyRootView = inflater.inflate(R.layout.pager_body_weather_forecast, bodyContainer, true);
        weatherChart = (LineAndBarChartView) bodyRootView.findViewById(R.id.weather_stat_chart);
        new FetchWeather().execute();
        return rootView;
    }



    public class FetchWeather extends AsyncTask<Void, Void, ArrayList<Object>> {

        String unit = "metric";
        String unitTempSymbol = unit.equals("metric") ? "\u2103":"\u2109";

        @Override
        protected ArrayList<Object> doInBackground(Void...params){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;

            final String preUrl = "http://api.wunderground.com/api/";
            Uri builtUri = Uri.parse(preUrl).buildUpon()
                    .appendPath(BuildConfig.WUNDERGROUND_API_KEY)
                    .appendPath("conditions")
                    .appendPath("forecast")
                    .appendPath("hourly")
                    .appendPath("geolookup")
                    .appendPath("q")
                    .appendPath("25.039350,121.614756.json").build();

            String LOG_TAG = "Weather_Fragment";

            try {
                URL url = new URL(builtUri.toString());

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
                return getWeatherDataFromJson(forecastJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;

        }

        private ArrayList<Object> getWeatherDataFromJson(String forecastJsonStr)
                throws JSONException {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            JSONArray forecastJsonArray = forecastJson.getJSONArray("hourly_forecast");
            JSONObject locationJson = forecastJson.getJSONObject("location");

            String city = locationJson.getString("city");
            Log.d(LOG_TAG, city);

            int dataPointsNum = Math.min(24, forecastJsonArray.length());
            if (dataPointsNum == 0){ return null; }


            String[] dates = new String[dataPointsNum];
            String[] forecasts = new String[dataPointsNum];
            String[] hours = new String[dataPointsNum];
            Integer[] temps = new Integer[dataPointsNum];
            Integer[] humidities = new Integer[dataPointsNum];
            Integer[] qpf = new Integer[dataPointsNum];
            Integer[] pop = new Integer[dataPointsNum];


            for (int i = 0; i < dataPointsNum ; i++){
                String hour, date, day, temperature, condition, humidity,
                       quantitativePrecipitationForecast, probabilityOfPrecipitation;

                JSONObject hourlyForecastJson = forecastJsonArray.getJSONObject(i);
                JSONObject FCTTIME = hourlyForecastJson.getJSONObject("FCTTIME");
                date = FCTTIME.getString("mon_padded") + "/" + FCTTIME.getString("mday_padded");
                day = FCTTIME.getString("weekday_name_abbrev");
                hour = FCTTIME.getString("hour_padded");

                temperature = hourlyForecastJson.getJSONObject("temp").getString(unit);
                condition  = hourlyForecastJson.getString("condition");
                humidity = hourlyForecastJson.getString("humidity");
                quantitativePrecipitationForecast = hourlyForecastJson.getJSONObject("qpf").getString(unit);
                probabilityOfPrecipitation = hourlyForecastJson.getString("pop");

                dates[i] = date + " (" + day + ") " + hour + ":00 ";
                forecasts[i] = condition + ", " + temperature + unitTempSymbol;
                hours[i] = hour;
                temps[i] = Integer.parseInt(temperature);
                humidities[i] = Integer.parseInt(humidity);
                qpf[i] = Integer.parseInt(quantitativePrecipitationForecast);
                pop[i] = Integer.parseInt(probabilityOfPrecipitation);
            }

            forecastData = new ArrayList<>();
            forecastData.add(dates);
            forecastData.add(forecasts);
            forecastData.add(hours);
            forecastData.add(temps);
            forecastData.add(humidities);
            forecastData.add(qpf);
            forecastData.add(pop);
            return forecastData;

        }

        @Override
        protected void onPostExecute(ArrayList<Object> array) {

            weatherChart.mHour.addAll(Arrays.asList( (String[]) array.get(2)) );
            weatherChart.mTemperature.addAll(Arrays.asList( (Integer[]) array.get(3)) );
            weatherChart.mHumidity.addAll(Arrays.asList( (Integer[]) array.get(4)) );
            weatherChart.mQpf.addAll(Arrays.asList( (Integer[]) array.get(5)) );
            weatherChart.mPop.addAll(Arrays.asList( (Integer[]) array.get(6)) );
            //Log.d(LOG_TAG,"mHour size: " + weatherChart.mHour.size());
            weatherChart.requestLayout();

        }

    }
}
