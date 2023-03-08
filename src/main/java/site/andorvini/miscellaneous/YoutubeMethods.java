package site.andorvini.miscellaneous;

import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeMethods {

//  ============= Returns TRUE if str is youtube link ==============
    public static boolean isYouTubeLink(String str) {
        String pattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+";
        Pattern youtubePattern = Pattern.compile(pattern);
        Matcher matcher = youtubePattern.matcher(str);
        return matcher.matches();
    }

//  ============== Returns video from name ================

    public static String getVideoUrlFromName(String videoName) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();

        YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory, null)
                .setApplicationName("Dingus Player")
                .setGoogleClientRequestInitializer(new CommonGoogleClientRequestInitializer(System.getenv("DP_YOUTUBE_API_KEY")))
                .build();

        YouTube.Search.List search = youtube.search().list(Collections.singletonList("id,snippet"));
        search.setKey(System.getenv("DP_YOUTUBE_API_KEY"));
        search.setQ(videoName);
        search.setType(Collections.singletonList("video"));
        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)");
        search.setMaxResults(10L);

        SearchListResponse searchListResponse = search.execute();
        List<SearchResult> searchResults = searchListResponse.getItems();

        String videoId = searchResults.get(0).getId().getVideoId();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("https://www.youtube.com/watch?v=");
        stringBuilder.append(videoId);

        return stringBuilder.toString();
    }

//  ============== Returns video title from url if isTitle == true, else returns video duration ==============

    public static String getYoutubeVideoTitleFromUrl(String url, boolean isTitle) throws IOException {

        String newUrl = null;

        String regexVideoId = "^[^v]+v=(.{11}).*";

        Pattern pattern = Pattern.compile(regexVideoId);
        Matcher matcher = pattern.matcher(url);

        if (url.contains("youtube") || url.contains("youtu.be")) {
            if (matcher.matches()) {
                newUrl = matcher.group(1);
            }
        }

        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();

        YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory, null)
                .setApplicationName("Dingus Player")
                .setGoogleClientRequestInitializer(new CommonGoogleClientRequestInitializer(System.getenv("DP_YOUTUBE_API_KEY")))
                .build();

        String methodResult;

        if (isTitle) {
            try {
                YouTube.Videos.List request = youtube.videos().list(Collections.singletonList("snippet"));
                request.setId(Collections.singletonList(newUrl));

                VideoListResponse response = request.execute();
                List<Video> videos = response.getItems();

                Video video = videos.get(0);

                methodResult = video.getSnippet().getTitle();
            } catch (Exception e) {
                System.out.println("[WARN] Not youtube link");
                methodResult = "Uknown title";
            }

        } else {
            YouTube.Videos.List request = youtube.videos().list(Collections.singletonList("contentDetails"));
            request.setId(Collections.singletonList(newUrl));

            VideoListResponse response = request.execute();
            List<Video> videos = response.getItems();

            Video video = videos.get(0);

            String isoDuration = video.getContentDetails().getDuration();

            Duration duration = Duration.parse(isoDuration);
            long minutes = duration.toMinutes();
            long seconds = duration.getSeconds() % 60;
            methodResult = String.format("%d:%02d", minutes, seconds);
        }
        return methodResult;
    }
}
