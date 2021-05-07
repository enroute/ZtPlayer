package com.ztfun.retrofit.cryptonator;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ICryptonatorService {
    @GET("{coin}-usd")
    Observable<Crypto> getCoinData(@Path("coin") String coin);
}
