package config

import pureconfig.{ ConfigReader, ConfigSource }
import scala.reflect.ClassTag
// import pureconfig._
// import pureconfig.generic.auto._

object ConfigUtils {

  /** loads a configuration case class
    */
  def loadAppConfig[A: ConfigReader: ClassTag](path: String): A =
    ConfigSource.default.at(path).loadOrThrow[A]
}
