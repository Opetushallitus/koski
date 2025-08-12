package fi.oph.koski.koskiuser

import fi.oph.koski.util.Timing
import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.schema._

import scala.language.{higherKinds, implicitConversions}

object Rooli {
  type Role = String
  val READ = "READ"
  val READ_UPDATE = "READ_UPDATE"
  val READ_UPDATE_ESIOPETUS = "READ_UPDATE_ESIOPETUS"
  val TIEDONSIIRRON_MITATOINTI = "TIEDONSIIRRON_MITATOINTI"
  val KAYTTOLIITTYMASIIRRON_MITATOINTI = "KAYTTOLIITTYMASIIRRON_MITATOINTI"
  val OPHKATSELIJA = "OPHKATSELIJA"
  val OPHPAAKAYTTAJA = "OPHPAAKAYTTAJA"
  val YLLAPITAJA = "YLLAPITAJA"
  val TIEDONSIIRTO = "TIEDONSIIRTO"
  val LUOTTAMUKSELLINEN_KAIKKI_TIEDOT = "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT"
  val LUOTTAMUKSELLINEN_KELA_SUPPEA = "LUOTTAMUKSELLINEN_KELA_SUPPEA"
  val LUOTTAMUKSELLINEN_KELA_LAAJA = "LUOTTAMUKSELLINEN_KELA_LAAJA"
  val LUKU_ESIOPETUS = "LUKU_ESIOPETUS"
  val GLOBAALI_LUKU_PERUSOPETUS = "GLOBAALI_LUKU_PERUSOPETUS"
  val GLOBAALI_LUKU_TOINEN_ASTE = "GLOBAALI_LUKU_TOINEN_ASTE"
  val GLOBAALI_LUKU_KORKEAKOULU = "GLOBAALI_LUKU_KORKEAKOULU"
  val GLOBAALI_LUKU_MUU_KUIN_SAANNELTY = "GLOBAALI_LUKU_MUU_KUIN_SAANNELTY"
  val GLOBAALI_LUKU_TAITEENPERUSOPETUS = "GLOBAALI_LUKU_TAITEENPERUSOPETUS"
  val GLOBAALI_LUKU_KIELITUTKINTO = "GLOBAALI_LUKU_KIELITUTKINTO"
  val TILASTOKESKUS = "TILASTOKESKUS"
  val VALVIRA = "VALVIRA"
  val MIGRI = "MIGRI"
  val YTL = "YTL"
  val VKT = "VKT"
  val HAKEMUSPALVELU_API = "HAKEMUSPALVELU_API"
  val HSL = "HSL"
  val SUOMIFI = "SUOMIFI"
  val OPPIVELVOLLISUUSTIETO_RAJAPINTA = "OPPIVELVOLLISUUSTIETO_RAJAPINTA"
  val MITATOIDYT_OPISKELUOIKEUDET = "MITATOIDYT_OPISKELUOIKEUDET" // Ei käyttöoikeus-palvelussa
  val POISTETUT_OPISKELUOIKEUDET = "POISTETUT_OPISKELUOIKEUDET" // Ei käyttöoikeus-palvelussa
  val LAHDEJARJESTELMAKYTKENNAN_PURKAMINEN = "LAHDEJARJESTELMAKYTKENNAN_PURKAMINEN" // Oikeus muuttaa tiedonsiirron kautta lisätty opiskeluoikeus käyttöliittymällä muokattavaksi
  val SUORITUSJAKO_KATSELIJA = "SUORITUSJAKO_KATSELIJA" // Ei käyttöoikeus-palvelussa
  val KIELITUTKINTOREKISTERI = "KIELITUTKINTOREKISTERI"

  // Suostumusperustaisen (OAuth2) -rajapinnan scopeissa käytetyt käyttöoikeudet. "OMADATAOAUTH2_"-jälkeinen osuus on sellaisenaan
  // tuettuna merkkijonona myös scopessa, joten näiden muuttamista ei voi tehdä vapaasti.
  val omadataOAuth2Prefix = "OMADATAOAUTH2_"
  val OMADATAOAUTH2_HENKILOTIEDOT_NIMI = "OMADATAOAUTH2_HENKILOTIEDOT_NIMI"
  val OMADATAOAUTH2_HENKILOTIEDOT_SYNTYMAAIKA = "OMADATAOAUTH2_HENKILOTIEDOT_SYNTYMAAIKA"
  val OMADATAOAUTH2_HENKILOTIEDOT_HETU = "OMADATAOAUTH2_HENKILOTIEDOT_HETU"
  val OMADATAOAUTH2_HENKILOTIEDOT_OPPIJANUMERO = "OMADATAOAUTH2_HENKILOTIEDOT_OPPIJANUMERO"
  val OMADATAOAUTH2_HENKILOTIEDOT_KAIKKI_TIEDOT = "OMADATAOAUTH2_HENKILOTIEDOT_KAIKKI_TIEDOT"
  val OMADATAOAUTH2_OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT = "OMADATAOAUTH2_OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"
  val OMADATAOAUTH2_OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT = "OMADATAOAUTH2_OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT"
  val OMADATAOAUTH2_OPISKELUOIKEUDET_KAIKKI_TIEDOT = "OMADATAOAUTH2_OPISKELUOIKEUDET_KAIKKI_TIEDOT"

