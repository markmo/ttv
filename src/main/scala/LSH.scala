import com.databricks.spark.corenlp.CoreNLP
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

import scala.io.{BufferedSource, Source}

object LSH {

  val stopwords: BufferedSource =
    Source.fromInputStream(getClass.getResourceAsStream("stopwords.txt"))

  def shingle(text: String, k: Int): Set[String] =
    text
      .toLowerCase
      .replaceAll("[^\\w\\s]", "")
      .replaceAll("\\s+", " ")
      .toList
      .sliding(k)
      .map(_.mkString)
      .toSet

  def findSimilar(df: DataFrame,
                  idField: String,
                  textField: String,
                  shingleLength: Int = 5,
                  minHashLength: Int = 100,
                  numberBands: Int = 25): RDD[Iterable[Int]] = {
    val input = tokenize(df, textField)
      .select(idField, "parsed.sentence_token_word")
      .map { row =>
        (
          row.getInt(0),
          row.getSeq[String](1) map { word =>
            word.toLowerCase
          } filterNot { word =>
            stopwords contains word
          }
        )
      }
      .map {
        case (id, words) => (id, shingle(words.mkString(" "), shingleLength))
      }
    val vocab = vocabulary(input)
    val inputVectors = input map {
      case (id, shingles) => (id, shingles.map(vocab).toVector)
    }
    val randomHashFunctions = (new RandomHashIterator(vocab.size) take minHashLength).toVector
    val minHashed = minHash(inputVectors, randomHashFunctions)
    val candidates = partition(minHashed, minHashLength, numberBands)
      .groupByKey
      .flatMapValues {
        _
          .groupBy(_._1)
          .map(_._2.map(_._2))
          .filter(_.size > 1)
      }
      .values
    candidates
  }

  private def tokenize(df: DataFrame, textField: String) = {
    new CoreNLP()
      .setInputCol(textField)
      .setAnnotators(Array("tokenize", "ssplit"))
      .setFlattenNestedFields(Array("sentence_token_word"))
      .setOutputCol("parsed")
      .transform(df)
  }

  private def vocabulary(input: RDD[(Int, Set[String])]): Map[String, Int] =
    input
      .map(_._2)
      .fold(Set()) {
        case (acc, shingles) => acc ++ shingles
      }
      .toIndexedSeq
      .zipWithIndex
      .toMap

  private def minHash(inputVectors: RDD[(Int, Vector[Int])],
                      randomHashFunctions: Vector[Int => Int]): RDD[(Int, Vector[Int])] =
    inputVectors flatMap {
      case (_, shingleIndexes) if shingleIndexes.isEmpty => None
      case (id, shingleIndexes) => Some((id, randomHashFunctions map { fn =>
        (shingleIndexes map fn).min
      }))
    }

  private def partition(minHashed: RDD[(Int, Vector[Int])],
                        minHashLength: Int,
                        numberBands: Int): RDD[(Int, (Int, Int))] = {
    minHashed flatMap {
      case (jobId, hash) =>
        hash.grouped(minHashLength / numberBands).zipWithIndex map {
          case (band, bandIndex) => (bandIndex, (band.hashCode, jobId))
        }
    }
  }

}
