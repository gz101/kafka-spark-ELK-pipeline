import org.apache.spark.sql.SparkSession

object StreamHandler {
  def main(args: Array[String]): Unit = {

    val spark = 
      SparkSession
        .builder()
        .appName("Stream Handler")
        .master("local[*]")
        .getOrCreate()
    
    spark.sparkContext.setLogLevel("WARN")

    import spark.implicits._
    val streamingDS = 
      spark
        .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", "kafka:9092")
        .option("subscribe", "messages")
        .load()
        .selectExpr("CAST(value AS STRING)")
        .as[String]

    val transformDS = 
      streamingDS
        .map(s => s"${s} has been transformed.")
      
    val ds = 
      transformDS
        .selectExpr("CAST(value AS STRING)")
        .writeStream
        .format("kafka")
        .option("kafka.bootstrap.servers", "kafka:9092")
        .option("checkpointLocation", "opt/spark-checkpoints")
        .option("topic", "responses")
        .start()
    
    ds.awaitTermination()
  }
}
