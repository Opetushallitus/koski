package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

object AmmatillinenExampleData {
  val exampleHenkilö = MockOppijat.ammattilainen.vainHenkilötiedot

  val autoalanPerustutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", "koulutus"), Some("39/011/2014"))
  val parturikampaaja: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("381301", "koulutus"), None)
  val puutarhuri: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("361255", "koulutus"), None)

  def autoalanPerustutkinnonSuoritus(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = ammatillisenPerustutkinnonSuoritus(autoalanPerustutkinto, toimipiste)

  def ammatillisenPerustutkinnonSuoritus(koulutusmoduuli: AmmatillinenTutkintoKoulutus, toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = AmmatillisenTutkinnonSuoritus(
    koulutusmoduuli = koulutusmoduuli,
    tila = tilaKesken,
    alkamispäivä = Some(date(2016, 9, 1)),
    toimipiste = toimipiste
  )

  lazy val h2: Koodistokoodiviite = Koodistokoodiviite("2", Some("H2"), "arviointiasteikkoammatillinent1k3", None)
  lazy val k3: Koodistokoodiviite = Koodistokoodiviite("3", Some("K3"), "arviointiasteikkoammatillinent1k3", None)
  lazy val näytönArviointi = NäytönArviointi(
    arvosana = arviointiKiitettävä.arvosana,
    päivä = arviointiKiitettävä.päivä,
    arvioitsijat = arviointiHyväksytty.arvioitsijat,
    arviointikohteet = Some(List(
      NäytönArviointikohde(Koodistokoodiviite("1", Some("Työprosessin hallinta"), "ammatillisennaytonarviointikohde", None), k3),
      NäytönArviointikohde(Koodistokoodiviite("2", Some("Työmenetelmien, -välineiden ja materiaalin hallinta"), "ammatillisennaytonarviointikohde", None), h2),
      NäytönArviointikohde(Koodistokoodiviite("3", Some("Työn perustana olevan tiedon hallinta"), "ammatillisennaytonarviointikohde", None), h2),
      NäytönArviointikohde(Koodistokoodiviite("4", Some("Elinikäisen oppimisen avaintaidot"), "ammatillisennaytonarviointikohde", None), k3))),
    arvioinnistaPäättäneet = Koodistokoodiviite("1", Some("Opettaja"), "ammatillisennaytonarvioinnistapaattaneet", None),
    arviointikeskusteluunOsallistuneet = Koodistokoodiviite("1", Some("Opiskelija ja opettaja"), "ammatillisennaytonarviointikeskusteluunosallistuneet", None)
  )

  def näyttö(kuvaus: String, paikka: String, arviointi: Option[NäytönArviointi] = None) = Näyttö(
    kuvaus,
    NäytönSuorituspaikka(Koodistokoodiviite("1", Some("työpaikka"), "ammatillisennaytonsuorituspaikka", Some(1)), paikka),
    arviointi,
    työssäoppimisenYhteydessä = false
  )

  lazy val suoritustapaNäyttö = Koodistokoodiviite("naytto", Some("Näyttö"), None, "ammatillisentutkinnonsuoritustapa", Some(1))
  lazy val suoritustapaOps = Koodistokoodiviite("ops", Some("Opetussuunnitelman mukainen"), "ammatillisentutkinnonsuoritustapa", Some(1))
  lazy val järjestämismuotoOppisopimus = Koodistokoodiviite("20", Some("Oppisopimusmuotoinen"), "jarjestamismuoto", Some(1))
  lazy val järjestämismuotoOppilaitos = Koodistokoodiviite("10", Some("Oppilaitosmuotoinen"), "jarjestamismuoto", Some(1))
  lazy val stadinAmmattiopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto, Some(Koodistokoodiviite("10105", None, "oppilaitosnumero", None)), Some("Stadin ammattiopisto"))
  lazy val stadinToimipiste: OidOrganisaatio = OidOrganisaatio(MockOrganisaatiot.lehtikuusentienToimipiste, Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))
  lazy val tutkintotoimikunta: Organisaatio = Tutkintotoimikunta("Autokorjaamoalan tutkintotoimikunta", 8406)
  lazy val lähdeWinnova = Koodistokoodiviite("winnova", Some("Winnova"), "lahdejarjestelma", Some(1))
  lazy val winnovaLähdejärjestelmäId = LähdejärjestelmäId(Some("12345"), lähdeWinnova)
  lazy val hyväksytty: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1))
  lazy val tunnustettu: OsaamisenTunnustaminen = OsaamisenTunnustaminen(
    Some(AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = ValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100238", Some("Asennushitsaus"), "tutkinnonosat", Some(1)), true, None),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = None
    )),
    "Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta"
  )

  lazy val arviointiHyväksytty = AmmatillinenArviointi(
    arvosana = hyväksytty, date(2013, 3, 20),
    arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen"))))


  lazy val paikallisenOsanSuoritus = AmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = PaikallinenTutkinnonOsa(PaikallinenKoodi("123456789", "Pintavauriotyöt"), "Opetellaan korjaamaan pinnallisia vaurioita", false, None),
    tunnustettu = None,
    näyttö = Some(näyttö("Pintavaurioiden korjausta", "Autokorjaamo Oy, Riihimäki")),
    lisätiedot = None,
    suorituskieli = None,
    tila = tilaValmis,
    alkamispäivä = None,
    toimipiste = Some(stadinToimipiste),
    arviointi = Some(List(arviointiHyväksytty)),
    vahvistus = vahvistus(date(2013, 5, 31), stadinAmmattiopisto)
  )

  def autonLisävarustetyöt(pakollinen: Boolean) = ValtakunnallinenTutkinnonOsa(
    Koodistokoodiviite("100037", Some("Auton lisävarustetyöt"), "tutkinnonosat", Some(1)),
    pakollinen,
    Some(LaajuusOsaamispisteissä(15))
  )

  lazy val arviointiKiitettävä = AmmatillinenArviointi(
    arvosana = k3,
    date(2014, 10, 20)
  )

  def opiskeluoikeus(oppilaitos: Oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto),
                     tutkinto: AmmatillisenTutkinnonSuoritus = autoalanPerustutkinnonSuoritus(stadinToimipiste),
                     osat: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None): AmmatillinenOpiskeluoikeus = {
    AmmatillinenOpiskeluoikeus(
      alkamispäivä = Some(date(2016, 9, 1)),
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(date(2016, 9, 1), opiskeluoikeusLäsnä, None))),
      oppilaitos = oppilaitos,
      suoritukset = List(tutkinto.copy(osasuoritukset = osat))
    )
  }

  def oppija( henkilö: Henkilö = exampleHenkilö,
    opiskeluoikeus: Opiskeluoikeus = opiskeluoikeus()) = {
    Oppija(
      henkilö,
      List(opiskeluoikeus)
    )
  }

  def tutkinnonOsanSuoritus(koodi: String, nimi: String, arvosana: Koodistokoodiviite, laajuus: Float): AmmatillisenTutkinnonOsanSuoritus = {
    tutkinnonOsanSuoritus(koodi, nimi, arvosana, Some(laajuus))
  }

  def tutkinnonOsanSuoritus(koodi: String, nimi: String, arvosana: Koodistokoodiviite, laajuus: Option[Float] = None): AmmatillisenTutkinnonOsanSuoritus = {
    val osa: ValtakunnallinenTutkinnonOsa = ValtakunnallinenTutkinnonOsa(Koodistokoodiviite(koodi, Some(nimi), "tutkinnonosat", Some(1)), true, laajuus.map(l =>LaajuusOsaamispisteissä(l)))
    tutkonnonOsanSuoritus(arvosana, osa)
  }

  def paikallisenTutkinnonOsanSuoritus(koodi: String, nimi: String, arvosana: Koodistokoodiviite, laajuus: Float): AmmatillisenTutkinnonOsanSuoritus = {
    val osa: PaikallinenTutkinnonOsa = PaikallinenTutkinnonOsa(PaikallinenKoodi(koodi, nimi), nimi, false, Some(LaajuusOsaamispisteissä(laajuus)))
    tutkonnonOsanSuoritus(arvosana, osa)
  }

  def tutkonnonOsanSuoritus(arvosana: Koodistokoodiviite, osa: AmmatillisenTutkinnonOsa): AmmatillisenTutkinnonOsanSuoritus = {
    AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = osa,
      näyttö = None,
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(arvosana = arvosana, date(2014, 10, 20)))),
      vahvistus = vahvistus(date(2016, 5, 31), stadinAmmattiopisto)
    )
  }

  def perustutkintoOpiskeluoikeus(oppilaitos: Oppilaitos = stadinAmmattiopisto, toimipiste: OrganisaatioWithOid = stadinToimipiste) = AmmatillinenOpiskeluoikeus(
    alkamispäivä = Some(date(2012, 9, 1)),
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    päättymispäivä = Some(date(2016, 5, 31)),
    oppilaitos = oppilaitos,
    suoritukset = List(ympäristöalanPerustutkintoValmis(toimipiste)),
    tila = AmmatillinenOpiskeluoikeudenTila(
      List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
        AmmatillinenOpiskeluoikeusjakso(date(2016, 5, 31), opiskeluoikeusValmistunut, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
      )
    )
  )

  def ympäristöalanPerustutkintoValmis(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = {
    AmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = AmmatillinenTutkintoKoulutus(
        Koodistokoodiviite("361902", Some("Luonto- ja ympäristöalan perustutkinto"), "koulutus", None),
        Some("62/011/2014")
      ),
      tutkintonimike = Some(List(Koodistokoodiviite("10083", Some("Ympäristönhoitaja"), "tutkintonimikkeet", None))),
      osaamisala = Some(List(Koodistokoodiviite("1590", Some("Ympäristöalan osaamisala"), "osaamisala", None))),
      suoritustapa = Some(suoritustapaOps),
      järjestämismuoto = Some(JärjestämismuotoIlmanLisätietoja(järjestämismuotoOppilaitos)),
      suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      vahvistus = vahvistusPaikkakunnalla(date(2016, 5, 31), stadinAmmattiopisto, helsinki),
      osasuoritukset = Some(List(
        tutkinnonOsanSuoritus("100431", "Kestävällä tavalla toimiminen", k3, 40).copy(työssäoppimisjaksot = Some(List(
          Työssäoppimisjakso(date(2014, 1, 1), Some(date(2014, 3, 15)), jyväskylä, suomi, LocalizedString.finnish("Toimi harjoittelijana Sortti-asemalla"), LaajuusOsaamispisteissä(5))
        ))),
        tutkinnonOsanSuoritus("100432", "Ympäristön hoitaminen", k3, 35),
        tutkinnonOsanSuoritus("100439", "Uusiutuvien energialähteiden hyödyntäminen", k3, 15),
        tutkinnonOsanSuoritus("100442", "Ulkoilureittien rakentaminen ja hoitaminen", k3, 15),
        tutkinnonOsanSuoritus("100443", "Kulttuuriympäristöjen kunnostaminen ja hoitaminen", k3, 15),
        tutkinnonOsanSuoritus("100447", "Vesistöjen kunnostaminen ja hoitaminen", hyväksytty, 15).copy(
          lisätiedot = Some(List(AmmatillisenTutkinnonOsanLisätieto(Koodistokoodiviite("muutosarviointiasteikossa", "ammatillisentutkinnonosanlisatieto"),
            "Tutkinnon osa on koulutuksen järjestäjän päätöksellä arvioitu asteikolla hyväksytty/hylätty.")))
        ),
        tutkinnonOsanSuoritus("101053", "Viestintä- ja vuorovaikutusosaaminen", k3, 11),
        tutkinnonOsanSuoritus("101054", "Matemaattis-luonnontieteellinen osaaminen", k3, 9).copy(
          lisätiedot = Some(List(AmmatillisenTutkinnonOsanLisätieto(Koodistokoodiviite("mukautettu", "ammatillisentutkinnonosanlisatieto"),
            "Tutkinnon osan ammattitaitovaatimuksia tai osaamistavoitteita ja osaamisen arviointia on mukautettu ammatillisesta peruskoulutuksesta annetun lain (630/1998, muutos 246/2015) 19 a tai 21 §:n perusteella")))
        ),
        tutkinnonOsanSuoritus("101055", "Yhteiskunnassa ja työelämässä tarvittava osaaminen", k3, 8),
        tutkinnonOsanSuoritus("101056", "Sosiaalinen ja kulttuurinen osaaminen", k3, 7),

        paikallisenTutkinnonOsanSuoritus("enkku3", "Matkailuenglanti", k3, 5),
        paikallisenTutkinnonOsanSuoritus("soskultos1", "Sosiaalinen ja kulttuurinen osaaminen", k3, 5)
      ).map(_.copy(toimipiste = Some(toimipiste))))
    )
  }
}
