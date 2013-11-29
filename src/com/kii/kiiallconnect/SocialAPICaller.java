package com.kii.kiiallconnect;

import android.content.Context;
import android.os.Bundle;

public interface SocialAPICaller {
    public void call(Context context, Bundle tokenBundle, SocialAPICallback callback);
}
