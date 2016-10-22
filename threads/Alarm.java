package nachos.threads;

import nachos.machine.*;
import java.util.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        Machine.interrupt().disable();
        Iterator threadWaiting = waitQueue.iterator();
        ThreadTime wakeUp;

        while(threadWaiting.hasNext()){
            wakeUp = (ThreadTime) threadWaiting.next();

            if(Machine.timer().getTime() >= wakeUp.getWakeTime()){
                wakeUp.getThread().ready();
                threadWaiting.remove();    
            }
        }
        KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        long wakeTime = Machine.timer().getTime() + x;
        
        KThread thread = KThread.currentThread();
        ThreadTime threadTime = new ThreadTime(thread, wakeTime);
        
        boolean intStatus = Machine.interrupt().disable();
        waitQueue.add(threadTime);
        thread.sleep();

        Machine.interrupt().restore(intStatus);
    }

    private class ThreadTime{
        public ThreadTime (KThread thread, long waketime){
            this.thread = thread;
            this.wakeTime = waketime;
        }
        
        public void setWakeTime(long wakeTime){
            this.wakeTime = wakeTime;
        }

        public void setThread(KThread thread){
            this.thread = thread;
        }

        public long getWakeTime(){
            return this.wakeTime;
        }

        public KThread getThread(){
            return this.thread;
        }
        private KThread thread;
        private long wakeTime;
    }

    private ArrayList<ThreadTime> waitQueue = new ArrayList<ThreadTime>(); 
}

