import java.util.Hashtable;

/** Coordinator for the set of philosophers.
 * Students should not modify this code.
 */
public class Table {
    static int VERSION = 2;

    /** Bug report address */
    /* Not the author, but responsible for bugs now */
    private static final String author = "dusseau@cs.wisc.edu";

    /** Number of philosophers */
    public static int NPHIL;

    /** Maximum number of conversations to complete before terminating the
     * program.
     */
    private int maxCalls;

    /** Number of conversations completed thus far. */
    private int totalCalls = 0;

    /** Conversations per philosopher */
    private int[] callCount;

    /** Flag to prevent more than one instance from being created. */
    private static Table instance = null;

    /** Level of verbosity of debugging output. 
     * Zero means no debugging output.
     * Default is 1.
     */
    private static int debugLevel = 1;

    /** Set the level of verbosity of debugging output.
     * @param level level of verbosity.
     *        Zero means no debugging output. Higher numbers give more output.
     *        The default is 1.
     * @return the previous level of verbosity.
     */
    public static int setDebugLevel(int level) {
        int previousLevel = debugLevel;
        debugLevel = level;
        return previousLevel;
    }

    /** Get the level of verbosity of debugging output.
     * @return the current level of verbosity.
     */
    public static int getDebugLevel() {
        return debugLevel;
    }

    /** Time when this program started (to time-stamp output). */
    private static long startTime = System.currentTimeMillis();

    /** Table mapping threads to philsopher id's (for debugging). */
    private static Hashtable threadToId = new Hashtable();

    private synchronized void pl(Object o, boolean ts) {
        if (ts)
            System.out.print(timestamp());
        //System.out.println(o);
        pr(o);
        System.out.println();
    }

    /** Create a timestamp, for debugging. */
    static private String timestamp() {
        int now = (int)(System.currentTimeMillis() - startTime);

        // Too bad there's no sprintf in Java!
        StringBuffer buf = new StringBuffer("0123.567");
        for (int p=7; p>=0; p--) {
            int digit = now % 10;
            char c = Character.forDigit(digit, 10);
            if (now == 0 && p < 3)
                c = ' ';
            now /= 10;
            if (buf.charAt(p) == '.') p--;
            buf.setCharAt(p, c);
        }
        Object o = threadToId.get(Thread.currentThread());
        if (o == null)
            return buf + "(?) : ";
        else
            return buf + "(" + ((Integer)o).intValue() + "): ";
    }

    static private void pr(Object o) {
        System.out.print(o);
    }

    /** Convenience function to print a message.
     * The message is printed to System.out, prefixed by a timestamp indicating
     * the number of seconds and milliseconds since the start of the program.
     * @param o message to print.
     */
    static public void pl(Object o) {
        instance.pl(o,true);
    }

    /* States of philosophers vis a vis the Table.  Note that these are not
     * the same as the states used by Bernstein's algorithm.  These states
     * are used here to detect certain illegal operations such as attempting
     * to talk to someone who is currently talking to someone else.
     */
    private int[] state;
    private static final int IDLE = 0;
    private static final int WAITING = 1;
    private static final int TALKING = 2;

    /** friend[i] == j means that philosopher i is waiting for or talking to
     * philosopher j.  That is, it has called talk(i,j).  friend[i] is not
     * meaningful if philosopher i is in state IDLE.
     */
    private int[] friend;

    /** currentFriends[i] is the value last returned by think(i).  
     * It is used to catch invalid calls to talk().  It is only or interest
     * when state[i]==IDLE.
     */
    private int[][] currentFriends;

    /** delay[i] is he amount of time philsopher i should delay after a
     * rendezvous with another philsopher for a conversation.  For the
     * responder to a call (the philosopher whose talk(i,j) matches an
     * earlier talk(j,i)), this is the duration of the call.  For the
     * the other philosopher, it is zero, since it does not see the
     * rendezvous completing until the end of the call.
     */
    private int[] delay;

    /** All of the philosophers in the room. */
    private Philosopher[] phil;

    /** Threads for running the philosophers. */
    private Thread[] tphil;

    /** Number of "friends" to pick for each philosopher (must be strictly
     * greater than (NPHIL-1)/2.0 -- see the comment in think() below).
     */
    private int nfriends;

    /** Phone booths for use in calling.
     * This array is indexed by the philosopher id of the first philosopher
     * to place the call.
     */
    private PhoneBooth phoneBooth[];

    private static final int MEAN_THINK_TIME = 1000; // milliseconds
    private static final int MEAN_TALK_TIME = 1000; // milliseconds

    private String stateInfo(int i) {
        switch (state[i]) {
            case IDLE:
                return "idle";
            case WAITING: 
                return "waiting for philosopher " + friend[i];
            case TALKING:
                return "talking to philosopher " + friend[i];
            default: // can't happen
                return null;
        }
    }

