import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.event.EventListenerList;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.utils.LuceneUtils;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

public class Model {

	private EventListenerList ixCompleteList = new EventListenerList();
	
	private DocumentBuilder getDocBuilder (Feature f) {
		switch (f) {
		case AUTO_COLOR_CORRELOGRAM:
			return DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder();
		case CEED:
			return DocumentBuilderFactory.getCEDDDocumentBuilder();
		case COLOR_HISTOGRAM:
			return DocumentBuilderFactory.getColorHistogramDocumentBuilder();
		case COLOR_LAYOUT:
			return DocumentBuilderFactory.getColorLayoutBuilder();
		case COLOR_STRUCTURE:
			return DocumentBuilderFactory.getColorStructureBuilder();
		case EDGE_HISTOGRAM:
			return DocumentBuilderFactory.getEdgeHistogramBuilder();
		case FCTH:
			return DocumentBuilderFactory.getFCTHDocumentBuilder();
		case GABOR:
			return DocumentBuilderFactory.getGaborDocumentBuilder();
		case JPEG_COEFFICIENT_HISTOGRAM:
			return DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder();
		case SCALABLE_COLOR:
			return DocumentBuilderFactory.getScalableColorBuilder();
		case TAMURA:
			return DocumentBuilderFactory.getTamuraDocumentBuilder();
		default:
			return null;
		}
	}

	public void index(List<File> images, Feature[] features) {
		if (images == null || images.size() <= 0 || features == null
				|| features.length <= 0)
			return;

		int count = 0;
		long time = 0;
		Queue<File> queue = new LinkedList<File>(images);
		IndexWriter iw = null;

		/*
		 * Create a new DocumentBuilder with ChainedDocumentBuilder which contains
		 * all the features in it.
		 */
		ChainedDocumentBuilder cBuilder = new ChainedDocumentBuilder();

		for (Feature f : features) {
			cBuilder.addBuilder(getDocBuilder(f));
		}

		try {
			iw = new IndexWriter(FSDirectory.open(new File("indexes")),
					new IndexWriterConfig(LuceneUtils.LUCENE_VERSION,

					new SimpleAnalyzer(LuceneUtils.LUCENE_VERSION)));
			while (!queue.isEmpty()) {
				File identifier = queue.peek();
				time = System.currentTimeMillis();
				Document doc = cBuilder.createDocument(new FileInputStream(
						identifier), identifier.getName());
				iw.addDocument(doc);
				count++;
				fireIndexCompleteEvent(identifier, System.currentTimeMillis()
						- time);
				queue.poll();
			}
		} catch (Exception e) {
			e.printStackTrace();
			/*
			 * Send error events
			 */
			while (!queue.isEmpty())
				fireIndexErrorEvent(queue.poll());
		} finally {
			if (iw != null) {
				try {
					iw.optimize();
					iw.close();
				} catch (CorruptIndexException e) {
				} catch (IOException e) {
				}
			}
		}

	}

	public void addIndexCompleteListener(IndexCompleteListener listener) {
		ixCompleteList.add(IndexCompleteListener.class, listener);
	}

	public void removeIndexCompleteListener(IndexCompleteListener listener) {
		ixCompleteList.remove(IndexCompleteListener.class, listener);
	}

	private void fireIndexCompleteEvent(File file, long timeTaken) {
		for (IndexCompleteListener l : ixCompleteList
				.getListeners(IndexCompleteListener.class)) {
			if (l != null) {
				l.indexComplete(file, timeTaken);
			}
		}
	}

	private void fireIndexErrorEvent(File file) {
		for (IndexCompleteListener l : ixCompleteList
				.getListeners(IndexCompleteListener.class)) {
			if (l != null) {
				l.indexError(file);
			}
		}
	}

	public interface IndexCompleteListener extends EventListener {
		public void indexComplete(File file, long timeTaken);

		public void indexError(File file);
	}
}
