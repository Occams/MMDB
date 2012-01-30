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
				1, 7, 8, 9, 10, 11, 12, 15, 16, 18, 20, 21, 23, 24, 25, 26, 27,
				29, 32, 34, 35, 36, 37, 38, 39, 41, 42, 43, 44, 47, 49, 53, 57,
				59, 60, 61, 62, 64, 65, 66, 67, 68, 71, 74, 75, 76, 92, 96, 97,
				98 }));

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
				float recall = foundRelevant.size() / (float) best.size();

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