    /** Constructs a new Table.
     * @param count number of philosophers.
     * @param conversations the number of phone conversations to complete
     *        before terminating the program.
     * @exception TalkingException if more than one instance is created or if
     *        count < 2
     */
    public Table(int count, int conversations, int testnum)
        throws TalkingException
    {
        if (VERSION != TalkingException.VERSION) {
            System.out.println(
                "You have inconsistent versions of Table.class and"
                + " TalkingException.class");
            System.exit(1);
        }
            
        if (VERSION != PhoneBooth.VERSION) {
            System.out.println(
                "You have inconsistent versions of Table.class and"
                + " PhoneBooth.class");
            System.exit(1);
        }
            
        if (count < 2) {
            throw new TalkingException(
                "There must be at least two philosophers");
        }
        if (instance != null) {
            throw new TalkingException("There can only be one table");
        }
        instance = this;
        maxCalls = conversations;
        NPHIL = count;
        nfriends = (count + 1)/2;
        state = new int[count];
        friend = new int[count];
        currentFriends = new int[count][];
        callCount = new int[count];
        delay = new int[count];

        phoneBooth = new PhoneBooth[count];
        for (int i=0; i<count; i++) {
            phoneBooth[i] = new PhoneBooth(this);
        }

        phil = new Philosopher[NPHIL];
        tphil = new Thread[NPHIL];
        for (int i=0; i<count; i++) {
            phil[i] = new Philosopher(i, this);
            tphil[i] = new Thread(phil[i]);
            threadToId.put(tphil[i], new Integer(i));
        }
        for (int i=0; i<count; i++) {
            tphil[i].start();
	}

        /*
        for (int i=0; i<count; i++) {
            try {
                tphil[i].join();
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
        */
    } // Table

    /** Translate a philosopher id to a Philosopher reference.
     * @param id a philosopher id.
     * @return a reference to the corresponding Philosopher instance.
     */
    public Philosopher getPhilosopher(int id) {
        return phil[id];
    }

    /** Return a uniform random integer from the set {0, 1, ..., max-1} */
    private int randInt(int max) {
        return (int)(Math.random() * max);
    }

    /** Start thinking.
     * The calling thread is delayed for a random amount of time and then is
     * given a list of friends to talk to.
     * @param caller the identifier of the calling philosopher.
     * @return the set of friends.
     */
    public int[] think(int caller) {
        if (debugLevel > 1)
            pl("Philosopher " + caller + " thinking");
        try {
            Thread.sleep(randInt(2*MEAN_THINK_TIME));
        } catch (InterruptedException e) {
            //e.printStackTrace();
            Table.pl("philosopher killed while thinking");
            Thread.currentThread().stop();
        }
        // Choose a set of friends for the philosopher.  By choosing a majority
        // of the other philosophers as friends, we ensure that if all
        // philosophers are waiting, at least one rendezvous is possible.
        // Proof:
        // Let n = NPHIL.  There are n(n-1)/2 pairs of distinct philosophers.
        // Since each philosopher has more than (n-1)/2 friends, each of the
        // n philosophers contributes more than (n-1)/2 "offers" to talk, for
        // a total of more than n(n-1)/2 offers.  Since the number of offers
        // (ordered pairs of philosophers) is greater than the number of
        // unordered pairs, there must be at least one pair with two offers
        // -- i.e., offers in both directions.
        // QED.

        // set of possible friends (first n elements of the array)
        int[] unchosen = new int[NPHIL-1];
        int n = 0;
        int[] result = new int[nfriends];
        
        // Mark all other philosophers as unchosen
        for (int i=0; i<NPHIL; i++) {
            if (i != caller)
                unchosen[n++] = i;
        }

        for (int i=0; i<nfriends; i++) {
            // Choose a new friend randomly from the set of philosophers who
            // have not yet been chosen.
            int c = randInt(n);
            result[i] = unchosen[c];

            // remove choice from the set
            unchosen[c] = unchosen[--n];
        }
        if (debugLevel > 1) {
            String s = timestamp()
                + "Philosopher " + caller + " done thinking.  Friends [";
            for (int i=0; i<result.length; i++) {
                if (i > 0)
                    s += ",";
                s += result[i];
            }
            pl(s+"]", false);
        }
        currentFriends[caller] = result;
        return result;
    } // think

