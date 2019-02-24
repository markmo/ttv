ttv
===

Entity Resolution using Locality Sensitive Hashing.

Locality-Sensitive Hashing (LSH) is an algorithm for solving the approximate or exact 
Near Neighbor Search in high dimensional spaces.

This implementation uses shingling and Minhash signatures to find similar documents.

Minhashing is used to compress large documents into small signatures and preserve the 
expected similarity of any pair of documents/records. However, used alone, it may still 
be impossible to find the pairs with greatest similarity efficiently.

The current implementation uses the Stanford CoreNLP library for tokenization. You could 
do this yourself. Just replace the 'tokenize' method in the NLP class.

To use spark-corenlp, you need one of the CoreNLP language models:

    # Download one of the language models. 
    wget http://repo1.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.9.1/stanford-corenlp-3.9.1-models.jar
    
    # Run spark-shell 
    spark-shell --packages databricks/spark-corenlp:0.4.0-spark_2.4-scala_2.11 --jars stanford-corenlp-3.9.1-models.jar