package org.venth.training.rxjavahystrix

import org.junit.Test
import spock.lang.Specification

/**
 * @author Venth on 25/07/2015
 */
class ExternalSystemClientTest extends Specification {

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
