package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{KoskiSession, RequiresAuthentication}
import fi.oph.koski.oppija.ReportingQueryFacade
import fi.oph.koski.schema._
import fi.oph.koski.servlet.ApiServlet
import fi.oph.scalaschema.annotation.Description

case class OpiskeluoikeudenPerustiedot(
  henkilö: NimitiedotJaOid,
  oppilaitos: Oppilaitos,
  @Description("Opiskelijan opiskeluoikeuden alkamisaika joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  alkamispäivä: Option[LocalDate],
  tyyppi: Koodistokoodiviite,
  suoritukset: List[SuorituksenPerustiedot],
  @KoodistoUri("virtaopiskeluoikeudentila")
  @KoodistoUri("koskiopiskeluoikeudentila")
  tila: Koodistokoodiviite
)

case class SuorituksenPerustiedot(
  @Description("Suorituksen tyyppi, jolla erotellaan eri koulutusmuotoihin (perusopetus, lukio, ammatillinen...) ja eri tasoihin (tutkinto, tutkinnon osa, kurssi, oppiaine...) liittyvät suoritukset")
  @KoodistoUri("suorituksentyyppi")
  @Hidden
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: KoulutusmoduulinPerustiedot,
  @Description("Tieto siitä mihin osaamisalaan/osaamisaloihin oppijan tutkinto liittyy")
  @KoodistoUri("osaamisala")
  @OksaUri(tunnus = "tmpOKSAID299", käsite = "osaamisala")
  osaamisala: Option[List[Koodistokoodiviite]] = None,
  @Description("Tieto siitä mihin tutkintonimikkeeseen oppijan tutkinto liittyy")
  @KoodistoUri("tutkintonimikkeet")
  @OksaUri("tmpOKSAID588", "tutkintonimike")
  tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  toimipiste: OrganisaatioWithOid,
  @Description("Luokan tai ryhmän tunniste, esimerkiksi 9C")
  luokka: Option[String]
)

case class KoulutusmoduulinPerustiedot(
  tunniste: KoodiViite
)

class OpiskeluoikeudenPerustiedotRepository(henkilöRepository: HenkilöRepository, opiskeluOikeusRepository: OpiskeluOikeusRepository) {
  def findAll(session: KoskiSession): Either[HttpStatus, List[OpiskeluoikeudenPerustiedot]] = {
    ReportingQueryFacade(henkilöRepository, opiskeluOikeusRepository).findOppijat(Nil, session).right.map { opiskeluoikeudetObservable =>
      opiskeluoikeudetObservable.take(100).toBlocking.toList.flatMap {
        case (henkilö, rivit) => rivit.map { rivi =>
          val oo = rivi.toOpiskeluOikeus
          OpiskeluoikeudenPerustiedot(henkilö.nimitiedotJaOid, oo.oppilaitos, oo.alkamispäivä, oo.tyyppi, oo.suoritukset.map { suoritus =>
            val (osaamisala, tutkintonimike) = suoritus match {
              case s: AmmatillisenTutkinnonSuoritus => (s.osaamisala, s.tutkintonimike)
              case s: NäyttötutkintoonValmistavanKoulutuksenSuoritus => (s.osaamisala, s.tutkintonimike)
              case _ => (None, None)
            }
            val ryhmä = suoritus match {
              case s: PerusopetuksenVuosiluokanSuoritus => Some(s.luokka)
              case _ => None
            }
            SuorituksenPerustiedot(suoritus.tyyppi, KoulutusmoduulinPerustiedot(suoritus.koulutusmoduuli.tunniste), osaamisala, tutkintonimike, suoritus.toimipiste, ryhmä)
          }, oo.tila.opiskeluoikeusjaksot.last.tila)
        }
      }
    }
  }
}

class OpiskeluoikeudenPerustiedotServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication {
  get("/") {
    renderEither(
      new OpiskeluoikeudenPerustiedotRepository(application.oppijaRepository, application.opiskeluOikeusRepository).findAll(koskiSession)
    )
  }
}