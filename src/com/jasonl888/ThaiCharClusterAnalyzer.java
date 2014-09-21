package com.jasonl888;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.th.ThaiWordFilter;
import org.apache.lucene.util.Version;
import com.wittawat.tcc.TCCTokenizer;

public final class ThaiCharClusterAnalyzer extends Analyzer {
	private Version matchVersion = Version.LUCENE_36;

	public ThaiCharClusterAnalyzer(Version version) {
		matchVersion = version;
	}

	/**
	 * Creates
	 * {@link org.apache.lucene.analysis.ReusableAnalyzerBase.TokenStreamComponents}
	 * used to tokenize all the text in the provided {@link Reader}.
	 * 
	 * @return {@link org.apache.lucene.analysis.ReusableAnalyzerBase.TokenStreamComponents}
	 *         built from a {@link StandardTokenizer} filtered with
	 *         {@link StandardFilter}, {@link LowerCaseFilter},
	 *         {@link ThaiWordFilter}, and {@link StopFilter}
	 */
	@Override
	public TokenStream reusableTokenStream(String fieldName, Reader reader) {
		return tokenStream(fieldName, reader);
	}

	/**
	 * Creates
	 * {@link org.apache.lucene.analysis.ReusableAnalyzerBase.TokenStreamComponents}
	 * tokenize all the Thai text in the provided {@link Reader} 
	 * using JTCC library - ref: https://code.google.com/p/jtcc/ 
	 * 
	 * @return {@link org.apache.lucene.analysis.ReusableAnalyzerBase.TokenStreamComponents}
	 *         built from a {@link StandardTokenizer} filtered with
	 *         {@link StandardFilter}, {@link LowerCaseFilter}
	 */
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		try {
			TCCTokenizer tccSource = new TCCTokenizer(reader);
			tccSource.setDelimiter(" ");
			Tokenizer source = new StandardTokenizer(matchVersion,
					new StringReader(tccSource.tokenizeOrNull()));
			TokenStream result = new StandardFilter(matchVersion, source);
			result = new LowerCaseFilter(matchVersion, result);
			return result;
		} catch (IOException ex) {
			throw new RuntimeException("TCCTokenizer IOException:" + ex);
		}
	}
}
