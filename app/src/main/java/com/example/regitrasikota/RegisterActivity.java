package com.example.regitrasikota;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.regitrasikota.api.ApiClient;
import com.example.regitrasikota.api.ApiResponse;
import com.example.regitrasikota.api.ApiService;
import com.example.regitrasikota.model.Kabupaten;
import com.example.regitrasikota.model.Kecamatan;
import com.example.regitrasikota.model.Kelurahan;
import com.example.regitrasikota.model.Provinsi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    Button btnRegister;
    TextView txvLogin;
    Spinner sprProvinsi, sprKabupaten;
    ApiService apiService;


    List<Provinsi> provinsiList = new ArrayList<>();
    List<Kabupaten> kabupatenList = new ArrayList<>();
    List<Kecamatan> kecamatanList = new ArrayList<>();
    List<Kelurahan> kelurahanList = new ArrayList<>();

    // Adapter list display names
    List<String> namaProvinsi = new ArrayList<>();
    List<String> namaKabupaten = new ArrayList<>();
    List<String> namaKecamatan = new ArrayList<>();
    List<String> namaKelurahan = new ArrayList<>();

    ArrayAdapter<String> provinsiAdapter, kabupatenAdapter, kecamatanAdapter, kelurahanAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi view
        sprProvinsi = findViewById(R.id.sprProvinsi);
        sprKabupaten = findViewById(R.id.sprKabupaten);
        sprKecamatan = findViewById(R.id.sprKecamatan);
        sprKelurahan = findViewById(R.id.sprKelurahan);

        // Inisialisasi Retrofit
        apiService = ApiClient.getClient().create(ApiService.class);

        // Siapkan adapter
        provinsiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namaProvinsi);
        provinsiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprProvinsi.setAdapter(provinsiAdapter);

        kabupatenAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namaKabupaten);
        kabupatenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprKabupaten.setAdapter(kabupatenAdapter);

        kecamatanAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namaKecamatan);
        kecamatanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprKecamatan.setAdapter(kecamatanAdapter);

        kelurahanAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namaKelurahan);
        kelurahanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprKelurahan.setAdapter(kelurahanAdapter);

        // Load Provinsi
        loadProvinsi();

        // Setup Listeners
        setupProvinsiListener();
        setupKabupatenListener();
        setupKecamatanListener();
    }

    private void loadProvinsi() {
        apiService.getProvinsi().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    provinsiList = response.body().getData();
                    namaProvinsi.clear();
                    for (Provinsi p : provinsiList) {
                        if (p.getName() != null) {
                            namaProvinsi.add(p.getName());
                        }
                    }
                    provinsiAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Gagal ambil provinsi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupProvinsiListener() {
        sprProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Provinsi selected = provinsiList.get(position);
                String kodeProv = selected.getCode();
                Log.d("Provinsi", selected.getCode() + " - " + selected.getName());

                // Reset spinner selanjutnya
                kabupatenList.clear();
                kecamatanList.clear();
                kelurahanList.clear();
                namaKabupaten.clear();
                namaKecamatan.clear();
                namaKelurahan.clear();
                kabupatenAdapter.notifyDataSetChanged();
                kecamatanAdapter.notifyDataSetChanged();
                kelurahanAdapter.notifyDataSetChanged();

                // Load Kabupaten
                loadKabupaten(kodeProv);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadKabupaten(String kodeProvinsi) {
        apiService.getKabupaten(kodeProvinsi).enqueue(new Callback<List<Kabupaten>>() {
            @Override
            public void onResponse(Call<List<Kabupaten>> call, Response<List<Kabupaten>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    kabupatenList = response.body();
                    namaKabupaten.clear();
                    for (Kabupaten k : kabupatenList) {
                        if (k.getName() != null) {
                            Log.d("Kabupaten", k.getCode() + " - " + k.getName());
                            namaKabupaten.add(k.getName());
                        }
                    }
                    kabupatenAdapter.notifyDataSetChanged();
                } else {
                    Log.e("Kabupaten", "Response gagal: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Kabupaten>> call, Throwable t) {
                Log.e("Kabupaten", "Error: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "Gagal ambil kabupaten: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupKabupatenListener() {
        sprKabupaten.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (kabupatenList.isEmpty()) return;

                Kabupaten selected = kabupatenList.get(position);
                String kodeKab = selected.getCode();
                Log.d("Kabupaten Selected", selected.getCode() + " - " + selected.getName());

                // Reset spinner selanjutnya
                kecamatanList.clear();
                kelurahanList.clear();
                namaKecamatan.clear();
                namaKelurahan.clear();
                kecamatanAdapter.notifyDataSetChanged();
                kelurahanAdapter.notifyDataSetChanged();

                // Load Kecamatan
                loadKecamatan(kodeKab);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadKecamatan(String kodeKabupaten) {
        apiService.getKecamatan(kodeKabupaten).enqueue(new Callback<List<Kecamatan>>() {
            @Override
            public void onResponse(Call<List<Kecamatan>> call, Response<List<Kecamatan>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    kecamatanList = response.body();
                    namaKecamatan.clear();
                    for (Kecamatan kec : kecamatanList) {
                        if (kec.getName() != null) {
                            Log.d("Kecamatan", kec.getCode() + " - " + kec.getName());
                            namaKecamatan.add(kec.getName());
                        }
                    }
                    kecamatanAdapter.notifyDataSetChanged();
                } else {
                    Log.e("Kecamatan", "Response gagal: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Kecamatan>> call, Throwable t) {
                Log.e("Kecamatan", "Error: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "Gagal ambil kecamatan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupKecamatanListener() {
        sprKecamatan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (kecamatanList.isEmpty()) return;

                Kecamatan selected = kecamatanList.get(position);
                String kodeKec = selected.getCode();
                Log.d("Kecamatan Selected", selected.getCode() + " - " + selected.getName());

                // Reset spinner selanjutnya
                kelurahanList.clear();
                namaKelurahan.clear();
                kelurahanAdapter.notifyDataSetChanged();

                // Load Kelurahan
                loadKelurahan(kodeKec);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadKelurahan(String kodeKecamatan) {
        apiService.getKelurahan(kodeKecamatan).enqueue(new Callback<List<Kelurahan>>() {
            @Override
            public void onResponse(Call<List<Kelurahan>> call, Response<List<Kelurahan>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    kelurahanList = response.body();
                    namaKelurahan.clear();
                    for (Kelurahan kel : kelurahanList) {
                        if (kel.getName() != null) {
                            Log.d("Kelurahan", kel.getCode() + " - " + kel.getName());
                            namaKelurahan.add(kel.getName());
                        }
                    }
                    kelurahanAdapter.notifyDataSetChanged();
                } else {
                    Log.e("Kelurahan", "Response gagal: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Kelurahan>> call, Throwable t) {
                Log.e("Kelurahan", "Error: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "Gagal ambil kelurahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}