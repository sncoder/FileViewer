package cn.sncoder.fv.bean;

/**
 * 用于前后端传输信息的类，表示磁盘空间信息，单位：字节
 *
 * @author shibiao
 */
public class DiskInfo {

    /**
     * 磁盘总空间
     */
    private long total;

    /**
     * 磁盘可用空间
     */
    private long usable;

    /**
     * 磁盘已用空间
     */
    private long used;

    public DiskInfo() {
    }

    public DiskInfo(long total, long usable, long used) {
        this.total = total;
        this.usable = usable;
        this.used = used;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getUsable() {
        return usable;
    }

    public void setUsable(long usable) {
        this.usable = usable;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }
}
