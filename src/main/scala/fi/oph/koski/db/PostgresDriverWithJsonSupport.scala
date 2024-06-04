package fi.oph.koski.db

import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.str.PgStringSupport
import org.json4s.JValue
import slick.jdbc.{PositionedParameters, PositionedResult, PostgresProfile, SQLActionBuilder}

import java.time.LocalDate

trait PostgresDriverWithJsonSupport extends PostgresProfile
  with PgJson4sSupport
  with PgArraySupport
  with array.PgArrayJdbcTypes
  with PgSearchSupport
  with PgStringSupport {

  /// for json support
  override val pgjson = "jsonb"
  type DOCType = JValue
  override val jsonMethods = org.json4s.jackson.JsonMethods

  trait API extends super.API
    with JsonImplicits
    with SearchAssistants
    with SearchImplicits
    with ArrayImplicits
    with PgStringImplicits {

    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)

    implicit val json4sJsonArrayTypeMapper =
      new AdvancedArrayJdbcType[JValue](pgjson,
        (s) => utils.SimpleArrayUtils.fromString[JValue](jsonMethods.parse(_))(s).orNull,
        (v) => utils.SimpleArrayUtils.mkString[JValue](j => jsonMethods.compact(jsonMethods.render(j)))(v)
      ).to(_.toList)

    implicit class ExtendedPlainSqlOps(r: PositionedResult) {
      // Add support for getting complex datatype columns by column label

      def getArray[T](columnName: String): IndexedSeq[T] = r.rs.getArray(columnName).getArray.asInstanceOf[Array[T]]

      def getArraySafe[T](columnName: String): IndexedSeq[T] =
        try {
          getArray(columnName)
        } catch {
          case _: NullPointerException => IndexedSeq.empty
        }

      def getJson(columnName: String): JValue = jsonMethods.parse(r.rs.getString(columnName))

      def getNullableJson(columnName: String): Option[JValue] = Option(r.rs.getString(columnName)).map(j => jsonMethods.parse(j))

      def getLocalDate(columnName: String): LocalDate = r.rs.getObject(columnName, classOf[LocalDate])

      def getLocalDateOption(columnName: String): Option[LocalDate] =
        getLocalDate(columnName) match {
          case d: LocalDate => Some(d)
          case _ => None
        }
    }
  }

  override val api: API = new API {}

  val plainAPI = new API with Json4sJsonPlainImplicits with SimpleArrayPlainImplicits
}

object PostgresDriverWithJsonSupport extends PostgresDriverWithJsonSupport

object SQLHelpers {
  def concatMany(builders: Option[SQLActionBuilder]*): SQLActionBuilder =
    builders.flatten.reduce(concat)

  def concat(a: SQLActionBuilder, b: SQLActionBuilder): SQLActionBuilder = {
    SQLActionBuilder(a.queryParts ++ b.queryParts, (p: Unit, pp: PositionedParameters) => {
      a.unitPConv.apply(p, pp)
      b.unitPConv.apply(p, pp)
    })
  }
}
