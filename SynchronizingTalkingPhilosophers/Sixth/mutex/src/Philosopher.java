import java.util.Random;

public class Philosopher implements Runnable {
    // Only an outline is presented here.  You have to fill in the bulk of the
    // code.
    private int id;

    private int[] friends;
    private int choosedFriend;
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
     * The calling thread will be delayed if  this philosopher is
     * in ASKING state and has a larger id than the caller.  In all other
     * cases, return is immediate.
     * @param caller the id of the calling philosopher.
     * @return true if this philosopher is immediately willing to talk
     * to the caller, foresaking all others; false in otherwise.
     */
    public synchronized boolean canWeTalk(int caller) {

        if ((state == TALKING) || (caller == id)) {
            System.out.println(id + " said no to " + caller+ " because he was talking");
            return  false;
        } else if (state == ASKING) {
            if (caller < id){
                System.out.println(id + " said no to " + caller+ " because he was asking someone else");
                return  false;
            } else {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (state == TALKING){
                    System.out.println(id + " said no to " + caller+ " after some wait");
                    return  false;
                } else {
                    System.out.println(id + " said yes to " + caller+ " after some wait");
                    return  true;
                }
            }

        } else if (state == LOOKING) {
            for (int friend : friends) {
                if (friend == caller) {
                    choosedFriend = caller;
                    state = TALKING;
                    table.getPhilosopher(id).notify();
                    System.out.println(id + " said yes to " + caller+ " quickly");
                    return  true;
                }
            }
            System.out.println(id + " said no to " + caller+ " because he wasn't his friend");
            return  false;
        } else if ((state == THINKING) && (caller > id)) {
            System.out.println(id + " said no to " + caller+ " because he was thinking");
            return  false;
        } else {
            System.out.println(id + "said no because he was thinking");
            return  false;
        }
    }

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

    public synchronized int askingOthers(int friendToAsk){
        if (state == TALKING) {
            return choosedFriend;
        }else if (friendToAsk == -1) {
            try {
                System.out.println(id + " enter waiting");
                while (state != TALKING) {
                    wait();
                }
                System.out.println(id + " waiting ended");
                return 0;
            } catch (InterruptedException e) {
            throw new RuntimeException(e);
            }
        } else {
            state = ASKING;
            boolean answer = table.getPhilosopher(friendToAsk).canWeTalk(id);
            if (answer) {
                state = TALKING;
                notify();
                return friendToAsk;
            } else {
                state = LOOKING;
                notify();
                return -1;
            }

        }
    }
    private int choose(int[] friends) {
        int answer;
        System.out.println("inside choose "+ id);
        for (int friend : friends) {
            System.out.println(id + " ask " +friend);
            if (state != LOOKING) {break;}
            answer = askingOthers(friend);
            if (answer != -1) {
                return answer;
            }
        }
        //enter waiting
        askingOthers(-1);
        // end of waiting
        return choosedFriend;
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
                friends = table.think(id);
                state = LOOKING;
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
