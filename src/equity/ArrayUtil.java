package equity;

import java.util.*;

/**
 * utilities for arrays.
 * Note: if you want subarrays, use Arrays.copyOfRange
 */
public class ArrayUtil {
    /**
     * Join two arrays together
     * @param a first array
     * @param b second array
     * @return Conjoined in the order they were found in
     */
    public static <T> T[] join(T[] a, T[] b) {
        T[] x = Arrays.copyOf(a, a.length + b.length);
        for (int n = 0; n < b.length; n++) {
            x[a.length + n] = b[n];
        }
        return x;
    }

    /**
     * Pick an element of a (max length 63) that hasn't been picked before
     * Picked[0] contains the information, make sure to associate the 
     * input String array with picked (and use it every time) 
     * @param r Random generator
     * @param a Array to pick element out of
     * @param picked 
     * @return random String from a
     */
    public static String pick(Random r, String[] a, long[] picked) {
        if (a.length > 63) {
            throw new RuntimeException("array is longer than 63");
        }
        if (picked[0] >= ((1L << a.length) - 1)) {
            throw new RuntimeException("none left to pick");
        }
        while (true) {
            int i = r.nextInt(a.length);
            long m = 1L << i;
            if ((picked[0] & m) == 0) {
                picked[0] |= m;
                return a[i];
            }
        }
    }
    
    /**
     * Shuffles the array randomly (modifies it)
     */
    public static void shuffle(Object[] a, Random r) {
        for (int n = 0; n < a.length; n++) {
            // don't just pick random position!
            int x = r.nextInt(a.length - n) + n;
            Object o = a[n];
            a[n] = a[x];
            a[x] = o;
        }
    }
    
    /**
     * Removes all elements in b from a and returns a *new* array
     * Previous arrays will never be modified
     */
    public static String[] sub(String[] a, String[] b) {
        Set<String> newSet = new HashSet<>();
        for (String s: a)
            newSet.add(s);
        for (String s: b)
            newSet.remove(s);
        String[] c = new String[newSet.size()];
        newSet.toArray(c);
        return c;
    }
    
    /**
     * Sorts array a more efficiently than the default Java implementation
     * @param a
     */
    public static void sort (int[] a) {
        // simple insertion sort derived from wikipedia
        for (int i = 1; i < a.length; i++) {
            int v = a[i];
            int h = i;
            while (h > 0 && v < a[h - 1]) {
                a[h] = a[h - 1];
                h--;
            }
            a[h] = v;
        }
    }
    
    public static void main (String[] args) {
//        Random r = new Random();
//        for (int n = 0; n < 10; n++) {
//            int[] a = new int[r.nextInt(5) + 1];
//            for (int i = 0; i < a.length; i++) {
//                a[i] = r.nextInt(10);
//            }
//            int[] b = a.clone();
//            sort(b);
//            System.out.println(Arrays.toString(a) + " => " + Arrays.toString(b));
//        }
        String[] a = {"Jack", "box", "hey"};
        String[] b = {"Jack", "box", "hey", "2"};
        System.out.println(Arrays.toString(sub(a, b)));
    }
}
