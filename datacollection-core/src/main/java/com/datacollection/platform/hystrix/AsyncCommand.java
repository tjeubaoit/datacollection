package com.datacollection.platform.hystrix;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;

import java.util.concurrent.Callable;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class AsyncCommand<T> extends HystrixObservableCommand<T> {

    private final Callable<T> callable;

    public AsyncCommand(String group, String name, Callable<T> callable) {
        super(HystrixObservableCommand.Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(group))
                .andCommandKey(HystrixCommandKey.Factory.asKey(name)));
        this.callable = callable;
    }

    @Override
    protected Observable<T> construct() {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(callable.call());
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }
}
