public class Main {
    public static void main(String[] args) throws TalkingException {

        System.out.println("Hello world!");
        int philosophers = Integer.parseInt(args[0]);
        int cycles = Integer.parseInt(args[1]);
        int testNumber = Integer.parseInt(args[2]);

//        var ty = new nclas();
        try {
            Table myTable = new Table(philosophers,cycles,testNumber);
        } catch (TalkingException e) {
            throw new RuntimeException(e);
        }
//        talkingexception myException = new talkingexception();
//        phonebooth myPhoneBooth = new phonebooth();

        System.out.println("Number of Philosophers: " + philosophers);
        System.out.println("Number of Cycles: " + cycles);
        System.out.println("Test Number: " + testNumber);
    }
}