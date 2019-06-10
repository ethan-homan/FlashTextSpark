package com.github.ethanh.flashtext

import java.util

import org.scalatest.FlatSpec
import org.apache.spark.sql.test.SharedSparkSession
import org.apache.spark.sql.functions.col

class SparkKeywordProcessorSpec extends FlatSpec with SharedSparkSession {
  "The apply method" should
    "add keywords when Iterable[String]" in {
    val processor = SparkKeywordProcessor(Array("python", "java"))
    val sentence = "python and java and python and python"
    val extracted = processor.extractKeywords(sentence)
    assert(extracted.get("python") == 3)
    assert(extracted.get("java") == 1)
  }
  it should "add keywords and the cleaned string to extract" in {
    val processor = SparkKeywordProcessor(Array(("python", "programming language"), ("java", "programming language")))
    val sentence = "python and java and python and python"
    val extracted = processor.extractKeywords(sentence)
    assert(extracted.get("programming language") == 4)
  }
  it should "add keywords and the cleaned string to extract using the UDF" in {
    val processor = SparkKeywordProcessor(Array(("python", "programming language"), ("java", "programming language")))
    val df = spark.sql(
      """
        |SELECT 'python and java and python and python' as sample_text_col
      """.stripMargin)
    val dfExtracted = df.withColumn("extracted_text", processor.extractKeywordsUdf(col("sample_text_col")))
    val extracted = dfExtracted.first().getAs[Map[String,Int]]("extracted_text")
    assert(extracted("programming language") == 4)
  }
  it should "add keywords when Iterable[String] using UDF" in {
    val processor = SparkKeywordProcessor(Array("python", "java"))
    val df = spark.sql(
      """
        |SELECT 'python and java and python and python' as sample_text_col
      """.stripMargin)
    val dfExtracted = df.withColumn("extracted_text", processor.extractKeywordsUdf(col("sample_text_col")))
    val extracted = dfExtracted.first().getAs[Map[String,Int]]("extracted_text")
    assert(extracted("python") == 3)
    assert(extracted("java") == 1)
  }
}
