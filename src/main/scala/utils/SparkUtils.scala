package utils

import org.apache.spark.sql.SparkSession

object SparkUtils {
  // helper to build a spark session
  def sparkSession(
    appName: String = "spark-app", 
    masterURL: String = "local[*]"
  ): SparkSession = {
    lazy val spark = SparkSession
      .builder()
      .appName(appName)
      .master(masterURL)
      .getOrCreate()
    spark
  }
}
