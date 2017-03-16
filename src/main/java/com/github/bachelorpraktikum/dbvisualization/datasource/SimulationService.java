package com.github.bachelorpraktikum.dbvisualization.datasource;


import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

interface SimulationService {

    @GET("o")
    Call<List<String>> getObjectNames();

    @GET("o/{name}")
    Call<LiveTrain> getTrain(@Path("name") String objectName);

    @GET("call/APP/next")
    Call<ResponseBody> resumeSimulation();

    @GET("call/{object}/{method}")
    Call<ResponseBody> callMethod(
        @Path("object") String objectName,
        @Path("method") String method
    );

    @POST("o/call/{element}/breakNow")
    Call<ResponseBody> breakNow(String element);
}
