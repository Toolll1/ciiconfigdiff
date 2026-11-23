package ru.tccenter.ciiconfigdiff.service.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class CiiConfigFetchServiceTest {

    @InjectMocks
    private CiiConfigFetchService ciiConfigFetchService;

    @Mock
    private  RestClient restClient;
    @Mock
    private  ObjectMapper objectMapper;
}