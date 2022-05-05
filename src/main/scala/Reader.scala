import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._

object Reader {
  // loads data from Kafka using a configuration object 
  def loadStream(spark: SparkSession)
                (implicit conf: SparkJobConfig)
                : DataFrame =
    spark
      .readStream
      .format(conf.kafkaFormat)
      .option("kafka.bootstrap.servers", conf.kafkaBootstrapServers)
      .option("subscribe", conf.kafkaSubscribe)
      .option("startingOffsets", conf.kafkaStartingOffsets)
      .load()
      .selectExpr("CAST(value AS STRING)")

  // model the input JSON data as a DataFrame in Spark
  def readJson(dataDF: DataFrame): DataFrame = 
    dataDF
      .select(from_json(col("value"), Instrument.schema).as("data"))
      .select("data.*")
}
