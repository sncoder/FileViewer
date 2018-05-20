package cn.sncoder.fv.bean;

public class DiskInfo {

    private long total;
    private long usable;
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
