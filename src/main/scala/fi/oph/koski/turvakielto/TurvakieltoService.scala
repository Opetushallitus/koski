package fi.oph.koski.turvakielto

import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema._
import mojave._
import scala.util.chaining._

object TurvakieltoService {
  def poistaOpiskeluoikeudenTurvakiellonAlaisetTiedot(opiskeluoikeus: Opiskeluoikeus): Opiskeluoikeus =
    opiskeluoikeus match {
      case oo: KoskeenTallennettavaOpiskeluoikeus =>
        val withOrganisaatiot = oo
          .withOppilaitos(turvakieltooppilaitos)
          .withKoulutustoimija(turvakieltokoulutustoimija)
        // Useat tyypit (esim. YlioppilastutkinnonOpiskeluoikeus) perivät organisaatiohistorian
        // kiinteänä def-jäsenenä eivätkä sisällytä sitä konstruktoriparametrina, jolloin lensin
        // ajaminen heittäisi NoSuchMethodExceptionin. Ohitetaan jos ei ole muutettavaa.
        val withHistoria =
          if (withOrganisaatiot.organisaatiohistoria.isDefined) withOrganisaatiot.withHistoria(None)
          else withOrganisaatiot
        withHistoria
          .withSuoritukset(withHistoria.suoritukset.map(poistaPäätasonSuorituksenTurvakiellonAlaisetTiedot))
      case oo: Any => oo
    }

  def poistaPäätasonSuorituksenTurvakiellonAlaisetTiedot(päätasonSuoritus: KoskeenTallennettavaPäätasonSuoritus): KoskeenTallennettavaPäätasonSuoritus = {
    val toimipisteL = shapeless.lens[KoskeenTallennettavaPäätasonSuoritus].field[OrganisaatioWithOid]("toimipiste")
    val vahvistusL = shapeless.lens[KoskeenTallennettavaPäätasonSuoritus].field[Option[Vahvistus]]("vahvistus")
    val osasuorituksetL = shapeless.lens[KoskeenTallennettavaPäätasonSuoritus].field[Option[List[Suoritus]]]("osasuoritukset")

    // Linssipohjaiset päivitykset tehdään vain, kun kohdekentällä on muutettavaa arvoa.
    // Useat skeematyypit perivät esim. osasuoritukset/vahvistus arvoltaan kiinteänä def-jäsenenä
    // (Suoritus-/Vahvistukseton-traitin oletukset), jolloin mojaven reflektiopohjainen setField
    // heittäisi NoSuchMethodExceptionin — eikä lensin ajaminen kuitenkaan toisi puhdistettavaa tietoa.
    val withToimipiste = toimipisteL.set(päätasonSuoritus)(turvakieltotoimipiste)
    val withVahvistus =
      if (withToimipiste.vahvistus.isDefined) vahvistusL.modify(withToimipiste)(poistaVahvistuksenTurvakiellonAlaisetTiedot)
      else withToimipiste

    if (withVahvistus.osasuoritukset.isDefined)
      osasuorituksetL.modify(withVahvistus)(_.map(_.map(poistaOsasuorituksenTurvakiellonAlaisetTiedot)))
    else
      withVahvistus
  }

  def poistaOsasuorituksenTurvakiellonAlaisetTiedot(suoritus: Suoritus): Suoritus = {
    val vahvistusL = shapeless.lens[Suoritus].field[Option[Vahvistus]]("vahvistus")
    val toimipisteOptionL = shapeless.lens[MahdollisestiToimipisteellinen].field[Option[OrganisaatioWithOid]]("toimipiste")
    val toimipisteL = shapeless.lens[Toimipisteellinen].field[OrganisaatioWithOid]("toimipiste")

    val withVahvistus = suoritus match {
      case s: Vahvistukseton => s
      case s if s.vahvistus.isEmpty => s
      case s: Any => vahvistusL.modify(s)(poistaVahvistuksenTurvakiellonAlaisetTiedot)
    }
    withVahvistus match {
      case s: Toimipisteellinen => toimipisteL.set(s)(turvakieltotoimipiste)
      case s: MahdollisestiToimipisteellinen if s.toimipiste.isEmpty => s
      case s: MahdollisestiToimipisteellinen => toimipisteOptionL.set(s)(None)
      case s: Any => s
    }
  }


  def poistaVahvistuksenTurvakiellonAlaisetTiedot(vahvistus: Option[Vahvistus]): Option[Vahvistus] =
    vahvistus.map {
      case h: VahvistusPaikkakunnalla => shapeless.lens[VahvistusPaikkakunnalla].field[Koodistokoodiviite]("paikkakunta").set(h)(turvakieltopaikkakunta)
      case h: VahvistusValinnaisellaPaikkakunnalla if h.paikkakunta.isEmpty => h
      case h: VahvistusValinnaisellaPaikkakunnalla => shapeless.lens[VahvistusValinnaisellaPaikkakunnalla].field[Option[Koodistokoodiviite]]("paikkakunta").set(h)(None)
      case h: Any => h
    }

  def turvakieltooppilaitos: Oppilaitos = Oppilaitos(
      oid = Opetushallitus.organisaatioOid,
      nimi = Some(Finnish("Oppilaitos")),
    )

  def turvakieltokoulutustoimija: Koulutustoimija = Koulutustoimija(
      oid = Opetushallitus.organisaatioOid,
      nimi = Some(Finnish("Koulutustoimija")),
    )

  def turvakieltotoimipiste: Toimipiste = Toimipiste(
    oid = Opetushallitus.organisaatioOid,
    nimi = Some(Finnish("Oppilaitos")),
  )

  def turvakieltopaikkakunta: Koodistokoodiviite = Koodistokoodiviite("999", Some(Finnish("–")), "kunta")
}
