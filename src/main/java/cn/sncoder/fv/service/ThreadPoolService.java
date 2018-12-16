package cn.sncoder.fv.service;

import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 主要用途是创建一个由Spring IOC容器管理的线程池
 *
 * @author shibiao
 */
@Service
public class ThreadPoolService extends ScheduledThreadPoolExecutor {

    public ThreadPoolService() {
        super(10);
    }

    /**
     * 销毁方法，在Spring容器销毁时由Spring自动调用
     */
    @PreDestroy
    public void destroy() {
        shutdownNow();
    }

}
