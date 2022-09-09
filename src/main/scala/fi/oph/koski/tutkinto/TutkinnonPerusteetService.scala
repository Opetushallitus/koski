package fi.oph.koski.tutkinto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schema.SuorituksenTyyppi.SuorituksenTyyppi
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi
import fi.oph.koski.tutkinto.Perusteet.sallitutPerusteet

class TutkinnonPerusteetService(application: KoskiApplication) {
  def diaarinumerotBySuorituksenTyyppi(suorituksenTyyppi: SuorituksenTyyppi): List[Koodistokoodiviite] = {
    val koulutustyypit: Set[Koulutustyyppi] = Koulutustyyppi.fromSuorituksenTyyppi(suorituksenTyyppi)
    val diaarinumerot: List[Koodistokoodiviite] = diaarinumerotByKoulutustyypit(koulutustyypit)
    diaarinumerot.filter(diaarinumero => sallitutPerusteet(suorituksenTyyppi).matches(diaarinumero.koodiarvo))
  }

  private def diaarinumerotByKoulutustyypit(koulutustyypit: Set[Koulutustyyppi]): List[Koodistokoodiviite] = {
    val diaaritEperusteista = application.ePerusteet.findPerusteetByKoulutustyyppi(koulutustyypit)
      .sortBy(p => -p.id)
      .map(p => Koodistokoodiviite(koodiarvo = p.diaarinumero, nimi = LocalizedString.sanitize(p.nimi), koodistoUri = "koskikoulutustendiaarinumerot"))

    val diaaritKoskesta = koulutustyypit.flatMap(koulutusTyyppi =>
      application.koodistoViitePalvelu.getSisältyvätKoodiViitteet(application.koodistoViitePalvelu.getLatestVersionRequired("koskikoulutustendiaarinumerot"), koulutusTyyppi)
    ).flatten.toList

    (diaaritEperusteista ++ diaaritKoskesta).distinct
  }
}
