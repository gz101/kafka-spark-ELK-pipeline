import org.apache.spark.sql.{SparkSession, Dataset, DataFrame}
import org.apache.spark.sql.streaming.StreamingQuery
import pureconfig.generic.auto._

import config.ConfigUtils
import utils.SparkUtils

// manages the overall spark configurations
case class SparkJobConfig(
  name: String,
  masterURL: String,
  kafkaFormat: String,
  kafkaBootstrapServers: String,
  kafkaSubscribe: String,
  kafkaStartingOffsets: String,
  kafkaCheckpoint: String,
  postgresFormat: String,
  postgresDriver: String,
  postgresURL: String,
  postgresUser: String,
  postgresPassword: String,
  postgresTable: String,
  postgresMode: String
)

object StreamHandler {
  // main application
  def main(args: Array[String]): Unit = {
    // set up configurations
    implicit val appSettings = 
      ConfigUtils.loadAppConfig[SparkJobConfig]("pipeline.spark-app")
    val spark = 
      SparkUtils.sparkSession(appSettings.name, appSettings.masterURL)
    spark.sparkContext.setLogLevel("ERROR")
    
    // run the spark job
    runJob(spark)
  }

  // helper to run the spark job
  def runJob(spark: SparkSession)(implicit conf: SparkJobConfig): Unit = {
    // read the data from Kafka
    val dataDF: DataFrame = Reader.loadStream(spark)
    val jsonDF: DataFrame = Reader.readJson(dataDF)

    // process the data using Spark Structured Streaming
    val flattenedDF: DataFrame = Processor.flattenCols(spark, jsonDF)
    val cleanedDS: Dataset[Instrument] = Processor.cleanCols(spark, flattenedDF)
    val aggregatedDS: Dataset[Instrument] = 
      Processor.aggregateInstruments(spark, cleanedDS)
    
    // save the data to Kafka and PostgreSQL
    val savedStream: StreamingQuery = Writer.saveStream(aggregatedDS)
    savedStream.awaitTermination()
  }
}
