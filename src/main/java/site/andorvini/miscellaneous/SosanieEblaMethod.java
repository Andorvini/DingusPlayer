package site.andorvini.miscellaneous;

import okhttp3.*;

public class SosanieEblaMethod {
    public static String getSosaniaEblaUrl(String text) {
        try {

            OkHttpClient okHttpClient = new OkHttpClient();

            String requestUrl = "https://api.sosanie-ebla-bot-premium.vapronva.pw/tts/request/wav";

            RequestBody body = RequestBody.create(MediaType.parse("application/json"),
                    "{\"query\":\"" + text + "\"," +
                            "\"user_id\": 1,"  +
                            "\"voice\": {"+
                            "          \"speakerLang\": \"ru\"," +
                            "          \"speakerName\": \"alyona\"," +
                            "          \"speakerEmotion\": \"flirt\"," +
                            "          \"company\": \"tinkoff\"" +
                            "}}");

            Request.Builder requestBuilder = new Request.Builder()
                    .url(requestUrl)
                    .post(body)
                    .addHeader("X-API-key", System.getenv("DP_SOSANIE_API_KEY"));

            Call call = okHttpClient.newCall(requestBuilder.build());

            Response response = call.execute();
            String responseBody = response.body().string();

            return responseBody;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
