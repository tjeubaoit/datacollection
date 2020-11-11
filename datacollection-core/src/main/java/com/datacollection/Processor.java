package com.datacollection;

public interface Processor {

    void onLoaded(ProcessorContext context);

    void onProcess(ProcessorContext context, ProcessorRuntime runtime);
}
