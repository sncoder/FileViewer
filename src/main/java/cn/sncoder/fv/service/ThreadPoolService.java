package cn.sncoder.fv.service;

import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class ThreadPoolService extends ThreadPoolExecutor {

    public ThreadPoolService() {
        super(10, 1000, 10, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
    }

    @PreDestroy
    public void destroy() {
        shutdownNow();
    }

}
