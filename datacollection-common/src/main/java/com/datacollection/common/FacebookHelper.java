package com.datacollection.common;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FacebookHelper {

    public static final String FACEBOOK_ENDPOINT = "https://www.facebook.com/";

    /*
    type 1 baseURL:https://www.facebook.com/permalink.php?story_fbid=1678237445836245&id=100009500168785
    type 2 baseURL:https://www.facebook.com/photo.php?fbid=180772828996946&set=a.178405662566996.1073741827.100011926892264&type=3
    type 3 baseURL:https://www.facebook.com/dungmon123/posts/358819354491216
     */
    public static String parseBaseURLToId(String baseUrl) {
        StringBuilder id = new StringBuilder();
        //type 2
        if (baseUrl.contains("fbid") && !baseUrl.contains("permalink")) {
            String pattern = ".";
            int indexOfId = baseUrl.lastIndexOf(pattern) + pattern.length();
            while (indexOfId < baseUrl.length() && baseUrl.charAt(indexOfId) >= '0'
                    && baseUrl.charAt(indexOfId) <= '9') {
                id.append(baseUrl.charAt(indexOfId));
                indexOfId++;
            }
            return id.toString();
        } else return parseURLProfileToId(baseUrl);
    }

    /*
    type 1 urlProfile "https://www.facebook.com/phang.lequang";
    type 2 urlProfile "https://www.facebook.com/profile.php?id=100014645524716";
     */
    public static String parseURLProfileToId(String urlProfile) {
        StringBuilder id = new StringBuilder();
        if (urlProfile.contains("id=")) {
            //type 1
            String pattern = "id=";
            int indexOfId = urlProfile.lastIndexOf(pattern) + pattern.length();
            while (indexOfId < urlProfile.length() && urlProfile.charAt(indexOfId) >= '0'
                    && urlProfile.charAt(indexOfId) <= '9') {
                id.append(urlProfile.charAt(indexOfId));
                indexOfId++;
            }
            return id.toString();
        } else {
            // type 2
            String pattern = "www.facebook.com/";
            int indexOfId = urlProfile.lastIndexOf(pattern) + pattern.length();
            while (indexOfId < urlProfile.length() && urlProfile.charAt(indexOfId) != '/') {
                id.append(urlProfile.charAt(indexOfId));
                indexOfId++;
            }
            return id.toString().startsWith("profile.php") ? "" : id.toString();
        }
    }

}
