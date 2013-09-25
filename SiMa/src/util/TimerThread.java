package util;

public class TimerThread extends Thread {
    int dTime;                  // milliseconds
    //volatile boolean shouldRun; // false to end thread

    public TimerThread(int dTime) {
        this.dTime = dTime;
    }

    public void run() {
        try {
            sleep(dTime);
        } catch(InterruptedException ie) {}
    }

}

