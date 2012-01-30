import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.util.ArrayUtil;

public class Main {
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File qbe = new File("image.orig/1.jpg");
		Set<Integer> best = new HashSet<Integer>(getLinkedList(new int[] { 1,
				10, 11, 16, 21, 36, 66, 74, 92, 97 }));
		Set<Integer> irrelevant = new HashSet<Integer>(getLinkedList(new int[] {
				4, 14, 17, 30, 31, 28, 51, 63, 73, 72, 79, 90 }));

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
				float recall = foundRelevant.size()
						/ (100f - irrelevant.size());

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
