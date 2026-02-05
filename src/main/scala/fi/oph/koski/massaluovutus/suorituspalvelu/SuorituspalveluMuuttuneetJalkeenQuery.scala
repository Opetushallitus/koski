package fi.oph.koski.massaluovutus.suorituspalvelu

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods, SQLHelpers}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.Session
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.{MassaluovutusQueryParameters, QueryFormat}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.sql.Timestamp
import java.time.LocalDateTime

@Title("Suorituspalvelun kysely päivämäärän perusteella")
@Description("Massaluovutusrajapinnan kysely suorituspalvelulle.")
@Description("Palauttaa Suorituspalvelua varten räätälöidyt tiedot annettujen oppijoiden ja koulutusmuodon mukaisista opiskeluoikeuksista tietyn ajanhetken jälkeen.")
@Description("Vastauksen skeema on taulukko <a href=\"/koski/json-schema-viewer/?schema=suorituspalvelu-result.json\">SupaResponse</a>-objekteja.")
case class SuorituspalveluMuuttuneetJalkeenQuery(
  @EnumValues(Set("supa-muuttuneet"))
  `type`: String = "supa-muuttuneet",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Aikaraja, jonka jälkeen muuttuneet opiskeluoikeudet haetaan.")
  @Description("Teknisistä syistä johtuen kannattaa aina hakea tiedot hieman pidemmältä ajalta ja suodattaa itse tulosjoukosta tarpeettomat tiedot.")
  muuttuneetJälkeen: LocalDateTime,
  @Description("Valinnainen aikaraja, jota ennen muuttuneet opiskeluoikeudet haetaan.")
  @Description("Käytetään yhdessä \"muuttuneetJälkeen\"-aikarajan kanssa haun rajaamiseen tiettyyn aikaikkunaan.")
  @Description("Ei voi olla aiemmin kuin \"muuttuneetJälkeen\"-aikaraja.")
  muuttuneetEnnen: Option[LocalDateTime] = None
) extends SuorituspalveluQuery {

  override def fillAndValidate(implicit user: Session): Either[HttpStatus, MassaluovutusQueryParameters] =
    muuttuneetEnnen match {
      case Some(ennen) if ennen.isBefore(muuttuneetJälkeen) =>
        Left(KoskiErrorCategory.badRequest("Kyselyn muuttuneetEnnen-aika ei voi olla ennen muuttuneetJälkeen-aikaa"))
      case _ => Right(this)
    }

  def getOpiskeluoikeusIds(db: DB): Seq[(Int, Timestamp, String)] =
    QueryMethods.runDbSync(
      db,
      SQLHelpers.concatMany(
        Some(
          sql"""
        SELECT opiskeluoikeus.id,
          opiskeluoikeus.aikaleima,
          COALESCE(henkilo.master_oid, henkilo.oid) AS master_oid
        FROM opiskeluoikeus
          JOIN henkilo ON henkilo.oid = opiskeluoikeus.oppija_oid
        WHERE COALESCE(henkilo.master_oid, henkilo.oid) IN (
          SELECT DISTINCT COALESCE(h.master_oid, h.oid) AS master_oid
          FROM henkilo h
            JOIN opiskeluoikeus o ON h.oid = o.oppija_oid
          WHERE o.aikaleima >= ${Timestamp.valueOf(muuttuneetJälkeen)}
          """
        ),
        muuttuneetEnnen.map(ennen => sql" AND o.aikaleima <= ${Timestamp.valueOf(ennen)} "),
        Some(
          sql"""
            AND (
              o.suoritustyypit && ${SuorituspalveluQuery.suoritustenTyypit}::text[]
              OR o.poistettu = true
            )
        )
        AND (
          opiskeluoikeus.suoritustyypit && ${SuorituspalveluQuery.suoritustenTyypit}::text[]
          OR opiskeluoikeus.poistettu = true
        )
        ORDER BY opiskeluoikeus.aikaleima
        """
        )
      ).as[(Int, Timestamp, String)]
    )
}
