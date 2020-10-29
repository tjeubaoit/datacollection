package com.datacollection.core.platform.hystrix;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;

import java.util.concurrent.Future;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FutureCommand<T> extends HystrixObservableCommand<T> {

    private final Future<T> future;

    public FutureCommand(String group, String name, Future<T> future) {
        super(HystrixObservableCommand.Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(group))
                .andCommandKey(HystrixCommandKey.Factory.asKey(name)));
        this.future = future;
    }

    @Override
    protected Observable<T> construct() {
        return Observable.from(this.future);
    }
}
