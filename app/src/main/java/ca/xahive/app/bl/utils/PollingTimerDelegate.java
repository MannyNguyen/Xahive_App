package ca.xahive.app.bl.utils;

public interface PollingTimerDelegate {
    public abstract void timerDidFire(PollingTimer timer);
}
