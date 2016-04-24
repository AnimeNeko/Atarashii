package net.somethingdreadful.MAL.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class APIInterceptor implements Interceptor {
    private final String userAgent;
    private String permission = "";

    public APIInterceptor(String userAgent, String permission) {
        this.userAgent = userAgent;
        this.permission = permission;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request requestWithUserAgent = originalRequest.newBuilder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", userAgent)
                .addHeader("Authorization", permission)
                .build();
        return chain.proceed(requestWithUserAgent);
    }
}
