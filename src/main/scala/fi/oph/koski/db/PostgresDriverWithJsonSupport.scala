package fi.oph.koski.db

import com.github.tminglei.slickpg._
import org.json4s.JValue
import slick.driver.PostgresDriver

trait PostgresDriverWithJsonSupport extends PostgresDriver with PgJson4sSupport with array.PgArrayJdbcTypes {
  /// for json support
  override val pgjson = "jsonb"
  type DOCType = JValue
  override val jsonMethods = org.json4s.jackson.JsonMethods

  override val api = new API with JsonImplicits {
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val json4sJsonArrayTypeMapper =
      new AdvancedArrayJdbcType[JValue](pgjson,
        (s) => utils.SimpleArrayUtils.fromString[JValue](jsonMethods.parse(_))(s).orNull,
        (v) => utils.SimpleArrayUtils.mkString[JValue](j=>jsonMethods.compact(jsonMethods.render(j)))(v)
      ).to(_.toList)
  }

  val plainAPI = new API with Json4sJsonPlainImplicits
}

object PostgresDriverWithJsonSupport extends PostgresDriverWithJsonSupport