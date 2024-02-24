package ca.xahive.app.bl.utils;

import android.os.Handler;

public class PollingTimer {
    private static final long DEFAULT_INTERVAL = 5;
    private long interval;
    private Handler handler;
    private Runnable runnable;
    private PollingTimerDelegate delegate;

    public PollingTimer(PollingTimerDelegate delegate) {
        this(delegate, 0);
    }

    public PollingTimer(PollingTimerDelegate delegate, long interval) {
        this.delegate = delegate;
        this.interval = interval;
    }

    private Runnable getRunnable() {
        if (runnable == null) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    timerDidComplete();
                }
            };
        }
        return runnable;
    }

    public long getInterval() {
        return ((interval > 0) ? interval : DEFAULT_INTERVAL) * 1000;
    }

    public void start(boolean immediate) {
        if (handler != null) {
            return;
        }

        if (immediate) {
            getRunnable().run();
        }

        handler = new Handler();
        handler.postDelayed(getRunnable(), getInterval());

    }

    public void stop() {
        if (handler != null) {
            handler.removeCallbacks(getRunnable());
            handler = null;
        }
    }

    private void timerDidComplete() {
        delegate.timerDidFire(this);

        if (handler != null) {
            handler.postDelayed(getRunnable(), getInterval());
        }
    }
}
