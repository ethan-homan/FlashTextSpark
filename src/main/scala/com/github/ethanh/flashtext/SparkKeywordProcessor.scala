package com.github.ethanh.flashtext

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import scala.collection.JavaConverters._

object SparkKeywordProcessor {
  def apply(words: Iterable[_]): SparkKeywordProcessor = {
    val processor = new SparkKeywordProcessor()
    processor.addKeywords(words)
    processor
  }
}

class SparkKeywordProcessor extends KeywordProcessor {
  def addKeywords(words: Iterable[_]): Unit = {
    words.foreach{
      case (word: String, cleanName: String) => this.addKeyword(word, cleanName)
      case word: String => this.addKeyword(word)
    }
  }
  def extractKeywordsUdf: UserDefinedFunction = udf((s: String) => this.extractKeywords(s).asScala)
  def replaceUdf: UserDefinedFunction = udf((s: String) => this.replace(s))
}
