package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
	static Lock lock;
	static Island oahu;
	static Island molokai;
	static int passengers;
	static int boatPosition = 1;
    static Communicator communicator;

    public static void selfTest()
    {
		BoatGrader b = new BoatGrader();
	
		//System.out.println("\n ***Testing Boats with only 2 children***");
		//begin(0, 2, b);

		//System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		//begin(1, 2, b);

  		System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
  		begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	

		lock = new Lock();
    	oahu = new Island(adults, children, lock);
    	molokai = new Island(0,0,lock);

    	for(int i = 0; i < adults; i++){
    		Runnable r = new Runnable() {
		    public void run() {
	                AdultItinerary();
	            }
	        };
	        KThread t = new KThread(r);
	        t.setName("Thread Adult: "+i);
	        t.fork();
    	}
    	for(int i = 0; i < children; i++){
    		Runnable r = new Runnable() {
		    public void run() {
	                ChildItinerary();
	            }
	        };
	        KThread t = new KThread(r);
	        t.setName("Thread Child: "+i);
	        t.fork();
    	}

    	// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;


		communicator = new Communicator();
    }

    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
	   int position = 1;
    	lock.acquire();
    	while(true){
    		if( position==boatPosition){
    			if(position==1){
    				if(oahu.getChildren() >=2){
    					oahu.getWaitingLock().sleep();
    				}else if(passengers == 0){

    					passengers = 2;
    					position = 2;
    					oahu.setAdults(oahu.getAdult()-1);
    					bg.AdultRowToMolokai();
    					molokai.setAdults(molokai.getAdult()+1);
    					molokai.setPersons(oahu.getChildren()+ oahu.getAdult());
    					bg.AdultRideToMolokai();
    					molokai.getWaitingLock().wakeAll();
    					passengers = 0;
    					boatPosition = 2;
    					molokai.getWaitingLock().sleep();
    				}
    			}else{
    				molokai.getWaitingLock().sleep();
    			}
    		}else
    			if(position == 1){
    				oahu.getWaitingLock().sleep();
    			}else
    				molokai.getWaitingLock().sleep();
		}
	}

    static void ChildItinerary()
    {
    	int position = 1;
    	lock.acquire();
    	while(true){
    		if( position==boatPosition){
    			if(position==1){
    				if(oahu.getChildren() < 2){
    					oahu.getWaitingLock().sleep();
    				}else if( passengers == 0){
    					position = 2;
    					passengers = 1;
    					bg.ChildRowToMolokai();
    					oahu.getWaitingLock().wakeAll();
    					molokai.waitingLock.sleep();
    				}else if(passengers == 1){
    					position = 2;
    					bg.ChildRideToMolokai();
    					oahu.setChildren(oahu.getChildren()-2);
    					boatPosition = 2;
    					molokai.setChildren(molokai.getChildren()+2);
    					molokai.setPersons(oahu.getChildren()+oahu.getAdult());
    					passengers = 0;
    					molokai.getWaitingLock().wakeAll();
    					molokai.getWaitingLock().sleep();
    				}
    			}else{
    				if(molokai.getPersons() == 0){
    					return;
    				}else{
    					position = 1;
    					boatPosition = 1;
    					bg.ChildRowToOahu();
    					molokai.setChildren(molokai.getChildren()-1);
    					oahu.setChildren(oahu.getChildren()+1);
    					bg.ChildRideToOahu();
    					passengers = 0;
    					oahu.getWaitingLock().wakeAll();
    					oahu.getWaitingLock().sleep();
    				}
    			}
    		}else{
    			if(position == 1){
    				oahu.getWaitingLock().sleep();
    			}else
    				molokai.getWaitingLock().sleep();
    		}
    	}
    }

    static void SampleItinerary()
    {
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
    }
    
    static protected class Island{
    	private int children;
    	private int adults;
    	private Condition2 waitingLock;
    	private int persons;

    	public Island(int adults, int children, Lock lock){
    		this.children = children;
    		this.adults = adults;
    		waitingLock = new Condition2(lock);
    	}

    	public void setChildren(int children){
    		this.children = children;
    	}

    	public int getChildren(){
    		return this.children;
    	}

    	public void setAdults(int adults){
    		this.adults = adults;
    	}

    	public int getAdult(){
    		return this.adults;
    	}

    	public void setPersons(int persons){
    		this.persons = persons;
    	}

    	public int getPersons(){
    		return this.persons;
    	}

    	public Condition2 getWaitingLock(){
    		return this.waitingLock;
    	}
    }
}
