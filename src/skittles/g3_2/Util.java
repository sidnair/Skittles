package skittles.g3_2;

import java.util.Arrays;

public class Util {
	/* Copy a part array */
	public static int[] copy(int[] arr, int len) {
		return Arrays.copyOf(arr, len);
	}

	/* Copy the whole array */
	public static int[] copy(int[] arr) {
		return Arrays.copyOf(arr, arr.length);
	}

	/* Swap elements of array */
	public static void swap(int[] a, int i, int j) {
		int t = a[i];
		a[i] = a[j];
		a[j] = t;
	}

	public static int[] add(int[] a, int[] b) {
		int[] c = new int[a.length];
		if (a.length == b.length)
			for (int i = 0; i < a.length; i++)
				c[i] = a[i] + b[i];
		return c;
	}

	/* Get the max value */
	public static double max(double... values) {
		int max = 0;
		for (int i = 1; i != values.length; ++i)
			if (values[i] > values[max])
				max = i;
		return values[max];
	}

	/* Sum of a list of integers */
	public static int sum(int... v) {
		int sum = 0;
		for (int i = 0; i != v.length; ++i)
			sum += v[i];
		return sum;
	}

	/* Return the sum of all doubles */
	public static double sum(double... vs) {
		double sum = 0.0;
		for (double v : vs)
			sum += v;
		return sum;
	}

	/* Return an array as a string */
	public static String toString(int... v) {
		String s = "[";
		for (int i : v)
			s += i + ", ";
		s = s.substring(0, s.length() - 2) + "]";
		return s;
	}

	/* Return an array as a string */
	public static String toString(double... v) {
		String s = "[";
		for (double i : v)
			s += i + ", ";
		s = s.substring(0, s.length() - 2) + "]";
		return s;
	}

	public static int[] rank(int[] value) {
		double[] dvalue = new double[value.length];
		for (int i = 0; i != value.length; ++i)
			dvalue[i] = value[i];
		return rank(dvalue);
	}

	public static int[] rank(double[] value) {
		int[] index = index(value);
		int[] rank = new int[index.length];
		for (int i = 0; i != index.length; ++i)
			rank[index[i]] = i;
		return rank;
	}

	public static int[] index(double[] value) {
		int colors = value.length;
		int[] index = new int[colors];
		for (int i = 0; i != colors; ++i)
			index[i] = i;
		for (int i = 0; i != colors; ++i) {
			int max = i;
			for (int j = i + 1; j != colors; ++j)
				if (value[index[j]] > value[index[max]])
					max = j;
			swap(index, max, i);
		}
		return index;
	}

	public static void main(String args[]) {
		double values[] = new double[] { 1, 2, 3, 4, 5 };
		int[] ranks = index(values);
		print(values);
		for (int i = 0; i < ranks.length; i++)
			System.out.print(values[ranks[i]] + " ");
	}

	public static void print(double[] val) {
		System.out.println(toString(val));
	}

	public static void print(int[] val) {
		System.out.println(toString(val));
	}
}
