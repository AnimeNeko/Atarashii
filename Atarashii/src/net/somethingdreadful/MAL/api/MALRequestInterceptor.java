package net.somethingdreadful.MAL.api;

import android.util.Base64;
import retrofit.RequestInterceptor;

/**
 * 
 * @author d-sko
 *
 * This interceptor adds the authentication header and user agent to all retrofit requests
 * 
 */
public class MALRequestInterceptor implements RequestInterceptor {
	private String auth;
	
	public MALRequestInterceptor(String username, String password) {
		updateAuthentication(username, password);
	}
	
	public void updateAuthentication(String username, String password) {
		auth = Base64.encodeToString(String.format("%s:%s", username, password).getBytes(), Base64.DEFAULT);
	}

	@Override
	public void intercept(RequestFacade request) {
		request.addHeader("User-Agent", MALApi.USER_AGENT);
		request.addHeader("Authorization", "Basic " + auth);
	}

}
