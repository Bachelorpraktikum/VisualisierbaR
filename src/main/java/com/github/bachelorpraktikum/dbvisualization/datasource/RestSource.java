package com.github.bachelorpraktikum.dbvisualization.datasource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
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
        try {
            getService().resumeSimulation().execute();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        listenToOutput(200, TimeUnit.MILLISECONDS);
    }


}
