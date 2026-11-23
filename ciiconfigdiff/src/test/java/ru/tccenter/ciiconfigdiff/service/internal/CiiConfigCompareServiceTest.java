package ru.tccenter.ciiconfigdiff.service.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CiiConfigCompareServiceTest {

    @InjectMocks
    private CiiConfigCompareService compareService;

    @Mock
    private CiiConfigHashService hashService;
    @Mock
    private ObjectMapper mapper;

}
