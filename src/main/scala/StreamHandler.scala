import org.apache.spark.sql.{SparkSession, Dataset, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.streaming._
import pureconfig.generic.auto._
import cats.implicits._
import config.ConfigUtils
import utils.SparkUtils

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
  def main(args: Array[String]): Unit = {
    implicit val appSettings = 
      ConfigUtils.loadAppConfig[SparkJobConfig]("pipeline.spark-app")
    val spark = 
      SparkUtils.sparkSession(appSettings.name, appSettings.masterURL)
    spark.sparkContext.setLogLevel("ERROR")
    runJob(spark)
  }

  def runJob(spark: SparkSession)(implicit conf: SparkJobConfig): Unit = {
    val dataDF: DataFrame = loadStream(spark)
    val jsonDF: DataFrame = readJson(dataDF)
    val flattenedDF: DataFrame = flattenCols(spark, jsonDF)
    val cleanedDS: Dataset[Instrument] = cleanCols(spark, flattenedDF)
    val aggregatedDS: Dataset[Instrument] = 
      aggregateInstruments(spark, cleanedDS)
    val savedStream: StreamingQuery = saveStream(aggregatedDS)
    savedStream.awaitTermination()
  }

  def loadStream(spark: SparkSession)
                (implicit conf: SparkJobConfig)
                : DataFrame = {
    spark
      .readStream
      .format(conf.kafkaFormat)
      .option("kafka.bootstrap.servers", conf.kafkaBootstrapServers)
      .option("subscribe", conf.kafkaSubscribe)
      .option("startingOffsets", conf.kafkaStartingOffsets)
      .load()
      .selectExpr("CAST(value AS STRING)")
  }

  def readJson(dataDF: DataFrame): DataFrame = {
    dataDF
      .select(from_json(col("value"), Instrument.schema)
      .as("data"))
      .select("data.*")
  }

  def flattenCols(spark: SparkSession, dataDF: DataFrame): DataFrame = {
    import spark.implicits._
    dataDF
      .select($"*", explode($"items") as "itemsFlattened")
      .drop($"items")
      .select($"itemsFlattened.*")
      .withColumnRenamed("borehole_number", "boreholeNumber")
      .withColumnRenamed("surface_level", "surfaceLevel")
      .withColumnRenamed("timestamp", "ts")
  }

  def cleanCols(spark: SparkSession, dataDF: DataFrame): Dataset[Instrument] = {
    import spark.implicits._
    dataDF
      .na.drop()
      .filter(
        !($"boreholeNumber" === "") && 
        !($"instrument" === "") &&
        !($"ts" === "")
      )
      .as[Instrument]
  }

  def aggregateInstruments(
    spark: SparkSession,
    dataDS: Dataset[Instrument]
  ): Dataset[Instrument] = {
    import spark.implicits._
    dataDS
      .groupByKey(_.boreholeNumber)
      .reduceGroups(_ |+| _)
      .map {
        case (_, instrument) => instrument
      }
  }

  def writePostgres(batch: Dataset[Instrument])
                   (implicit conf: SparkJobConfig)
                   : Unit = 
    batch
      .write
      .format(conf.postgresFormat)
      .option("driver", conf.postgresDriver)
      .option("url", conf.postgresURL)
      .option("user", conf.postgresUser)
      .option("password", conf.postgresPassword)
      .option("dbtable", conf.postgresTable)
      .mode(conf.postgresMode)
      .save()
  
  def writeKafka(batch: Dataset[Instrument])
                (implicit conf: SparkJobConfig)
                : Unit = 
    batch
      .toDF()
      .select(col("instrument").as("topic"), to_json(struct("*")).as("value"))
      .selectExpr("topic", "CAST(value AS STRING)")
      .write
      .format(conf.kafkaFormat)
      .option("kafka.bootstrap.servers", conf.kafkaBootstrapServers)
      .option("checkpointLocation", conf.kafkaCheckpoint)
      .save()

  def saveStream(dataDS: Dataset[Instrument])
                (implicit conf: SparkJobConfig)
                : StreamingQuery = {
    dataDS
      .writeStream
      .trigger(Trigger.ProcessingTime("10 seconds"))
      .foreachBatch { (batch: Dataset[Instrument], _: Long) =>
        batch.persist()
        writePostgres(batch)
        writeKafka(batch)
        batch.unpersist()
        ()
      }
      .outputMode("update")
      .start()
  }
}
