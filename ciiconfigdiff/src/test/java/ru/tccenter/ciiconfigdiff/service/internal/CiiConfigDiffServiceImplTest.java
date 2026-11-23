package ru.tccenter.ciiconfigdiff.service.internal;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CiiConfigDiffServiceImplTest {

    @InjectMocks
    private CiiConfigDiffServiceImpl ciiConfigDiffService;

    @Mock
    private  CiiConfigFetchService fetchService;
    @Mock
    private  CiiConfigNormalizeService normalizeService;
    @Mock
    private  CiiConfigHashService hashService;
    @Mock
    private  CiiConfigCompareService compareService;
    @Mock
    private  CiiConfigReportService reportService;
}