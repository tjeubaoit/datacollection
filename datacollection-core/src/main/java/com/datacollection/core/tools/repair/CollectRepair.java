package com.datacollection.core.tools.repair;

import com.datacollection.core.collect.CollectService;
import com.datacollection.core.collect.Constants;
import com.datacollection.core.collect.GraphCollectService;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.utils.Utils;
import com.datacollection.core.extract.model.GenericModel;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class CollectRepair {

    @SuppressWarnings("ConstantConditions")
    public static void main(String[] args) throws IOException {
        Configuration config = new Configuration();
        Logger.getLogger(CollectRepair.class);

        CollectService service = new GraphCollectService(config);

        String logDir = args[0];
        if (!logDir.endsWith("/")) logDir = logDir + "/";
        File dir = new File(logDir);

        int total = 0;
        for (String fileName : dir.list()) {
            if (!fileName.startsWith(Constants.ERROR_LOG_FILE)) continue;
            System.out.println("Handle file: " + fileName);
            int count = 0;
            String line = null;
            try (BufferedReader reader = new BufferedReader(new FileReader(logDir + "/" + fileName))) {
                while ((line = reader.readLine()) != null) {
                    GenericModel model = Utils.fromJson(line.trim(), GenericModel.class, null);
                    if (model != null) {
                        try {
                            service.collect(model);
                            System.out.println("Processing: " + ++count);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Process line error: " + (line != null ? line : "null"));
                throw e;
            }
            total += count;
        }
        System.out.println(total + " records repaired");
    }
}