  // Ei käyttöoikeus-palvelussa. Ilman tätä ei pääse käsiksi Kosken tietokantaan tallennettuihin YO-opiskeluoikeuksiin
  val TALLENNETUT_YLIOPPILASTUTKINNON_OPISKELUOIKEUDET = "TALLENNETUT_YLIOPPILASTUTKINNON_OPISKELUOIKEUDET"

  val KAIKKI_OPISKELUOIKEUS_TYYPIT = "KAIKKI_OPISKELUOIKEUS_TYYPIT"
  val AIKUISTENPERUSOPETUS = "AIKUISTENPERUSOPETUS"
  val AMMATILLINENKOULUTUS = "AMMATILLINENKOULUTUS"
  val DIATUTKINTO = "DIATUTKINTO"
  val ESIOPETUS = "ESIOPETUS"
  val IBTUTKINTO = "IBTUTKINTO"
  val INTERNATIONALSCHOOL = "INTERNATIONALSCHOOL"
  val KORKEAKOULUTUS = "KORKEAKOULUTUS"
  val LUKIOKOULUTUS = "LUKIOKOULUTUS"
  val LUVA = "LUVA"
  val PERUSOPETUKSEENVALMISTAVAOPETUS = "PERUSOPETUKSEENVALMISTAVAOPETUS"
  val PERUSOPETUKSENLISAOPETUS = "PERUSOPETUKSENLISAOPETUS"
  val PERUSOPETUS = "PERUSOPETUS"
  val YLIOPPILASTUTKINTO = "YLIOPPILASTUTKINTO"
  val VAPAANSIVISTYSTYONKOULUTUS = "VAPAANSIVISTYSTYONKOULUTUS"
  val TUVA = "TUVA" // Ei käyttöoikeus-palvelussa
  val ESH = "EUROPEANSCHOOLOFHELSINKI" // Ei käyttöoikeus-palvelussa
  val EBTUTKINTO = "EBTUTKINTO" // Ei käyttöoikeus-palvelussa
  val MUUKUINSAANNELTYKOULUTUS = "MUUKUINSAANNELTYKOULUTUS" // Ei käyttöoikeus-palvelussa
  val TAITEENPERUSOPETUS = "TAITEENPERUSOPETUS" // Ei käyttöoikeus-palvelussa
  val TAITEENPERUSOPETUS_HANKINTAKOULUTUS = "TAITEENPERUSOPETUS_HANKINTAKOULUTUS"
  val KIELITUTKINTO = "KIELITUTKINTO" // Ei käyttöoikeus-palvelussa

  def globaalitKoulutusmuotoRoolit = List(
    GLOBAALI_LUKU_PERUSOPETUS,
    GLOBAALI_LUKU_TOINEN_ASTE,
    GLOBAALI_LUKU_KORKEAKOULU,
    GLOBAALI_LUKU_MUU_KUIN_SAANNELTY,
    GLOBAALI_LUKU_TAITEENPERUSOPETUS,
    GLOBAALI_LUKU_KIELITUTKINTO,
  )

  def rooliPäätasonSuoritukseen(opiskeluoikeudenTyyppi: String, päätasonSuorituksenTyyppi: SuorituksenTyyppi.SuorituksenTyyppi): String =
    s"$opiskeluoikeudenTyyppi${Käyttöoikeus.opiskeluoikeusPäätasonSuoritusErotin}${päätasonSuorituksenTyyppi.koodiarvo}".toUpperCase
}

object Käyttöoikeus extends Timing {
  val opiskeluoikeusPäätasonSuoritusErotin = "__"

  def withPalveluroolitFilter(käyttöoikeudet: Set[Käyttöoikeus], palvelurooliFilter: Palvelurooli => Boolean): Set[Käyttöoikeus] = {
    käyttöoikeudet.flatMap(withPalveluroolitFilter(palvelurooliFilter))
  }

