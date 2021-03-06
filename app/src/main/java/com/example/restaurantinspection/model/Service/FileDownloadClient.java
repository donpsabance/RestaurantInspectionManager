package com.example.restaurantinspection.model.Service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface FileDownloadClient {
    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);
    @Streaming
    @GET
    Call<ResponseBody> downloadFileStream(@Url String fileUrl);
}
