public class Monitor {
	private enum State { THINKING, HUNGRY, EATING }
	private State[] state;
	private final Object[] self; // condition variables for each philosopher
	private final int numOfPhilosophers;
	private boolean isTalking = false; // Tracks if any philosopher is currently talking


	// Constructor
	public Monitor(int numOfPhilosophers) {
		this.numOfPhilosophers = numOfPhilosophers;
		state = new State[numOfPhilosophers];
		self = new Object[numOfPhilosophers];
		for (int i = 0; i < numOfPhilosophers; i++) {
			state[i] = State.THINKING;
			self[i] = new Object(); // each philosopher's condition object
		}
	}

	// Request to talk
	public void requestTalk(int piTID) {
		synchronized (self[piTID]) {
			while (isTalking || state[piTID] == State.EATING) {
				try {
					self[piTID].wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			isTalking = true;
		}
	}

	// End the talk
	public void endTalk(int piTID) {
		synchronized (self[piTID]) {
			isTalking = false;
			self[piTID].notifyAll();
		}
	}

	// Pick up the chopsticks
	public void pickUp(int piTID) {
		synchronized (self[piTID]) {
			state[piTID] = State.HUNGRY;
			test(piTID);
			while (state[piTID] != State.EATING) {
				try {
					self[piTID].wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	// Put down the chopsticks
	public void putDown(int piTID) {
		synchronized (self[piTID]) {
			state[piTID] = State.THINKING;
			// Test left and right neighbors
			test((piTID + numOfPhilosophers - 1) % numOfPhilosophers); // Left philosopher
			test((piTID + 1) % numOfPhilosophers); // Right philosopher
		}
	}

	// Test if the current philosopher can eat
	private void test(int piTID) {
		int left = (piTID + numOfPhilosophers - 1) % numOfPhilosophers;
		int right = (piTID + 1) % numOfPhilosophers;
		if (state[left] != State.EATING && state[piTID] == State.HUNGRY && state[right] != State.EATING) {
			state[piTID] = State.EATING;
			synchronized (self[piTID]) {
				self[piTID].notify(); // Notify the current philosopher they can eat
			}
		}
	}
}