  def hasPalvelurooli(käyttöoikeudet: Set[Käyttöoikeus], palvelurooliFilter: Palvelurooli => Boolean): Boolean = {
    käyttöoikeudet.exists(withPalveluroolitFilter(palvelurooliFilter)(_).nonEmpty)
  }

  private def withPalveluroolitFilter(palvelurooliFilter: Palvelurooli => Boolean)(käyttöoikeus: Käyttöoikeus): Set[Käyttöoikeus] = {
    käyttöoikeus match {
      case KäyttöoikeusOrg(organisaatio, roolit, juuri, oppilaitostyyppi) =>
      {
        val filteredRoolit = roolit.filter(palvelurooliFilter)
        if (filteredRoolit.isEmpty) Set.empty else Set(KäyttöoikeusOrg(organisaatio, filteredRoolit, juuri, oppilaitostyyppi))
      }
      case KäyttöoikeusGlobal(globalPalveluroolit) =>
      {
        val filteredRoolit = globalPalveluroolit.filter(palvelurooliFilter)
        if (filteredRoolit.isEmpty) Set.empty else Set(KäyttöoikeusGlobal(filteredRoolit))
      }
      case KäyttöoikeusViranomainen(globalPalveluroolit) =>
      {
        val filteredRoolit = globalPalveluroolit.filter(palvelurooliFilter)
        if (filteredRoolit.isEmpty) Set.empty else Set(KäyttöoikeusViranomainen(filteredRoolit))
      }
      case KäyttöoikeusVarhaiskasvatusToimipiste(koulutustoimija, ulkopuolinenOrganisaatio, organisaatiokohtaisetPalveluroolit, onVarhaiskasvatuksenToimipiste) =>
      {
        val filteredRoolit = organisaatiokohtaisetPalveluroolit.filter(palvelurooliFilter)
        if (filteredRoolit.isEmpty) Set.empty else Set(KäyttöoikeusVarhaiskasvatusToimipiste(koulutustoimija, ulkopuolinenOrganisaatio, filteredRoolit, onVarhaiskasvatuksenToimipiste))
      }
      case _ => Set.empty
    }
  }

  def parseAllowedOpiskeluoikeudenTyypit(roolit: List[Palvelurooli], accessTypes: List[AccessType.Value], isRootUser: Boolean = false): Set[OoPtsMask] = {
    if (!accessTypes.contains(AccessType.read)) {
      Set.empty
    } else {
      timed("parseAllowedOpiskeluoikeudenTyypit", thresholdMs = 0) {
        val käyttäjänRoolit = unifyRoolit(roolit, isRootUser)
          .filter(_.palveluName == "KOSKI")

        logger.info("käyttäjänRoolit.size: " + käyttäjänRoolit.size)
        
        val käyttäjänOoPtsRoolit = käyttäjänRoolit
          .flatMap(OoPtsMask.fromPalvelurooli)
          .distinct

        val kaikkiOpiskeluoikeudenTyypit = OpiskeluoikeudenTyyppi
          .kaikkiTyypit(true)
          .flatMap(OoPtsMask.fromKoodistokoodiviite)
          .toList

        val tyypitJoihinKäyttöRajoitettu = käyttäjänOoPtsRoolit
          .flatMap { rooli => kaikkiOpiskeluoikeudenTyypit.flatMap(rooli.intersect) }
          .toSet

        val oikeuksiaEiOleRajoitettuTiettyihinTyyppeihin = tyypitJoihinKäyttöRajoitettu.isEmpty

        if (oikeuksiaEiOleRajoitettuTiettyihinTyyppeihin) {
          OpiskeluoikeudenTyyppi
            .kaikkiTyypit(isRootUser)
            .flatMap(OoPtsMask.fromKoodistokoodiviite)
        } else {
          tyypitJoihinKäyttöRajoitettu
        }
      }
    }
  }

