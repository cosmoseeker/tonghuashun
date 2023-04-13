package org.qianyuxiang.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.qianyuxiang.model.IdentityEntity;
import org.qianyuxiang.repositry.IndicatorDao;
import org.qianyuxiang.repositry.impl.FileIndicatorDaoImpl;

import java.nio.file.Paths;
import java.util.List;

//@RunWith(SpringRunner.class)
//@SpringBootTest
@ExtendWith({JUnitPerfInterceptor.class})
public class IndicatorServiceImplTest {
    //@Resource
    public IndicatorServiceImpl indicatorService;

    @JUnitPerfTestActiveConfig
    private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator(Paths.get("src", "test", "resources").toAbsolutePath() + "/build/reports/" + "success.html"))
            .build();

    @BeforeEach
    public void setUp() {
        indicatorService = new IndicatorServiceImpl();
        IndicatorDao indicatorDao = new FileIndicatorDaoImpl();
        indicatorService.setIndicatorDao(indicatorDao);
    }

    @Test
    @JUnitPerfTest(threads = 100, durationMs = 10_000)
    public void testQuery() {
        try {
            List<IdentityEntity> result = indicatorService.query("2022", "ROE", true, 10);
            //System.out.println(JSON.toJSONString(result, JSONWriter.Feature.PrettyFormat));

            //result = fileQueryServiceImpl.query("2022", "ROE", false, Integer.valueOf(10));
            //System.out.println(JSON.toJSONString(result, JSONWriter.Feature.PrettyFormat));
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme