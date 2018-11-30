# Anserini-embeddings

Anserini utilities for working with word embeddings.
Currently, these tools are held in a separate repository because these are experimental features, and direct inclusion in Anserini would blow up the size of the fatjar.

Here's a sample invocation of taking GloVe embeddings and creating a Lucene index for lookup.
This is treating Lucene as a simple key-value store.

```
$ target/appassembler/bin/IndexGloVe -index glove -input glove.840B.300d.txt
```

Simple lookup example:

```
$ target/appassembler/bin/LookupGloVe -index glove-float -word "happy"
```
