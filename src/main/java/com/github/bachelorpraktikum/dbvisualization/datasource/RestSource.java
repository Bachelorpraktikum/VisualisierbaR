package com.github.bachelorpraktikum.dbvisualization.datasource;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestSource extends SubprocessSource {

    private static final Logger log = Logger.getLogger(RestSource.class.getName());

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
            Response<ResponseBody> response = getService().resumeSimulation().execute();
            if (!response.isSuccessful()) {
                log.severe("Call to APP/next was unsucessful. Code: " + response.code());
                return;
            }
        } catch (IOException e) {
            log.severe("Error when trying to call APP/next.");
            e.printStackTrace();
            return;
        }
        listenToOutput(200, TimeUnit.MILLISECONDS);
    }

    public void breakElement(Element element, Runnable onDone) {
        getService().breakNow(element.getName()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    log.fine("Successfully broke element " + element.getName());
                } else {
                    log.severe("Break element failed. Error code: " + response.code());
                }
                onDone.run();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                log.severe("Failed to execute breakNow call");
                onDone.run();
            }
        });
    }

    public int getTime() {
        try {
            Response<LiveTime> time = getService().tellTime().execute();
            if (time.isSuccessful()) {
                return time.body().getTime();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("error");
        return -1;
    }
}
