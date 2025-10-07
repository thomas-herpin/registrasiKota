package com.example.regitrasikota;

import android.content.Intent;
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
import com.example.regitrasikota.api.KotaResponse;
import com.example.regitrasikota.model.Kota;
import com.example.regitrasikota.model.Provinsi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {
    Button btnRegister;
    TextView txvLogin;
    Spinner sprProvinsi, sprKota;
    EditText edtNama, edtStudentID, edtUserName, edtPassword, edtTanggalLahir;
    ApiService apiService;
    private FirebaseAuth mAuth;


    List<Provinsi> provinsiList = new ArrayList<>();
    List<String> namaProvinsi = new ArrayList<>();
    ArrayAdapter<String> provinsiAdapter;

    List<String> namaKota = new ArrayList<>();
    ArrayAdapter<String> kotaAdapter;


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


        sprProvinsi = findViewById(R.id.sprProvinsi);
        sprKota = findViewById(R.id.sprKota);

        provinsiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namaProvinsi);
        provinsiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        sprProvinsi.setAdapter(provinsiAdapter);

        kotaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namaKota);
        kotaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        sprKota.setAdapter(kotaAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wilayah.id/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Panggil API Provinsi
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

        sprProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Provinsi selected = provinsiList.get(position);
                String kodeProv = selected.getCode();
                Log.d("Provinsi", selected.getCode() + " - " + selected.getName());

                // Panggil API Kota
                apiService.getKota(kodeProv).enqueue(new Callback<KotaResponse>() {
                    @Override
                    public void onResponse(Call<KotaResponse> call, Response<KotaResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            namaKota.clear();
                            for (Kota k : response.body().getData()) {
                                Log.d("Kota", k.getCode() + " - " + k.getName());
                                namaKota.add(k.getName());
                            }
                            kotaAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("Kota", "Response gagal: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<KotaResponse> call, Throwable t) {
                        Log.e("Kota", "Error: " + t.getMessage(), t);
                        Toast.makeText(RegisterActivity.this, "Gagal ambil kota: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        btnRegister = findViewById(R.id.btnRegister);
        edtNama = findViewById(R.id.edtNama);
        edtStudentID = findViewById(R.id.edtStudentID);
        edtUserName = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        edtTanggalLahir = findViewById(R.id.edtTanggalLahir);
        txvLogin = findViewById(R.id.txvLogin);

        txvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLogin();
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtUserName.getText().toString().trim();
                String password = edtPassword.getText().toString();
                register(email, password);
                toLogin();

                String nama = edtNama.getText().toString().trim();
                String studentID = edtStudentID.getText().toString().trim();
                String provinsi = sprProvinsi.getSelectedItem().toString().trim();
                String kota = sprKota.getSelectedItem().toString().trim();
                String tanggalLahir = edtTanggalLahir.getText().toString().trim();
                registerUser(nama, studentID, provinsi, kota, tanggalLahir, email);
            }
        });

        txvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLogin();
            }
        });


    }

    public void registerUser(String nama,String studentID,
                             String provinsi, String kota,
                             String tanggalLahir, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("kota", kota);
        user.put("nama", nama);
        user.put("provinsi", provinsi);
        user.put("studentID", studentID);
        user.put("tanggalLahir", tanggalLahir);

// Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("USER", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("USER", "Error adding document", e);
                    }
                });

    }

    public void register(String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Register", "createUserWithEmail:success");
                            Toast.makeText(RegisterActivity.this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();
                            toLogin();
                        } else {
                            Log.w("Register", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    public void toLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}