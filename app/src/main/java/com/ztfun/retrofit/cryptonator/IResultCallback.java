package com.ztfun.retrofit.cryptonator;

import java.util.List;

public interface IResultCallback {
    void onNext(List<Crypto.Market> marketList);
    void onError(Throwable t);
}
