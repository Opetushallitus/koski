package fi.oph.tor.todistus

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.schema._
import fi.oph.tor.servlet.ErrorHandlingServlet
import fi.oph.tor.tor.TodennetunOsaamisenRekisteri
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.oph.tor.tutkinto.{SuoritustapaJaRakenne, TutkintoRakenne, TutkintoRepository}
import fi.vm.sade.security.ldap.DirectoryClient

class TodistusServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, rekisteri: TodennetunOsaamisenRekisteri, tutkintoRepository: TutkintoRepository) extends ErrorHandlingServlet with RequiresAuthentication {
  get("/opiskeluoikeus/:opiskeluoikeusId") {
    val opiskeluoikeusId = getIntegerParam("opiskeluoikeusId")
    rekisteri.findOpiskeluOikeus(opiskeluoikeusId)(torUser) match {
      case Right((henkilötiedot, opiskeluoikeus)) =>
          opiskeluoikeus.suoritukset.head match {
            case t: PeruskoulunPäättötodistus if t.tila.koodiarvo == "VALMIS" =>
              PeruskoulunPaattotodistusHtml.renderPeruskoulunPäättötodistus(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)
            case t: AmmatillisenTutkinnonSuoritus if t.tila.koodiarvo == "VALMIS" => // TODO: vain perustutkinnot


              t.koulutusmoduuli.perusteenDiaarinumero.flatMap(tutkintoRepository.findPerusteRakenne(_)) match {
                case Some(rakenne: TutkintoRakenne) =>
                  val maybeSuoritustapaJaRakenne: Option[SuoritustapaJaRakenne] = rakenne.suoritustavat.find(x => Some(x.suoritustapa) == t.suoritustapa.map(_.tunniste))
                  maybeSuoritustapaJaRakenne match {
                    case Some(suoritustapaJaRakenne) => AmmatillisenPerustutkinnonPaattotodistusHtml.renderAmmatillisenPerustutkinnonPaattotodistus(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t, suoritustapaJaRakenne)
                    case _ => TorErrorCategory.badRequest.validation.rakenne.suoritustapaPuuttuu
                  }
                case None => TorErrorCategory.notFound.diaarinumeroaEiLöydy("Tutkinnon rakennetta diaarinumerolla " + t.koulutusmoduuli.perusteenDiaarinumero.getOrElse("(puuttuu)") + " ei löydy")
              }

            case t: LukionOppimääränSuoritus if t.tila.koodiarvo == "VALMIS" =>
              LukionPaattotodistusHtml.renderLukionPäättötodistus(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)
            case _ => TorErrorCategory.notFound.todistustaEiLöydy()
          }
      case Left(status) => renderStatus(status)
    }
  }
}