package fi.oph.koski.api.oppijavalidation

import com.typesafe.config.ConfigFactory
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.AhvenanmaanAmmatillinenExampleData
import fi.oph.koski.documentation.AhvenanmaanAmmatillinenExampleData._
import fi.oph.koski.documentation.AmmatillinenExampleData.lisätietoOsaamistavoitteet
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusLäsnä, ruotsinKieli}
import fi.oph.koski.eperusteetvalidation.{EPerusteetFiller, EPerusteisiinPerustuvaValidator}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, MockUsers, UserWithPassword}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}

class OppijaValidationAhvenanmaanAmmatillinenSpec
  extends AnyFreeSpec
  with KoskiHttpSpec
  with PutOpiskeluoikeusTestMethods[AhvenanmaanAmmatillinenOpiskeluoikeus] {

  def tag = implicitly[reflect.runtime.universe.TypeTag[AhvenanmaanAmmatillinenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = AhvenanmaanAmmatillinenExampleData.opiskeluoikeus

  private val korotettu = Some(Koodistokoodiviite("korotettu", "ammatillisensuorituksenkorotus"))

  private def keskeneräinen(päätasonSuoritus: AhvenanmaanAmmatillinenPäätasonSuoritus) = AhvenanmaanAmmatillinenOpiskeluoikeus(
    oppilaitos = Some(ålandsYrkesgymnasium),
    tila = AhvenanmaanAmmatillinenOpiskeluoikeudenTila(List(
      AhvenanmaanAmmatillinenOpiskeluoikeusjakso(date(2025, 8, 15), opiskeluoikeusLäsnä)
    )),
    suoritukset = List(päätasonSuoritus)
  )

  private def osittainen(osat: List[OsittaisenAmmatillisenTutkinnonOsanSuoritus]) = keskeneräinen(AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus(
    koulutusmoduuli = sosiaaliJaTerveysalanPerustutkinto,
    toimipiste = ålandsYrkesgymnasiumToimipiste,
    suorituskieli = ruotsinKieli,
    osasuoritukset = Some(osat)
  ))

  private def tutkinto(osat: List[AmmatillisenTutkinnonOsanSuoritus]) = keskeneräinen(AhvenanmaanAmmatillisenTutkinnonSuoritus(
    koulutusmoduuli = sosiaaliJaTerveysalanPerustutkinto,
    toimipiste = ålandsYrkesgymnasiumToimipiste,
    suorituskieli = ruotsinKieli,
    osasuoritukset = Some(osat)
  ))

  private def osaAlueet(korotettu: Option[Koodistokoodiviite]) =
    matemaattisLuonnontieteellinenOsaaminen.osasuoritukset.map(_.map(_.copy(korotettu = korotettu)))

  "Ahvenanmaan ammatillisen koulutuksen opiskeluoikeus" - {
    "Valmis perustutkinto voidaan tallentaa, ja koulutustoimijaksi tulee Ålands landskapsregering" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
      lastOpiskeluoikeusByHetu(defaultHenkilö).koulutustoimija.map(_.oid) should equal(Some(MockOrganisaatiot.ålandsLandskapsregering))
    }

    "Tutkinnon osa/osia voidaan tallentaa korotuksena manner-Suomen osasuoritusluokilla" in {
      setupOppijaWithOpiskeluoikeus(osittainen(List(
        MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(
          koulutusmoduuli = kasvunTukeminenJaOhjaus.koulutusmoduuli,
          tutkinnonOsanRyhmä = kasvunTukeminenJaOhjaus.tutkinnonOsanRyhmä,
          toimipiste = kasvunTukeminenJaOhjaus.toimipiste,
          arviointi = kasvunTukeminenJaOhjaus.arviointi,
          korotettu = korotettu
        ),
        YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(
          koulutusmoduuli = matemaattisLuonnontieteellinenOsaaminen.koulutusmoduuli,
          tutkinnonOsanRyhmä = matemaattisLuonnontieteellinenOsaaminen.tutkinnonOsanRyhmä,
          toimipiste = matemaattisLuonnontieteellinenOsaaminen.toimipiste,
          arviointi = matemaattisLuonnontieteellinenOsaaminen.arviointi,
          osasuoritukset = osaAlueet(korotettu)
        ),
        OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
          koulutusmoduuli = JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa(),
          osasuoritukset = Some(List(MuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
            koulutusmoduuli = PaikallinenOpintovalmiuksiaTukevaOpinto(PaikallinenKoodi("htm", "Hoitotarpeen määrittäminen"), "Hoitotarpeen määrittäminen"),
            arviointi = arviointi("4", date(2025, 5, 20)),
            korotettu = korotettu
          )))
        )
      ))) {
        verifyResponseStatusOk()
      }
    }

    "Koko tutkinnon suorituksella osa-alueen korotettu-kenttää ei sallita" in {
      setupOppijaWithOpiskeluoikeus(tutkinto(List(matemaattisLuonnontieteellinenOsaaminen.copy(osasuoritukset = osaAlueet(korotettu))))) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*korotettu.*".r))
      }
    }

    "Tilaa loma ei sallita" in {
      val lomalla = keskeneräinen(perustutkinnonSuoritus.copy(vahvistus = None)).copy(
        tila = AhvenanmaanAmmatillinenOpiskeluoikeudenTila(List(
          AhvenanmaanAmmatillinenOpiskeluoikeusjakso(date(2025, 8, 15), opiskeluoikeusLäsnä),
          AhvenanmaanAmmatillinenOpiskeluoikeusjakso(date(2025, 10, 1), Koodistokoodiviite("loma", "koskiopiskeluoikeudentila"))
        ))
      )
      setupOppijaWithOpiskeluoikeus(lomalla) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*enumValueMismatch.*".r))
      }
    }

    // TODO TOR-2837: ePerusteet-validointi on ohitettu, kunnes kaikki Ahvenanmaan tutkinnot on todettu manner-Suomen perusteiden mukaisiksi.
    "ePerusteista puuttuva diaarinumero hyväksytään ja koulutustyyppi päätellään koulutuskoodista" in {
      val tuntemattomallaDiaarinumerolla = perustutkinnonSuoritus.copy(
        koulutusmoduuli = sosiaaliJaTerveysalanPerustutkinto.copy(perusteenDiaarinumero = Some("ÅLR-TESTI/1"))
      )
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(tuntemattomallaDiaarinumerolla))) {
        verifyResponseStatusOk()
      }
      lastOpiskeluoikeusByHetu(defaultHenkilö).suoritukset.head.koulutusmoduuli match {
        case k: AmmatillinenTutkintoKoulutus => k.koulutustyyppi.map(_.koodistoUri) should equal(Some("koulutustyyppi"))
        case k => fail(s"Odottamaton koulutusmoduuli $k")
      }
    }

    "Mukautetun arvioinnin lisätieto piilotetaan käyttäjältä, jolla ei ole luottamuksellisia oikeuksia" in {
      val muuLisätieto = AmmatillisenTutkinnonOsanLisätieto(Koodistokoodiviite("muu", "ammatillisentutkinnonosanlisatieto"), "Muu lisätieto")
      val lisätiedoilla = defaultOpiskeluoikeus.copy(suoritukset = List(perustutkinnonSuoritus.copy(
        osasuoritukset = Some(List(kasvunTukeminenJaOhjaus.copy(lisätiedot = Some(List(lisätietoOsaamistavoitteet, muuLisätieto)))))
      )))
      setupOppijaWithOpiskeluoikeus(lisätiedoilla) {
        verifyResponseStatusOk()
      }

      def lisätiedot(user: UserWithPassword) =
        lastOpiskeluoikeus(KoskiSpecificMockOppijat.eero.oid, user).suoritukset.head.osasuoritusLista.collect {
          case s: AmmatillisenTutkinnonOsanLisätiedollinen => s.lisätiedot.toList.flatten.map(_.tunniste.koodiarvo)
        }

      lisätiedot(MockUsers.paakayttaja) should equal(List(List("mukautettu", "muu")))
      lisätiedot(MockUsers.viranomainenGlobaaliKatselija) should equal(List(List("muu")))
    }

    "Tuotantokonfiguraatio estää tallennuksen disabledPäätasonSuoritusLuokat-asetuksella" in {
      implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
      implicit val accessType: AccessType.Value = AccessType.write
      val mockConfig = ConfigFactory.parseString(
        """
          features = {
            disabledPäätasonSuoritusTyypit = []
            disabledPäätasonSuoritusLuokat = [
              AhvenanmaanAmmatillisenTutkinnonSuoritus
              AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus
            ]
            disabledOsasuoritusTyypit = []
          }
        """.stripMargin)
      val config = KoskiApplicationForTests.config.withoutPath("features").withFallback(mockConfig)
      val validator = new KoskiValidator(
        KoskiApplicationForTests.organisaatioRepository,
        KoskiApplicationForTests.possu,
        KoskiApplicationForTests.henkilöRepository,
        new EPerusteisiinPerustuvaValidator(
          KoskiApplicationForTests.ePerusteet,
          KoskiApplicationForTests.tutkintoRepository,
          KoskiApplicationForTests.koodistoViitePalvelu,
          config
        ),
        KoskiApplicationForTests.ePerusteetLops2019Validator,
        new EPerusteetFiller(
          KoskiApplicationForTests.ePerusteet,
          KoskiApplicationForTests.tutkintoRepository,
          KoskiApplicationForTests.koodistoViitePalvelu
        ),
        KoskiApplicationForTests.validatingAndResolvingExtractor,
        KoskiApplicationForTests.suostumuksenPeruutusService,
        KoskiApplicationForTests.koodistoViitePalvelu,
        config,
        KoskiApplicationForTests.validationContext,
      )

      val opiskelija = oppija(KoskiSpecificMockOppijat.ahvenanmaanAmmattiopiskelija.oid)
      validator.updateFieldsAndValidateAsJson(opiskelija).swap.toOption.get should equal(
        KoskiErrorCategory.notImplemented("Päätason suorituksen luokka AhvenanmaanAmmatillisenTutkinnonSuoritus ei ole käytössä tässä ympäristössä")
      )
    }
  }
}
