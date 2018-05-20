package cn.sncoder.fv.db;

import cn.sncoder.fv.service.ThreadPoolService;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Repository
public abstract class BaseDB<T> {

    private static final String ENCODING = "UTF-8";

    protected File dbFile;

    protected Set<T> objs;

    private Class<T> clazz;

    protected Lock lock = new ReentrantLock();

    private volatile boolean needUpdate;

    protected abstract String getDBPath();

    @Resource
    private ThreadPoolService threadPoolService;

    protected int writeFileInterval = 60000;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() throws URISyntaxException, IOException {
        URL url = getClass().getResource(getDBPath());
        dbFile = new File(url.toURI());
        clazz = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        reload();
        threadPoolService.execute(new WriteFileRunnable());
    }

    @PreDestroy
    public void destroy() throws IOException {
        write();
    }

    protected void reload() throws IOException {
        try {
            lock.lock();
            String text = FileUtils.readFileToString(dbFile, ENCODING);
            objs = Collections.synchronizedSet(new HashSet<>(JSONArray.parseArray(text, clazz)));
        } finally {
            lock.unlock();
        }
    }

    private void write() throws IOException {
        try {
            lock.lock();
            String text = JSONArray.toJSONString(objs);
            FileUtils.writeStringToFile(dbFile, text, ENCODING);
        } finally {
            lock.unlock();
        }
    }

    protected void needUpdate() {
        needUpdate = true;
    }

    protected void needUpdate(boolean isNeed) {
        if (isNeed) {
            needUpdate = true;
        }
    }

    private class WriteFileRunnable implements Runnable {

        private boolean stop;

        @Override
        public void run() {
            try {
                Thread.sleep(writeFileInterval);
                if (needUpdate) {
                    write();
                    needUpdate = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException ignore) {
                stop = true;
            } finally {
                if (!stop) {
                    threadPoolService.execute(new WriteFileRunnable());
                }
            }
        }
    }

}
