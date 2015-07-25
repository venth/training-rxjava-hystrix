package org.venth.training.rxjavahystrix;

import rx.Observable;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import java.util.concurrent.TimeUnit;

/**
 * @author Venth on 23/07/2015
 */
public class ExternalSystemClient {
    static final int FIRST_NAME_SERVICE_TIMEOUT_IN_SECONDS = 5;
    static final long LAST_NAME_SERVICE_TIMEOUT_IN_SECONDS = 10;

    private final FirstNameService firstNameService;
    private final LastNameService lastNameService;

    public ExternalSystemClient(FirstNameService firstNameService, LastNameService lastNameService) {

        this.firstNameService = firstNameService;
        this.lastNameService = lastNameService;
    }

    public String personName() {
        return createFetchFirstNameCommand()
                .zipWith(createFetchLastNameCommand(), (firstName, lastName) -> firstName + lastName)
                .firstOrDefault("not found")
                .toBlocking()
                .first();
    }

    private Observable<String> createFetchFirstNameCommand() {
        return new HystrixCommand<String>(HystrixCommandGroupKey.Factory.asKey("person name services")) {

            @Override
            protected String run() throws Exception {
                return firstNameService.firstName();
            }
        }.toObservable().timeout(FIRST_NAME_SERVICE_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    }

    private Observable<String> createFetchLastNameCommand() {
        return new HystrixCommand<String>(HystrixCommandGroupKey.Factory.asKey("person name services")) {

            @Override
            protected String run() throws Exception {
                return lastNameService.lastName();
            }
        }.toObservable().timeout(LAST_NAME_SERVICE_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    }
}

