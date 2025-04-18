package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.documentation.ExampleData.muutaKauttaRahoitettu
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.KoskiOpiskeluoikeusRepository
import fi.oph.koski.schema.{EBOpiskeluoikeus, EBTutkinnonOsasuoritus, EBTutkinnonSuoritus, EuropeanSchoolOfHelsinkiOpiskeluoikeus, EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso, EuropeanSchoolOfHelsinkiPäätasonSuoritus, Henkilö, HenkilöWithOid, Koodistokoodiviite, KoskeenTallennettavaOpiskeluoikeus, NurseryVuosiluokanSuoritus, Opiskeluoikeus, PrimaryVuosiluokanSuoritus, SecondaryLowerVuosiluokanSuoritus, SecondaryUpperOppiaineenSuoritus, SecondaryUpperVuosiluokanSuoritus, UusiHenkilö}
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat

import java.time.LocalDate

object EuropeanSchoolOfHelsinkiValidation {

  def validateOpiskeluoikeus(config: Config)(henkilöRepository: HenkilöRepository, koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository, henkilö: Option[Henkilö], oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    oo match {
      case eshOo: EuropeanSchoolOfHelsinkiOpiskeluoikeus =>
        HttpStatus.fold(
          validateTallennuspäivä(
            LocalDate.parse(config.getString("validaatiot.europeanSchoolOfHelsinkiAikaisinSallittuTallennuspaiva"))
          ),
          validatePäättymispäivä(
            eshOo,
            LocalDate.parse(config.getString("validaatiot.europeanSchoolOfHelsinkiAikaisinSallittuPaattymispaiva")),
          )
        )
      case ebOo: EBOpiskeluoikeus =>
        HttpStatus.fold(
          validateTallennuspäivä(
            LocalDate.parse(config.getString("validaatiot.europeanSchoolOfHelsinkiAikaisinSallittuTallennuspaiva"))
          ),
          validatePäättymispäivä(
            ebOo,
            LocalDate.parse(config.getString("validaatiot.europeanSchoolOfHelsinkiAikaisinSallittuPaattymispaiva")),
          ),
          validateESHS7SamallaOppijalla(henkilöRepository, koskiOpiskeluoikeudet, henkilö, ebOo)
        )
      case _ => HttpStatus.ok
    }
  }

  private def validateTallennuspäivä(aikaisinTallennuspäivä: LocalDate): HttpStatus = {
    if (LocalDate.now.isBefore(aikaisinTallennuspäivä)) {
      KoskiErrorCategory.badRequest.validation.esh.tallennuspäivä(s"Helsingin eurooppalaisen koulun opiskeluoikeuksia voi alkaa tallentaa vasta ${finnishDateFormat.format(aikaisinTallennuspäivä)} alkaen")
    } else {
      HttpStatus.ok
    }
  }

  private def validatePäättymispäivä(oo: Opiskeluoikeus, aikaisinPäättymispäivä: LocalDate): HttpStatus = {
    oo.päättymispäivä.filter(_.isBefore(aikaisinPäättymispäivä)) match {
      case Some(_) =>
        KoskiErrorCategory.badRequest.validation.esh.päättymispäivä(s"Helsingin eurooppalaisen koulun tallennettavat opiskeluoikeudet eivät voi olla päättyneet ennen lain voimaantuloa ${finnishDateFormat.format(aikaisinPäättymispäivä)}")
      case _ => HttpStatus.ok
    }
  }

  private def validateESHS7SamallaOppijalla(henkilöRepository: HenkilöRepository, koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository, henkilö: Option[Henkilö], ebOo: EBOpiskeluoikeus): HttpStatus = {
    def oppijallaOnESHS7(oppijaOidit: List[Henkilö.Oid]) = {
      val oot = koskiOpiskeluoikeudet.findByOppijaOids(oppijaOidit)(KoskiSpecificSession.systemUser)

      val eshOo = oot.collectFirst {
        case eshOo: EuropeanSchoolOfHelsinkiOpiskeluoikeus if eshOo.suoritukset.exists(_.koulutusmoduuli.tunniste.koodiarvo == "S7") => eshOo
      }

      eshOo match {
        case Some(eshOo: EuropeanSchoolOfHelsinkiOpiskeluoikeus) =>
          if (ebOo.tila.opiskeluoikeusjaksot.lastOption.exists(_.tila.koodiarvo == "valmistunut") && !eshOo.tila.opiskeluoikeusjaksot.lastOption.exists(_.tila.koodiarvo == "valmistunut")) {
            KoskiErrorCategory.badRequest.validation.eb.eshEiValmistunut()
          }
          else {
            HttpStatus.ok
          }
        case None => KoskiErrorCategory.badRequest.validation.eb.puuttuvaESHS7()
      }

    }

    val henkilöOid = henkilö match {
      case Some(h: HenkilöWithOid) => Some(h.oid)
      case Some(h: UusiHenkilö) => henkilöRepository.opintopolku.findByHetu(h.hetu) match {
        case Some(henkilö) => Some(henkilö.oid)
        case _ => None
      }
      case _ => None
    }

    henkilöOid
      .flatMap(henkilöOid => henkilöRepository.findByOid(henkilöOid, findMasterIfSlaveOid = true))
      .map(hlö => oppijallaOnESHS7(hlö.kaikkiOidit))
    match {
        case Some(h: HttpStatus) => h
        case None => KoskiErrorCategory.badRequest.validation.eb.puuttuvaESHS7()
    }
  }

