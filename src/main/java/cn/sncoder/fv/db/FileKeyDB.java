package cn.sncoder.fv.db;

import cn.sncoder.fv.bean.FileKey;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 操作{@link FileKey}的DB类
 *
 * @author shibiao
 */
@Repository
public class FileKeyDB extends BaseDB<FileKey> {

    @Override
    protected String getDBPath() {
        return "/db/file.json";
    }

    /**
     * 根据key值获取{@link FileKey}对象
     *
     * @param key key值
     * @return 返回 {@link FileKey}对象，不存在则返回 {@link null}
     */
    public FileKey byKey(String key) {
        Optional<FileKey> optional = objs.stream().filter(fileKey -> fileKey.getKey().equals(key)).findFirst();
        return optional.orElse(null);
    }

    /**
     * 根据绝对路径获取{@link FileKey}对象
     *
     * @param absPath 绝对路径
     * @return 返回 {@link FileKey}对象，不存在则返回 {@link null}
     */
    public FileKey byAbsPath(String absPath) {
        Optional<FileKey> optional = objs.stream().filter(fileKey -> fileKey.getAbsPath().equals(absPath)).findFirst();
        return optional.orElse(null);
    }

    /**
     * 判断key值是否存在
     *
     * @param fileKey {@link FileKey}对象
     * @return 如果存在，返回{@code true}，否则返回{@code false}
     */
    public boolean contains(FileKey fileKey) {
        return objs.stream().anyMatch(fk -> fk.equals(fileKey));
    }

    /**
     * 判断绝对路径是否存在
     *
     * @param absPath 绝对路径
     * @return 如果存在，返回{@code true}，否则返回{@code false}
     */
    public boolean containsAbsPath(String absPath) {
        return objs.stream().anyMatch(fileKey -> fileKey.getAbsPath().equals(absPath));
    }

    /**
     * 判断key值是否存在
     *
     * @param key key值
     * @return 如果存在，返回{@code true}，否则返回{@code false}
     */
    public boolean containsKey(String key) {
        return objs.stream().anyMatch(fileKey -> fileKey.getKey().equals(key));
    }

    /**
     * 保存一个{@link FileKey}
     *
     * @param fileKey {@link FileKey}对象
     * @return 如果保存成功，返回{@code true}，否则返回{@code false}
     */
    public boolean save(FileKey fileKey) {
        if (contains(fileKey)) {
            return false;
        }
        boolean success = objs.add(fileKey);
        needUpdate(success);
        return success;
    }

    /**
     * 修改绝对路径
     *
     * @param oldAbsPath 旧的绝对路径
     * @param newAbsPath 新的绝对路径
     * @return 如果修改成功，返回{@code true}，否则返回{@code false}
     */
    public boolean updateAbsPath(String oldAbsPath, String newAbsPath) {
        if (!containsAbsPath(oldAbsPath)) { return false; }

        try {
            lock.lock();
            for (FileKey fileKey : objs) {
                if (fileKey.getAbsPath().equals(oldAbsPath)) {
                    fileKey.setAbsPath(newAbsPath);
                    needUpdate();
                    return true;
                }
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * 根据key值删除一个{@link FileKey}
     *
     * @param key key值
     * @return 如果删除成功，返回{@code true}，否则返回{@code false}
     */
    public boolean removeByKey(String key) {
        try {
            lock.lock();
            boolean success = objs.removeIf(fileKey -> fileKey.getKey().equals(key));
            needUpdate(success);
            return success;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 根据绝对路径删除一个{@link FileKey}
     *
     * @param absPath 绝对路径
     * @return 如果删除成功，返回{@code true}，否则返回{@code false}
     */
    public boolean removeByAbsPath(String absPath) {
        try {
            lock.lock();
            boolean success = objs.removeIf(fileKey -> fileKey.getAbsPath().equals(absPath));
            needUpdate(success);
            return success;
        } finally {
            lock.unlock();
        }
    }

}
