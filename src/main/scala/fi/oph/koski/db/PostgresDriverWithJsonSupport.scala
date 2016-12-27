package fi.oph.koski.db

import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.str.PgStringSupport
import org.json4s.JValue
import slick.jdbc.PostgresProfile

trait PostgresDriverWithJsonSupport extends PostgresProfile with PgJson4sSupport with PgArraySupport with array.PgArrayJdbcTypes with PgSearchSupport with PgStringSupport {
  /// for json support
  override val pgjson = "jsonb"
  type DOCType = JValue
  override val jsonMethods = org.json4s.jackson.JsonMethods

  trait API extends super.API with JsonImplicits with SearchAssistants with SearchImplicits with ArrayImplicits with PgStringImplicits {
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val json4sJsonArrayTypeMapper =
      new AdvancedArrayJdbcType[JValue](pgjson,
        (s) => utils.SimpleArrayUtils.fromString[JValue](jsonMethods.parse(_))(s).orNull,
        (v) => utils.SimpleArrayUtils.mkString[JValue](j=>jsonMethods.compact(jsonMethods.render(j)))(v)
      ).to(_.toList)
  }

  override val api: API = new API {}
  val plainAPI = new API with Json4sJsonPlainImplicits
}

object PostgresDriverWithJsonSupport extends PostgresDriverWithJsonSupport