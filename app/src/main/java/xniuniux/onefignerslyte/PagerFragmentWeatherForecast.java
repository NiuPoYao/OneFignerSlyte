package xniuniux.onefignerslyte;


import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
import java.util.HashMap;
import java.util.Map;


public class PagerFragmentWeatherForecast extends Fragment {

    private String LOG_TAG = "pagerFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_UNIT = "unit";

    public static final int UNIT_METRIC = 0;
    public static final int UNIT_IMPERIAL = 1;

    private Map<String, String> currentObsData;
    private ArrayList<Object> hourlyForecastData;
    private ArrayList<Map<String, String>> dailyForecastData;
    private Map<String, Float> absLocation = new HashMap<>();

    private String mLocation;
    private int mUnit =  PagerFragmentWeatherForecast.UNIT_METRIC;
    private String mUnitTempSymbol;
    private int mSectionNumber;

    private class DetailDailyForecast{
        public String high, low, pop, humidity;;
        public String condition, iconString;
        public String year, moth, date, day;


    }

    private LineAndBarChartView weatherChart;
    private LinearLayout dailyForecast;

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
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLocation = getArguments().getString(ARG_LOCATION);
            mUnit = getArguments().getInt(ARG_UNIT);
            mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        mUnitTempSymbol = mUnit == PagerFragmentWeatherForecast.UNIT_IMPERIAL ? "\u2109":"\u2103";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_section, container, false);
        ViewGroup bodyContainer =  (ViewGroup) rootView.findViewById(R.id.pager_body);
        View bodyRootView = inflater.inflate(R.layout.pager_body_weather_forecast, bodyContainer, true);
        weatherChart = (LineAndBarChartView) bodyRootView.findViewById(R.id.weather_stat_chart);
        dailyForecast = (LinearLayout) bodyRootView.findViewById(R.id.bottom_information);
        new FetchWeather().execute();
        return rootView;
    }



    public class FetchWeather extends AsyncTask<Void, Void, Void> {
        String Log_Tag = "Weather_Fragment";


        @Override
        protected Void doInBackground(Void...params){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
            absLocation = ((MainActivity) getActivity()).getLocation();
            String latitude;
            String longitude;
            if (absLocation != null) {
                latitude = Float.toString(absLocation.get("latitude"));
                longitude = Float.toString(absLocation.get("longitude"));
            } else {
                latitude = "51.500125";
                longitude = "0";
            }
            Log.d(Log_Tag, latitude + ", " + longitude);
            final String preUrl = "http://api.wunderground.com/api/";
            Uri builtUri = Uri.parse(preUrl).buildUpon()
                    .appendPath(BuildConfig.WUNDERGROUND_API_KEY)
                    .appendPath("conditions")
                    .appendPath("forecast10day")
                    .appendPath("hourly")
                    .appendPath("geolookup")
                    .appendPath("q")
                    .appendPath(latitude + "," + longitude + ".json").build();


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
                getWeatherDataFromJson(forecastJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void getWeatherDataFromJson(String forecastJsonStr)
                throws JSONException {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            JSONArray hourlyForecastJsonArray = forecastJson.getJSONArray("hourly_forecast");
            JSONObject locationJson = forecastJson.getJSONObject("location");
            JSONObject currentJson = forecastJson.getJSONObject("current_observation");
            JSONArray dailyTxtForecastJsonArray = forecastJson.getJSONObject("forecast")
                                                    .getJSONObject("txt_forecast")
                                                    .getJSONArray("forecastday");
            JSONArray dailySimpleForecastJson = forecastJson.getJSONObject("forecast")
                                                    .getJSONObject("simpleforecast")
                                                    .getJSONArray("forecastday");

            String city = locationJson.getString("city");
            Log.d(LOG_TAG, city);

            hourlyForecastData = getHourlyForecast(hourlyForecastJsonArray);
            currentObsData = getCurrentObservation(currentJson);
            dailyForecastData = getDailyForecast(dailySimpleForecastJson, dailyTxtForecastJsonArray);
        }

        private ArrayList<Map<String,String>> getDailyForecast(JSONArray simpleArray, JSONArray txtArray)
                throws JSONException{
            int dataPointsNum = Math.min(5, simpleArray.length());
            if (dataPointsNum == 0){ return null; }

            ArrayList<Map<String,String>> dailyForecast = new ArrayList<>(dataPointsNum);

            for (int i = 0; i < dataPointsNum; i++){
                Map<String,String> dayData = new HashMap<>();
                JSONObject dayForecast = simpleArray.getJSONObject(i);
                JSONObject date = dayForecast.getJSONObject("date");
                dayData.put("month", date.getString("monthname_short"));
                dayData.put("day", date.getString("day"));
                dayData.put("week", date.getString("weekday"));
                dayData.put("week_short", date.getString("weekday_short"));
                dayData.put("condition", dayForecast.getString("conditions"));
                dayData.put("pop", dayForecast.getString("pop") + "%");
                dayData.put("humidity_ave", String.valueOf( dayForecast.getInt("avehumidity")) );
                dayData.put("humidity_max", String.valueOf( dayForecast.getInt("maxhumidity")) );
                dayData.put("humidity_min", String.valueOf( dayForecast.getInt("minhumidity")) );

                String high, low, qpf_day, qpf_night, qpf_all, snow_day, snow_night, snow_all,
                        wind, wind_max;
                if (mUnit == PagerFragmentWeatherForecast.UNIT_IMPERIAL){
                    high = dayForecast.getJSONObject("high").getString("fahrenheit");
                    low = dayForecast.getJSONObject("low").getString("fahrenheit");
                    qpf_all = String.valueOf( dayForecast.getJSONObject("qpf_allday").getDouble("in"));
                    qpf_day = String.valueOf( dayForecast.getJSONObject("qpf_day").getDouble("in"));
                    qpf_night = String.valueOf( dayForecast.getJSONObject("qpf_night").getDouble("in"));
                    snow_all = String.valueOf( dayForecast.getJSONObject("snow_allday").getDouble("in"));
                    snow_day = String.valueOf( dayForecast.getJSONObject("snow_day").getDouble("in"));
                    snow_night = String.valueOf( dayForecast.getJSONObject("snow_night").getDouble("in"));
                    wind = String.valueOf( dayForecast.getJSONObject("avewind").getDouble("mph"));
                    wind_max = String.valueOf( dayForecast.getJSONObject("maxwind").getDouble("mph"));
                } else {
                    high = dayForecast.getJSONObject("high").getString("fahrenheit");
                    low = dayForecast.getJSONObject("low").getString("fahrenheit");
                    qpf_all = String.valueOf( dayForecast.getJSONObject("qpf_allday").getDouble("mm"));
                    qpf_day = String.valueOf( dayForecast.getJSONObject("qpf_day").getDouble("mm"));
                    qpf_night = String.valueOf( dayForecast.getJSONObject("qpf_night").getDouble("mm"));
                    snow_all = String.valueOf( dayForecast.getJSONObject("snow_allday").getDouble("cm"));
                    snow_day = String.valueOf( dayForecast.getJSONObject("snow_day").getDouble("cm"));
                    snow_night = String.valueOf( dayForecast.getJSONObject("snow_night").getDouble("cm"));
                    wind = String.valueOf( dayForecast.getJSONObject("avewind").getDouble("kph"));
                    wind_max = String.valueOf( dayForecast.getJSONObject("maxwind").getDouble("kph"));
                }
                dayData.put("high", high);
                dayData.put("low", low);
                dayData.put("qpf_all", qpf_all);
                dayData.put("qpf_day", qpf_day);
                dayData.put("qpf_night", qpf_night);
                dayData.put("snow_all", snow_all);
                dayData.put("snow_day", snow_day);
                dayData.put("snow_day", snow_day);
                dayData.put("snow_night", snow_night);
                dayData.put("wind", wind);
                dayData.put("wind_max", wind_max);

                dailyForecast.add(dayData);
            }
            return dailyForecast;
        }

        private Map<String, String> getCurrentObservation(JSONObject currentJson)
                throws JSONException{
            Map<String, String> observationData = new HashMap<>();
            observationData.put("condition", currentJson.getString("weather"));
            observationData.put("humidity", currentJson.getString("relative_humidity"));
            observationData.put("wind_direction", String.valueOf( currentJson.getInt("wind_degrees") ) );
            observationData.put("UV", currentJson.getString("UV"));

            String temp, feels_like, pressure, wind_speed, visibility, precip_hour, precip_day;
            if (mUnit == PagerFragmentWeatherForecast.UNIT_IMPERIAL){
                temp = String.valueOf( currentJson.getDouble("temp_f") );
                feels_like = currentJson.getString("feelslike_f");
                pressure = currentJson.getString("pressure_in");
                wind_speed = String.valueOf( currentJson.getDouble("wind_mph"));
                visibility = currentJson.getString("visibility_mi");
                precip_hour = currentJson.getString("precip_1hr_in");
                precip_day = currentJson.getString("precip_today_in");
            } else {
                temp = String.valueOf( currentJson.getDouble("temp_c") );
                feels_like = currentJson.getString("feelslike_c");
                pressure = currentJson.getString("pressure_mb");
                wind_speed = String.valueOf( currentJson.getDouble("wind_kph"));
                visibility = currentJson.getString("visibility_km");
                precip_hour = currentJson.getString("precip_1hr_metric");
                precip_day = currentJson.getString("precip_today_metric");
            }
            observationData.put("temp", String.valueOf( temp ));
            observationData.put("feels_like", feels_like );
            observationData.put("pressure", pressure );
            observationData.put("wind_speed",  wind_speed );
            observationData.put("visibility", visibility );
            observationData.put("precipitation_hour", precip_hour );
            observationData.put("precipitation_day", precip_day );
            return observationData;
        }

        private ArrayList<Object> getHourlyForecast(JSONArray array)
                throws JSONException{
            int dataPointsNum = Math.min(24, array.length());
            if (dataPointsNum == 0){ return null; }

            String[] dates = new String[dataPointsNum];
            String[] forecasts = new String[dataPointsNum];
            String[] hours = new String[dataPointsNum];
            Integer[] temps = new Integer[dataPointsNum];
            Integer[] humidities = new Integer[dataPointsNum];
            Integer[] qpf = new Integer[dataPointsNum];
            Integer[] pop = new Integer[dataPointsNum];

            String unit = mUnit == PagerFragmentWeatherForecast.UNIT_IMPERIAL? "english" : "metric" ;

            for (int i = 0; i < dataPointsNum ; i++){
                String hour, date, day, temperature, condition, humidity,
                        quantitativePrecipitationForecast, probabilityOfPrecipitation;

                JSONObject hourlyForecastJson = array.getJSONObject(i);
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
                forecasts[i] = condition + ", " + temperature + mUnitTempSymbol;
                hours[i] = hour;
                temps[i] = Integer.parseInt(temperature);
                humidities[i] = Integer.parseInt(humidity);
                qpf[i] = Integer.parseInt(quantitativePrecipitationForecast);
                pop[i] = Integer.parseInt(probabilityOfPrecipitation);
            }

            ArrayList<Object> forecastData = new ArrayList<>();
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
        protected void onPostExecute(Void param) {

            weatherChart.mHour.addAll(Arrays.asList( (String[]) hourlyForecastData.get(2)) );
            weatherChart.mTemperature.addAll(Arrays.asList( (Integer[]) hourlyForecastData.get(3)) );
            weatherChart.mHumidity.addAll(Arrays.asList( (Integer[]) hourlyForecastData.get(4)) );
            weatherChart.mQpf.addAll(Arrays.asList( (Integer[]) hourlyForecastData.get(5)) );
            weatherChart.mPop.addAll(Arrays.asList( (Integer[]) hourlyForecastData.get(6)) );
            //Log.d(LOG_TAG,"mHour size: " + weatherChart.mHour.size());

            weatherChart.requestLayout();

        }

    }
}
