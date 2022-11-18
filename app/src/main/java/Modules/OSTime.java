package Modules;

public class OSTime {

    public int dayofweak;

    public int open_hour;
    public int open_min;

    public int close_hour;
    public int close_min;

    public OSTime(int dayofweak, int open_hour, int open_min, int close_hour, int close_min) {
        this.dayofweak = dayofweak;
        this.open_hour = open_hour;
        this.open_min = open_min;
        this.close_hour = close_hour;
        this.close_min = close_min;
    }

    public OSTime() {

    }

    public int getDayofweak() {
        return dayofweak;
    }

    public void setDayofweak(int dayofweak) {
        this.dayofweak = dayofweak;
    }

    public int getOpen_hour() {
        return open_hour;
    }

    public void setOpen_hour(int open_hour) {
        this.open_hour = open_hour;
    }

    public int getOpen_min() {
        return open_min;
    }

    public void setOpen_min(int open_min) {
        this.open_min = open_min;
    }

    public int getClose_hour() {
        return close_hour;
    }

    public void setClose_hour(int close_hour) {
        this.close_hour = close_hour;
    }

    public int getClose_min() {
        return close_min;
    }

    public void setClose_min(int close_min) {
        this.close_min = close_min;
    }
}
