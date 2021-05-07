package com.ztfun.retrofit.akid;

import com.ztfun.retrofit.cryptonator.Crypto;

import io.reactivex.Observable;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface IAkidService {
    @POST("{coin}-usd")
    Observable<Crypto> getCoinData(@Path("coin") String coin);
}
