import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.utils.LuceneUtils;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

public class CreateIndex {

	/**
	 * @param args
	 * @throws IOException
	 * @throws LockObtainFailedException
	 * @throws CorruptIndexException
	 */
	public static void main(String[] images) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		System.out.println(">> Indexing " + images.length + " files.");
		DocumentBuilder builder = DocumentBuilderFactory
				.getExtensiveDocumentBuilder();

		IndexWriter iw = new IndexWriter(FSDirectory.open(new File("indexes")),
				new IndexWriterConfig(LuceneUtils.LUCENE_VERSION,
						new SimpleAnalyzer(LuceneUtils.LUCENE_VERSION)));
		int count = 0;
		long time = System.currentTimeMillis();
		for (String identifier : images) {
			Document doc = builder.createDocument(new FileInputStream(
					identifier), identifier);
			iw.addDocument(doc);
			count++;
			System.out.println(count + " files indexed.");
		}
		long timeTaken = (System.currentTimeMillis() - time);
		float sec = ((float) timeTaken) / 1000f;

		System.out.println(sec + " seconds taken, " + (timeTaken / count)
				+ " ms per image.");
		iw.optimize();
		iw.close();
	}
}
