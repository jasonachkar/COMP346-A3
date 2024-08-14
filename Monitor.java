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
		synchronized (this) {
			while (isTalking || state[piTID] == State.EATING) {
				try {
					wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			isTalking = true;
		}
	}

	// End the talk
	public void endTalk(int piTID) {
		synchronized (this) {
			isTalking = false;
			notifyAll(); // Notify all waiting philosophers
		}
	}

	// Pick up the chopsticks
	public void pickUp(int philosopherId) {
		synchronized (self[philosopherId]) {
			state[philosopherId] = State.HUNGRY;
			test(philosopherId); // Test if the philosopher can start eating
			while (state[philosopherId] != State.EATING) {
				try {
					self[philosopherId].wait(); // Wait until the philosopher can eat
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt(); // Re-interrupt the thread
				}
			}
		}
	}

	// Put down the chopsticks
	public void putDown(int philosopherId) {
		synchronized (self[philosopherId]) {
			state[philosopherId] = State.THINKING;
			// Test if the left and right neighbors can start eating
			test((philosopherId + numOfPhilosophers - 1) % numOfPhilosophers);
			test((philosopherId + 1) % numOfPhilosophers);
		}
	}

	// Test if the current philosopher can eat
	private void test(int piTID) {
		int left = (piTID + numOfPhilosophers - 1) % numOfPhilosophers;
		int right = (piTID + 1) % numOfPhilosophers;

		synchronized (self[piTID]) {
			if (state[piTID] == State.HUNGRY && state[left] != State.EATING && state[right] != State.EATING) {
				state[piTID] = State.EATING;
				self[piTID].notify(); // Notify the philosopher that they can eat
			}
		}
	}
}
