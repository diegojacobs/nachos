package nachos.threads;

import nachos.machine.*;

import java.util.*; 

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    private Lock lock;

    private Condition waitingListeners;
    private Condition listener; 

    private Condition waitingSpeakers;
    private Condition speaker; 
     
    private boolean al; 
    private boolean as; 

    private int message;
    private boolean messageReceived;
    

    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        lock = new Lock();

        waitingListeners = new Condition(lock);
        listener = new Condition(lock);

        waitingSpeakers = new Condition(lock);
        speaker = new Condition(lock);

        al = false;
        as = false;

        message = 0;
        messageReceived = false;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param   word    the integer to transfer.
     */

    public void speak(int word) {
        lock.acquire();

        while(as){ 
            //El speaker debe entrar a la lista de espera
            waitingSpeakers.sleep();
        }

        //Tenemos un speaker listo para hablar
        as = true;

        message = word;
        
        //Si el mensaje no ha sido recibido y no haya un listener activo
        //Despertamos un listener y mandamos el mensaje
        while(!al || !messageReceived){
            System.out.println(KThread.currentThread.getName() + " speaking " + message);
            listener.wake(); //wake up a potential partner
            speaker.sleep(); //put this speaker to the sending queue
        }

        //Ya no tenemos speaker ni listener activos
        al = false;
        as = false;
        messageReceived = false;

        //Despertamos algun listener y algun speaker
        waitingSpeakers.wake(); 
        waitingListeners.wake();

        lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return  the integer transferred.
     */    
    public int listen() {
        lock.acquire();

        while(al){
            //EL listener entra a la lista de espera
            waitingListeners.sleep();
        }

        //Hay un listener esperando por el speaker
        al = true;

        //No tenemos algun speaker que quiera hablar, debemos dormir al listener
        while(!as){
            listener.sleep();
        }

        //Tenemos un speaker activo, recibimos el mensaje
        speaker.wake();
        messageReceived = true;
        System.out.println(KThread.currentThread.getName() + " listining " + message);

        lock.release();

        return message;
    }
}
