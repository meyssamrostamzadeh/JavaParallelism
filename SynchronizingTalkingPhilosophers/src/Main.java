
import CS537Sources.table;
import CS537Sources.talkingexception;
import CS537Sources.phonebooth;
public class Main {
    public static void main(String[] args) {
        int philosophers = Integer.parseInt(args[0]);
        int cycles = Integer.parseInt(args[1]);
        int testNumber = Integer.parseInt(args[2]);

        table myTable = new table();
        talkingexception myException = new talkingexception();
        phonebooth myPhoneBooth = new phonebooth();

        System.out.println("Number of Philosophers: " + philosophers);
        System.out.println("Number of Cycles: " + cycles);
        System.out.println("Test Number: " + testNumber);
    }
}