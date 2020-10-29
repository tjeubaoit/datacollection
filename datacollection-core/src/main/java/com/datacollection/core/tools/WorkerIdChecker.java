package com.datacollection.core.tools;

import com.datacollection.common.types.IdGenerator;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class WorkerIdChecker {

    public static void main(String[] args) {
        long workerId = IdGenerator.createWorkerIdentifier();
        for (int i = 10; i <= 13; i++) {
            int max = 1 << i;
            int mask = max - 1;
            System.out.println(i + " bits identifier: " + ((workerId % max) & mask));
        }
        System.out.println("Raw id: " + (workerId & Integer.MAX_VALUE));
    }
}
