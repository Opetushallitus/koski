package fi.oph.koski.organisaatio

import fi.oph.koski.schema._
import OrganisaatioHierarkia._
import Organisaatiotyyppi._

case class OrganisaatioHierarkia(
  oid: String,
  oppilaitosnumero: Option[Koodistokoodiviite],
  nimi: LocalizedString,
  yTunnus: Option[String],
  kotipaikka: Option[Koodistokoodiviite],
  organisaatiotyypit: List[String],
  oppilaitostyyppi: Option[String],
  aktiivinen: Boolean,
  kielikoodit: List[String],
  children: List[OrganisaatioHierarkia]
) {
  def find(oid: String): Option[OrganisaatioHierarkia] = {
    if (oid == this.oid) {
      Some(this)
    } else {
      children.foldLeft(None: Option[OrganisaatioHierarkia]) {
        case (Some(found), _) => Some(found)
        case (_, child) => child.find(oid)
      }
    }
  }

  def toOrganisaatio: OrganisaatioWithOid =
    if (organisaatiotyypit.intersect(oppilaitosTyypit).nonEmpty) {
      Oppilaitos(oid, oppilaitosnumero, Some(nimi), kotipaikka)
    } else if (organisaatiotyypit.contains(KOULUTUSTOIMIJA)) {
      Koulutustoimija(oid, Some(nimi), yTunnus, kotipaikka)
    } else if (organisaatiotyypit.contains(TOIMIPISTE)) {
      Toimipiste(oid, Some(nimi), kotipaikka)
    } else {
      OidOrganisaatio(oid, Some(nimi), kotipaikka)
    }

  def toKunta: Option[OrganisaatioWithOid] =
    organisaatiotyypit.find(_ == KUNTA).map(_ => OidOrganisaatio(oid, Some(nimi), kotipaikka))

  def toKoulutustoimija: Option[Koulutustoimija] = toOrganisaatio match {
    case k: Koulutustoimija => Some(k)
    case _ => None
  }

  def toOppilaitos: Option[Oppilaitos] = toOrganisaatio match {
    case o: Oppilaitos => Some(o)
    case _ => None
  }

  def flatten: List[OrganisaatioWithOid] = OrganisaatioHierarkia.flatten(List(this)).map(_.toOrganisaatio)

  def varhaiskasvatuksenJärjestäjä: Boolean = organisaatiotyypit.contains(VARHAISKASVATUKSEN_JARJESTAJA)
  def varhaiskasvatusToimipaikka: Boolean = organisaatiotyypit.contains(VARHAISKASVATUKSEN_TOIMIPAIKKA)

  def sortBy(lang: String): OrganisaatioHierarkia = {
    assert(LocalizedString.languages.contains(lang), s"Sallitut kielivaihtoehdot: ${LocalizedString.languages.mkString(",")}")
    this.copy(children = children.map(_.sortBy(lang)).sortBy(_.nimi.get(lang)))
  }
}

object OrganisaatioHierarkia {
  def apply(
    oid: String,
    nimi: LocalizedString,
    children: List[OrganisaatioHierarkia],
    organisaatiotyypit: List[String]
  ): OrganisaatioHierarkia = OrganisaatioHierarkia(
    oid = oid,
    nimi = nimi,
    children = children,
    oppilaitosnumero = None,
    yTunnus = None,
    kotipaikka = None,
    organisaatiotyypit = organisaatiotyypit,
    oppilaitostyyppi = None,
    aktiivinen = true,
    kielikoodit = Nil
  )

  def flatten(orgs: List[OrganisaatioHierarkia]): List[OrganisaatioHierarkia] = {
    orgs.flatMap { org => org :: flatten(org.children) }
  }

  val oppilaitosTyypit = List(OPPILAITOS, OPPISOPIMUSTOIMIPISTE, VARHAISKASVATUKSEN_TOIMIPAIKKA)
}

case class OrganisaatioHierarkiaJaKayttooikeusrooli(organisaatioHierarkia: OrganisaatioHierarkia, kayttooikeusrooli: String)
