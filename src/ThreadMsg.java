
import java.util.LinkedList;
import java.lang.Thread;

public class ThreadMsg extends Thread {
	
	volatile int state;
	
	LinkedList<String> commQueue;
	
	// ThreadMsg Constructor
	ThreadMsg() { commQueue = new LinkedList<String>(); }
	
	LinkedList<String> commList = new LinkedList<String>();
	
	// Starting point for the new thread.
	public void run() 
	{
		getMsg();
	}
	
	public void getMsg()
	{
		String queueString = null;
		
		// Keep looping, reading from the queue, whenever a "notify" is sent to the queue object.
		// Print each of the strings read.
		// We will leave the loop when the item read from the queue says "EndMsg".  
		while (true)
		{
			// Lock up the commQueue and then
			// wait on the commQueue until our parent Thread has sent us our messages and notify.
			// The process of waiting frees up the commQueue lock.
			// System.out.println("    getMsg Pausing on synchronized statement 1.");
			synchronized(commQueue) 
			{
				// System.out.println("    getMsg Thread Waiting.");
					
				// Wait and free up commQueue lock.
				try { commQueue.wait(); } catch (InterruptedException e) { }
				
				// If I am here, the other thread sent us a message and a notify.	
				// System.out.println("    getMsg notify received. Thread Not waiting.");
			}

			// We've received a notify, waking this thread from the wait.
			// Get the message(s) from the queue, print them, and 
			// then tell the other thread - via a notify - that we have the message.
			// That thread will wake from its wait and send our thread another message.
			// System.out.println("    getMsg Thread Pausing before testing and reading from commQueue.");

			// Wait for a moment if the the other thread is touching the commQueue.
			// Then apply a lock on the commQueue and execute, reading the message.
			// When done reading the message, while still locked, notify the other thread.
			// Another way is to make the variable atomic.
			synchronized(commQueue)	
			{
				// Keep looping as long as there are messages in the queue.
				while (!commQueue.isEmpty())
				{
					// System.out.println("    getMsg Thread Removing and printing message.");
					queueString = commQueue.remove();
					System.out.println(queueString);
				}

				// System.out.println("    getMsg Thread notifying.\n");
				commQueue.notify();
			}
			// The lock on the commQueue is now freed. Both threads can execute now.
			
			// Exit infinite while loop when we see ending message.
			if (queueString == "EndMsg") break;	
		}		// End of ... Infinite while loop.
		// System.out.println("NewThread ending.");
	}
	
	public void putMsg()
	{
		final String farmFood[] = {
			    "Marezee doats, and",
			    "doazee doats, and", 
			    "liddle lamzee divy.",
			    "A kidlea divy too.",
			    "Wouldn’t you?\n",
			    "EndMsg"
			};
		/*
		final String farmFood[] = {
			    "Mares eat oats, and",
			    "does eat oats, and", 
			    "little lambs eat ivy.",
			    "A kid’ll eat ivy too.",
			    "Wouldn’t you?\n",
			    "EndMsg"
			};
		*/
		
		for (int i = 0; i < farmFood.length; i++)
		{
			// Add one of the lines from the song to the shared queue.
			// After doing so, notify the other thread.
			// After the other thread wakes and reads from the queue, it prints that line.

			// System.out.println("    putMsg Thread Pausing before adding to Queue.");
			synchronized(commQueue) {
				// System.out.println("    putMsg Thread Adding to Queue, Notifying, and then Waiting.");
				commQueue.add(farmFood[i]);			
				commQueue.notify();
				
				// Wait and free up commQueue lock.
				try { commQueue.wait(); } catch (InterruptedException e) { }
			}	// End of synchronized block, freeing up commQueue lock.
			
		}	// End of ... for loop
	}
	
	public static void main(String[] args) {
		
		// Start by showing the number of processors on this system.
		int processorCount = Runtime.getRuntime().availableProcessors();
		System.out.println("Processor Count = " + processorCount + "\n" );

		// To start up the new Thread, … 
		// This will not only create a new Thread 
		// (and have it start executing in its Run() method),
		// but it will also invoke the constructor of the ThreadMsg object,
		// a process which includes the construction of a LinkedList<String> object
		// which will be acting as a Queue.
		// This construction is done within the context of this parent Thread,
		// as opposed to the context of the new "child" Thread.
		// So when we get back to processing here, that object will already exist.
		ThreadMsg newThread = new ThreadMsg();
		newThread.start();
		
		// Pause for a moment to allow the new child Thread to execute its Wait().
		try { Thread.sleep(1000); }	catch(InterruptedException e) {}
		
		newThread.putMsg();

		try { Thread.sleep(2000); }	catch(InterruptedException e) {}
		System.out.println("Ending main program.");
		
	}	
}
