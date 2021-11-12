package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus
import fi.oph.koski.suostumus.SuostumuksenPeruutusService

object SuostumuksenPeruutusValidaatiot {
  def validateSuostumuksenPeruutus(oo: KoskeenTallennettavaOpiskeluoikeus, suostumusService: SuostumuksenPeruutusService): HttpStatus = {
    suostumusService.suorituksetPerutettavaaTyyppiä(oo) match {
      case true => validateLähdejärjestelmäId(oo, suostumusService)
      case false => HttpStatus.ok
    }
  }

  private def validateLähdejärjestelmäId(oo: KoskeenTallennettavaOpiskeluoikeus, suostumusService: SuostumuksenPeruutusService): HttpStatus = {
    val id = oo.lähdejärjestelmänId.map(_.id).flatten

    id match {
      case Some(id) =>
        val perutut = suostumusService.listaaPerututSuostumukset()
        val koodi = oo.lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo).get
        perutut.exists( peruttu =>
          peruttu.lähdejärjestelmäId.exists(_ == id) && peruttu.lähdejärjestelmäKoodi.exists(_ == koodi)
        ) match {
          case true => KoskiErrorCategory.forbidden.suostumusPeruttu()
          case false => HttpStatus.ok
        }
      case None => HttpStatus.ok
    }
  }
}
