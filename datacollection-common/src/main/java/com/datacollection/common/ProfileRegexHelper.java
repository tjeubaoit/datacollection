package com.datacollection.common;

import com.datacollection.common.config.ConfigurationException;
import com.datacollection.common.io.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mở rộng của RegexHelper, đã được thêm các regex để làm việc với phone/email
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ProfileRegexHelper extends RegexHelper {

    public static final String EMAIL_REGEX_PATH = "config/email-regex.txt";
    public static final String PHONE_REGEX_PATH = "config/phone-regex.txt";

    private static final Logger logger = LoggerFactory.getLogger(ProfileRegexHelper.class);
    private final String emailRegex;
    private final String phoneRegex;

    /**
     * Khởi tạo từ phone regex và email regex cụ thể
     *
     * @param emailRegex regex để validate email
     * @param phoneRegex regex để validate phone
     */
    public ProfileRegexHelper(String emailRegex, String phoneRegex) {
        this.emailRegex = emailRegex;
        this.phoneRegex = phoneRegex;
    }

    /**
     * Khởi tạo giá trị mặc định. Các regex phone và email được load tự động từ các
     * file <i>email-regex.txt</i> và <i>phone-regex.txt</i> trong thư mục config
     */
    private ProfileRegexHelper() {
        try {
            this.emailRegex = FileHelper.readAsString(EMAIL_REGEX_PATH);
            this.phoneRegex = FileHelper.readAsString(PHONE_REGEX_PATH);
            addRegex("email", emailRegex);
            addRegex("phone", phoneRegex);
        } catch (IOException e) {
            logger.warn("Cannot load regex from file");
            throw new ConfigurationException(e);
        }
    }

    /**
     * Kiểm tra một chuỗi có phải là email hay không
     *
     * @param input chuỗi cần kiểm tra
     * @return true nếu chuỗi là email hợp lệ và ngược lại
     */
    public boolean isEmail(String input) {
        return isMatch("email", input);
    }

    /**
     * Kiểm tra một chuỗi có phải là phone hay không
     *
     * @param input chuỗi cần kiểm tra
     * @return true nếu chuỗi là phone hợp lệ và ngược lại
     */
    public boolean isPhone(String input) {
        return isMatch("phone", input);
    }

    /**
     * Bóc tách các email từ một chuỗi
     *
     * @param input chuỗi đầu vào chứa các emails
     * @return Danh sách các emails lấy được từ chuỗi đã được lọc trùng
     */
    public Set<String> extractEmails(String input) {
        Set<String> set = new HashSet<>();
        for (String email : listMatched(input, emailRegex)) {
            set.add(email.toLowerCase());
        }
        return set;
    }

    /**
     * Bóc tách các phone từ một chuỗi
     *
     * @param input chuỗi đầu vào chứa các phones
     * @return Danh sách các phones lấy được từ chuỗi đã được lọc trùng
     */
    public Set<String> extractPhones(String input) {
        String newInput = preProcessingPhone(input);
        Set<String> set = new HashSet<>();
        for (String phone : listMatched(newInput, phoneRegex)) {
            set.add(phone.replaceAll("\\D", ""));
        }
        return set;
    }

    /**
     * @return Lấy về regex dùng để validate email
     */
    public String emailRegex() {
        return emailRegex;
    }

    /**
     * @return Lấy về regex dùng để validate phone
     */
    public String phoneRegex() {
        return phoneRegex;
    }

    /**
     * Tiền xử lý phone trước khi đưa vào kiểm tra bởi Regex
     *
     * @param content nội dung chuỗi cần xử lý
     * @return chuỗi đã được tiền xử lý
     */
    public static String preProcessingPhone(String content) {
        List<String> regexList = Arrays.asList("\\.", "-", "\\(", "\\)", "\\[", "\\]");
        for (String regex : regexList) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(content);
            content = m.replaceAll(" ");
        }
        Pattern p = Pattern.compile("\\+84");
        Matcher m = p.matcher(content);
        content = m.replaceAll("0");
        return content;
    }

    private static volatile ProfileRegexHelper instance;

    /**
     * @return Khởi tạo hoặc lấy về giá trị mặc định
     */
    public static synchronized ProfileRegexHelper getDefault() {
        if (instance == null) {
            instance = new ProfileRegexHelper();
            logger.info("Email regex: " + instance.emailRegex());
            logger.info("Phone regex: " + instance.phoneRegex());
        }
        return instance;
    }
}
