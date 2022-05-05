import org.apache.spark.sql.types._
import cats._

// represents each reading ingested from Kafka
final case class Instrument(
  boreholeNumber: String,
  instrument: String,
  surfaceLevel: Double,
  northing: Long,
  easting: Long,
  reading: Double,
  ts: String
)

object Instrument {
  // used for unit tests only
  def apply(): Instrument = Instrument(
    "BH-1234", "water_standpipe", 80.80, 555555, 33333, 90.90, 
    "2022-02-02T02:02:02.022Z"
  )

  // schema to read JSON formatted strings into a Spark DataFrame
  val innerSchema = StructType(Seq(
    StructField("borehole_number", StringType, true),
    StructField("instrument", StringType, true),
    StructField("surface_level", DoubleType, true),
    StructField("northing", IntegerType, true),
    StructField("easting", IntegerType, true),
    StructField("reading", DoubleType, true),
    StructField("timestamp", StringType, true)
  ))

  val schema = StructType(Seq(
    StructField("instruments", IntegerType, true),
    StructField("units", StringType, true),
    StructField("timezone", StringType, true),
    StructField("items", ArrayType(innerSchema), true)
  ))

  // used for aggregating (average) monitoring records
  implicit val averageCloseInstrumentsMonoid: Monoid[Instrument] = 
    new Monoid[Instrument] {
      override def empty: Instrument = Instrument("", "", 0, 0, 0, 0, "")

      override def combine(x: Instrument, y: Instrument): Instrument = 
        Instrument(
          x.boreholeNumber,
          x.instrument,
          (x.surfaceLevel + y.surfaceLevel) / 2,
          (x.northing + y.northing) / 2,
          (x.easting + y.easting) / 2,
          (x.reading + y.reading) / 2,
          y.ts
        )
    }
}
