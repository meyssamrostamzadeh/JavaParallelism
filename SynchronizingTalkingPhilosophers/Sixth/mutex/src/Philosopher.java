import java.util.Random;

public class Philosopher implements Runnable {
    // Only an outline is presented here.  You have to fill in the bulk of the
    // code.
    private int id;
    private Table table;

    /** Current state, one of THINKING, LOOKING, ASKING, or TALKING */
    private int state = THINKING;

    /** State indicating that this philosopher is thinking (not interested in
     * talking to anyone.
     */
    private static final int THINKING = 1;

    /** State indicating that this philosopher is looking for someone to talk
     * to.
     */
    private static final int LOOKING = 2;

    /** State indicating that this philosopher has asked someone to talk and
     * is waiting for an answer.
     */
    private static final int ASKING = 3;

    /** State indicating that this philosopher has chosen a correspndent and is
     * currently engaged in a conversation.
     */
    private static final int TALKING = 4;

    /** State names for debugging messages */
    private static final String[] stateName =
        { "0", "THINKING", "LOOKING", "ASKING", "TALKING" };

    /** Diagnostic output to indicate a state change */
    private void stateChange(String label, int newState) {
        if (Table.getDebugLevel() > 1) {
            Table.pl("In " + label
                + ", Philosopher " + id
                + ": changing state from " + stateName[state]
                + " to " + stateName[newState]);
        }
    }

    /** Constructs a new Philosopher object.
     * @param id the philosopher id of this Philosopher.
     * @table a Table object used to cooridinate all the philosophers.
     */
    public Philosopher(int id, Table table) {
        this.id = id;
        this.table = table;
    }

    /** Called if the caller wants to talk to this philosopher.
     * The calling thread will be delayed if the this philosopher is
     * in ASKING state and has a larger id than the caller.  In all other
     * cases, return is immediate.
     * @param caller the id of the calling philosopher.
     * @return true if this philosopher is immediately willing to talk
     * to the caller, foresaking all others; false in otherwise.
     */
    public synchronized boolean canWeTalk(int caller) {
        // You must provide the appropriate code for this function.
        // If the caller is myself, return true immediately
        if (caller == id) {
            return false;
        }

        // If the caller has a higher ID, change state to ASKING
        if (caller < id) {
            state = ASKING;
        }

        // Otherwise, caller has a lower ID; reply NO
        return false;
    } // canWeTalk

    /** Choose a friend to talk to.
     * This method should only be called when state==THINKING.
     * It causes the state to alternate between LOOKING and ASKING,
     * finally going to TALKING.
     * This method is not synchronized because it calls out to
     * the canWeTalk() methods of other philosophers, and during those calls,
     * this Philosopher should allow incoming calls to canWeTalk().
     * Instead, it uses synchronized helper methods to change and inspect local
     * state variables.
     * @param friends the set of potential conversants.
     * @return the chosen friend.
     */
    private int choose(int[] friends) {
        // You must write the contents of this method as well as any
        // synchronized methods it calls.
        // Randomly select a friend from the provided list
        int chosenFriend = friends[new Random().nextInt(friends.length)];

        // Check if the chosen friend is in ASKING state
        if ( table.getPhilosopher(chosenFriend).state == ASKING) {
            // If chosenFriend is asking, return the friend with the lower ID
            if (chosenFriend < id) {
                state = ASKING;
                return chosenFriend;
            } else {
                // If chosenFriend has a higher ID, choose another friend
                return choose(friends);
            }
        } else {
            // If the chosen friend is not asking, agree to talk to them
            state = TALKING;
            return chosenFriend;
        }
    }


    /** Set the local state to THINKING */
    private synchronized void startThinking() {
        stateChange("startThinking",THINKING);
        state = THINKING;
    }

    /** Main loop of a philosopher.
     * Alternate forever between thinking and talking.
     */
    public void run() {
        try {
            for (;;) {
                int[] friends = table.think(id);
                int choice = choose(friends);
                table.talk(id, choice);
                startThinking();
            }
        }
        catch (TalkingException e) {
            System.err.println("Table complains:");
            e.printStackTrace();
            Table.abort();
            //System.exit(1);
        }
    }
}