  private def unifyRoolit(roolit: List[Palvelurooli], isRootUser: Boolean): List[Palvelurooli] = roolit flatMap {
    case Palvelurooli("KOSKI", GLOBAALI_LUKU_PERUSOPETUS) => List(
      ESIOPETUS,
      PERUSOPETUS,
      AIKUISTENPERUSOPETUS,
      PERUSOPETUKSENLISAOPETUS,
      PERUSOPETUKSEENVALMISTAVAOPETUS,
      INTERNATIONALSCHOOL,
      ESH
    ).map(Palvelurooli(_))
    case Palvelurooli("KOSKI", GLOBAALI_LUKU_TOINEN_ASTE) => List(
      AMMATILLINENKOULUTUS,
      IBTUTKINTO,
      DIATUTKINTO,
      LUKIOKOULUTUS,
      LUVA,
      YLIOPPILASTUTKINTO,
      INTERNATIONALSCHOOL,
      VAPAANSIVISTYSTYONKOULUTUS,
      TUVA,
      ESH,
      EBTUTKINTO
    ).map(Palvelurooli(_))
    case Palvelurooli("KOSKI", READ_UPDATE_ESIOPETUS) => List(Palvelurooli(ESIOPETUS))
    case Palvelurooli("KOSKI", LUKU_ESIOPETUS) => List(Palvelurooli(ESIOPETUS))
    case Palvelurooli("KOSKI", GLOBAALI_LUKU_KORKEAKOULU) => List(Palvelurooli(KORKEAKOULUTUS))
    case Palvelurooli("KOSKI", GLOBAALI_LUKU_MUU_KUIN_SAANNELTY) => List(Palvelurooli(MUUKUINSAANNELTYKOULUTUS))
    case Palvelurooli("KOSKI", GLOBAALI_LUKU_TAITEENPERUSOPETUS) => List(Palvelurooli(TAITEENPERUSOPETUS))
    case Palvelurooli("KOSKI", TAITEENPERUSOPETUS_HANKINTAKOULUTUS) => List(Palvelurooli(TAITEENPERUSOPETUS))
    case Palvelurooli("KOSKI", GLOBAALI_LUKU_KIELITUTKINTO) => List(Palvelurooli(KIELITUTKINTO))
    case Palvelurooli("KOSKI", KIELITUTKINTOREKISTERI) => List(Palvelurooli(KIELITUTKINTO))
    case Palvelurooli("KOSKI", KAIKKI_OPISKELUOIKEUS_TYYPIT) => OpiskeluoikeudenTyyppi.kaikkiTyypit(isRootUser).map(t => Palvelurooli(t.koodiarvo.toUpperCase))
    case rooli => List(rooli)
  }
}

// this trait is intentionally left mostly blank to make it harder to accidentally mix global and organization-specific rights
trait Käyttöoikeus {
  def allowedOpiskeluoikeusTyypit: Set[OoPtsMask] = Set.empty
}

case class KäyttöoikeusGlobal(globalPalveluroolit: List[Palvelurooli]) extends Käyttöoikeus {
  def globalAccessType: List[AccessType.Value] = globalPalveluroolit flatMap {
    case Palvelurooli("KOSKI", "OPHKATSELIJA") => List(AccessType.read)
    case Palvelurooli("KOSKI", "OPHPAAKAYTTAJA") => List(
      AccessType.read,
      AccessType.write,
      AccessType.tiedonsiirronMitätöinti,
      AccessType.käyttöliittymäsiirronMitätöinti,
      AccessType.lähdejärjestelmäkytkennänPurkaminen,
    )
    case Palvelurooli("KOSKI", KIELITUTKINTOREKISTERI) => List(
      AccessType.read,
      AccessType.write,
    )
    case _ => Nil
  }

  override lazy val allowedOpiskeluoikeusTyypit: Set[OoPtsMask] = Käyttöoikeus.parseAllowedOpiskeluoikeudenTyypit(globalPalveluroolit, globalAccessType, isRootUser = true)
}

trait OrgKäyttöoikeus extends Käyttöoikeus {
  def organisaatiokohtaisetPalveluroolit: List[Palvelurooli]
  def organisaatioAccessType: List[AccessType.Value] = organisaatiokohtaisetPalveluroolit flatMap {
    case Palvelurooli("KOSKI", "READ") => List(AccessType.read)
    case Palvelurooli("KOSKI", "READ_UPDATE") => List(AccessType.read, AccessType.write)
    case Palvelurooli("KOSKI", "TIEDONSIIRRON_MITATOINTI") => List(AccessType.tiedonsiirronMitätöinti)
    case Palvelurooli("KOSKI", "KAYTTOLIITTYMASIIRRON_MITATOINTI") => List(AccessType.käyttöliittymäsiirronMitätöinti)
    case Palvelurooli("KOSKI", "READ_UPDATE_ESIOPETUS") => List(AccessType.read, AccessType.write)
    case Palvelurooli("KOSKI", "LUKU_ESIOPETUS") => List(AccessType.read)
    case Palvelurooli("KOSKI", "TAITEENPERUSOPETUS_HANKINTAKOULUTUS") => List(AccessType.read, AccessType.editOnly)
    case Palvelurooli("KOSKI", LAHDEJARJESTELMAKYTKENNAN_PURKAMINEN) => List(AccessType.lähdejärjestelmäkytkennänPurkaminen)
    case _ => Nil
  }

