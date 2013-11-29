package com.kii.kiiallconnect;

import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class DropboxAPICaller implements SocialAPICaller {
	private static final String OAUTH_AUTHORIZATION_URL = "https://www.dropbox.com/1/oauth2/authorize";
	private static final String OAUTH_ACCESS_TOKEN_URL = "https://api.dropbox.com/1/oauth2/token";

	static String TAG = "DropboxAPICaller";

	@Override
	public void call(Context context, Bundle tokenBundle,
			final SocialAPICallback callback) {

		String oauthToken = tokenBundle.getString("oauth_token");
		Uri uri = Uri.parse(OAUTH_AUTHORIZATION_URL).buildUpon()
				.appendQueryParameter("access_token", oauthToken).build();
		Log.v(TAG, "Dropbox API endpoint:" + uri.toString());
		HttpGet req = new HttpGet(uri.toString());
		new RequestExecutor().execute(req, callback);
	}
}
