import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.search.FieldComparator.RelevanceComparator;
import org.apache.lucene.util.ArrayUtil;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File qbe = new File("image.orig/1.jpg");
		Set<Integer> best = new HashSet<Integer>(getLinkedList(new int[] { 0,
				1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 18, 19, 20, 21,
				22, 23, 24, 25, 26, 27, 29, 32, 33, 34, 35, 36, 37, 38, 39, 40,
				41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 52, 53, 54, 55, 56, 57,
				58, 59, 60, 61, 62, 64, 65, 66, 67, 68, 69, 70, 71, 74, 75, 76,
				77, 78, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 91, 92, 93, 94,
				95, 96, 97, 98, 99 }));

		Model model = new Model();
		int step = 15;

		System.out.print(" & ");
		for (int x = step; x < 100; x += step) {
			System.out.printf("\\multicolumn{2}{|c|}{k=%d} ", x);
			if (x + step < 100)
				System.out.print(" & ");
		}
		System.out.println("\\\\");
		System.out.print(" & ");
		for (int x = step; x < 100; x += step) {
			System.out.print("Pre & Rec ");
			if (x + step < 100)
				System.out.print(" & ");
		}
		System.out.println("\\\\\n\\hline\n");

		for (Feature f : Feature.values()) {
			System.out.print(f.toString().replace("_", "\\_") + " & ");
			for (int x = step; x < 100; x += step) {
				Hashtable<String, Float> results = model.qbe(f, qbe, x);
				Set<Integer> found = new HashSet<Integer>(
						getPictureIds(results));

				/*
				 * Precision
				 */
				Set<Integer> foundRelevant = new HashSet<Integer>(found);
				foundRelevant.retainAll(best);

				float precision = foundRelevant.size() / (float) (found.size());

				/*
				 * Recall
				 */
				float recall = foundRelevant.size() / best.size();

				System.out.printf("%.4f & %.4f", precision, recall);

				if (x + step < 100)
					System.out.print(" & ");
			}
			System.out.println("\\\\ \\hline");
		}
	}

	public static List<Integer> getLinkedList(int[] arr) {
		List<Integer> l = new LinkedList<Integer>();
		for (int i : arr)
			l.add(i);
		return l;
	}

	public static List<Integer> getPictureIds(Hashtable<String, Float> results) {
		List<Integer> l = new LinkedList<Integer>();
		for (Entry<String, Float> entry : results.entrySet()) {
			File f = new File(entry.getKey());
			l.add(Integer.parseInt(f.getName().split("\\.")[0]));
		}
		return l;
	}
}
