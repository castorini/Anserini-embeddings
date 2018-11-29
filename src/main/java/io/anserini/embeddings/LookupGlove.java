/**
 * Anserini: A toolkit for reproducible information retrieval research built on Lucene
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.anserini.embeddings;

import io.anserini.util.AnalyzerUtils;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class LookupGlove {
  public static void main(String[] args) throws Exception {
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("glove-index")));

    IndexSearcher searcher = new IndexSearcher(reader);

    // cat
    TermQuery query = new TermQuery(new Term(IndexGloVe.FIELD_WORD, "happy"));

    TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
    if (topDocs.totalHits == 0) {
      System.err.println("Error: term not found!");
      return;// null;
    }

    for ( int i=0; i<topDocs.scoreDocs.length; i++ ) {
      Document doc = reader.document(topDocs.scoreDocs[i].doc);
      List<String> tokens = AnalyzerUtils.tokenize(new SimpleAnalyzer(), doc.getField(IndexGloVe.FIELD_WORD).stringValue());
      byte[] value = doc.getField(IndexGloVe.FIELD_VECTOR).binaryValue().bytes;
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(value));

      int cnt = in.readInt();
      double[] vector = new double[cnt];
      for (int n=0; n<vector.length; n++) {
        vector[n] = in.readDouble();
      }

      System.out.println(tokens.size() + " " + doc.getField(IndexGloVe.FIELD_WORD).stringValue() + " " +
          Arrays.toString(vector));
    }
  }
}
