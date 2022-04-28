import org.apache.spark.sql.types._

final case class Request(
  requestType: String,
  length: Int,
  units: String,
  timezone: String,
  items: Array[Instrument]
)

object Request {
  val innerSchema = StructType(Seq(
    StructField("borehole_number", StringType, true),
    StructField("instrument", StringType, true),
    StructField("surface_level", FloatType, true),
    StructField("northing", IntegerType, true),
    StructField("easting", IntegerType, true),
    StructField("reading", FloatType, true),
    StructField("timestamp", StringType, true)
  ))

  val schema = StructType(Seq(
    StructField("instruments", IntegerType, true),
    StructField("units", StringType, true),
    StructField("timezone", StringType, true),
    StructField("items", ArrayType(innerSchema), true)
  ))
}
