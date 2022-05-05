import org.apache.spark.sql.{SparkSession, Dataset, DataFrame}
import org.apache.spark.sql.functions._
import cats.implicits._

object Processor {
  // expand out columns for each borehole reading
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

  // clean the data by dropping empty and null values
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

  // aggregate readings where intervals are close together
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
}