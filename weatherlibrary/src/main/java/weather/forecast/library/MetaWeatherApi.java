package weather.forecast.library;


import java.util.List;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import weather.forecast.library.pojo.CityInfo;
import weather.forecast.library.pojo.Forecast;

public interface MetaWeatherApi {
    @GET("search?")
    Call<List<CityInfo>> getCityId(@Query("query") String location);

    @GET("{id}")
    Call<Forecast> getWeather(@Path("id") int id);

}
