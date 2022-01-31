package com.seleniumtests.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.net.Proxy;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.remote.http.HttpClient;
//import org.openqa.selenium.remote.internal.OkHttpClient;

import com.google.common.base.Strings;

//import okhttp3.ConnectionPool;
//import okhttp3.Credentials;
//import okhttp3.Request;
//import okhttp3.Response;

public class NetworkUtility {

	private NetworkUtility() {
		// nothing to do
	}

//	private static final ConnectionPool pool = new ConnectionPool();
	private static Proxy proxy = null;
	
	public static HttpClient createClient(URL url, Duration readTimeout, Duration connectionTimeout) {
        /*okhttp3.OkHttpClient.Builder client = new okhttp3.OkHttpClient.Builder()
            .connectionPool(pool)
            .followRedirects(true)
            .followSslRedirects(true)
            .proxy(proxy)
            .readTimeout(readTimeout.toMillis(), MILLISECONDS)
            .connectTimeout(connectionTimeout.toMillis(), MILLISECONDS);

        String info = url.getUserInfo();
        if (!Strings.isNullOrEmpty(info)) {
          String[] parts = info.split(":", 2);
          String user = parts[0];
          String pass = parts.length > 1 ? parts[1] : "";

          String credentials = Credentials.basic(user, pass);

          client.authenticator((route, response) -> {
            if (response.request().header("Authorization") != null) {
              return null; // Give up, we've already attempted to authenticate.
            }

            return response.request().newBuilder()
                .header("Authorization", credentials)
                .build();
          });
        }

        client.addNetworkInterceptor(chain -> {
          Request request = chain.request();
          Response response = chain.proceed(request);
          return response.code() == 408
                 ? response.newBuilder().code(500).message("Server-Side Timeout").build()
                 : response;
        });*/
return null;
//        return new OkHttpClient(client.build(), url);
      }	
}
