import org.apache.spark.sql.{SparkSession, Dataset}
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

case class WaterStandpipe(
  boreholeNumber: String,
  instrument: String,
  surfaceLevel: Float,
  northing: Long,
  easting: Long,
  waterLevel: Float,
  ts: String
)

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
        .option("subscribe", "waterStandpipeIn")
        .option("startingOffsets", "earliest")
        .load()
        .selectExpr("CAST(value AS STRING)")

    val innerSchema = StructType(Seq(
      StructField("borehole_number", StringType, true),
      StructField("instrument", StringType, true),
      StructField("surface_level", FloatType, true),
      StructField("northing", IntegerType, true),
      StructField("easting", IntegerType, true),
      StructField("water_level", FloatType, true),
      StructField("timestamp", StringType, true)
    ))

    val schema = StructType(Seq(
      StructField("request_type", StringType, true),
      StructField("length", IntegerType, true),
      StructField("RL_units", StringType, true),
      StructField("timezone", StringType, true),
      StructField("items", ArrayType(innerSchema), true)
    ))
    
    val dataDF = df.select(from_json(col("value"), schema).as("data"))
      .select("data.*")
    
    val expandedDF = dataDF.select($"*", explode($"items") as "itemsFlattened")
      .drop($"items")

    val itemsDF = expandedDF.select($"itemsFlattened.*")

    val outputDS = itemsDF
      .withColumnRenamed("borehole_number", "boreholeNumber")
      .withColumnRenamed("surface_level", "surfaceLevel")
      .withColumnRenamed("water_level", "waterLevel")
      .withColumnRenamed("borehole_number", "boreholeNumber")
      .withColumnRenamed("timestamp", "ts")
      .as[WaterStandpipe]

    def writePostgres(batch: Dataset[WaterStandpipe]): Unit = 
      batch
        .write
        .format("jdbc")
        .option("driver", "org.postgresql.Driver")
        .option("url", "jdbc:postgresql://postgres:5432/monitoring")
        .option("user", "postgres")
        .option("password", "postgres")
        .option("dbtable", "public.waterstandpipe")
        .mode("append")
        .save()
    
    def writeKafka(batch: Dataset[WaterStandpipe]): Unit = 
      batch.toDF()
        .select(to_json(struct("*")).as("value"))
        .selectExpr("CAST(value AS STRING)")
        .write
        .format("kafka")
        .option("kafka.bootstrap.servers", "kafka:9092")
        .option("checkpointLocation", "opt/spark-checkpoints")
        .option("topic", "waterStandpipeOut")
        .save()

    val writeDS = 
      outputDS
        .writeStream
        .foreachBatch { (batch: Dataset[WaterStandpipe], _: Long) =>
          batch.persist()
          writePostgres(batch)
          writeKafka(batch)
          batch.unpersist()
          ()
        }
        .start()
    
    writeDS.awaitTermination()
  }
}
