package io.anserini.embeddings;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.deeplearning4j.models.embeddings.wordvectors.*;
import org.deeplearning4j.models.embeddings.loader.*;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class LoadGloVe {
  private static final Logger LOG = LogManager.getLogger(LoadGloVe.class);

  public static final String FIELD_WORD = "word";
  public static final String FIELD_VECTOR = "vector";

  public static void main(String[] args) throws Exception {
    long startTime = System.currentTimeMillis();
    LOG.info("Loading GloVe vectors...");
    WordVectors wordVectors = WordVectorSerializer.loadTxtVectors(new File("../Castor-data/embeddings/GloVe/glove.840B.300d.txt"));
    LOG.info("Completed in " + (System.currentTimeMillis()-startTime)/1000 + "s elapsed.");

    final long start = System.nanoTime();
    LOG.info("Starting indexer...");

    final Directory dir = FSDirectory.open(Paths.get("glove-index"));
    final SimpleAnalyzer analyzer = new SimpleAnalyzer();
    final IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

    final IndexWriter writer = new IndexWriter(dir, config);
    final AtomicInteger cnt = new AtomicInteger();

    VocabCache vocab = wordVectors.vocab();
    vocab.words().forEach(obj -> {
      String word = (String) obj;
      //System.out.println(word + " " + Arrays.toString(wordVectors.getWordVector(word)));

      Document doc = new Document();

      String v = Arrays.toString(wordVectors.getWordVector(word));

      doc.add(new TextField(FIELD_WORD, word, Field.Store.YES));
      doc.add(new StringField(FIELD_VECTOR, v, Field.Store.YES));
      try {
        writer.addDocument(doc);
        int cur = cnt.incrementAndGet();
        if (cur % 100000 == 0) {
          LOG.info(cnt + " words added.");
        }
      } catch (IOException e) {
        LOG.error(e);
      }
    });

    LOG.info(cnt.get() + " words added.");
    int numIndexed = writer.maxDoc();

    try {
      writer.commit();
    } finally {
      try {
        writer.close();
      } catch (IOException e) {
        LOG.error(e);
      }
    }

    long duration = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    LOG.info("Total " + numIndexed + " documents indexed in " +
        DurationFormatUtils.formatDuration(duration, "HH:mm:ss"));

  }

}
