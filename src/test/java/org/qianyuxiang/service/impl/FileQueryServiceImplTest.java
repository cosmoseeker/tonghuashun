package org.qianyuxiang.service.impl;

import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.qianyuxiang.model.IdentityEntity;

import java.nio.file.Paths;
import java.util.List;

import static java.lang.System.getProperty;

@ExtendWith({JUnitPerfInterceptor.class})
public class FileQueryServiceImplTest {
    public FileQueryServiceImpl fileQueryServiceImpl;

    @JUnitPerfTestActiveConfig
    private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator(Paths.get("src","test","resources").toAbsolutePath() + "/build/reports/" + "success.html"))
            .build();

    @BeforeEach
    public void setUp() {
        fileQueryServiceImpl = new FileQueryServiceImpl();
    }

    @Test
    @JUnitPerfTest(threads = 100, durationMs = 10_000)
    public void testQuery() {
        try {
            List<IdentityEntity> result = fileQueryServiceImpl.query("2022", "ROE", true, Integer.valueOf(10));
            //System.out.println(JSON.toJSONString(result, JSONWriter.Feature.PrettyFormat));

            //result = fileQueryServiceImpl.query("2022", "ROE", false, Integer.valueOf(10));
            //System.out.println(JSON.toJSONString(result, JSONWriter.Feature.PrettyFormat));
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme