import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;


public class MergeSort {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Seed your randomizer
        Random rand = new Random(0);

        // Get array size and thread count from user
        System.out.print("Enter Array Size: ");
        int array_size = scanner.nextInt();
        System.out.print("Enter the number of threads: ");
        int thread_count = scanner.nextInt();

        // Generate a random array of given size
        int[] array = new int[array_size];
        for (int i = 0; i < array_size; i++) {
            array[i] = i + 1;
        }

        for (int i = 0; i < array_size; i++) {
            int randomIndexToSwap = rand.nextInt(array_size);
            int temp = array[randomIndexToSwap];
            array[randomIndexToSwap] = array[i];
            array[i] = temp;
        }

        
        // Print the shuffled array for debugging
        // COMMENT OUT WHEN BENCHMARKING
        /* System.out.println("Shuffled Array:");
         for (int i = 0; i < array_size; i++) {
             System.out.print(array[i] + " ");
         }
         System.out.println(); 
        */
        // Call the generate_intervals method to generate the merge sequence
        List<Interval> intervals = generate_intervals(0, array_size - 1);

        long startTime = System.nanoTime();
        
        if (thread_count == 1){
            // START SINGLE THREADED
            // Call merge on each interval in sequence
            for (int i = 0; i < intervals.size(); i++) {
                merge(array, intervals.get(i).getStart(), intervals.get(i).getEnd());
            }
            // END SINGLE THREADED
        }

        // Once you get the single-threaded version to work, it's time to 
        // implement the concurrent version. Good luck :)

        else {
            // START MULTI THREADED
            ExecutorService executor = Executors.newFixedThreadPool(thread_count);
            List<Callable<Void>> sortTasks = new ArrayList<>();
            int subArraySize = (int) Math.ceil(array_size / (double) thread_count);
            
            for (int i = 0; i < thread_count; i++) {
                final int start = i * subArraySize;
                final int end = Math.min((i + 1) * subArraySize, array_size); 
                sortTasks.add(() -> {
                    Arrays.sort(array, start, end);
                    return null;
                });
            }
        
            try {
                executor.invokeAll(sortTasks); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread was interrupted during sorting: " + e.getMessage());
            }
        
            int currentSize = subArraySize;
            while (currentSize < array_size) {
                List<Callable<Void>> mergeTasks = new ArrayList<>();
                for (int i = 0; i < array_size; i += 2 * currentSize) {
                    final int start = i;
                    final int mid = Math.min(i + currentSize, array_size);
                    final int end = Math.min(i + 2 * currentSize, array_size);
        
                    mergeTasks.add(() -> {
                        merge(array, start, end - 1);
                        return null;
                    });
                }
        
                try {
                    executor.invokeAll(mergeTasks); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread was interrupted during merging: " + e.getMessage());
                }
        
                currentSize *= 2;
            }
        
            executor.shutdown();
            // END MULTI THREADED
        }        
        
        long endTime = System.nanoTime();
        long elapsedTimeNanos = endTime - startTime;
        double elapsedTimeMillis = elapsedTimeNanos / 1_000_000.0;

        // COMMENT OUT WHEN BENCHMARKING
        // Print the sorted array
        /* System.out.println("\nSorted:");
         for (int i = 0; i < array_size; i++) {     
             System.out.print(array[i] + " ");
         }
         System.out.println(); 
         */
         if (!isSorted(array)) {
            System.out.println("\nThe array is not sorted properly.");
         } else {
            System.out.println("\nThe array is sorted properly.");
         }
        System.out.println("\nRuntime: " + elapsedTimeMillis + " milliseconds");

        scanner.close();
    }

    /*
    This function generates all the intervals for merge sort iteratively, given 
    the range of indices to sort. Algorithm runs in O(n).

    Parameters:
    start : int - start of range
    end : int - end of range (inclusive)

    Returns a list of Interval objects indicating the ranges for merge sort.
    */
    public static List<Interval> generate_intervals(int start, int end) {
        List<Interval> frontier = new ArrayList<>();
        frontier.add(new Interval(start,end));

        int i = 0;
        while(i < frontier.size()){
            int s = frontier.get(i).getStart();
            int e = frontier.get(i).getEnd();

            i++;

            // if base case
            if(s == e){
                continue;
            }

            // compute midpoint
            int m = s + (e - s) / 2;

            // add prerequisite intervals
            frontier.add(new Interval(m + 1,e));
            frontier.add(new Interval(s,m));
        }

        List<Interval> retval = new ArrayList<>();
        for(i = frontier.size() - 1; i >= 0; i--) {
            retval.add(frontier.get(i));
        }

        return retval;
    }

    /*
    This function performs the merge operation of merge sort.

    Parameters:
    array : vector<int> - array to sort
    s     : int         - start index of merge
    e     : int         - end index (inclusive) of merge
    */
    public static void merge(int[] array, int s, int e) {
        int leftDone = 0, rightDone = 0;
        int m = s + (e - s) / 2;
        int[] left = new int[m - s + 1];
        int[] right = new int[e - m];
        int l_ptr = 0, r_ptr = 0;
        for(int i = s; i <= e; i++) {
            if(i <= m) {
                left[l_ptr++] = array[i];
            } else {
                right[r_ptr++] = array[i];
            }
        }
        l_ptr = r_ptr = 0;

        for(int i = s; i <= e; i++) {
            if(leftDone == 0 && (r_ptr == e - m || left[l_ptr] <= right[r_ptr])) {
                array[i] = left[l_ptr];
                if (l_ptr + 1 == left.length) {
                    leftDone = 1;
                }
                l_ptr++;
            } else {
                array[i] = right[r_ptr];
                if (r_ptr + 1 == right.length) {
                    rightDone = 1;
                }
                r_ptr++;
            }
        }
    }

    public static boolean isSorted(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }
}

class Interval {
    private int start;
    private int end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
