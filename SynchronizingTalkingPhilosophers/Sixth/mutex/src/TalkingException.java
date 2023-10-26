/** An exception thrown by the Table if any of its methods is incorrectly
 *  called.
 */
public class TalkingException extends Exception {
    static int VERSION = 2;

    /** Constructs a TalkingException with the specified detail message.
     * @param s the detail message.
     */
    public TalkingException(String s) {
        super(s);
    }

    /** Constructs a TalkingException with no detail message.  */
    public TalkingException() {
        super();
    }
}
