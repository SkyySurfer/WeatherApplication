package weather.forecast.library;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
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
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .build();

            metaWeatherApi = retrofit.create(MetaWeatherApi.class);
            Single<List<CityInfo>> callCityInfo = metaWeatherApi.getCityId(location);
            getId(callCityInfo);
        }
        catch (Exception ex){
            uiListener.onDataAvailable(error);
        }
    }

    void getId(final Single<List<CityInfo>> callCityInfo){

         callCityInfo
                 .flatMap(new Function<List<CityInfo>, SingleSource<Forecast>>() { // на io
                     @Override
                     public SingleSource<Forecast> apply(@NonNull List<CityInfo> cityInfos)  {
                         CityInfo cityInfo = cityInfos.get(0);
                         int id = cityInfo.getWoeid();
                         return metaWeatherApi.getWeather(id);

                     }
                 })
                 .map(new Function<Forecast, String>() { // на io
                     @Override
                     public String apply(@NonNull Forecast forecast) throws Exception {
                         ConsolidatedWeather consolidatedWeather = forecast.getConsolidatedWeather().get(0);
                         return "Weather state: " + consolidatedWeather.getWeatherStateName() + "\n"
                                 + "Temp: " + consolidatedWeather.getTheTemp() + "\n"
                                 + "Wind direction: " + consolidatedWeather.getWindDirectionCompass() + "\n"
                                 + "Wind speed: " + consolidatedWeather.getWindSpeed() + "\n"
                                 + "Air pressure: " + consolidatedWeather.getAirPressure() + "\n"
                                 + "Humidity: " + consolidatedWeather.getHumidity();
                     }
                 })
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(new Consumer<String>() { // на ui
                     @Override
                     public void accept(String s) throws Exception {
                         uiListener.onDataAvailable(result);
                         LogClass.log("result:" + result);
                     }
                 }, new Consumer<Throwable>() {
                     @Override
                     public void accept(Throwable throwable) throws Exception {
                         uiListener.onDataAvailable(error);
                         LogClass.log("getting weather error:" + throwable.getLocalizedMessage());
                     }
                 });



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
