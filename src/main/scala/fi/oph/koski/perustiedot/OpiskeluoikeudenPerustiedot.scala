package fi.oph.koski.perustiedot

import java.time.LocalDate

import fi.oph.koski.db.{HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.henkilo.TäydellisetHenkilötiedotWithMasterInfo
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.{Hidden, KoodistoUri, OksaUri}
import fi.oph.scalaschema.annotation.{Description, Discriminator}
import fi.oph.scalaschema.{SerializationContext, Serializer}
import org.json4s.{JArray, JValue}

trait OpiskeluoikeudenOsittaisetTiedot {
  def id: Int
}

case class OpiskeluoikeudenPerustiedot(
  id: Int,
  // Näytettävät henkilötiedot (joko henkilö johon opiskeluoikesu on linkitetty, tai tämän henkilön "master-henkilö")
  henkilö: Option[NimitiedotJaOid],
  // Oid, johon opiskeluoikeus on suoraan linkitetty
  henkilöOid: Option[Henkilö.Oid],
  oppilaitos: Oppilaitos,
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  @Description("Opiskelijan opiskeluoikeuden alkamisaika joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  @Discriminator
  tyyppi: Koodistokoodiviite,
  suoritukset: List[SuorituksenPerustiedot],
  @KoodistoUri("virtaopiskeluoikeudentila")
  @KoodistoUri("koskiopiskeluoikeudentila")
  tilat: Option[List[OpiskeluoikeusJaksonPerustiedot]], // Optionality can be removed after re-indexing
  @Description("Luokan tai ryhmän tunniste, esimerkiksi 9C")
  luokka: Option[String]
) extends OpiskeluoikeudenOsittaisetTiedot {
  def henkilötiedot = henkilö.map(h => OpiskeluoikeudenHenkilötiedot(id, h, henkilöOid))
}

case class NimitiedotJaOid(oid: String, etunimet: String, kutsumanimi: String, sukunimi: String)
object NimitiedotJaOid {
  def apply(henkilö: TäydellisetHenkilötiedot): NimitiedotJaOid = NimitiedotJaOid(henkilö.oid, henkilö.etunimet, henkilö.kutsumanimi, henkilö.sukunimi)

}
case class OpiskeluoikeudenHenkilötiedot(
  id: Int, henkilö: NimitiedotJaOid,
  henkilöOid: Option[Henkilö.Oid]
) extends OpiskeluoikeudenOsittaisetTiedot

object OpiskeluoikeudenHenkilötiedot {
  def apply(id: Int, henkilö: TäydellisetHenkilötiedotWithMasterInfo): OpiskeluoikeudenHenkilötiedot = OpiskeluoikeudenHenkilötiedot(id, NimitiedotJaOid(OpiskeluoikeudenPerustiedot.näytettäväHenkilö(henkilö)), Some(henkilö.henkilö.oid))
}

case class OpiskeluoikeusJaksonPerustiedot(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: Koodistokoodiviite
)

object OpiskeluoikeudenPerustiedot {
  val serializationContext = SerializationContext(KoskiSchema.schemaFactory, omitEmptyFields = false)

  def makePerustiedot(row: OpiskeluoikeusRow, henkilöRow: HenkilöRow, masterHenkilöRow: Option[HenkilöRow]): OpiskeluoikeudenPerustiedot = {
    makePerustiedot(row.id, row.data, row.luokka, Some(TäydellisetHenkilötiedotWithMasterInfo(henkilöRow.toHenkilötiedot, masterHenkilöRow.map(_.toHenkilötiedot))))
  }

  def makePerustiedot(id: Int, oo: Opiskeluoikeus, henkilö: Option[TäydellisetHenkilötiedotWithMasterInfo]): OpiskeluoikeudenPerustiedot = {
    makePerustiedot(id, JsonSerializer.serializeWithUser(KoskiSession.untrustedUser)(oo), oo.luokka.orElse(oo.ryhmä), henkilö)
  }

  def makePerustiedot(id: Int, data: JValue, luokka: Option[String], henkilö: Option[TäydellisetHenkilötiedotWithMasterInfo]): OpiskeluoikeudenPerustiedot = {
    val suoritukset: List[SuorituksenPerustiedot] = (data \ "suoritukset").asInstanceOf[JArray].arr
      .map { suoritus =>
        SuorituksenPerustiedot(
          extract[Koodistokoodiviite](suoritus \ "tyyppi"),
          KoulutusmoduulinPerustiedot(extract[KoodiViitteenPerustiedot](suoritus \ "koulutusmoduuli" \ "tunniste")),
          extract[Option[List[Osaamisalajakso]]](suoritus \ "osaamisala").map(_.map(_.osaamisala)),
          extract[Option[List[Koodistokoodiviite]]](suoritus \ "tutkintonimike"),
          extract[OidOrganisaatio](suoritus \ "toimipiste", ignoreExtras = true)
        )
      }
      .filter(_.tyyppi.koodiarvo != "perusopetuksenvuosiluokka")
    OpiskeluoikeudenPerustiedot(
      id,
      henkilö.map(h => NimitiedotJaOid(näytettäväHenkilö(h))),
      henkilö.map(h => h.henkilö.oid),
      extract[Oppilaitos](data \ "oppilaitos"),
      extract[Option[SisältäväOpiskeluoikeus]](data \ "sisältyyOpiskeluoikeuteen"),
      extract[Option[LocalDate]](data \ "alkamispäivä"),
      extract[Option[LocalDate]](data \ "päättymispäivä"),
      extract[Koodistokoodiviite](data \ "tyyppi"),
      suoritukset,
      Some(fixTilat(extract[List[OpiskeluoikeusJaksonPerustiedot]](data \ "tila" \ "opiskeluoikeusjaksot", ignoreExtras = true))),
      luokka)
  }

  def serializePerustiedot(tiedot: OpiskeluoikeudenOsittaisetTiedot) = Serializer.serialize(tiedot, serializationContext)

  private def fixTilat(tilat: List[OpiskeluoikeusJaksonPerustiedot]) = {
    tilat.zip(tilat.drop(1)).map { case (tila, next) =>
      tila.copy(loppu = Some(next.alku))
    } ++ List(tilat.last)
  }

  def docId(doc: JValue) = extract[Int](doc \ "id")

  def näytettäväHenkilö(henkilö: TäydellisetHenkilötiedotWithMasterInfo) = henkilö.master.getOrElse(henkilö.henkilö)
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
  toimipiste: OidOrganisaatio
)

case class KoulutusmoduulinPerustiedot(
  tunniste: KoodiViitteenPerustiedot
)

case class KoodiViitteenPerustiedot(
  koodiarvo: Option[String],
  nimi: Option[LocalizedString],
  lyhytNimi: Option[LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
)
