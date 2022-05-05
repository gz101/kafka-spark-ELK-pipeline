import org.apache.spark.sql.{DataFrame, Dataset}
import pureconfig.generic.auto._
import testutils.{StandardTest}
import config.ConfigUtils
import utils.SparkUtils

class StreamHandlerTest extends StandardTest {
  "StreamHandler functions" when {
    val step1: String = 
      scala.io.Source.fromFile("src/test/resources/data/raw_step1.txt").mkString

    implicit val appSettings = 
      ConfigUtils.loadAppConfig[SparkJobConfig]("pipeline.spark-app")
    val spark = 
      SparkUtils.sparkSession(appSettings.name, appSettings.masterURL)
    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._
    val dataDF: DataFrame = 
      Seq(("topic1", step1, "timestamp1"))
      .toDF("topic", "value", "timestamp")
      .selectExpr("CAST(value AS STRING)")
    
    var outputDF: DataFrame = Seq("init").toDF()
    var outputDS: Dataset[Instrument] = Seq(Instrument()).toDS()

    "calling the readJson() function" should {
      "return a dataframe based on the Instrument schema" in {
        val items = Array(
          ("BH-4493", "water_standpipe", 79.45, 5815854, 323112, 77.66, 
           "2022-05-02T08:06:42.882Z"),
          ("BH-1009", "water_standpipe", 103.81, 5815483, 323522, 103.8, 
           "2022-05-02T08:06:42.882Z"),
        )

        val expectedDF: DataFrame = Seq(
          (134, "water level (m)", "UTC", items)
        ).toDF("instruments", "units", "timezone", "items")

        outputDF = Reader.readJson(dataDF)

        outputDF.collect().mkString shouldEqual expectedDF.collect().mkString
      }
    }

    "calling the flattenCols() should" should {
      "expand and rename dataframe columns to include `items` column only" in {
        val expectedDF = Seq(
          ("BH-4493", "water_standpipe", 79.45, 5815854, 323112, 77.66, 
           "2022-05-02T08:06:42.882Z"),
          ("BH-1009", "water_standpipe", 103.81, 5815483, 323522, 103.8, 
           "2022-05-02T08:06:42.882Z"),
        ).toDF(
          "boreholeNumber", "instrument", "surfaceLevel", "northing", "easting", 
          "reading", "ts"
        )

        outputDF = Cleaner.flattenCols(spark, outputDF)

        outputDF.collect().mkString shouldEqual expectedDF.collect().mkString
      }
    }

    "calling the cleanCols() function" should {
      "not affect valid data in a dataframe" in {
        val expectedDS = outputDF.as[Instrument]
        
        outputDS = Cleaner.cleanCols(spark, outputDF)

        outputDS.collect().mkString shouldEqual expectedDS.collect().mkString
      }

      "drop invalid rows in a dataframe" in {
        val input = Seq(
          ("BH-4493", "water_standpipe", 79.45, 5815854, 323112, 77.66, 
           "2022-05-02T08:06:42.882Z"),
          ("", "water_standpipe", 103.81, 5815483, 323522, 103.8, 
           "2022-05-02T08:06:42.882Z"),
        ).toDF(
          "boreholeNumber", "instrument", "surfaceLevel", "northing", "easting", 
          "reading", "ts"
        )

        val expected = Seq(
          ("BH-4493", "water_standpipe", 79.45, 5815854, 323112, 77.66, 
           "2022-05-02T08:06:42.882Z"),
        ).toDF(
          "boreholeNumber", "instrument", "surfaceLevel", "northing", "easting", 
          "reading", "ts"
        ).as[Instrument]

        val output = Cleaner.cleanCols(spark, input)

        output.collect().mkString shouldEqual expected.collect().mkString
      }
    }

    "calling the aggregateInstruments() function" should {
      "do nothing for all unique borehole numbers" in {
        val expectedDS = outputDS

        outputDS = Cleaner.aggregateInstruments(spark, outputDS)

        outputDS.collect().mkString shouldEqual expectedDS.collect().mkString
      }

      "aggregate borehole numbers which are not unique" in {
        val input = Seq(
          ("BH-4493", "water_standpipe", 79.45, 5815854, 323112, 20.00, 
           "2022-05-02T08:06:42.882Z"),
          ("BH-1009", "water_standpipe", 103.81, 5815483, 323522, 103.8, 
           "2022-05-02T08:06:42.882Z"),
          ("BH-4493", "water_standpipe", 79.45, 5815854, 323112, 100.00, 
           "2020-07-15T10:01:00.123Z"),
        ).toDF(
          "boreholeNumber", "instrument", "surfaceLevel", "northing", "easting", 
          "reading", "ts"
        ).as[Instrument]
      
        val expected = Seq(
          ("BH-4493", "water_standpipe", 79.45, 5815854, 323112, 60.00, 
           "2020-07-15T10:01:00.123Z"),
          ("BH-1009", "water_standpipe", 103.81, 5815483, 323522, 103.8, 
           "2022-05-02T08:06:42.882Z"),
        ).toDF(
          "boreholeNumber", "instrument", "surfaceLevel", "northing", "easting", 
          "reading", "ts"
        ).as[Instrument]

        val output = Cleaner.aggregateInstruments(spark, input)

        output.collect().mkString shouldEqual expected.collect().mkString
      }
    }
  }
}
