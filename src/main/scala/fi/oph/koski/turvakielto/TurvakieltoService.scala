package fi.oph.koski.turvakielto

import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema._
import mojave._
import scala.util.chaining._

object TurvakieltoService {
  def poistaOpiskeluoikeudenTurvakiellonAlaisetTiedot(opiskeluoikeus: Opiskeluoikeus): Opiskeluoikeus =
    opiskeluoikeus match {
      case oo: KoskeenTallennettavaOpiskeluoikeus => oo
        .withOppilaitos(turvakieltooppilaitos)
        .withKoulutustoimija(turvakieltokoulutustoimija)
        .withHistoria(None)
        .withSuoritukset(oo.suoritukset.map(poistaPäätasonSuorituksenTurvakiellonAlaisetTiedot))
      case oo: Any => oo
    }

  def poistaPäätasonSuorituksenTurvakiellonAlaisetTiedot(päätasonSuoritus: KoskeenTallennettavaPäätasonSuoritus): KoskeenTallennettavaPäätasonSuoritus = {
    val toimipisteL = shapeless.lens[KoskeenTallennettavaPäätasonSuoritus].field[OrganisaatioWithOid]("toimipiste")
    val vahvistusL = shapeless.lens[KoskeenTallennettavaPäätasonSuoritus].field[Option[Vahvistus]]("vahvistus")
    val osasuorituksetL = shapeless.lens[KoskeenTallennettavaPäätasonSuoritus].field[Option[List[Suoritus]]]("osasuoritukset")

    val withToimipisteJaVahvistus = toimipisteL.set(päätasonSuoritus)(turvakieltotoimipiste)
      .pipe(vahvistusL.modify(_)(poistaVahvistuksenTurvakiellonAlaisetTiedot))

    // Vain niillä päätason suorituksilla, joilla on osasuoritukset-kenttä (Suoritus-traitin oletus on None),
    // ajetaan linssin kautta — muutoin mojaven reflektiopohjainen setField heittää NoSuchMethodExceptionin.
    if (withToimipisteJaVahvistus.osasuoritukset.isDefined)
      osasuorituksetL.modify(withToimipisteJaVahvistus)(_.map(_.map(poistaOsasuorituksenTurvakiellonAlaisetTiedot)))
    else
      withToimipisteJaVahvistus
  }

  def poistaOsasuorituksenTurvakiellonAlaisetTiedot(suoritus: Suoritus): Suoritus = {
    val vahvistusL = shapeless.lens[Suoritus].field[Option[Vahvistus]]("vahvistus")
    val toimipisteOptionL = shapeless.lens[MahdollisestiToimipisteellinen].field[Option[OrganisaatioWithOid]]("toimipiste")
    val toimipisteL = shapeless.lens[Toimipisteellinen].field[OrganisaatioWithOid]("toimipiste")

    (suoritus match {
      case s: Vahvistukseton => s
      case s: Any => vahvistusL.modify(s)(poistaVahvistuksenTurvakiellonAlaisetTiedot)
    }) match {
      case s: Toimipisteellinen => toimipisteL.set(s)(turvakieltotoimipiste)
      case s: MahdollisestiToimipisteellinen => toimipisteOptionL.set(s)(None)
      case s: Any => s
    }
  }


  def poistaVahvistuksenTurvakiellonAlaisetTiedot(vahvistus: Option[Vahvistus]): Option[Vahvistus] =
    vahvistus.map {
      case h: VahvistusPaikkakunnalla => shapeless.lens[VahvistusPaikkakunnalla].field[Koodistokoodiviite]("paikkakunta").set(h)(turvakieltopaikkakunta)
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
