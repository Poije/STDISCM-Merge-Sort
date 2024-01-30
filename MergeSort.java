import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

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
            array[i] = rand.nextInt(array_size);
            System.out.println(array[i]);
        }

        // Call the generate_intervals method to generate the merge sequence

        List<Interval> intervals = generate_intervals(0, array_size - 1);

        // Call merge on each interval in sequence

        for (int i = 0; i < intervals.size(); i++) {
            merge(array, intervals.get(i).getStart(), intervals.get(i).getEnd());
        }

        // Once you get the single-threaded version to work, it's time to 
        // implement the concurrent version. Good luck :)

        // Print the sorted array
        System.out.println("Sorted");
        for (int i = 0; i < array_size; i++) {     
            System.out.print(array[i] + " ");
        }
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
