/** A place for philosophers to wait while making calls.
 * The first philosopher to make the call waits here until the call is
 * completed.  The other philosopher passes right through, and wakes up the
 * first philosopher when the call is completed.
 * This class is really just a binary semaphore in disguise.
 * 
 * This class is meant to be used only by class Table.  It is not meant to
 * be referenced directly by the Philsopher code.
 * @see Table
 */
public class PhoneBooth {
    static int VERSION = 2;

    private int count = 0;

    /** The table. */
    private Table table;

    /** Construct a PhoneBooth good for just one call.
     * @param table the table.
     */
    PhoneBooth(Table table) {
        this.table = table;
    }

    /** Enter the phone booth and wait.
     */
    public synchronized void down() {
        while (count==0) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                Table.pl("philosopher killed in phone booth");
                Thread.currentThread().stop();
            }
        }
        count--;
    }

    /** Wake up the waiter (there should be just one) */
    public synchronized void up() {
        count++;
        notifyAll();
    }
}
