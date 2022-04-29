import org.apache.spark.sql.types._
import cats._

final case class Instrument(
  boreholeNumber: String,
  instrument: String,
  surfaceLevel: Float,
  northing: Long,
  easting: Long,
  reading: Double,
  ts: String
) {
  def locationCoverage: (Long, Long) = (northing % 50, easting % 50)
}

object Instrument {
  val innerSchema = StructType(Seq(
    StructField("borehole_number", StringType, true),
    StructField("instrument", StringType, true),
    StructField("surface_level", FloatType, true),
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
