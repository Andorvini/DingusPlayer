package site.andorvini.miscellaneous;

import com.google.gson.Gson;
import io.sentry.Sentry;
import okhttp3.*;
import java.net.URLEncoder;
import site.andorvini.Main;

class ProsloykaResponse {
    public String error;
    public ProsloykaResult result;
}

class ProsloykaResult {
    public ProsloykaData data;
}

class ProsloykaData {
    public String url;
    public String video_id;
}

public class YoutubeProsloykaMethod {
    public static String getYoutubePlayingUrl(String videoUrl) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            String requestUrl = System.getenv("DP_PROSLOYKA_API_URL") + ("/petrushka");
            String encodedUrl = URLEncoder.encode(videoUrl, "UTF-8");
            Request.Builder requestBuilder = new Request.Builder().url(requestUrl + "?url=" + encodedUrl).get();
            Call call = okHttpClient.newCall(requestBuilder.build());
            Response response = call.execute();
            String responseBody = response.body().string();
            ProsloykaResponse prosloykaResponse = new Gson().fromJson(responseBody, ProsloykaResponse.class);
            return prosloykaResponse.result.data.url;
        } catch (Exception e) {
            if (Main.sentryAvailable) {
                Sentry.captureException(e);
            }
            e.printStackTrace();
        }
        return null;
    }
}
