import org.apache.spark.sql.{Dataset}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.streaming._

object Writer {
  // writes processed data to the PostgreSQL database for batch analytics
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
  
  // writes processed data to a Kafka topic for streaming analytics
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

  // write the same Spark query into two different sinks (Kafka and PostgreSQL)
  def saveStream(dataDS: Dataset[Instrument])
                (implicit conf: SparkJobConfig)
                : StreamingQuery = 
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
