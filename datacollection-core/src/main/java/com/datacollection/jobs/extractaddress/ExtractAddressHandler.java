package com.datacollection.jobs.extractaddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
class ExtractAddressHandler implements Runnable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String provincesRegex;
    private final Map<String, String> districtsRegexMap;
    private final long uid;
    private final ExtractAddressRepository repository;

    public ExtractAddressHandler(long uid, String provincesRegex, Map<String, String> districtsRegexMap,
                                 ExtractAddressRepository repository) {
        this.provincesRegex = provincesRegex;
        this.districtsRegexMap = districtsRegexMap;
        this.uid = uid;
        this.repository = repository;
    }

    @Override
    public void run() {
        extractAddress();
    }

    private void extractAddress() {
        for (String content : repository.findAllContentsByUid(uid)) {
            content = content.toLowerCase().trim();

            String address = firstMatched(content, provincesRegex).trim();
            if (!address.isEmpty()) {
                String regex = districtsRegexMap.get(address) + "[ ,\\.-]*" + address + "";
                String district = firstMatched(content, regex);
                if (!district.isEmpty()) {
                    if (district.equals(address.trim())) {
                        district = "tp " + district;
                    } else if ("hồ chí minh".equals(address) && isNumber(district)) {
                        district = "quận " + district;
                    }
                    address = district + " - " + address;
                }
            }

            if (!address.trim().isEmpty()) {
                repository.updateAddress(uid, address);
                logger.info(Thread.currentThread().getName() + " " +
                        String.format(Locale.US, "[%s - %s]", String.valueOf(uid), address));
                break;
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public static Map<String, String> buildDistrictsRegex(List<Province> provinces) {
        Map<String, String> regexes = new HashMap<>();
        for (Province p : provinces) {
            StringBuilder sb = new StringBuilder();
            for (District d : p.districts) {
                sb.append("|").append(d.name.replace('_', ' '));
            }
            regexes.put(p.name.replace('_', ' '), sb.toString().substring(1));
        }
        return regexes;
    }

    public static String buildProvincesRegex(List<Province> provinces) {
        StringBuilder sb = new StringBuilder();
        for (Province p : provinces) {
            sb.append("|").append(p.name.replace('_', ' '));
        }
        return sb.toString().substring(1);
    }

    public static String firstMatched(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
}
