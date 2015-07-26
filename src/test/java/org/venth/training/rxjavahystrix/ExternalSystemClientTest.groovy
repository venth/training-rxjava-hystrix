package org.venth.training.rxjavahystrix

import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier
import java.util.stream.Stream

/**
 * @author Venth on 25/07/2015
 */
class ExternalSystemClientTest extends Specification {

    final static Logger LOG = LoggerFactory.getLogger(ExternalSystemClientTest)

    @Test
    def "retry 4 time(s) in case of timeout"() {

        given: "after 4 call(s) first name service returns a person name "
        def retryTimesInCaseOfTimeout = 4
        def personFirstName = "Luke"
        def exceedTimeoutByASecond = (ExternalSystemClient.FIRST_NAME_SERVICE_TIMEOUT_IN_SECONDS + 1) * 1000
        def firstNameService = Mock(FirstNameService)

        firstNameService.firstName() >> {
            LOG.debug("Latency appeared")
            Thread.sleep(exceedTimeoutByASecond)

            personFirstName
        } >> {
            LOG.debug("Latency appeared")
            Thread.sleep(exceedTimeoutByASecond)

            personFirstName
        } >> {
            LOG.debug("Latency appeared")
            Thread.sleep(exceedTimeoutByASecond)

            personFirstName
        } >> {
            LOG.debug("Latency appeared")
            Thread.sleep(exceedTimeoutByASecond)

            personFirstName
        } >> personFirstName

        and: "last name service returns luke's last name"
        def personLastName = 'Skywalker'
        def lastNameService = Mock(LastNameService)
        lastNameService.lastName() >> personLastName

        and: 'the system client uses external systems'
        def client = new ExternalSystemClient()
        client.setFirstNameService(firstNameService, retryTimesInCaseOfTimeout)
        client.setLastNameService(lastNameService, 0)

        when: "external services are called for person's name"
        def personName = client.personName()

        then: "because occurred timeouts on first name service one less than $retryTimesInCaseOfTimeout times person's name is combined"
        "$personFirstName$personLastName" == personName
    }

    @Test
    def "non blocking services are called in a parallel"() {
        given: 'external first name service returns a first name'
        def firstName = "Combined"
        def firstNameService = Mock(FirstNameService)
        firstNameService.firstName() >> firstName

        and: 'external last name service returns a last name'
        def lastName = "Result"
        def lastNameService = Mock(LastNameService)
        lastNameService.lastName() >> lastName

        and: 'the system client uses external systems'
        def client = new ExternalSystemClient(firstNameService, lastNameService)

        when: 'external services are called'
        def combinedResult = client.personName()

        then: 'parallel results are combined'
        combinedResult == firstName + lastName

    }

}
