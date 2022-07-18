package fi.oph.koski.perustiedot

import java.time.LocalDate

import fi.oph.koski.db.{HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.henkilo.{OppijaHenkilö, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.{Hidden, KoodistoUri, OksaUri}
import fi.oph.koski.util.DateOrdering.localDateOrdering
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
  koulutustoimija: Option[Koulutustoimija],
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
) extends OpiskeluoikeudenOsittaisetTiedot

case class NimitiedotJaOid(oid: String, etunimet: String, kutsumanimi: String, sukunimi: String)
object NimitiedotJaOid {
  def apply(henkilö: TäydellisetHenkilötiedot): NimitiedotJaOid = NimitiedotJaOid(henkilö.oid, henkilö.etunimet, henkilö.kutsumanimi, henkilö.sukunimi)
  def apply(henkilöRow: HenkilöRow): NimitiedotJaOid = NimitiedotJaOid(henkilöRow.oid, henkilöRow.etunimet, henkilöRow.kutsumanimi, henkilöRow.sukunimi)
  def apply(henkilö: OppijaHenkilö): NimitiedotJaOid = NimitiedotJaOid(henkilö.oid, henkilö.etunimet, henkilö.kutsumanimi, henkilö.sukunimi)
}
case class OpiskeluoikeudenHenkilötiedot(
  id: Int,
  henkilö: NimitiedotJaOid,
  henkilöOid: Option[Henkilö.Oid]
) extends OpiskeluoikeudenOsittaisetTiedot

object OpiskeluoikeudenHenkilötiedot {
  def apply(id: Int, henkilö: OppijaHenkilöWithMasterInfo): OpiskeluoikeudenHenkilötiedot =
    OpiskeluoikeudenHenkilötiedot(id, NimitiedotJaOid(henkilö.master.getOrElse(henkilö.henkilö)), Some(henkilö.henkilö.oid))
  def apply(id: Int, henkilöRow: HenkilöRow, masterHenkilöRow: Option[HenkilöRow]): OpiskeluoikeudenHenkilötiedot =
    OpiskeluoikeudenHenkilötiedot(id, NimitiedotJaOid(masterHenkilöRow.getOrElse(henkilöRow)), Some(henkilöRow.oid))
}

case class OpiskeluoikeusJaksonPerustiedot(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: Koodistokoodiviite
)

object OpiskeluoikeudenPerustiedot {
  val serializationContext = SerializationContext(KoskiSchema.schemaFactory, omitEmptyFields = false)

  def makePerustiedot(row: OpiskeluoikeusRow, henkilöRow: HenkilöRow, masterHenkilöRow: Option[HenkilöRow]): OpiskeluoikeudenPerustiedot = {
    makePerustiedot(
      row.id,
      row.toOpiskeluoikeusUnsafe(KoskiSpecificSession.untrustedUser),
      henkilöRow,
      masterHenkilöRow
    )
  }

  def makePerustiedot(id: Int, oo: Opiskeluoikeus, henkilö: OppijaHenkilöWithMasterInfo): OpiskeluoikeudenPerustiedot = {
    makePerustiedot(
      id,
      JsonSerializer.serializeWithUser(KoskiSpecificSession.untrustedUser)(oo),
      getLuokka(oo),
      OpiskeluoikeudenHenkilötiedot(id, henkilö)
    )
  }

  def makePerustiedot(id: Int, oo: Opiskeluoikeus, henkilöRow: HenkilöRow, masterHenkilöRow: Option[HenkilöRow]): OpiskeluoikeudenPerustiedot = {
    makePerustiedot(
      id,
      JsonSerializer.serializeWithUser(KoskiSpecificSession.untrustedUser)(oo),
      getLuokka(oo),
      OpiskeluoikeudenHenkilötiedot(id, henkilöRow, masterHenkilöRow)
    )
  }

  private def makePerustiedot(id: Int, data: JValue, luokka: Option[String], henkilö: OpiskeluoikeudenHenkilötiedot): OpiskeluoikeudenPerustiedot = {
    val suoritukset: List[SuorituksenPerustiedot] = (data \ "suoritukset").asInstanceOf[JArray].arr
      .map { suoritus =>
        SuorituksenPerustiedot(
          extract[Koodistokoodiviite](suoritus \ "tyyppi"),
          KoulutusmoduulinPerustiedot.makeKoulutusmoduulinPerustiedot(extract[KoodiViitteenPerustiedot](suoritus \ "koulutusmoduuli" \ "tunniste")),
          extract[Option[List[Osaamisalajakso]]](suoritus \ "osaamisala").map(_.map(_.osaamisala)),
          extract[Option[List[Koodistokoodiviite]]](suoritus \ "tutkintonimike"),
          extract[OidOrganisaatio](suoritus \ "toimipiste", ignoreExtras = true)
        )
      }
      .filter(_.tyyppi.koodiarvo != "perusopetuksenvuosiluokka")
    OpiskeluoikeudenPerustiedot(
      id,
      Some(henkilö.henkilö),
      henkilö.henkilöOid,
      extract[Oppilaitos](data \ "oppilaitos"),
      extract[Option[Koulutustoimija]](data \ "koulutustoimija"),
      extract[Option[SisältäväOpiskeluoikeus]](data \ "sisältyyOpiskeluoikeuteen"),
      extract[Option[LocalDate]](data \ "alkamispäivä"),
      extract[Option[LocalDate]](data \ "päättymispäivä"),
      extract[Koodistokoodiviite](data \ "tyyppi"),
      suoritukset,
      Some(fixTilat(extract[List[OpiskeluoikeusJaksonPerustiedot]](data \ "tila" \ "opiskeluoikeusjaksot", ignoreExtras = true))),
      luokka)
  }

  private def getLuokka(oo: Opiskeluoikeus): Option[String] = oo match {
    case is: InternationalSchoolOpiskeluoikeus => is.suoritukset.filter(_.alkamispäivä.isDefined).sortBy(_.alkamispäivä).lastOption match {
      case Some(suoritus) => suoritus.luokka.orElse(suoritus.koulutusmoduuli.tunniste.getNimi.getOrElse(Finnish("")).getOptional("en"))
      case _ => None
    }
    case oo: Opiskeluoikeus => oo.luokka.orElse(oo.ryhmä)
  }

  def serializePerustiedot(tiedot: OpiskeluoikeudenOsittaisetTiedot) = Serializer.serialize(tiedot, serializationContext)

  private def fixTilat(tilat: List[OpiskeluoikeusJaksonPerustiedot]) = {
    tilat.zip(tilat.drop(1)).map { case (tila, next) =>
      tila.copy(loppu = Some(next.alku))
    } ++ List(tilat.last)
  }

  def docId(doc: JValue) = extract[Int](doc \ "id")
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

object KoulutusmoduulinPerustiedot {
  def makeKoulutusmoduulinPerustiedot(tunniste: KoodiViitteenPerustiedot) = KoulutusmoduulinPerustiedot(tunniste.copy(
    nimi = tunniste.nimi.flatMap(fillInMissingLanguages),
    lyhytNimi = tunniste.lyhytNimi.flatMap(fillInMissingLanguages)
  ))

  private def fillInMissingLanguages(n: LocalizedString) = {
    LocalizedString.sanitize(n.values).map { ls =>
      ls.copy(sv = Some(ls.sv.getOrElse(ls.fi)))
    }
  }
}

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
