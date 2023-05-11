package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, KoskiTables, QueryMethods, SQLHelpers}
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import org.json4s.JsonAST.JValue
import org.json4s.MappingException
import slick.jdbc.GetResult

import java.sql.Timestamp

class SuoritetutTutkinnotOpiskeluoikeusRepository(
  val db: DB,
  val validatingAndResolvingExtractor: ValidatingAndResolvingExtractor
) extends QueryMethods {
  def getOppijanKaikkiOpiskeluoikeudet(
    palautettavatOpiskeluoikeudenTyypit: Seq[String],
    oppijaMasterOid: String
  ): Seq[SuoritetutTutkinnotOppijanOpiskeluoikeusRow] = {

    runDbSync(SQLHelpers.concatMany(Some(
      sql"""
with
  haettu_oppija as (
    select
      oid as oppija_oid,
      coalesce(master_oid, oid) as oppija_master_oid
    from henkilo
    where henkilo.oid = $oppijaMasterOid or henkilo.master_oid = $oppijaMasterOid
  )
  , linkitetty as (
    select
      distinct haettu_oppija.oppija_master_oid
    from
      haettu_oppija
      inner join haettu_oppija h2 on h2.oppija_master_oid = haettu_oppija.oppija_master_oid and h2.oppija_master_oid <> h2.oppija_oid
  )
  , opiskeluoikeus_kaikki as (
    select
      haettu_oppija.oppija_master_oid,
      oid as opiskeluoikeus_oid,
      versionumero,
      aikaleima,
      data
    from
      opiskeluoikeus
      join haettu_oppija on haettu_oppija.oppija_oid = opiskeluoikeus.oppija_oid
      left join linkitetty on linkitetty.oppija_master_oid = haettu_oppija.oppija_master_oid
    where opiskeluoikeus.koulutusmuoto = any($palautettavatOpiskeluoikeudenTyypit)
      and mitatoity = false
    order by oppija_master_oid
  )
  , opiskeluoikeus_palautettavat as (
    select
      distinct on (opiskeluoikeus_kaikki.opiskeluoikeus_oid) opiskeluoikeus_kaikki.opiskeluoikeus_oid "opiskeluoikeusOid",
      opiskeluoikeus_kaikki.oppija_master_oid as "masterOppijaOid",
      opiskeluoikeus_kaikki.versionumero,
      opiskeluoikeus_kaikki.aikaleima,
      opiskeluoikeus_kaikki.data,
      opiskeluoikeus_kaikki.opiskeluoikeus_oid
    from
      opiskeluoikeus_kaikki
      inner join opiskeluoikeus_kaikki o2 on o2.oppija_master_oid = opiskeluoikeus_kaikki.oppija_master_oid
  )
select
  "opiskeluoikeusOid",
  "masterOppijaOid",
  versionumero,
  aikaleima,
  data
from opiskeluoikeus_palautettavat
order by "masterOppijaOid", opiskeluoikeus_oid
    """)).as[SuoritetutTutkinnotOppijanOpiskeluoikeusRow])
  }

  private implicit def getOppijanOpiskeluoikeusRow: GetResult[SuoritetutTutkinnotOppijanOpiskeluoikeusRow] = GetResult(r => {
    val opiskeluoikeus = deserializeSuoritetutTutkinnotOpiskeluoikeus(
      data = r.getJson("data"),
      oid = r.rs.getString("opiskeluoikeusOid"),
      versionumero = r.rs.getInt("versionumero"),
      aikaleima = r.rs.getTimestamp("aikaleima")
    )

    SuoritetutTutkinnotOppijanOpiskeluoikeusRow(
      masterOppijaOid = r.rs.getString("masterOppijaOid"),
      opiskeluoikeus = opiskeluoikeus
    )
  })

  private def deserializeSuoritetutTutkinnotOpiskeluoikeus(data: JValue, oid: String, versionumero: Int, aikaleima: Timestamp): SuoritetutTutkinnotOpiskeluoikeus = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(data, oid, versionumero, aikaleima)

    validatingAndResolvingExtractor.extract[SuoritetutTutkinnotOpiskeluoikeus](
      KoskiSchema.lenientDeserializationWithIgnoringNonValidatingListItemsWithoutValidation
    )(json) match {
      case Right(oo) => oo
      case Left(errors) =>
        throw new MappingException(s"Error deserializing Suoritetut tutkinnot opiskeluoikeus ${oid}: ${errors}")
    }
  }
}

case class SuoritetutTutkinnotOppijanOpiskeluoikeusRow(
  masterOppijaOid: String,
  opiskeluoikeus: SuoritetutTutkinnotOpiskeluoikeus
)

