package cn.sncoder.fv.db;

import cn.sncoder.fv.bean.FileKey;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.function.Predicate;

@Repository
public class FileKeyDB extends BaseDB<FileKey> {

    @Override
    protected String getDBPath() {
        return "/db/file.json";
    }

    public FileKey byKey(String key) {
        Optional<FileKey> optional = objs.stream().filter(fileKey -> fileKey.getKey().equals(key)).findFirst();
        return optional.orElse(null);
    }

    public FileKey byAbsPath(String absPath) {
        Optional<FileKey> optional = objs.stream().filter(fileKey -> fileKey.getAbsPath().equals(absPath)).findFirst();
        return optional.orElse(null);
    }

    public boolean contains(FileKey fileKey) {
        return objs.stream().anyMatch(fk -> fk.equals(fileKey));
    }

    public boolean containsAbsPath(String absPath) {
        return objs.stream().anyMatch(fileKey -> fileKey.getAbsPath().equals(absPath));
    }

    public boolean containsKey(String key) {
        return objs.stream().anyMatch(fileKey -> fileKey.getKey().equals(key));
    }

    public boolean save(FileKey fileKey) {
        if (contains(fileKey)) {
            return false;
        }
        boolean success = objs.add(fileKey);
        needUpdate(success);
        return success;
    }

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

    public boolean removeByKey(String key) {
        try {
            lock.lock();
            boolean success = removeIf(fileKey -> fileKey.getKey().equals(key)) > 0;
            needUpdate(success);
            return success;
        } finally {
            lock.unlock();
        }
    }

    public boolean removeByAbsPath(String absPath) {
        try {
            lock.lock();
            boolean success = removeIf(fileKey -> fileKey.getAbsPath().equals(absPath)) > 0;
            needUpdate(success);
            return success;
        } finally {
            lock.unlock();
        }
    }

    private int removeIf(Predicate<FileKey> predicate) {
        int oldSize = objs.size();
        objs.removeIf(predicate);
        return objs.size() - oldSize;
    }

}
