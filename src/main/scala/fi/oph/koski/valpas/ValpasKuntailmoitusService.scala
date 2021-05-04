package fi.oph.koski.valpas

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoitusLaajatTiedotJaOppijaOid, ValpasKuntailmoitusQueryService}
import fi.oph.koski.valpas.valpasuser.ValpasSession

class ValpasKuntailmoitusService(
  queryService: ValpasKuntailmoitusQueryService,
  oppijaService: ValpasOppijaService,
  accessResolver: ValpasAccessResolver
) extends Logging with Timing {

  def createKuntailmoitus
    (kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    val organisaatioOid = kuntailmoitusInput.kuntailmoitus.tekijä.organisaatio.oid

    accessResolver.organisaatiohierarkiaOids(Set(organisaatioOid))
      .left.map(_ => ValpasErrorCategory.forbidden.organisaatio("Käyttäjällä ei ole oikeutta tehdä kuntailmoitusta annetun organisaation nimissä"))
      .flatMap(_ => oppijaService.getOppijaLaajatTiedot(kuntailmoitusInput.oppijaOid))
      .flatMap(oppija =>
        accessResolver.withOppijaAccessAsOrganisaatio(organisaatioOid)(oppija)
          .left.map(_ => ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia tehdä kuntailmoitusta annetusta oppijasta"))
      )
      .flatMap(_ => queryService.create(kuntailmoitusInput))
  }
}
