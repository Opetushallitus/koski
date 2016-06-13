package fi.oph.koski.todistus

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koski.KoskiFacade
import fi.oph.koski.koskiuser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.oph.koski.schema._
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.suoritusote.OpiskeluoikeusFinder
import fi.oph.koski.tutkinto.{SuoritustapaJaRakenne, TutkintoRakenne, TutkintoRepository}
import fi.vm.sade.security.ldap.DirectoryClient

class TodistusServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, rekisteri: KoskiFacade, tutkintoRepository: TutkintoRepository)
  extends HtmlServlet with RequiresAuthentication {
  get("/:oppijaOid") {
    val oppijaOid = params("oppijaOid")
    implicit val user = koskiUser

    val filters: List[(Suoritus => Boolean)] = params.toList.flatMap {
      case ("koulutusmoduuli", koulutusmoduuli: String) => Some({ s: Suoritus => s.koulutusmoduuli.tunniste.toString == koulutusmoduuli })
      case (_, _) => None
    }

    renderEither(OpiskeluoikeusFinder(rekisteri).opiskeluoikeudet(oppijaOid, params).right.flatMap {
      case Oppija(henkilötiedot: TaydellisetHenkilötiedot, opiskeluoikeudet) =>
        val suoritukset: Seq[(Opiskeluoikeus, Suoritus)] = opiskeluoikeudet.flatMap {
          opiskeluoikeus => opiskeluoikeus.suoritukset.filter(suoritus => suoritus.tila.koodiarvo == "VALMIS" && filters.forall(f => f(suoritus)))
            .map (suoritus => (opiskeluoikeus, suoritus))
        }

        suoritukset match {
          case ((opiskeluoikeus, suoritus) :: Nil) =>
            suoritus match {
              case t: PerusopetuksenOppimääränSuoritus =>
                Right((new PerusopetuksenPaattotodistusHtml(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)).todistusHtml)
              case t: PerusopetuksenOppiaineenOppimääränSuoritus =>
                Right((new PerusopetuksenOppiaineenOppimaaranTodistusHtml(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)).todistusHtml)
              case t: PerusopetuksenVuosiluokanSuoritus =>
                Right((new PerusopetuksenLukuvuositodistusHtml(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)).todistusHtml)
              case t: PerusopetuksenLisäopetuksenSuoritus =>
                Right((new PerusopetuksenLisaopetuksenTodistusHtml(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)).todistusHtml)
              case t: AmmatillisenTutkinnonSuoritus =>
                t.koulutusmoduuli.perusteenDiaarinumero.flatMap(tutkintoRepository.findPerusteRakenne(_)) match {
                  case Some(rakenne: TutkintoRakenne) =>
                    val maybeSuoritustapaJaRakenne: Option[SuoritustapaJaRakenne] = rakenne.suoritustavat.find(x => Some(x.suoritustapa) == t.suoritustapa.map(_.tunniste))
                    maybeSuoritustapaJaRakenne match {
                      case Some(suoritustapaJaRakenne) => Right((new AmmatillisenPerustutkinnonPaattotodistusHtml).render(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t, suoritustapaJaRakenne))
                      case _ => Left(KoskiErrorCategory.badRequest.validation.rakenne.suoritustapaPuuttuu())
                    }
                  case None => Left(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy("Tutkinnon rakennetta diaarinumerolla " + t.koulutusmoduuli.perusteenDiaarinumero.getOrElse("(puuttuu)") + " ei löydy"))
                }
              case t: LukionOppimääränSuoritus =>
                Right((new LukionPaattoTodistusHtml).render(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t))
              case t: YlioppilastutkinnonSuoritus =>
                Right((new YlioppilastutkintotodistusHtml).render(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t))
              case t: LukioonValmistavanKoulutuksenSuoritus =>
                Right((new LuvaTodistusHtml).render(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t))
              case _ =>
                Left(KoskiErrorCategory.notFound.todistustaEiLöydy())
          }

          case _ =>
            Left(KoskiErrorCategory.notFound.todistustaEiLöydy())
        }
    })
  }
}