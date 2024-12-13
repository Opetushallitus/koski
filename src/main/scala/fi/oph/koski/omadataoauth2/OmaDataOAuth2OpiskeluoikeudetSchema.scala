package fi.oph.koski.omadataoauth2

import fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeus
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.schema
import fi.oph.koski.schema.{Opiskeluoikeus, TäydellisetHenkilötiedot}
import fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotOpiskeluoikeus
import fi.oph.scalaschema.annotation.Title
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate
import scala.util.chaining._

object OmaDataOAuth2KaikkiOpiskeluoikeudet {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[OmaDataOAuth2KaikkiOpiskeluoikeudet]).asInstanceOf[ClassSchema])
}

case class OmaDataOAuth2KaikkiOpiskeluoikeudet(
  henkilö: OmaDataOAuth2Henkilötiedot,
  opiskeluoikeudet: List[Opiskeluoikeus],
  tokenInfo: OmaDataOAuth2TokenInfo
)

object OmaDataOAuth2SuoritetutTutkinnot {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[OmaDataOAuth2SuoritetutTutkinnot]).asInstanceOf[ClassSchema])
}

@Title("Omadata OAuth2 suoritetut tutkinnot")
case class OmaDataOAuth2SuoritetutTutkinnot(
  henkilö: OmaDataOAuth2Henkilötiedot,
  opiskeluoikeudet: List[SuoritetutTutkinnotOpiskeluoikeus],
  tokenInfo: OmaDataOAuth2TokenInfo
)

object OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet]).asInstanceOf[ClassSchema])
}

@Title("Omadata OAuth2 aktiiviset ja päättyneet opiskeluoikeudet")
case class OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet(
  henkilö: OmaDataOAuth2Henkilötiedot,
  opiskeluoikeudet: List[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus],
  tokenInfo: OmaDataOAuth2TokenInfo
)

object OmaDataOAuth2Henkilötiedot {
  def apply(laajatTiedot: LaajatOppijaHenkilöTiedot, scope: String): OmaDataOAuth2Henkilötiedot = {
    val scopes = scope.split(" ").filter(_.startsWith("HENKILOTIEDOT_"))

    def withNimi(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_NIMI") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          sukunimi = Some(laajatTiedot.sukunimi),
          etunimet = Some(laajatTiedot.etunimet),
          kutsumanimi = Some(laajatTiedot.kutsumanimi)
        )
      } else {
        henkilö
      }
    }

    def withOppijanumero(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_OPPIJANUMERO") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          oid = Some(laajatTiedot.oid)
        )
      } else {
        henkilö
      }
    }

    def withHetu(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_HETU") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          hetu = laajatTiedot.hetu
        )
      } else {
        henkilö
      }
    }

    def withSyntymäaika(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_SYNTYMAAIKA") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          syntymäaika = laajatTiedot.syntymäaika
        )
      } else {
        henkilö
      }
    }

    OmaDataOAuth2Henkilötiedot()
      .pipe(withNimi)
      .pipe(withOppijanumero)
      .pipe(withHetu)
      .pipe(withSyntymäaika)
  }

  def apply(täydellisetTiedot: TäydellisetHenkilötiedot, scope: String): OmaDataOAuth2Henkilötiedot = {
    val scopes = scope.split(" ").filter(_.startsWith("HENKILOTIEDOT_"))

    def withNimi(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_NIMI") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          sukunimi = Some(täydellisetTiedot.sukunimi),
          etunimet = Some(täydellisetTiedot.etunimet),
          kutsumanimi = Some(täydellisetTiedot.kutsumanimi)
        )
      } else {
        henkilö
      }
    }

    def withOppijanumero(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_OPPIJANUMERO") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          oid = Some(täydellisetTiedot.oid)
        )
      } else {
        henkilö
      }
    }

    def withHetu(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_HETU") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          hetu = täydellisetTiedot.hetu
        )
      } else {
        henkilö
      }
    }

    def withSyntymäaika(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_SYNTYMAAIKA") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          syntymäaika = täydellisetTiedot.syntymäaika
        )
      } else {
        henkilö
      }
    }

    OmaDataOAuth2Henkilötiedot()
      .pipe(withNimi)
      .pipe(withOppijanumero)
      .pipe(withHetu)
      .pipe(withSyntymäaika)
  }
}

case class OmaDataOAuth2Henkilötiedot(
  oid: Option[String] = None,
  sukunimi: Option[String] = None,
  etunimet: Option[String] = None,
  kutsumanimi: Option[String] = None,
  hetu: Option[String] = None,
  syntymäaika: Option[LocalDate] = None
)

case class OmaDataOAuth2TokenInfo(
  scope: String,
  expirationTime: String
)
