package org.venth.training.rxjavahystrix;

import rx.Observable;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * @author Venth on 23/07/2015
 */
public class ExternalSystemClient {

    static final int FIRST_NAME_SERVICE_TIMEOUT_IN_SECONDS = 1;

    private FirstNameService firstNameService;
    private int firstNameRetryTimes;
    private LastNameService lastNameService;
    private int lastNameRetryTimes;

    public ExternalSystemClient(FirstNameService firstNameService, LastNameService lastNameService) {
        this();
        this.firstNameService = firstNameService;
        this.lastNameService = lastNameService;
    }

    public ExternalSystemClient() {
        firstNameRetryTimes = 0;
        lastNameRetryTimes = 0;
    }

    public String personName() {
        return createFetchFirstNameCommand()
                .zipWith(createFetchLastNameCommand(), (firstName, lastName) -> firstName + lastName)
                .toBlocking()
                .single();
    }

    private Observable<String> createFetchFirstNameCommand() {
        return Observable.defer(() ->
                new HystrixCommand<String>(
                    HystrixCommandGroupKey.Factory.asKey("person name services"),
                    FIRST_NAME_SERVICE_TIMEOUT_IN_SECONDS * 1000
                ) {

                    @Override
                    protected String run() throws Exception {
                        return firstNameService.firstName();
                    }

                }.toObservable()).retry(firstNameRetryTimes);
    }

    private Observable<String> createFetchLastNameCommand() {
        return Observable.defer(() -> new HystrixCommand<String>(HystrixCommandGroupKey.Factory.asKey("person name services")) {

            @Override
            protected String run() throws Exception {
                return lastNameService.lastName();
            }
        }.toObservable()).retry(lastNameRetryTimes);
    }

    public void setFirstNameService(FirstNameService firstNameService, int retryTimes) {
        this.firstNameService = firstNameService;
        this.firstNameRetryTimes = retryTimes;
    }

    public void setLastNameService(LastNameService lastNameService, int lastNameRetryTimes) {
        this.lastNameService = lastNameService;
        this.lastNameRetryTimes = lastNameRetryTimes;
    }
}

