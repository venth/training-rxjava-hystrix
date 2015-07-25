package org.venth.training.rxjavahystrix;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Venth on 23/07/2015
 */
public class ExternalSystemClientTest {
    @Test
    public void non_blocking_services_are_called_in_a_parallel() {
        //given external first name service returns a first name
        String firstName = "Combined";
        FirstNameService firstNameService = mock(FirstNameService.class);
        when(firstNameService.firstName()).thenReturn(firstName);

        //and external last name service returns a last name
        String lastName = "Result";
        LastNameService lastNameService = mock(LastNameService.class);
        when(lastNameService.lastName()).thenReturn(lastName);

        //and the system client uses external systems
        ExternalSystemClient client = new ExternalSystemClient(firstNameService, lastNameService);

        //when external services are called
        String combinedResult = client.personName();

        //then parallel results are combined
        assertThat(combinedResult).isEqualTo(firstName + lastName);

    }
}