  def validateEBTutkinnonArvioinnit(suoritus: EBTutkinnonSuoritus): HttpStatus = {
    if (suoritus.vahvistettu && suoritus.yleisarvosana.isEmpty) {
      KoskiErrorCategory.badRequest.validation.eb.yleisarvosana()
    } else {
      HttpStatus.ok
    }
  }

  def fillRahoitusmuodot(koodistoPalvelu: KoodistoViitePalvelu)(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    oo match {
      case e: EuropeanSchoolOfHelsinkiOpiskeluoikeus =>
        e.copy(tila = e.tila.copy(
          opiskeluoikeusjaksot =
            fillRahoitusmuodot(e.tila.opiskeluoikeusjaksot, koodistoPalvelu)
        ))
      case _ => oo
    }
  }

  private def fillRahoitusmuodot(opiskeluoikeusjaksot: List[EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso], koodistoPalvelu: KoodistoViitePalvelu): List[EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso] = {
    opiskeluoikeusjaksot.map {
      case j if rahoitusmuotoTäydennetään(j) =>
        j.copy(opintojenRahoitus = koodistoPalvelu.validate(muutaKauttaRahoitettu))
      case j => j
    }
  }

  private def rahoitusmuotoTäydennetään(opiskeluoikeusjakso: EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso): Boolean =
    opiskeluoikeusjakso.opintojenRahoitus.isEmpty && tilatJoilleRahoitusmuotoTäydennetään.contains(opiskeluoikeusjakso.tila.koodiarvo)

  private lazy val tilatJoilleRahoitusmuotoTäydennetään = List("lasna", "valmistunut")

  def fillKoulutustyyppi(koodistoPalvelu: KoodistoViitePalvelu)(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    // Huomaa, että koulutustyypin täydentäminen tehdään diaarinumerollisille ja koulutus-koodistoa käyttäville koulutuksille EPerusteetFiller.addKoulutustyyppi -metodissa
    // ESH:ssa ei ole vuosiluokilla kumpaakaan.
    val result = oo match {
      case e: EuropeanSchoolOfHelsinkiOpiskeluoikeus => {
        val result = e.copy(suoritukset = e.suoritukset.map(fillESHSuorituksenKoulutustyyppi(koodistoPalvelu)))
        result
      }
      case e: EBOpiskeluoikeus => {
        val result = e.copy(suoritukset = e.suoritukset.map(fillEBSuorituksenKoulutustyyppi(koodistoPalvelu)))
        result
      }
      case _ => oo
    }

    result
  }

  private def fillESHSuorituksenKoulutustyyppi(koodistoPalvelu: KoodistoViitePalvelu)(suoritus: EuropeanSchoolOfHelsinkiPäätasonSuoritus): EuropeanSchoolOfHelsinkiPäätasonSuoritus = {
    suoritus match {
      case s: NurseryVuosiluokanSuoritus => s
      case s: PrimaryVuosiluokanSuoritus => s.copy(
        koulutusmoduuli = s.koulutusmoduuli.copy(
          koulutustyyppi = eshKoulutustyyppi(koodistoPalvelu)
        )
      )
      case s: SecondaryLowerVuosiluokanSuoritus => s.copy(
        koulutusmoduuli = s.koulutusmoduuli.copy(
          koulutustyyppi = eshKoulutustyyppi(koodistoPalvelu)
        )
      )
      case s: SecondaryUpperVuosiluokanSuoritus => s.copy(
        koulutusmoduuli = s.koulutusmoduuli.copy(
          koulutustyyppi = eshKoulutustyyppi(koodistoPalvelu)
        )
      )
    }
  }

  private def fillEBSuorituksenKoulutustyyppi(koodistoPalvelu: KoodistoViitePalvelu)(suoritus: EBTutkinnonSuoritus): EBTutkinnonSuoritus = {
    suoritus.copy(
        koulutusmoduuli = suoritus.koulutusmoduuli.copy(
          koulutustyyppi = eshKoulutustyyppi(koodistoPalvelu)
        )
      )
    }

  private def eshKoulutustyyppi(koodistoPalvelu: KoodistoViitePalvelu): Option[Koodistokoodiviite] = {
    koodistoPalvelu.validate(Koodistokoodiviite(koodiarvo = "21", koodistoUri = "koulutustyyppi"))
  }

  def osasuorituksetKunnossa(s: SecondaryUpperVuosiluokanSuoritus): Boolean = {
    s.osasuoritukset.exists(os => !os.isEmpty && os.forall(osasuorituksetKunnossa))
  }

  private def osasuorituksetKunnossa(s: SecondaryUpperOppiaineenSuoritus): Boolean = {
    def sisältää(koodiarvo: String) =
      s.osasuoritukset.exists(_.exists(_.koulutusmoduuli.tunniste.koodiarvo == koodiarvo))

    (sisältää("A") && sisältää("B")) || sisältää("yearmark")
  }

  def osasuorituksetKunnossa(s: EBTutkinnonSuoritus): Boolean = {
    s.osasuoritukset.exists(os => !os.isEmpty && os.forall(osasuorituksetKunnossa))
  }

  private def osasuorituksetKunnossa(s: EBTutkinnonOsasuoritus): Boolean = {
    def sisältää(koodiarvo: String) =
      s.osasuoritukset.exists(_.exists(_.koulutusmoduuli.tunniste.koodiarvo == koodiarvo))

    sisältää("Final")
  }
}
