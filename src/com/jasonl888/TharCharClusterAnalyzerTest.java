package com.jasonl888;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.jasonl888.ThaiCharClusterAnalyzer;

public class TharCharClusterAnalyzerTest {

	public TharCharClusterAnalyzerTest() {

	}

	public static void main(String[] args) {
		String[] targetArray = {
				"เลือกโปรโมชั่นแบบรายเดือนหรือเติมเงินคุ้มกว่ากัน" 		// Message-1
				, "I have an English text to test", 		// Message-2
				"โปรโมชั่นรายเดือนแบบไหนเหมาะกับคนโทรเยอะ" 		// Message-3
				, "โปรโมชั่นคุยจ้อ" 								// Message-4
				, "โปรโมชั่นรายเดือนแบบไหนเหมาะกับคนโทรเยอะ" 		// Message-5
				, " เลือกโปรโมชั่นแบบรายเดือนหรือเติมเงินคุ้มกว่ากัน" 	// Message-6
		};
		String[] searchArray = { 
				"โปรโมชั่น" 					// search word-1
				, "english test"			// search word-2
				,"โปรโมชั่น" 					// search word-3
				, "คุยจ้อ" 					// search word-4
				, "โปรโมชั่น" 					// search word-5
				, "โปรโมชั่น" 					// search word-6
		};

		// 0. Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		ThaiCharClusterAnalyzer analyzer = new ThaiCharClusterAnalyzer(
				Version.LUCENE_36);

		// 1. create the index
		Directory index = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
				analyzer);
		IndexWriter w;
		try {
			w = new IndexWriter(index, config);
			w.deleteAll();
			w.commit();
			for (int i = 0; i < targetArray.length; i++) {
				addDoc(w, targetArray[i]);
			}
			w.close();
	
			for (int j = 0; j < searchArray.length; j++) {
				System.out.print("\n\nTarget string:" + targetArray[j]);
				getTokenizedString(analyzer, targetArray[j]);
				System.out.print("\n\nSearch string:" + searchArray[j]);
				getTokenizedString(analyzer, searchArray[j]);
				searchDoc(index, analyzer, searchArray[j]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void addDoc(IndexWriter w, String post) throws IOException {
		Document doc = new Document();
		doc.add(new Field("post", post, Field.Store.YES, Field.Index.ANALYZED));
		w.addDocument(doc);
	}

	private static void searchDoc(Directory index, Analyzer analyzer,
			String searchString) {
		try {
			// 2. query - parse the query string using same analyzer
			QueryParser parser = new QueryParser(Version.LUCENE_36, "post",
					analyzer);
			parser.setDefaultOperator(QueryParser.AND_OPERATOR);
			Query q = parser.parse(searchString);
			System.out.println("Query q:" + q);

			// 3. search - using the parsed query
			int hitsPerPage = 10;
			IndexReader reader = IndexReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(
					hitsPerPage, true);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			// 4. display results
			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				System.out.println("\t" + (i + 1) + ". " + d.get("post"));
			}

			// reader can only be closed when there
			// is no need to access the documents any more.
			reader.close();
			searcher.close();
		} catch (Exception ex) {
			System.err.println("Exception:" + ex);
			ex.printStackTrace();
		}
	}

	private static void getTokenizedString(Analyzer analyzer, String inputString) {
		try {
			String fieldName = "post";
			TokenStream stream = analyzer.tokenStream(fieldName, new StringReader(
					inputString));
			CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
			PositionIncrementAttribute posIncr = stream
					.addAttribute(PositionIncrementAttribute.class);
			OffsetAttribute offset = stream.addAttribute(OffsetAttribute.class);
			TypeAttribute type = stream.addAttribute(TypeAttribute.class);
	
			int position = 0;
			
			stream.reset();
			while (stream.incrementToken()) {
				int increment = posIncr.getPositionIncrement();
				if (increment > 0) {
					position = position + increment;
					System.out.println();
					System.out.print(position + ": ");
				}
				System.out.print("[" + term + ":" + offset.startOffset()
						+ "->" + offset.endOffset() + ":" + type.type() + "] ");
			}
			stream.close();
			System.out.println();
		} catch (Exception ex) {
			System.err.println("Exception:" + ex);
			ex.printStackTrace();
		}

	}


}
