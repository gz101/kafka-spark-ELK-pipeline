import org.apache.spark.sql.{DataFrame, SparkSession}
// import org.apache.spark.sql.functions._
import testutils.{StandardTest}

class StreamHandlerTest extends StandardTest {
  "StreamHandler functions" when {
    val value1: String = 
      scala.io.Source.fromFile("src/test/resources/data/input1.txt").mkString
    // val value2 = scala.io.Source.fromFile("data/input2.txt").mkString

    implicit val spark: SparkSession = 
      SparkSession
        .builder()
        .master("local[1]")
        .getOrCreate()

    import spark.implicits._
    val dataDF: DataFrame = 
      Seq(("topic1", value1, "timestamp1"))
      .toDF("topic", "value", "timestamp")
      .selectExpr("CAST(value AS STRING)")

    "calling readJson()" should {
      "return a dataframe based on the Instrument schema" in {
        val outputDF = StreamHandler.readJson(dataDF).collectAsList()
        val expectedDF = Seq(value1).toDF("data").collectAsList()
        outputDF shouldEqual expectedDF
      }

      // "calculate average transaction amount by month and year" in {
        // Prerequisite.getAverageTransactionAmountByMonthAndYear(
        //   testData
        // ) should contain(("Jan", "14") -> 98.0)
        // Prerequisite.getAverageTransactionAmountByMonthAndYear(
        //   testData
        // ) should contain(("Mar", "13") -> (93.0 + 75.0) / 2)
      // }
    }
  }
}
