import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class SearchExample {

	/**
	 * @param args
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	public static void main(String[] args) throws CorruptIndexException, IOException {
		// Use the ImageSearcherFactory for creating an ImageSearcher, which
		// will retrieve the images for you from the index.
		IndexReader reader = IndexReader.open(FSDirectory.open(new File("indexes")));
		ImageSearcher searcher = ImageSearcherFactory.createDefaultSearcher();
		FileInputStream imageStream = new FileInputStream("image.jpg");
		BufferedImage bimg = ImageIO.read(imageStream);
		// searching for an image:
		ImageSearchHits hits = null;
		hits = searcher.search(bimg, reader);
		for (int i = 0; i < 5; i++) {
			System.out.println(hits.score(i)
					+ ": "
					+ hits.doc(i)
							.getField(DocumentBuilder.FIELD_NAME_IDENTIFIER)
							.stringValue());
		}

		// searching for a document:
		Document document = hits.doc(0);
		hits = searcher.search(document, reader);
		for (int i = 0; i < 5; i++) {
			System.out.println(hits.score(i)
					+ ": "
					+ hits.doc(i)
							.getField(DocumentBuilder.FIELD_NAME_IDENTIFIER)
							.stringValue());
		}
	}
}
