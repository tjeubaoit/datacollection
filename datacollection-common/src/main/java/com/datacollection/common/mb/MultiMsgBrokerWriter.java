package com.datacollection.common.mb;

import com.datacollection.common.concurrency.AllInOneFuture;
import com.datacollection.common.concurrency.FutureAdapter;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Reflects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

public class MultiMsgBrokerWriter implements MsgBrokerWriter {

    public List<MsgBrokerWriter> msgBrokerWriters = new ArrayList<>();

    public MultiMsgBrokerWriter(MsgBrokerWriter... msgBrokerWriters) {
        this.msgBrokerWriters.addAll(Arrays.asList(msgBrokerWriters));
    }

    @Override
    public Future<Long> write(byte[] b) {
        List<Future<Long>> futures = new ArrayList<>();
        for (MsgBrokerWriter msgBrokerWriter : msgBrokerWriters) {
            futures.add(msgBrokerWriter.write(b));
        }

        Future<List<Long>> fut = AllInOneFuture.from(futures);
        return FutureAdapter.from(fut, list -> (long) list.size());
    }

    @Override
    public void configure(Properties p) {
        List<String> factoryClasses = p.getCollection("mb.multi.factory.classes");
        factoryClasses.forEach(factory -> {
            MsgBrokerFactory msgBrokerFactory = Reflects.newInstance(factory);
            MsgBrokerWriter msgBrokerWriter = msgBrokerFactory.createWriter();
            msgBrokerWriter.configure(p);
            this.msgBrokerWriters.add(msgBrokerWriter);
        });
    }

    public List<MsgBrokerWriter> getMsgBrokerWriters() {
        return msgBrokerWriters;
    }

    public void setMsgBrokerWriters(List<MsgBrokerWriter> msgBrokerWriters) {
        this.msgBrokerWriters = msgBrokerWriters;
    }
}
