package io.anserini.embeddings;

import io.anserini.util.AnalyzerUtils;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.List;

public class LookupGlove {
  public static void main(String[] args) throws Exception {
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("glove-index")));

    IndexSearcher searcher = new IndexSearcher(reader);

    TermQuery query = new TermQuery(new Term(LoadGloVe.FIELD_WORD, "cat"));

    TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
    if (topDocs.totalHits == 0) {
      System.err.println("Error: term not found!");
      return;// null;
    }

    for ( int i=0; i<topDocs.scoreDocs.length; i++ ) {
      Document doc = reader.document(topDocs.scoreDocs[i].doc);
      List<String> tokens = AnalyzerUtils.tokenize(new SimpleAnalyzer(), doc.getField(LoadGloVe.FIELD_WORD).stringValue());
      System.out.println(tokens.size() + " " + doc.getField(LoadGloVe.FIELD_WORD).stringValue());// + " " + doc.getField(LoadGloVe.FIELD_VECTOR).stringValue());
    }
  }
}
