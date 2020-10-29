package com.datacollection.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class để làm việc với Regex nhanh hơn. Cho phép thêm vào các
 * Regex cùng với tên và sử dụng tên để làm việc.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class RegexHelper {

    private final Map<String, String> regexes = new HashMap<>();

    /**
     * Thêm một regex
     *
     * @param name  tên regex
     * @param regex chuỗi regex
     */
    public void addRegex(String name, String regex) {
        regexes.put(name, regex);
    }

    /**
     * Kiểm tra một chuỗi có match với một regex hay không
     *
     * @param name  tên của regex cần kiểm tra
     * @param input chuỗi cần kiểm tra
     * @return true nếu chuỗi match và false nếu ngược lại
     */
    public boolean isMatch(String name, String input) {
        String regex = ensureValidName(name);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        return matcher.matches();
    }

    /**
     * Tách các chuỗi con của một chuỗi thỏa mãn một regex
     *
     * @param name  tên của regex cần kiểm tra
     * @param input chuỗi đầu vào
     * @return collection String chứa các chuỗi con thỏa mãn yêu cầu
     */
    public Collection<String> extract(String name, String input) {
        String regex = ensureValidName(name);
        return listMatched(input, regex);
    }

    private String ensureValidName(String name) {
        String regex = regexes.get(name);
        if (regex == null) throw new IllegalArgumentException("Not found regex with name: " + name);

        return regexes.get(name);
    }

    /**
     * Tách các chuỗi con của một chuỗi thỏa mãn một regex
     *
     * @param input chuỗi đầu vào
     * @param regex regex đang xét
     * @return collection String chứa các chuỗi con thỏa mãn yêu cầu
     */
    public static Collection<String> listMatched(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        Collection<String> results = new LinkedList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results;
    }
}
