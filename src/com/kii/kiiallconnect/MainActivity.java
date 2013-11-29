package com.kii.kiiallconnect;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiSocialCallBack;
import com.kii.cloud.storage.social.KiiSocialConnect;
import com.kii.cloud.storage.social.KiiSocialConnect.SocialNetwork;
import com.kii.cloud.storage.social.joint.KiiSocialJoint;
import com.kii.cloud.storage.social.joint.KiiSocialJoint.Provider;

public class MainActivity extends Activity implements OnClickListener,
		OnItemSelectedListener {
	private static final String TAG = "MainActivity";

	private static final String APPID = "socauth-app00";
	private static final String APPKEY = "dummy";
	private static final String SERVER = "https://dev-ufe-jp.internal.kii.com/api";

	Provider provider = null;
	KiiSocialConnect socialConnect = null;
	Spinner spinner = null;
	Button btnAuthorize = null;
	ImageView imgProvider = null;
	TextView txtMsg = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		Kii.initialize(APPID, APPKEY, SERVER);

		btnAuthorize = (Button) findViewById(R.id.btnAuthWithProvider);
		btnAuthorize.setOnClickListener(this);
		btnAuthorize.setVisibility(View.VISIBLE);

		spinner = (Spinner) findViewById(R.id.spinnerProviders);
		spinner.setOnItemSelectedListener(this);

		imgProvider = (ImageView) findViewById(R.id.imgProvider);
		imgProvider.setImageBitmap(null);

		txtMsg = (TextView) findViewById(R.id.txtMsg);

		socialConnect = Kii.socialConnect(SocialNetwork.KIISOCIALJOINT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		txtMsg.setText("");
		if (pos > 0) {
			provider = Provider.valueOf(KiiSocialJoint.Provider.class, spinner
					.getSelectedItem().toString().toUpperCase(Locale.US));
			setProviderImage(provider);

			Log.v(TAG, "Selected Provider : " + provider.toString());

			Bundle b = new Bundle();
			b.putParcelable("provider", provider);
			socialConnect.logIn(MainActivity.this, b, new MySocialCallback());
			// Log.v(TAG, socialConnect.getAccessTokenBundle().toString());
		} else {
			btnAuthorize.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "Main activity on activity result");
		socialConnect
				.respondAuthOnActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnAuthWithProvider) {
			Bundle bundle = socialConnect.getAccessTokenBundle();
			if (bundle == null) {
				Toast.makeText(MainActivity.this, "No access token!",
						Toast.LENGTH_LONG).show();
				return;
			}
			Log.v(TAG, "Access token bundle is:" + bundle.toString());
			SocialAPICaller caller = getSocialAPICaller(provider);
			caller.call(MainActivity.this.getApplicationContext(), bundle,
					new SocialAPICallback() {
						@Override
						public void onCompleted(String content, Exception e) {
							String msg = null;
							if (e != null) {
								msg = e.getLocalizedMessage();
							} else {
								msg = content;
							}
						}
					});
		}
	}

	private SocialAPICaller getSocialAPICaller(Provider provider) {
		SocialAPICaller caller = null;
		switch (provider) {
		case DROPBOX:
			caller = new DropboxAPICaller();
			break;
		case BOX:
			caller = new BoxAPICaller();
			break;
		case RENREN:
			break;
		case WEIBO:
			break;
		}
		return caller;
	}

	private void setProviderImage(Provider provider) {
		switch (provider) {
		case DROPBOX:
			imgProvider.setImageResource(R.drawable.dropbox);
			break;
		case BOX:
			imgProvider.setImageResource(R.drawable.boxnet);
			break;
		case RENREN:
			imgProvider.setImageResource(R.drawable.renren);
			break;
		case WEIBO:
			imgProvider.setImageResource(R.drawable.weibo);
			break;
		}
	}

	class MySocialCallback extends KiiSocialCallBack {
		@Override
		public void onLoginCompleted(SocialNetwork network, KiiUser user,
				Exception exception) {
			imgProvider.setImageBitmap(null);

			String msg = null;
			JSONObject obj = null;
			if (exception == null) {
				msg = "Login Successful. \n\rUser: " + user.toString(); // .toUri().toString();
				try {
					obj = new JSONObject(user.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// btnAuthorize.setVisibility(View.VISIBLE);

			} else {
				exception.printStackTrace();
				msg = "Login failed, error :" + exception.getMessage();
				// btnAuthorize.setVisibility(View.INVISIBLE);
				spinner.setSelection(0);

			}
			// Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
			if (obj != null) {
				try {
					txtMsg.setText("USER AUTHORIZED SUCCESSFULLY\r\n\r\nLoginName: " + obj.getString("loginName")
							+ "\n\r" + "EmailAddress: "
							+ obj.getString("emailAddress"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				txtMsg.setText(msg);
			}
		}
	}
}