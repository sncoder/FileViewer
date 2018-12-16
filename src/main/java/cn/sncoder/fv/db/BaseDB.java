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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 这是一个抽象基类，定义了基本的方法
 * 因为本项目目前需要存储的用户数据不多，暂时不需要关系型数据库，直接使用json纯文本存储数据，json文本在 classpath:/db/ 下
 *
 * @author shibiao
 * @param <T> 该DB类操作的数据类型
 */
@Repository
public abstract class BaseDB<T> {

    /**
     * json文本编码
     */
    private static final String ENCODING = "UTF-8";

    /**
     * json数据库文件的实际{@link File}对象
     */
    protected File dbFile;

    /**
     * 实际存储的对象集合
     */
    protected Set<T> objs;

    /**
     * 该DB类所操作的类型的{@link Class}对象
     */
    private Class<T> clazz;

    /**
     * 操作json数据库时的锁，防止同时操作文件产生的不可预知的后果
     */
    protected Lock lock = new ReentrantLock();

    /**
     * 标识是否需要将{@link #objs}的数据写入到json数据库中
     */
    private volatile boolean needUpdate;

    /**
     * 由子类实现，返回json数据库文件的classpath路径
     *
     * @return 返回数据库文件的classpath路径
     */
    protected abstract String getDBPath();

    @Resource
    private ThreadPoolService threadPoolService;

    /**
     * 将{@link #objs}写入json数据库的间隔时间（s），子类可以在static代码块或构造函数中修改此值
     */
    protected int saveJsonFileInterval = 60;

    /**
     * Spring容器初始化时调用此方法，将json数据库的数据读取到{@link #objs}中
     *
     * @throws URISyntaxException json数据库路径错误会发生此异常
     * @throws IOException IO操作可能会发生此异常
     */
    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() throws URISyntaxException, IOException {
        URL url = getClass().getResource(getDBPath());
        dbFile = new File(url.toURI());
        clazz = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        reload();
        // 启动自动保存 json 数据库的线程，每隔 writeFileInterval 秒执行一次
        threadPoolService.scheduleWithFixedDelay(new WriteFileRunnable(), saveJsonFileInterval, saveJsonFileInterval, TimeUnit.SECONDS);
    }

    /**
     * Spring 容器销毁时调用此方法，强制将 {@link #objs} 中的数据保存到 json 数据库中
     *
     * @throws IOException 保存中可能会发生异常
     */
    @PreDestroy
    public void destroy() throws IOException {
        write();
    }

    /**
     * 重新读取 json 数据库，未保存的数据会丢失
     *
     * @throws IOException 读取中可能会发生异常
     */
    protected void reload() throws IOException {
        try {
            lock.lock();
            String text = FileUtils.readFileToString(dbFile, ENCODING);
            objs = Collections.synchronizedSet(new HashSet<>(JSONArray.parseArray(text, clazz)));
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将 {@link #objs} 的对象转换成 json 文本并保存到 json 数据库中
     *
     * @throws IOException
     */
    private void write() throws IOException {
        try {
            lock.lock();
            String text = JSONArray.toJSONString(objs);
            FileUtils.writeStringToFile(dbFile, text, ENCODING);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 标识 {@link #objs} 的数据进行了更新，子类实现的方法中，如果更新了 {@link #objs} 的内容，则一定要调用此方法或 {@link #needUpdate(boolean)} 方法
     */
    protected void needUpdate() {
        needUpdate = true;
    }

    /**
     * 标识{@link #objs}的数据进行了更新，子类实现的方法中，如果更新了{@link #objs}的内容，则一定要调用此方法或{@link #needUpdate()}方法
     *
     * @param isNeed 如果传入{@code false}，则表示不需要更新
     */
    protected void needUpdate(boolean isNeed) {
        if (isNeed) {
            needUpdate = true;
        }
    }

    /**
     * 保存json数据库的线程
     */
    private class WriteFileRunnable implements Runnable {

        @Override
        public void run() {
            try {
                if (needUpdate) {
                    write();
                    needUpdate = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
