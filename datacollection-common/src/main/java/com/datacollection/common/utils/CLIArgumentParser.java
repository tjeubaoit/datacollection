package com.datacollection.common.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Map;
import java.util.TreeMap;

/**
 * Dùng để parse các argument được truyền vào ứng dụng dưới dạng command line
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class CLIArgumentParser {

    private final Options options;
    private final CommandLineParser parser;

    private CLIArgumentParser(Options options) {
        this.options = options;
        this.parser = new DefaultParser();
    }

    public void printHelpUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("utility-name", options);
    }

    public Result parse(String[] args) {
        try {
            CommandLine cmd = parser.parse(options, args);
            return new Result(cmd, options);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static class Result {

        private final CommandLine cmd;
        private final Options options;

        private Result(CommandLine cmd, Options options) {
            this.cmd = cmd;
            this.options = options;
        }

        public Map<String, String> getOptions() {
            Map<String, String> map = new TreeMap<>();
            for (Option opt : options.getOptions()) {
                map.put(opt.getLongOpt(), getOptionValue(opt.getLongOpt()));
            }
            return map;
        }

        public String getOptionValue(String opt) {
            return this.cmd.getOptionValue(opt);
        }

        public String getOptionValue(String opt, String defVal) {
            return this.cmd.getOptionValue(opt, defVal);
        }

        public String[] getAppArgs() {
            return this.cmd.getArgs();
        }
    }

    public static class Builder {

        private final Options options = new Options();

        public Builder addOpt(String opt, String longOpt, boolean hasArgs,
                              String description, boolean required) {
            Option option = new Option(opt, longOpt, hasArgs, description);
            option.setRequired(required);
            this.options.addOption(option);
            return this;
        }

        public CLIArgumentParser build() {
            return new CLIArgumentParser(this.options);
        }
    }
}
