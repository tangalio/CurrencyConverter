package com.example.currencyconverter.api;
import com.example.currencyconverter.model.Suser;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;

public interface API {
    @GET("countryInfoJSON?username=caoth")
    Observable<Suser> getdata();
}
