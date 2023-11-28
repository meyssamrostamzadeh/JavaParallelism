
import java.util.Scanner;
import java.util.ArrayList;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;


public class Main {
    private static final int ARRAY__SIZE = 1000000;

//    private static int[] array = new int[]{0};

    public static ArrayList<Integer> array = new ArrayList<>();
    private static final AtomicIntegerArray sharedArray = new AtomicIntegerArray(ARRAY__SIZE);
    public static int arr_size_temp = ARRAY__SIZE;
    private static final AtomicInteger arrayLength = new AtomicInteger(0);

//    public static ArrayList<Integer> csvdata = new ArrayList<>();
    public static ArrayList<Long> csvdata = new ArrayList<>();


    public static void main(String[] args) {

        int num_of_violations;
        array.add(0);

        //read the number of threads and starting them
        Scanner scanner = new Scanner(System.in);
//        System.out.print("Enter the number if threads: ");
//        int numberOfThreads = scanner.nextInt();
        int numberOfThreads = 10;

//        System.out.print("Enter size of the array: ");
//        arr_size_temp = scanner.nextInt();
        arr_size_temp = 1000000;
//        int numberOfThreads = 500;

//        long startTime = System.currentTimeMillis();
        long startTime = System.nanoTime();

//        for (int k = 1; k <= numberOfThreads; k++) {
//            Thread[] threads = new Thread[k];
//
//            for (int j = 0; j < k; j++) {
//                threads[j] = new Thread(new ArrayUpdater());
//                threads[j].start();
//            }

        for (int k = 1; k <= 500; k++) {
            startTime = System.nanoTime();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();


            Thread[] threads = new Thread[numberOfThreads];

            for (int j = 0; j < numberOfThreads; j++) {
                threads[j] = new Thread(new ArrayUpdater());
                threads[j].start();
            }

            try {
                for (Thread thread : threads) {
                    thread.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            long endTime = System.currentTimeMillis();
            long endTime = System.nanoTime();
            long elapsedTime = endTime - startTime;

            long elapsedTimeInMicroseconds = elapsedTime / 1000;

//            System.out.println("Elapsed Time: " + elapsedTimeInMicroseconds + " micro seconds");
//            System.out.println();
            csvdata.add(elapsedTimeInMicroseconds);

////            for non-atomic version
//            System.out.println("Final Array Length after " + k + " threads: " + array.size());
//            for (int i = 0; i < array.size(); i += 10) {
//                System.out.printf("Array contents %3d-%3d: ", i, Math.min(i + 10 - 1, array.size() - 1));
//                for (int j = i; j < Math.min(i + 10, array.size()); j++) {
//                    System.out.printf("%-4s", array.get(j));
//                }
//                System.out.println();
//            }

////            for atomic version
//            System.out.println("Final Array Length after " + k + " threads: " + sharedArray.length());
//            for (int i = 0; i < sharedArray.length(); i += 10) {
//                System.out.printf("Array contents %3d-%3d: ", i, Math.min(i + 10 - 1, sharedArray.length() - 1));
//                for (int j = i; j < Math.min(i + 10, sharedArray.length()); j++) {
//                    System.out.printf("%-4s",  sharedArray.get(j));
//                }
//                System.out.println();
//            }

//            // calculate number of violations for atomic version
//            num_of_violations = 0;
//            for (int m = 0; m < ARRAY_SIZE; m++) {
//                if ((m + 1) != sharedArray.get(m)) {
//                    num_of_violations++;
//                }
//            }
//            // clear atomic array and counter:
//            int length = sharedArray.length();
//            for (int n = 0; n < length; n++) {
//                sharedArray.set(n, 0); // Set each element to 0 (or any other desired initial value)
//            }
            arrayLength.set(0);
//
//            //add to final csv data
//            csvdata.add(num_of_violations);


//            // calculate number of violations for non-atomic version
//            num_of_violations = 0;
//            for (int m = 0; m < arr_size_temp; m++) {
//                if ((m) != array.get(m)) {
//                    num_of_violations++;
//                }
//            }
//            csvdata.add(num_of_violations);
//        csvdata.add(array.size());
            array.clear();
        }


        System.out.println("CSV Data(num_of_violations): ");
        for (long i : csvdata) {
            System.out.print(i + ",");
        }
        System.out.println();

    }

    static class ArrayUpdater implements Runnable {
        @Override
        public void run() {
//            // non-atomic
//            int length= array.size();
////            System.out.println(Thread.currentThread().threadId() + " current length of array is " + length);
//            while (length < arr_size_temp) {
////                System.out.println(Thread.currentThread().threadId() + " adding " + length + " to the end of array");
//                array.add(length);
//                length= array.size();
////                System.out.println(Thread.currentThread().threadId() + " now array length is: " + length);
////                System.out.print(Thread.currentThread().threadId() + ",");

              //atomic version
            int currentLength = arrayLength.get();
            while (currentLength < arr_size_temp) {
//                System.out.println(Thread.currentThread().threadId()+ " arraylength is: " + currentLength);
                sharedArray.set(currentLength , currentLength + 1);
                currentLength = arrayLength.getAndIncrement();
//                System.out.println(Thread.currentThread().threadId()+ " arraylength after inc is: " + currentLength);
            }
        }
    }
}