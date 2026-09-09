package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{
  AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus,
  AikuistenPerusopetuksenKurssinSuoritus,
  AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus,
  AikuistenPerusopetuksenOpiskeluoikeus,
  KoodiViite,
  KoskeenTallennettavaOpiskeluoikeus,
  LaajuusVuosiviikkotunneissa,
  Opiskeluoikeus,
  OppiaineenSuoritus,
  PerusopetuksenOppiaineenArviointi,
  Suoritus
}
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.util.FinnishDateFormat

import java.time.LocalDate

object AikuistenPerusopetuksenOpiskeluoikeudenValidation {

  def validateAikuistenPerusopetuksenOpiskeluoikeus(config: Config)(
    oo: Opiskeluoikeus
  ): HttpStatus = {
    oo match {
      case aipeOo: AikuistenPerusopetuksenOpiskeluoikeus =>
        HttpStatus.fold(
          validateAikuistenPerusopetusOppimääränJaAineopintojenSuoritusSamaanAikaan(aipeOo),
          validateKurssienArviointipäivät(aipeOo),
          validateLaajuudet(aipeOo, laajuusValidaatiotAlkaen(config))
        )
      case _ => HttpStatus.ok
    }
  }

  def validateAikuistenPerusopetusOppimääränJaAineopintojenSuoritusSamaanAikaan(oo: AikuistenPerusopetuksenOpiskeluoikeus): HttpStatus = {
    val sisältääAineopintoja = oo.suoritukset.exists(_.tyyppi.koodiarvo == "perusopetuksenoppiaineenoppimaara")
    val sisältääMuitaKuinAineopintoja = oo.suoritukset.exists(_.tyyppi.koodiarvo != "perusopetuksenoppiaineenoppimaara")

    HttpStatus.validate(!(sisältääAineopintoja && sisältääMuitaKuinAineopintoja))(
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia("Aikuisten perusopetuksen opiskeluoikeudella ei voi olla sekä oppimäärän että oppiaineen oppimäärän suorituksia")
    )
  }

  def validateKurssienArviointipäivät(oo: AikuistenPerusopetuksenOpiskeluoikeus): HttpStatus = {
    val puuttuvat = oo.suoritukset.flatMap(_.rekursiivisetOsasuoritukset).collect {
      case k: AikuistenPerusopetuksenKurssinSuoritus if arviointipäiväPuuttuu(k.arviointi) =>
        suorituksenTunniste(k.koulutusmoduuli.tunniste)
      case k: AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus if arviointipäiväPuuttuu(k.arviointi) =>
        suorituksenTunniste(k.koulutusmoduuli.tunniste)
    }
    HttpStatus.fold(puuttuvat.map(tunniste =>
      KoskiErrorCategory.badRequest.validation.arviointi.arviointipäiväPuuttuu(
        s"Aikuisten perusopetuksen kurssilta $tunniste puuttuu arviointipäivä"
      )
    ))
  }

  private def laajuusValidaatiotAlkaen(config: Config): LocalDate =
    LocalDate.parse(config.getString("validaatiot.aikuistenPerusopetuksenLaajuusValidaatiotAlkaen"))

  def validateLaajuudet(oo: AikuistenPerusopetuksenOpiskeluoikeus, rajapäivä: LocalDate): HttpStatus =
    if (!oo.alkamispäivä.exists(_.isEqualOrAfter(rajapäivä))) {
      HttpStatus.ok
    } else {
      HttpStatus.fold(
        oo.suoritukset.flatMap(s => s :: s.rekursiivisetOsasuoritukset).map {
          case kurssi: AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus =>
            validateKurssinLaajuus(kurssi, rajapäivä)
          case oppiaine: OppiaineenSuoritus =>
            validateOppiaineenLaajuus(oppiaine, rajapäivä)
          case _ => HttpStatus.ok
        }
      )
    }

  private def validateKurssinLaajuus(kurssi: Suoritus, rajapäivä: LocalDate): HttpStatus =
    kurssi.koulutusmoduuli.getLaajuus match {
      case None =>
        KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritusVääräLaajuus(
          s"Aikuisten perusopetuksen kurssilta ${suorituksenTunniste(kurssi.koulutusmoduuli.tunniste)} puuttuu laajuus${rajapäivänJälkeen(rajapäivä)}"
        )
      case Some(_: LaajuusVuosiviikkotunneissa) =>
        KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritusVääräLaajuus(
          s"Aikuisten perusopetuksen kurssin ${suorituksenTunniste(kurssi.koulutusmoduuli.tunniste)} laajuutta ei voi ilmoittaa vuosiviikkotunteina${rajapäivänJälkeen(rajapäivä)}"
        )
      case _ => HttpStatus.ok
    }

  private def validateOppiaineenLaajuus(oppiaine: Suoritus, rajapäivä: LocalDate): HttpStatus =
    oppiaine.koulutusmoduuli.getLaajuus match {
      case Some(_: LaajuusVuosiviikkotunneissa) =>
        KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritusVääräLaajuus(
          s"Aikuisten perusopetuksen oppiaineen ${suorituksenTunniste(oppiaine.koulutusmoduuli.tunniste)} laajuutta ei voi ilmoittaa vuosiviikkotunteina${rajapäivänJälkeen(rajapäivä)}"
        )
      case _ => HttpStatus.ok
    }

  private def rajapäivänJälkeen(rajapäivä: LocalDate): String =
    s" ${FinnishDateFormat.format(rajapäivä)} tai sen jälkeen alkaneissa opiskeluoikeuksissa"

  private def arviointipäiväPuuttuu(arvioinnit: Option[List[PerusopetuksenOppiaineenArviointi]]): Boolean =
    arvioinnit.exists(_.exists(_.arviointipäivä.isEmpty))

  private def suorituksenTunniste(tunniste: KoodiViite): String = {
    val nimi = tunniste.getNimi.flatMap(_.getOptional("fi")).map(" " + _).getOrElse("")
    s"${tunniste.koodiarvo}$nimi"
  }

  def validateAikuistenPerusopetusAineopinnotVaihto(oldState: KoskeenTallennettavaOpiskeluoikeus, newState: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = (oldState, newState) match {
    case (oldOo: AikuistenPerusopetuksenOpiskeluoikeus, newOo: AikuistenPerusopetuksenOpiskeluoikeus) =>
      val oldAineopinnot = oldOo.suoritukset.exists(_.tyyppi.koodiarvo == "perusopetuksenoppiaineenoppimaara")
      val newAineopinnot = newOo.suoritukset.exists(_.tyyppi.koodiarvo == "perusopetuksenoppiaineenoppimaara")
      if (oldAineopinnot && !newAineopinnot) {
        KoskiErrorCategory.badRequest.validation.rakenne.suorituksenTyyppiMuuttunut("Aikuisten perusopetuksen oppiaineen oppimäärän opiskeluoikeutta ei voi muuttaa oppimäärän opiskeluoikeudeksi")
      } else if (!oldAineopinnot && newAineopinnot) {
        KoskiErrorCategory.badRequest.validation.rakenne.suorituksenTyyppiMuuttunut("Aikuisten perusopetuksen oppimäärän opiskeluoikeutta ei voi muuttaa oppiaineen oppimäärän opiskeluoikeudeksi")
      } else {
        HttpStatus.ok
      }
    case _ => HttpStatus.ok
  }
}
