package com.example.regitrasikota.api;

import com.example.regitrasikota.model.Kabupaten;
import com.example.regitrasikota.model.Kecamatan;
import com.example.regitrasikota.model.Kelurahan;
import com.example.regitrasikota.model.Provinsi;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/provinces.json")
    Call<ApiResponse> getProvinsi();

    @GET("api/regencies/{id_provinsi}.json")
    Call<List<Kabupaten>> getKabupaten(@Path("id_provinsi") String idProvinsi);

    @GET("api/districts/{id_kabupaten}.json")
    Call<List<Kecamatan>> getKecamatan(@Path("id_kabupaten") String idKabupaten);

    @GET("api/villages/{id_kecamatan}.json")
    Call<List<Kelurahan>> getKelurahan(@Path("id_kecamatan") String idKecamatan);

}
