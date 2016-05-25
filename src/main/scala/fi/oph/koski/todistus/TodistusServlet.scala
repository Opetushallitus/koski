package fi.oph.koski.todistus

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.koski.KoskiFacade
import fi.oph.koski.koskiuser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.oph.koski.tutkinto.{SuoritustapaJaRakenne, TutkintoRakenne, TutkintoRepository}
import fi.vm.sade.security.ldap.DirectoryClient

class TodistusServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, rekisteri: KoskiFacade, tutkintoRepository: TutkintoRepository)
  extends HtmlServlet with RequiresAuthentication {
  get("/opiskeluoikeus/:opiskeluoikeusId") {
    val opiskeluoikeusId = getIntegerParam("opiskeluoikeusId")
    rekisteri.findOpiskeluOikeus(opiskeluoikeusId)(koskiUser) match {
      case Right((henkilötiedot, opiskeluoikeus)) =>
          implicit val user = koskiUser
          opiskeluoikeus.suoritukset.head match {
            case t: PerusopetuksenOppimääränSuoritus if t.tila.koodiarvo == "VALMIS" =>
              (new PerusopetuksenPaattotodistusHtml).render(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)

            case t: PerusopetuksenOppiaineenOppimääränSuoritus if t.tila.koodiarvo == "VALMIS" =>
              (new PerusopetuksenOppiaineenOppimaaranTodistusHtml).render(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)

            case t: PerusopetuksenLisäopetuksenSuoritus if t.tila.koodiarvo == "VALMIS" =>
              (new PerusopetuksenLisaopetuksenTodistusHtml).render(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)

            case t: AmmatillisenTutkinnonSuoritus if t.tila.koodiarvo == "VALMIS" =>
              t.koulutusmoduuli.perusteenDiaarinumero.flatMap(tutkintoRepository.findPerusteRakenne(_)) match {
                case Some(rakenne: TutkintoRakenne) =>
                  val maybeSuoritustapaJaRakenne: Option[SuoritustapaJaRakenne] = rakenne.suoritustavat.find(x => Some(x.suoritustapa) == t.suoritustapa.map(_.tunniste))
                  maybeSuoritustapaJaRakenne match {
                    case Some(suoritustapaJaRakenne) => (new AmmatillisenPerustutkinnonPaattotodistusHtml).render(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t, suoritustapaJaRakenne)
                    case _ => KoskiErrorCategory.badRequest.validation.rakenne.suoritustapaPuuttuu
                  }
                case None => KoskiErrorCategory.notFound.diaarinumeroaEiLöydy("Tutkinnon rakennetta diaarinumerolla " + t.koulutusmoduuli.perusteenDiaarinumero.getOrElse("(puuttuu)") + " ei löydy")
              }

            case t: LukionOppimääränSuoritus if t.tila.koodiarvo == "VALMIS" =>
              (new LukionPaattotodistusHtml).render(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)

            case _ => KoskiErrorCategory.notFound.todistustaEiLöydy()
          }
      case Left(status) => status
    }
  }
}