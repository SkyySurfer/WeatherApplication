package weather.forecast.library;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import weather.forecast.library.pojo.CityInfo;
import weather.forecast.library.pojo.ConsolidatedWeather;
import weather.forecast.library.pojo.Forecast;

public class GettingWeather {
    private MetaWeatherApi metaWeatherApi;
    private String baseURL = "https://www.metaweather.com/api/location/";
    private String result = "";
    private String error = "Error!";
    private UpdateUIListener uiListener;

     public void getCurrentWeather(UpdateUIListener listener, String location) {

         uiListener = listener;

         HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
         interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

         OkHttpClient client = new OkHttpClient.Builder()
                 .addInterceptor(interceptor)
                 .build();

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            metaWeatherApi = retrofit.create(MetaWeatherApi.class);
            Call<List<CityInfo>> callCityInfo = metaWeatherApi.getCityId(location);
            getId(callCityInfo);
        }
        catch (Exception ex){
            uiListener.onDataAvailable(error);
        }
    }

    void getId(final Call<List<CityInfo>> callCityInfo){


             callCityInfo.enqueue(new Callback<List<CityInfo>>() {
                 @Override
                 public void onResponse(Call<List<CityInfo>> call, Response<List<CityInfo>> response) {
                     try {
                         List<CityInfo> cityInfoList = response.body();
                         CityInfo cityInfo = cityInfoList.get(0);
                         int id = cityInfo.getWoeid();
                         LogClass.log("id: " + id);
                         Call<Forecast> callForecast = metaWeatherApi.getWeather(id);
                         getForecast(callForecast);
                     }
                      catch (Exception ex){
                             uiListener.onDataAvailable(error);
                         }
                 }

                 @Override
                 public void onFailure(Call<List<CityInfo>> call, Throwable t) {
                     uiListener.onDataAvailable(error);
                     LogClass.log("getting id error:" + t.getLocalizedMessage());
                 }
             });
    }


    void getForecast(Call<Forecast> callForecast) {



            callForecast.enqueue(new Callback<Forecast>() {
                @Override
                public void onResponse(Call<Forecast> call, Response<Forecast> response) {

                    try {
                    Forecast forecast = response.body();
                    ConsolidatedWeather consolidatedWeather = forecast.getConsolidatedWeather().get(0);
                    result = "Weather state: " + consolidatedWeather.getWeatherStateName() + "\n"
                            + "Temp: " + consolidatedWeather.getTheTemp() + "\n"
                            + "Wind direction: " + consolidatedWeather.getWindDirectionCompass() + "\n"
                            + "Wind speed: " + consolidatedWeather.getWindSpeed() + "\n"
                            + "Air pressure: " + consolidatedWeather.getAirPressure() + "\n"
                            + "Humidity: " + consolidatedWeather.getHumidity();

                    uiListener.onDataAvailable(result);
                    LogClass.log("result:" + result);

                    }
                    catch (Exception ex){
                        uiListener.onDataAvailable(error);
                    }
                }

                @Override
                public void onFailure(Call<Forecast> call, Throwable t) {
                    uiListener.onDataAvailable(error);
                    LogClass.log("getting weather error:" + t.getLocalizedMessage());
                }
            });

     }


}
