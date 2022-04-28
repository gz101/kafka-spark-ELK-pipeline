// import org.apache.spark.sql.types._

final case class Instrument(
  boreholeNumber: String,
  instrument: String,
  surfaceLevel: Float,
  northing: Long,
  easting: Long,
  reading: Float,
  ts: String
)

object Instrument {

}
