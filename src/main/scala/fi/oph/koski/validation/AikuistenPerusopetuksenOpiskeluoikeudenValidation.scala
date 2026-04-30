package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{
  AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus,
  AikuistenPerusopetuksenKurssinSuoritus,
  AikuistenPerusopetuksenOpiskeluoikeus,
  KoodiViite,
  KoskeenTallennettavaOpiskeluoikeus,
  Opiskeluoikeus,
  PerusopetuksenOppiaineenArviointi
}

object AikuistenPerusopetuksenOpiskeluoikeudenValidation {

  def validateAikuistenPerusopetuksenOpiskeluoikeus(
    oo: Opiskeluoikeus
  ): HttpStatus = {
    oo match {
      case aipeOo: AikuistenPerusopetuksenOpiskeluoikeus =>
        HttpStatus.fold(
          validateAikuistenPerusopetusOppimääränJaAineopintojenSuoritusSamaanAikaan(aipeOo),
          validateKurssienArviointipäivät(aipeOo)
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
        kurssinTunniste(k.koulutusmoduuli.tunniste)
      case k: AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus if arviointipäiväPuuttuu(k.arviointi) =>
        kurssinTunniste(k.koulutusmoduuli.tunniste)
    }
    HttpStatus.fold(puuttuvat.map(tunniste =>
      KoskiErrorCategory.badRequest.validation.arviointi.arviointipäiväPuuttuu(
        s"Aikuisten perusopetuksen kurssilta $tunniste puuttuu arviointipäivä"
      )
    ))
  }

  private def arviointipäiväPuuttuu(arvioinnit: Option[List[PerusopetuksenOppiaineenArviointi]]): Boolean =
    arvioinnit.exists(_.exists(_.arviointipäivä.isEmpty))

  private def kurssinTunniste(tunniste: KoodiViite): String = {
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
