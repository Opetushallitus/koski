package fi.oph.koski.perustiedot

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.json.{GenericJsonFormats, Json, LocalDateSerializer}
import fi.oph.koski.localization.LocalizedStringDeserializer
import fi.oph.koski.schema._
import fi.oph.koski.util._
import fi.oph.scalaschema.annotation.Description
import org.json4s.{JArray, JValue}

trait WithId {
  def id: Int
}

case class OpiskeluoikeudenPerustiedot(
  id: Int,
  henkilö: NimitiedotJaOid,
  oppilaitos: Oppilaitos,
  @Description("Opiskelijan opiskeluoikeuden alkamisaika joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  tyyppi: Koodistokoodiviite,
  suoritukset: List[SuorituksenPerustiedot],
  @KoodistoUri("virtaopiskeluoikeudentila")
  @KoodistoUri("koskiopiskeluoikeudentila")
  tilat: Option[List[OpiskeluoikeusJaksonPerustiedot]], // Optionality can be removed after re-indexing
  @Description("Luokan tai ryhmän tunniste, esimerkiksi 9C")
  luokka: Option[String]
) extends WithId

case class NimitiedotJaOid(oid: String, etunimet: String, kutsumanimi: String, sukunimi: String)
case class Henkilötiedot(id: Int, henkilö: NimitiedotJaOid) extends WithId

case class OpiskeluoikeusJaksonPerustiedot(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: Koodistokoodiviite
)

object OpiskeluoikeudenPerustiedot {
  import PerustiedotSearchIndex._

  def makePerustiedot(row: OpiskeluoikeusRow, henkilöRow: HenkilöRow): OpiskeluoikeudenPerustiedot = {
    makePerustiedot(row.id, row.data, row.luokka, henkilöRow.toHenkilötiedot)
  }

  def makePerustiedot(id: Int, oo: Opiskeluoikeus, henkilö: TäydellisetHenkilötiedot): OpiskeluoikeudenPerustiedot = {
    makePerustiedot(id, Json.toJValue(oo), oo.luokka.orElse(oo.ryhmä), henkilö)
  }

  def makePerustiedot(id: Int, data: JValue, luokka: Option[String], henkilö: TäydellisetHenkilötiedot): OpiskeluoikeudenPerustiedot = {
    val suoritukset: List[SuorituksenPerustiedot] = (data \ "suoritukset").asInstanceOf[JArray].arr
      .map { suoritus =>
        SuorituksenPerustiedot(
          (suoritus \ "tyyppi").extract[Koodistokoodiviite],
          KoulutusmoduulinPerustiedot((suoritus \ "koulutusmoduuli" \ "tunniste").extract[Koodistokoodiviite]), // TODO: voi olla paikallinen koodi
          (suoritus \ "osaamisala").extract[Option[List[Koodistokoodiviite]]],
          (suoritus \ "tutkintonimike").extract[Option[List[Koodistokoodiviite]]],
          (suoritus \ "toimipiste").extract[OidOrganisaatio],
          Some((suoritus \ "tila").extract[Koodistokoodiviite])
        )
      }
      .filter(_.tyyppi.koodiarvo != "perusopetuksenvuosiluokka")
    OpiskeluoikeudenPerustiedot(
      id,
      toNimitiedotJaOid(henkilö),
      (data \ "oppilaitos").extract[Oppilaitos],
      (data \ "alkamispäivä").extract[Option[LocalDate]],
      (data \ "päättymispäivä").extract[Option[LocalDate]],
      (data \ "tyyppi").extract[Koodistokoodiviite],
      suoritukset,
      Some(fixTilat((data \ "tila" \ "opiskeluoikeusjaksot").extract[List[OpiskeluoikeusJaksonPerustiedot]])),
      luokka)
  }

  private def fixTilat(tilat: List[OpiskeluoikeusJaksonPerustiedot]) = {
    tilat.zip(tilat.drop(1)).map { case (tila, next) =>
      tila.copy(loppu = Some(next.alku))
    } ++ List(tilat.last)
  }

  def toNimitiedotJaOid(henkilötiedot: TäydellisetHenkilötiedot): NimitiedotJaOid =
    NimitiedotJaOid(henkilötiedot.oid, henkilötiedot.etunimet, henkilötiedot.kutsumanimi, henkilötiedot.sukunimi)
}

case class SuorituksenPerustiedot(
  @Description("Suorituksen tyyppi, jolla erotellaan eri koulutusmuotoihin (perusopetus, lukio, ammatillinen...) ja eri tasoihin (tutkinto, tutkinnon osa, kurssi, oppiaine...) liittyvät suoritukset")
  @KoodistoUri("suorituksentyyppi")
  @Hidden
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: KoulutusmoduulinPerustiedot,
  @Description("Tieto siitä mihin osaamisalaan/osaamisaloihin oppijan tutkinto liittyy")
  @KoodistoUri("osaamisala")
  @OksaUri(tunnus = "tmpOKSAID299", käsite = "osaamisala")
  osaamisala: Option[List[Koodistokoodiviite]] = None,
  @Description("Tieto siitä mihin tutkintonimikkeeseen oppijan tutkinto liittyy")
  @KoodistoUri("tutkintonimikkeet")
  @OksaUri("tmpOKSAID588", "tutkintonimike")
  tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  toimipiste: OidOrganisaatio,
  tila: Option[Koodistokoodiviite] // Optionality can be removed after re-indexing
)

case class KoulutusmoduulinPerustiedot(
  tunniste: KoodiViite
)