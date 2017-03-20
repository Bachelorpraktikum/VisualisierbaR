package com.github.bachelorpraktikum.dbvisualization.datasource;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    private final Property<LiveTime> currentTime;

    public RestSource(String appPath) throws IOException {
        super(appPath);
        this.service = RETROFIT.create(SimulationService.class);
        this.currentTime = new SimpleObjectProperty<>();
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
        currentTime.setValue(fetchTime());
    }

    /**
     * Breaks the given element. When done, onDone is called on the JavaFX thread.
     *
     * @param element the element to break
     * @param onDone the Runnable to call when the call is done
     */
    public void breakElement(Element element, @Nullable Runnable onDone) {
        getService().breakNow(element.getName()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    log.fine("Successfully broke element " + element.getName());
                } else {
                    log.severe("Break element failed. Error code: " + response.code());
                }
                Platform.runLater(onDone);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                log.severe("Failed to execute breakNow call");
                Platform.runLater(onDone);
            }
        });
    }

    /**
     * Gets the current model time. This blocks the calling thread until the call is done.
     *
     * @return the current time, or null
     */
    @Nullable
    private LiveTime fetchTime() {
        try {
            Response<LiveTime> time = getService().tellTime().execute();
            if (time.isSuccessful()) {
                return time.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.severe("tellTime() call failed");
        return null;
    }

    /**
     * <p>Gets the current time of the simulated model.</p>
     *
     * <p>If there is no current time (probably because of a failed network call), -1 is
     * returned.</p>
     *
     * @return the model time in milliseconds
     */
    public int getTime() {
        LiveTime time = currentTime.getValue();
        return time == null ? -1 : time.getTime();
    }

    /**
     * <p>Gets the state of the specified train at the current {@link #getTime() model time}.</p>
     *
     * <p>Performs a blocking network operation.</p>
     *
     * @param train the train to look up
     * @return the current state of the train in the simulated model
     */
    @Nonnull
    public LiveTrain getTrain(@Nonnull Train train) {
        Response<LiveTrain> response;
        try {
            response = getService().getTrain(train.getReadableName()).execute();
            if (response.isSuccessful()) {
                LiveTrain result = response.body();
                return result.isValid() ? result : LiveTrain.INVALID;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return LiveTrain.INVALID;
    }

    public ReadOnlyProperty<LiveTime> timeProperty() {
        return currentTime;
    }
}
