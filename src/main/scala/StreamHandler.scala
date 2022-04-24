import org.apache.spark.sql.{SparkSession, Dataset}
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

case class DataNode(id: Int, input: String)

object StreamHandler {
  def main(args: Array[String]): Unit = {

    val spark = 
      SparkSession
        .builder()
        .appName("Stream Handler")
        .master("local[*]")
        .getOrCreate()
    
    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._
    val df = 
      spark
        .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", "kafka:9092")
        .option("subscribe", "messages")
        .option("startingOffsets", "earliest")
        .load()
        .selectExpr("CAST(value AS STRING)")

    val schema = new StructType()
      .add("id", IntegerType)
      .add("input", StringType)
    
    val dataDS = df.select(from_json(col("value"), schema).as("data"))
      .select("data.*")
      .as[DataNode]

    def writePostgres(batch: Dataset[DataNode]): Unit = 
      batch
        .write
        .format("jdbc")
        .option("driver", "org.postgresql.Driver")
        .option("url", "jdbc:postgresql://postgres:5432/testdb")
        .option("user", "postgres")
        .option("password", "postgres")
        .option("dbtable", "public.testtable")
        .mode("append")
        .save()
    
    def writeKafka(batch: Dataset[DataNode]): Unit = 
      batch 
        .write
        .format("kafka")
        .option("kafka.bootstrap.servers", "kafka:9092")
        .option("checkpointLocation", "opt/spark-checkpoints")
        .option("topic", "responses")

    val outputDS = 
      dataDS
        .writeStream
        .foreachBatch { (batch: Dataset[DataNode], _: Long) =>
          batch.persist()
          writePostgres(batch)
          writeKafka(batch)
          batch.unpersist()
          ()
        }
        // .format("console")
        // .outputMode("append")
        // .format("kafka")
        // .option("kafka.bootstrap.servers", "kafka:9092")
        // .option("checkpointLocation", "opt/spark-checkpoints")
        // .option("topic", "responses")
        .start()
    
    outputDS.awaitTermination()
  }
}
