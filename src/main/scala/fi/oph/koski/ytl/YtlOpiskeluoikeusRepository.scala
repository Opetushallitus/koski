package fi.oph.koski.ytl

import java.sql.Timestamp
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, KoskiTables, QueryMethods, SQLHelpers}
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import org.json4s.JsonAST.JValue
import org.json4s.MappingException
import slick.jdbc.GetResult

import java.time.Instant

class YtlOpiskeluoikeusRepository(
  val db: DB,
  val validatingAndResolvingExtractor: ValidatingAndResolvingExtractor
) extends QueryMethods {
  def getOppijanKaikkiOpiskeluoikeudetJosJokinNiistäOnPäivittynytAikaleimanJälkeen(
    palautettavatOpiskeluoikeudenTyypit: Seq[String],
    oppijaMasterOids: Seq[String],
    aikaleimaInstant: Option[Instant]
  ): Seq[OppijanOpiskeluoikeusRow] = {
    val aikaleima = aikaleimaInstant.map(Timestamp.from)

    runDbSync(SQLHelpers.concatMany(Some(
      sql"""
with
  haettu_oppija as (
    select
      oid as oppija_oid,
      coalesce(master_oid, oid) as oppija_master_oid
    from henkilo
    where henkilo.oid = any($oppijaMasterOids) or henkilo.master_oid = any($oppijaMasterOids)
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
      mitatoity,
      case
        when $aikaleima::timestamptz is null then true -- jos aikaleimaa ei ole annettu, palautetaan kaikki
        when linkitetty.oppija_master_oid is not null then true -- Palauta linkitettyjä sisältävät oppijat aina
        else $aikaleima::timestamptz < aikaleima
      end palauta,
      data
    from
      opiskeluoikeus
      join haettu_oppija on haettu_oppija.oppija_oid = opiskeluoikeus.oppija_oid
      left join linkitetty on linkitetty.oppija_master_oid = haettu_oppija.oppija_master_oid
    where opiskeluoikeus.koulutusmuoto = any($palautettavatOpiskeluoikeudenTyypit)
    order by oppija_master_oid
  )
  , opiskeluoikeus_palautettavat as (
    select
      distinct on (opiskeluoikeus_kaikki.opiskeluoikeus_oid) opiskeluoikeus_kaikki.opiskeluoikeus_oid "opiskeluoikeusOid",
      opiskeluoikeus_kaikki.oppija_master_oid as "masterOppijaOid",
      opiskeluoikeus_kaikki.versionumero,
      opiskeluoikeus_kaikki.aikaleima,
      opiskeluoikeus_kaikki.mitatoity as "mitätöity",
      case
        when $aikaleima::timestamptz is null then false
        when opiskeluoikeus_kaikki.mitatoity then $aikaleima::timestamptz < opiskeluoikeus_kaikki.aikaleima
        else false
      end "mitätöityAikaleimanJälkeen",
      opiskeluoikeus_kaikki.data,
      opiskeluoikeus_kaikki.opiskeluoikeus_oid
    from
      opiskeluoikeus_kaikki
      inner join opiskeluoikeus_kaikki o2 on o2.oppija_master_oid = opiskeluoikeus_kaikki.oppija_master_oid and o2.palauta is true
  )
select
  "opiskeluoikeusOid",
  "masterOppijaOid",
  versionumero,
  aikaleima,
  "mitätöity",
  "mitätöityAikaleimanJälkeen",
  data
from opiskeluoikeus_palautettavat
order by "masterOppijaOid", opiskeluoikeus_oid
    """)).as[OppijanOpiskeluoikeusRow])
  }

  private implicit def getOppijanOpiskeluoikeusRow: GetResult[OppijanOpiskeluoikeusRow] = GetResult(r => {
    val opiskeluoikeus = deserializeYtlOpiskeluoikeus(
      data = r.getJson("data"),
      oid = r.rs.getString("opiskeluoikeusOid"),
      versionumero = r.rs.getInt("versionumero"),
      aikaleima = r.rs.getTimestamp("aikaleima")
    )

    OppijanOpiskeluoikeusRow(
      masterOppijaOid = r.rs.getString("masterOppijaOid"),
      mitätöity = r.rs.getBoolean("mitätöity"),
      mitätöityAikaleimanJälkeen = r.rs.getBoolean("mitätöityAikaleimanJälkeen"),
      opiskeluoikeus = opiskeluoikeus
    )
  })

  private def deserializeYtlOpiskeluoikeus(data: JValue, oid: String, versionumero: Int, aikaleima: Timestamp): YtlOpiskeluoikeus = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(data, oid, versionumero, aikaleima)

    validatingAndResolvingExtractor.extract[YtlOpiskeluoikeus](
      KoskiSchema.lenientDeserializationWithIgnoringNonValidatingListItems
    )(json) match {
      case Right(oo) => oo
      case Left(errors) =>
        throw new MappingException(s"Error deserializing YTL opiskeluoikeus ${oid}: ${errors}")
    }
  }
}

case class OppijanOpiskeluoikeusRow(
  masterOppijaOid: String,
  mitätöity: Boolean,
  mitätöityAikaleimanJälkeen: Boolean,
  opiskeluoikeus: YtlOpiskeluoikeus
)