    /** Start a conversation between philsophers.
     * The calling thread should be the Philosopher identified by
     * caller, and it should have previoulsly reached agreement
     * with the Philosopher identified by callee to converse.
     * A matching call talk(callee,caller) should be made by
     * the other Philsopher soon before or after this call.  Each Philsopher
     * is delayed until a random amount of time after both calls have been made
     * and then allowed to continue.
     * @param caller the philosopher id of the calling Philsopher thread.
     * @param callee the philosopher to which the caller wishes to speak.
     * @exception TalkingException if callee is aleady waiting for
     * or talking to another philosopher.
     */
    public void talk(int caller, int callee)
        throws TalkingException
    {
        // Note that his method is not synchronized because it calls
        // PhoneBooth.down() which can delay the calling thread.
        if (debugLevel > 1)
            pl("Philosopher " + caller + " phones philosopher " + callee);
        startCall(caller, callee);
        phoneBooth[caller].down();
        try {
            Thread.sleep(delay[caller]);
        } catch (InterruptedException e) {
            //e.printStackTrace();
            Table.pl("philosopher killed while thinking");
            Thread.currentThread().stop();
        }
        endCall(caller, callee);
    } // talk

    /** Try to initiate a conversation.
     * @param caller the philsopher id of the calling thread.
     * @param callee the philsopher id of the thread to which it wants to talk.
     * @exception TalkingException if caller or callee is already talking to or
     *        waiting for someone else.
     */
    private synchronized void startCall(int caller, int callee)
        throws TalkingException
    {
        // Check that the calling thread is the right philsopher
        Object o = threadToId.get(Thread.currentThread());
        if (o == null)
            throw new TalkingException(
                "talk(" + caller + "," + callee
                + ") called by unknown thread");
        int callerId = ((Integer)o).intValue();
        if (callerId != caller)
            throw new TalkingException(
                "talk(" + caller + "," + callee
                + ") called by wrong philosopher (philosopher "
                + callerId + ")");

        if (state[caller] != IDLE)
            throw new TalkingException(
                "In talk(" + caller + "," + callee
                + "): Philosopher " + caller
                + " is already " + stateInfo(caller));
        // Check that callee is a friend of caller
        int i;
        for (i=0; i<currentFriends[caller].length; i++) {
            if (currentFriends[caller][i] == callee)
                break;
        }
        if (i == currentFriends[caller].length)
            throw new TalkingException(
                "In talk(" + caller + "," + callee
                + "): Philosopher " + callee
                + " is not a friend of philosopher " + caller);
        switch (state[callee]) {
            case TALKING:
                throw new TalkingException(
                    "In talk(" + caller + "," + callee
                    + "): Philosopher " + callee
                    + " is already " + stateInfo(callee));
            case IDLE:
                state[caller] = WAITING;
                friend[caller] = callee;
                delay[caller] = 0;
                break;
            case WAITING:
                if (friend[callee] != caller) {
                    throw new TalkingException(
                        "In talk(" + caller + "," + callee
                        + "): Philosopher " + callee
                        + " is trying to call philosopher " + friend[callee]
                        + ", not " + caller);
                }
                friend[caller] = callee;
                state[caller] = state[callee] = TALKING;
                delay[caller] = randInt(2*MEAN_TALK_TIME);
                if (delay[caller]==0)
                    delay[caller] = 1;
                phoneBooth[caller].up();
                if (debugLevel > 0)
                    pl(caller + "  " + callee + " starting");
                break;
            default:
                throw new TalkingException(
                    "In talk(" + caller + "," + callee
                    + "): Internal Error: unknown sate state " + state[callee]
                    + "\nPlease notify " + author);
        }
    } // startCall

    private synchronized void endCall(int caller, int callee) {
        if (delay[caller] == 0) {
            // This is the first party to place the call.  It only gets to
            // this point after the call was completed and the other party
            // has cleaned up, so there's nothing left to do.
            return;
        }
        state[caller] = state[callee] = IDLE;
        callCount[caller]++;
        callCount[callee]++;
        totalCalls++;
        if (debugLevel > 0)
            pl(caller + "  " + callee + " ending call " + totalCalls);
        if (totalCalls >= maxCalls) {
            pl("Program terminating after " + totalCalls
                    + " conversation" + (totalCalls==1 ? "" : "s"));
            terminate(false);
        }
        phoneBooth[callee].up();
    }

    private synchronized void terminate(boolean killAll) {
        System.out.flush();
        for (int i=0; i<NPHIL; i++) {
            pl("Philosopher " + i
                + " had "
                + callCount[i]
                + " conversation"
                + (callCount[i]==1 ? "" : "s")
                + "\n              and is currently " + stateInfo(i));
        }
        if (killAll) {
            for (int i=0; i<NPHIL; i++) {
                tphil[i].interrupt();
            }
        }
        else {
            System.exit(0);
        }
    }

    /** Shut down the simulation and kill all the philosophers.
     *  Any philosopher blocked in a wait() will throw an InterruptedException.
     */
    public static void abort() {
        pl("Terminated at user request");
        Thread.currentThread().dumpStack();
        instance.terminate(true);
    }
} // Table
