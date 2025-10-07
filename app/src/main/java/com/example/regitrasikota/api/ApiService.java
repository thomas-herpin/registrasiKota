package com.example.regitrasikota.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/provinces.json")
    Call<ApiResponse> getProvinsi();

    @GET("api/regencies/{provinceCode}.json")
    Call<KotaResponse> getKota(@Path("provinceCode") String provinceCode);
}
