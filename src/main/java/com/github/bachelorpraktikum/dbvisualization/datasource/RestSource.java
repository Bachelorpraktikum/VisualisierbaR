package com.github.bachelorpraktikum.dbvisualization.datasource;

import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestSource extends SubprocessSource {

    private static final Retrofit RETROFIT = new Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://localhost:8080")
        .build();

    private final SimulationService service;

    public RestSource(String appPath) throws IOException {
        super(appPath);
        this.service = RETROFIT.create(SimulationService.class);
    }

    private SimulationService getService() {
        return service;
    }

    public void continueSimulation() {
        getService().resumeSimulation().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
            }
        });
    }


}
