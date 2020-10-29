package com.datacollection.common;

import com.datacollection.common.utils.NullProtector;
import com.datacollection.common.utils.Strings;

/**
 * Chứa một số helper method trong tiền xử lý dữ liệu text
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class TextUtils {

    /**
     * Parse forum user ID từ profile URL. <br/>
     * Ví dụ với URL: <i>https://tinhte.vn/members/ngatnguong.81585/</i>
     * thì userId sẽ là: <i>ngatnguong.81585</i>
     *
     * @param url profile URL to be parsed
     * @return userId parsed from URL
     */
    public static String parseForumId(String url) {
        String[] split = url.split("/");
        return split.length < 4 ? null : split[split.length - 1];
    }

    /**
     * Parse domain từ một URL
     *
     * @param url URL to be parsed
     * @return domain parsed from URL
     */
    public static String parseDomain(String url) {
        String prefix = url.startsWith("https://") ? "https://" : "http://";
        int end = url.indexOf("/", prefix.length() + 1);
        if (end == -1) {
            return url.substring(prefix.length());
        } else {
            String domain = url.substring(prefix.length(), url.indexOf("/", prefix.length() + 1));
            String[] split = domain.split("\\.");
            int len = split.length;
            if (len > 2) {
                domain = split[len - 2] + "." + split[len - 1];
            }

            return domain;
        }
    }

    /**
     * Parse fanpage ID từ ID một bài post trong page trên Facebook. <br/>
     * Ví dụ với post ID: <i>503607546449544_1015637608579866</i>
     * thì page ID sẽ là: <i>503607546449544</i>
     *
     * @param postId post ID
     * @return page ID
     */
    public static String parsePageId(String postId) {
        return NullProtector.get(postId.split("_"), 0).orElse(null);
    }

    /**
     * Kiểm tra một chuỗi có đủ điều kiện là nickname (account) trên forum hay không ?
     *
     * @param source chuỗi đầu vào cần kiểm tra
     * @return true nếu source đủ điều kiện là nickname, ngược lại trả về false
     */
    public static boolean detectNicknameForum(String source) {
        boolean notOk = source.length() < 5
                || Strings.containsOnce(source, " ")
                || RegexHelper.listMatched(source, "[^\\x00-\\x7F]").size() > 0;
        return !notOk;
    }

    /**
     * Kiểm tra một chuỗi có đủ điều kiện là nickname (account) trên enbac.com hay không ?
     *
     * @param source chuỗi đầu vào cần kiểm tra
     * @return true nếu source đủ điều kiện là nickname, ngược lại trả về false
     */
    public static boolean detectNicknameEcom(String source) {
        boolean notOk = source.length() <= 3
                || Strings.containsOnce(source, " ")
                || RegexHelper.listMatched(source, "[^\\x00-\\x7F]").size() > 0;
        return !notOk;
    }

    /**
     * Kiểm tra một chuỗi có phải là địa chỉ website hay không ?
     *
     * @param source chuỗi đầu vào cần kiểm tra
     * @return true nếu source là địa chỉ website, ngược lại trả về false
     */
    public static boolean isWebsite(String source) {
        return source.toLowerCase().matches("(http://|https://)*(www.)*[a-z0-9_\\-]*[.a-z]*");
    }

    public static void main(String[] args) {
        System.out.println(detectNicknameForum("TanNg"));
        System.out.println(isWebsite("nhattao.com"));
    }
}
