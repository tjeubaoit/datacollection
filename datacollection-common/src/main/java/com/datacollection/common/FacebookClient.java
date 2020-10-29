package com.datacollection.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.io.FileHelper;
import com.datacollection.common.utils.Pair;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class cung cấp các hàm utils để làm việc với graph API
 * của Facebook hoặc query các thông tin bổ sung từ Facebook
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class FacebookClient {

    public static final String BASE_GRAPH_URL = "https://graph.facebook.com/v2.12/";
    public static final String DEFAULT_ACCESS_TOKEN = "1642562635819731|eVyRw8eUEHoZ5OK30VCEaDRlO7M";

    private static String fbAccessToken;
    private static final Logger logger = LoggerFactory.getLogger(FacebookClient.class);

    static {
        try {
            Configuration conf = new Configuration();
            fbAccessToken = FileHelper.readAsString(conf.getProperty("fb.access_token.path")).trim();
            logger.info("Fb access token: " + fbAccessToken);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            fbAccessToken = DEFAULT_ACCESS_TOKEN;
        }
    }

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Image {
        public ImageData data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ImageData {
        @JsonProperty("is_silhouette")
        public boolean isSilhouette;

        public String url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ProfileInfo {
        public Metadata metadata;
        public String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Metadata {
        public String type;
    }

    /**
     * Lấy avatar URL từ per-app fbId với ảnh có size large (200x200)
     *
     * @param perAppId per-app fbId của user cần lấy avatar
     * @return avatar URL hoăc rỗng nếu có lỗi từ phía Facebook server
     * hoặc user ứng với fbId này không có avatar
     * @throws IOException nếu có lỗi về network xảy ra
     */
    public static String fetchAvatarUrl(String perAppId) throws IOException {
        return fetchAvatarUrl(perAppId, 200, 200);
    }

    /**
     * Lấy avatar URL từ per-app fbId với ảnh có size tùy chọn
     *
     * @param perAppId per-app fbId của user cần lấy avatar
     * @return avatar URL hoăc rỗng nếu có lỗi từ phía Facebook server
     * hoặc user ứng với fbId này không có avatar
     * @throws IOException nếu có lỗi về network xảy ra
     */
    @SuppressWarnings("ConstantConditions")
    public static String fetchAvatarUrl(String perAppId, int width, int height) throws IOException {
        String url = BASE_GRAPH_URL + perAppId + "/picture?redirect=false&width="
                + width + "&height=" + height;
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) return "";
            Image image = MAPPER.readValue(body.string(), Image.class);
            return image.data.isSilhouette ? "" : image.data.url;
        }
    }

    /**
     * Lấy data image (bytes) từ URL
     *
     * @param imgUrl URL của ảnh cần lấy
     * @return chuỗi bytes chứa nội dung bức ảnh ở dạng nén tùy thuộc vào
     * định dạng ảnh (jpg, png...) hoặc null nếu có lỗi phía Facebook server
     * @throws IOException nếu có lỗi network xảy ra
     */
    public static Pair<String, byte[]> fetchAvatarSource(String imgUrl) throws IOException {
        Request request = new Request.Builder()
                .url(imgUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) return null;
            return new Pair<>(response.request().url().toString(), body.bytes());
        }
    }

    /**
     *
     * @param profileUrl profile username or full profile url
     * @return real profile id or null if no profile exists with given profileUrl
     * @throws IOException if a network error occur
     */
    public static String fetchUserIdFromUsername(String profileUrl) throws IOException {
        RequestBody req = RequestBody.create(MediaType.parse("x-www-form-urlencoded"), "url=" + profileUrl);
        Request request = new Request.Builder()
                .url("https://findmyfbid.com")
                .method("POST", req)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36")
                .header("Accept", "*/*")
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) return null;

            try {
                String content = body.string();
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(content).getAsJsonObject();
                return json.get("id").getAsString();
            } catch (RuntimeException e) {
                return null;
            }
        }
    }

    /**
     * Fetch user facebook int64 id by their username or their profile url
     *
     * @param profileUrl profile username or full profile url
     * @return real profile id or null if no profile exists with given profileUrl
     * @throws IOException if a network error occur
     */
    public static String fetchUserIdFromUsername_v2(String profileUrl) throws IOException {
        if (!profileUrl.startsWith("http")) {
            profileUrl = FacebookHelper.FACEBOOK_ENDPOINT + profileUrl;
        }

        RequestBody req = RequestBody.create(MediaType.parse("form-data"), "check=Lookup&fburl=" + profileUrl);
        Request request = new Request.Builder()
                .url("https://lookup-id.com/")
                .method("POST", req)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36")
                .header("Accept", "*/*")
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) return null;

            String content = body.string();
            String pattern = "id=\"code\">[0-9]+";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(content);
            if (m.find()) {
                return m.group(0).replace("id=\"code\">", "");
            }
        }
        return null;
    }

    /**
     * Get profile info from Graph API
     *
     * @param profileId profile id
     * @return a pair with first value is profile type (page, group, user...),
     * second value is profile name
     * @throws IOException if an error occur
     */
    public static Pair<String, String> fetchProfileInfo(String profileId) throws IOException {
        String url = BASE_GRAPH_URL + profileId + "?metadata=1&access_token=" + fbAccessToken;
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) return null;
            ProfileInfo profile = MAPPER.readValue(body.string(), ProfileInfo.class);
            return profile != null && profile.metadata != null
                    ? new Pair<>(profile.metadata.type, profile.name) : null;
        }
    }

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 1000000; i++) {
            System.out.println(fetchUserIdFromUsername_v2("tuanha1984"));
        }
    }
}