  override lazy val allowedOpiskeluoikeusTyypit: Set[OoPtsMask] = Käyttöoikeus.parseAllowedOpiskeluoikeudenTyypit(organisaatiokohtaisetPalveluroolit, organisaatioAccessType)

  def globalAccessType: List[AccessType.Value] = Nil
  def globalPalveluroolit: List[Palvelurooli] = Nil
}

case class KäyttöoikeusVarhaiskasvatusToimipiste(
  koulutustoimija: Koulutustoimija,
  ulkopuolinenOrganisaatio: OrganisaatioWithOid,
  organisaatiokohtaisetPalveluroolit: List[Palvelurooli],
  onVarhaiskasvatuksenToimipiste: Boolean
) extends OrgKäyttöoikeus

case class KäyttöoikeusOrg(
  organisaatio: OrganisaatioWithOid,
  organisaatiokohtaisetPalveluroolit: List[Palvelurooli],
  juuri: Boolean,
  oppilaitostyyppi: Option[String]
) extends OrgKäyttöoikeus

case class KäyttöoikeusViranomainen(globalPalveluroolit: List[Palvelurooli]) extends Käyttöoikeus {
  def globalAccessType: List[AccessType.Value] = if (globalPalveluroolit.exists(r => r.palveluName == "KOSKI" && Rooli.globaalitKoulutusmuotoRoolit.contains(r.rooli))) {
    List(AccessType.read)
  } else {
    Nil
  }

  override lazy val allowedOpiskeluoikeusTyypit: Set[OoPtsMask] = Käyttöoikeus.parseAllowedOpiskeluoikeudenTyypit(globalPalveluroolit, globalAccessType)
}

case class OoPtsMask(
  opiskeluoikeus: String,
  päätasonSuoritukset: Option[List[String]] = None // None = mikä tahansa päätason suoritus
) {
  override def toString: String = päätasonSuoritukset match {
    case None => opiskeluoikeus
    case Some(ptss) => s"$opiskeluoikeus (${ptss.mkString(", ")})"
  }

  def intersects(other: OoPtsMask): Boolean =
    intersect(other).isDefined

  def intersect(other: OoPtsMask): Option[OoPtsMask] =
    if (opiskeluoikeus != other.opiskeluoikeus) {
      None
    } else {
      (päätasonSuoritukset, other.päätasonSuoritukset) match {
        case (Some(a), Some(b)) => {
          val ab = a.intersect(b)
          if (ab.isEmpty) None else Some(copy(päätasonSuoritukset = Some(ab)))
        }
        case (Some(_), None) => Some(this)
        case (None, Some(_)) => Some(other)
        case (None, None) => Some(this)
      }
    }
}

object OoPtsMask {
  def fromPalvelurooli(palvelurooli: Palvelurooli): Option[OoPtsMask] = {
    val oo :: pts = palvelurooli.rooli.toLowerCase.split(Käyttöoikeus.opiskeluoikeusPäätasonSuoritusErotin).toList
    if (isOpiskeluoikeusrooli(oo)) {
      Some(OoPtsMask(oo, pts.headOption.map(p => List(p))))
    } else {
      None
    }
  }

  def fromKoodistokoodiviite(ooKoodiviite: Koodistokoodiviite): Option[OoPtsMask] =
    if (ooKoodiviite.koodistoUri == "opiskeluoikeudentyyppi" && isOpiskeluoikeusrooli(ooKoodiviite.koodiarvo)) {
      Some(OoPtsMask(ooKoodiviite.koodiarvo))
    } else {
      None
    }

  def fromOpiskeluoikeus(oo: Opiskeluoikeus): OoPtsMask =
    OoPtsMask(
      oo.tyyppi.koodiarvo,
      Some(oo.suoritukset.map(_.tyyppi.koodiarvo))
    )

  def intersects(ts: Iterable[OoPtsMask], a: OoPtsMask): Boolean =
    ts.exists(_.intersects(a))

  private def isOpiskeluoikeusrooli(rooli: String) = OpiskeluoikeudenTyyppi.kaikkiTyypit(true).exists(_.koodiarvo == rooli)

  implicit final def ooPtsMaskIterableOps[F[X] <: Iterable[X]](i: F[OoPtsMask]): OoPtsMaskIterableChainingOps[F] = new OoPtsMaskIterableChainingOps(i)

  final class OoPtsMaskIterableChainingOps[F[X] <: Iterable[X]](private val self: F[OoPtsMask]) extends AnyVal {
    def intersects(a: OoPtsMask): Boolean = OoPtsMask.intersects(self, a)

    def toOpiskeluoikeudenTyypit: F[String] = self.map(_.opiskeluoikeus).asInstanceOf[F[String]]
  }
}
