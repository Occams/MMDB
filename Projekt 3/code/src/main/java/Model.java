import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.swing.event.EventListenerList;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.ColorStructureDescriptor;
import net.semanticmetadata.lire.imageanalysis.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.FCTH;
import net.semanticmetadata.lire.imageanalysis.Gabor;
import net.semanticmetadata.lire.imageanalysis.JpegCoefficientHistogram;
import net.semanticmetadata.lire.imageanalysis.ScalableColor;
import net.semanticmetadata.lire.imageanalysis.SimpleColorHistogram;
import net.semanticmetadata.lire.imageanalysis.Tamura;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.utils.LuceneUtils;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

public class Model {
	private static final String indexPath = "indexes";

	private EventListenerList ixCompleteList = new EventListenerList();
	private EventListenerList searchCompleteList = new EventListenerList();

	private DocumentBuilder getDocBuilder(Feature f) {
		switch (f) {
		case AUTO_COLOR_CORRELOGRAM:
			return DocumentBuilderFactory
					.getAutoColorCorrelogramDocumentBuilder();
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
			return DocumentBuilderFactory
					.getJpegCoefficientHistogramDocumentBuilder();
		case SCALABLE_COLOR:
			return DocumentBuilderFactory.getScalableColorBuilder();
		case TAMURA:
			return DocumentBuilderFactory.getTamuraDocumentBuilder();
		default:
			return null;
		}
	}

	private Class getClass(Feature f) {
		switch (f) {
		case AUTO_COLOR_CORRELOGRAM:
			return AutoColorCorrelogram.class;
		case CEED:
			return CEDD.class;
		case COLOR_HISTOGRAM:
			return SimpleColorHistogram.class;
		case COLOR_LAYOUT:
			return ColorLayout.class;
		case COLOR_STRUCTURE:
			return ColorStructureDescriptor.class;
		case EDGE_HISTOGRAM:
			return EdgeHistogram.class;
		case FCTH:
			return FCTH.class;
		case GABOR:
			return Gabor.class;
		case JPEG_COEFFICIENT_HISTOGRAM:
			return JpegCoefficientHistogram.class;
		case SCALABLE_COLOR:
			return ScalableColor.class;
		case TAMURA:
			return Tamura.class;
		default:
			return null;
		}
	}

	private String getFieldName(Feature f) {
		switch (f) {
		case AUTO_COLOR_CORRELOGRAM:
			return DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM;
		case CEED:
			return DocumentBuilder.FIELD_NAME_CEDD;
		case COLOR_HISTOGRAM:
			return DocumentBuilder.FIELD_NAME_COLORHISTOGRAM;
		case COLOR_LAYOUT:
			return DocumentBuilder.FIELD_NAME_COLORLAYOUT;
		case COLOR_STRUCTURE:
			return DocumentBuilder.FIELD_NAME_COLORSTRUCTURE;
		case EDGE_HISTOGRAM:
			return DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM;
		case FCTH:
			return DocumentBuilder.FIELD_NAME_FCTH;
		case GABOR:
			return DocumentBuilder.FIELD_NAME_GABOR;
		case JPEG_COEFFICIENT_HISTOGRAM:
			return DocumentBuilder.FIELD_NAME_JPEGCOEFFS;
		case SCALABLE_COLOR:
			return DocumentBuilder.FIELD_NAME_SCALABLECOLOR;
		case TAMURA:
			return DocumentBuilder.FIELD_NAME_TAMURA;
		default:
			return null;
		}
	}

	public Hashtable<String, Float> qbe(Feature feature, File example,
			int amount) {
		if (feature == null || example == null)
			return null;

		Hashtable<String, Float> result = new Hashtable<String, Float>(amount);

		// Use the ImageSearcherFactory for creating an ImageSearcher, which
		// will retrieve the images for you from the index.
		IndexReader reader;
		try {
			System.out.println("searching...");
			reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
			ImageSearcher searcher = new GenericFastImageSearcher(amount,
					getClass(feature), getFieldName(feature));
			FileInputStream imageStream = new FileInputStream(example);
			BufferedImage bimg = ImageIO.read(imageStream);
			// searching for an image:
			ImageSearchHits hits = null;
			hits = searcher.search(bimg, reader);
			for (int i = 0; i < Math.min(hits.length(), amount); i++) {
				result.put(
						hits.doc(i)
								.getFieldable(
										DocumentBuilder.FIELD_NAME_IDENTIFIER)
								.stringValue(), hits.score(i));
			}
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		fireSearchCompleteEvent(result);
		return result;
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
		 * Create a new DocumentBuilder with ChainedDocumentBuilder which
		 * contains all the features in it.
		 */
		ChainedDocumentBuilder cBuilder = new ChainedDocumentBuilder();

		for (Feature f : features) {
			cBuilder.addBuilder(getDocBuilder(f));
		}

		try {
			iw = new IndexWriter(FSDirectory.open(new File(indexPath)),
					new IndexWriterConfig(LuceneUtils.LUCENE_VERSION,

					new SimpleAnalyzer(LuceneUtils.LUCENE_VERSION)));
			while (!queue.isEmpty()) {
				File identifier = queue.peek();
				time = System.currentTimeMillis();
				Document doc = cBuilder.createDocument(new FileInputStream(
						identifier), identifier.getAbsolutePath());
				System.out.println(identifier.getAbsolutePath());
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

	public void addSearchCompleteListener(SearchCompleteListener listener) {
		searchCompleteList.add(SearchCompleteListener.class, listener);
	}

	public void removeSearchCompleteListener(SearchCompleteListener listener) {
		searchCompleteList.remove(SearchCompleteListener.class, listener);
	}

	private void fireIndexCompleteEvent(File file, long timeTaken) {
		for (IndexCompleteListener l : ixCompleteList
				.getListeners(IndexCompleteListener.class)) {
			if (l != null) {
				l.indexComplete(file, timeTaken);
			}
		}
	}

	private void fireSearchCompleteEvent(Hashtable<String, Float> results) {
		for (SearchCompleteListener l : searchCompleteList
				.getListeners(SearchCompleteListener.class)) {
			if (l != null) {
				l.searchComplete(results);
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

	public interface SearchCompleteListener extends EventListener {
		public void searchComplete(Hashtable<String, Float> results);
	}
}
