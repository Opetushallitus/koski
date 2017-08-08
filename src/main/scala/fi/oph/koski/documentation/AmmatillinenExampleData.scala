package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{AmmatillisenTutkinnonOsanOsaAlueenSuoritus, Koodistokoodiviite, _}

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

  lazy val näytönArvioitsijat = Some(List(NäytönArvioitsija("Jaana Arstila", Some(true)), NäytönArvioitsija("Pekka Saurmann", Some(true)), NäytönArvioitsija("Juhani Mykkänen", Some(false))))

  lazy val näytönArviointi = NäytönArviointi(
    arvosana = arviointiKiitettävä.arvosana,
    päivä = arviointiKiitettävä.päivä,
    arvioitsijat = näytönArvioitsijat,
    arviointikohteet = Some(List(
      NäytönArviointikohde(Koodistokoodiviite("1", Some("Työprosessin hallinta"), "ammatillisennaytonarviointikohde", None), k3),
      NäytönArviointikohde(Koodistokoodiviite("2", Some("Työmenetelmien, -välineiden ja materiaalin hallinta"), "ammatillisennaytonarviointikohde", None), h2),
      NäytönArviointikohde(Koodistokoodiviite("3", Some("Työn perustana olevan tiedon hallinta"), "ammatillisennaytonarviointikohde", None), h2),
      NäytönArviointikohde(Koodistokoodiviite("4", Some("Elinikäisen oppimisen avaintaidot"), "ammatillisennaytonarviointikohde", None), k3))),
    arvioinnistaPäättäneet = List(Koodistokoodiviite("1", Some("Opettaja"), "ammatillisennaytonarvioinnistapaattaneet", None)),
    arviointikeskusteluunOsallistuneet =
      List(
        Koodistokoodiviite("1", Some("Opettaja"), "ammatillisennaytonarviointikeskusteluunosallistuneet", None),
        Koodistokoodiviite("4", Some("Opiskelija"), "ammatillisennaytonarviointikeskusteluunosallistuneet", None)
      )
  )

  def näyttö(päivä: LocalDate, kuvaus: String, paikka: String, arviointi: Option[NäytönArviointi] = None) = Näyttö(
    Some(kuvaus),
    Some(NäytönSuorituspaikka(Koodistokoodiviite("1", Some("työpaikka"), "ammatillisennaytonsuorituspaikka", Some(1)), paikka)),
    Some(NäytönSuoritusaika(päivä, päivä)),
    false,
    arviointi
  )

  lazy val suoritustapaNäyttö = Koodistokoodiviite("naytto", Some("Näyttö"), None, "ammatillisentutkinnonsuoritustapa", Some(1))
  lazy val suoritustapaOps = Koodistokoodiviite("ops", Some("Opetussuunnitelman mukainen"), "ammatillisentutkinnonsuoritustapa", Some(1))
  lazy val järjestämismuotoOppisopimus = OppisopimuksellinenJärjestämismuoto(Koodistokoodiviite("20", Some("Oppisopimusmuotoinen"), "jarjestamismuoto", Some(1)), Oppisopimus(Yritys("Autokorjaamo Oy", "1234567-8")))
  lazy val järjestämismuotoOppilaitos = JärjestämismuotoIlmanLisätietoja(Koodistokoodiviite("10", Some("Oppilaitosmuotoinen"), "jarjestamismuoto", Some(1)))
  lazy val stadinAmmattiopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto, Some(Koodistokoodiviite("10105", None, "oppilaitosnumero", None)), Some("Stadin ammattiopisto"))
  lazy val stadinToimipiste: OidOrganisaatio = OidOrganisaatio(MockOrganisaatiot.lehtikuusentienToimipiste, Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))
  lazy val tutkintotoimikunta: Organisaatio = Tutkintotoimikunta("Autokorjaamoalan tutkintotoimikunta", "8406")
  lazy val lähdeWinnova = Koodistokoodiviite("winnova", Some("Winnova"), "lahdejarjestelma", Some(1))
  lazy val winnovaLähdejärjestelmäId = LähdejärjestelmäId(Some("12345"), lähdeWinnova)
  lazy val hyväksytty: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1))
  lazy val tunnustettu: OsaamisenTunnustaminen = OsaamisenTunnustaminen(
    Some(MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100238", Some("Asennushitsaus"), "tutkinnonosat", Some(1)), true, None),
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


  lazy val paikallisenOsanSuoritus = MuunAmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = PaikallinenTutkinnonOsa(PaikallinenKoodi("123456789", "Pintavauriotyöt"), "Opetellaan korjaamaan pinnallisia vaurioita", false, None),
    tunnustettu = None,
    näyttö = Some(näyttö(date(2013, 5, 20), "Pintavaurioiden korjausta", "Autokorjaamo Oy, Riihimäki")),
    lisätiedot = None,
    suorituskieli = None,
    tila = tilaValmis,
    alkamispäivä = None,
    toimipiste = Some(stadinToimipiste),
    arviointi = Some(List(arviointiHyväksytty)),
    vahvistus = vahvistusValinnaisellaTittelillä(date(2013, 5, 31), stadinAmmattiopisto)
  )

  def autonLisävarustetyöt(pakollinen: Boolean) = MuuValtakunnallinenTutkinnonOsa(
    Koodistokoodiviite("100037", Some("Auton lisävarustetyöt"), "tutkinnonosat", Some(1)),
    pakollinen,
    Some(LaajuusOsaamispisteissä(15))
  )

  lazy val arviointiKiitettävä = AmmatillinenArviointi(
    arvosana = k3,
    date(2014, 10, 20)
  )

  lazy val ammatillisetTutkinnonOsat = Some(Koodistokoodiviite("1", "ammatillisentutkinnonosanryhma"))
  lazy val yhteisetTutkinnonOsat = Some(Koodistokoodiviite("2", "ammatillisentutkinnonosanryhma"))
  lazy val vapaavalintaisetTutkinnonOsat = Some(Koodistokoodiviite("3", "ammatillisentutkinnonosanryhma"))
  lazy val yksilöllisestiLaajentavatTutkinnonOsat = Some(Koodistokoodiviite("4", "ammatillisentutkinnonosanryhma"))

  def opiskeluoikeus(oppilaitos: Oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto),
                     tutkinto: AmmatillisenTutkinnonSuoritus = autoalanPerustutkinnonSuoritus(stadinToimipiste),
                     osat: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None): AmmatillinenOpiskeluoikeus = {
    AmmatillinenOpiskeluoikeus(
      alkamispäivä = Some(date(2016, 9, 1)),
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(date(2016, 9, 1), opiskeluoikeusLäsnä, None))),
      oppilaitos = Some(oppilaitos),
      suoritukset = List(tutkinto.copy(osasuoritukset = osat))
    )
  }

  def oppija( henkilö: Henkilö = exampleHenkilö, opiskeluoikeus: Opiskeluoikeus = this.opiskeluoikeus()) = {
    Oppija(
      henkilö,
      List(opiskeluoikeus)
    )
  }

  def tutkinnonOsanSuoritus(koodi: String, nimi: String, ryhmä: Option[Koodistokoodiviite], arvosana: Koodistokoodiviite, laajuus: Float): MuunAmmatillisenTutkinnonOsanSuoritus = {
    tutkinnonOsanSuoritus(koodi, nimi, ryhmä, arvosana, Some(laajuus))
  }

  def yhteisenTutkinnonOsanSuoritus(koodi: String, nimi: String, arvosana: Koodistokoodiviite, laajuus: Float): YhteisenAmmatillisenTutkinnonOsanSuoritus = {
    val osa = YhteinenTutkinnonOsa(Koodistokoodiviite(koodi, Some(nimi), "tutkinnonosat", Some(1)), true, Some(LaajuusOsaamispisteissä(laajuus)))
    YhteisenAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = osa,
      tutkinnonOsanRyhmä = yhteisetTutkinnonOsat,
      näyttö = None,
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(arvosana = arvosana, date(2014, 10, 20)))),
      vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 5, 31), stadinAmmattiopisto)
    )
  }

  def tutkinnonOsanSuoritusTilassa(koodi: String, nimi: String, ryhmä: Option[Koodistokoodiviite], tila: Koodistokoodiviite, laajuus: Option[Float]): AmmatillisenTutkinnonOsanSuoritus = {
    val osa = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite(koodi, Some(nimi), "tutkinnonosat", Some(1)), true, laajuus.map(l =>LaajuusOsaamispisteissä(l)))
    MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = osa,
      tutkinnonOsanRyhmä = ryhmä,
      näyttö = None,
      suorituskieli = None,
      tila = tila,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste)
    )
  }

  def tutkinnonOsanSuoritus(koodi: String, nimi: String, ryhmä: Option[Koodistokoodiviite], arvosana: Koodistokoodiviite, laajuus: Option[Float] = None): MuunAmmatillisenTutkinnonOsanSuoritus = {
    val osa = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite(koodi, Some(nimi), "tutkinnonosat", Some(1)), true, laajuus.map(l =>LaajuusOsaamispisteissä(l)))
    tutkinnonOsanSuoritus(arvosana, osa, ryhmä)
  }

  def paikallisenTutkinnonOsanSuoritus(koodi: String, nimi: String, ryhmä: Option[Koodistokoodiviite], arvosana: Koodistokoodiviite, laajuus: Float): AmmatillisenTutkinnonOsanSuoritus = {
    val osa: PaikallinenTutkinnonOsa = PaikallinenTutkinnonOsa(PaikallinenKoodi(koodi, nimi), nimi, false, Some(LaajuusOsaamispisteissä(laajuus)))
    tutkinnonOsanSuoritus(arvosana, osa, ryhmä)
  }

  def tutkinnonOsanSuoritus(arvosana: Koodistokoodiviite, osa: MuuKuinYhteinenTutkinnonOsa, ryhmä: Option[Koodistokoodiviite]): MuunAmmatillisenTutkinnonOsanSuoritus = {
    MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = osa,
      tutkinnonOsanRyhmä = ryhmä,
      näyttö = None,
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(arvosana = arvosana, date(2014, 10, 20)))),
      vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 5, 31), stadinAmmattiopisto)
    )
  }

  val opiskeluoikeudenLisätiedot = AmmatillisenOpiskeluoikeudenLisätiedot(
    hojks = Some(Hojks(
      opetusryhmä = Koodistokoodiviite("1", Some("Yleinen opetusryhmä"), "opetusryhma")
    )),
    oikeusMaksuttomaanAsuntolapaikkaan = true,
    ulkomaanjaksot = Some(List(Ulkomaanjakso(date(2012, 9, 1), Some(date(2013, 9, 1)), ruotsi, "Harjoittelua ulkomailla"))),
    vaikeastiVammainen = true,
    vammainenJaAvustaja = true,
    majoitus = Some(List(Majoitusjakso(date(2012, 9, 1), Some(date(2013, 9, 1))))),
    sisäoppilaitosmainenMajoitus = Some(List(Majoitusjakso(date(2012, 9, 1), Some(date(2013, 9, 1))))),
    vaativanErityisenTuenYhteydessäJärjestettäväMajoitus = Some(List(Majoitusjakso(date(2012, 9, 1), Some(date(2013, 9, 1))))),
    henkilöstökoulutus = true,
    vankilaopetuksessa = true,
    osaAikaisuus = Some(80),
    poissaolojaksot = Some(List(Poissaolojakso(date(2013, 10, 1), Some(date(2013, 10, 31)), Koodistokoodiviite("2", "ammatillistenopintojenpoissaolonsyy"))))
  )

  def perustutkintoOpiskeluoikeusValmis(oppilaitos: Oppilaitos = stadinAmmattiopisto, toimipiste: OrganisaatioWithOid = stadinToimipiste) = AmmatillinenOpiskeluoikeus(
    alkamispäivä = Some(date(2012, 9, 1)),
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    päättymispäivä = Some(date(2016, 5, 31)),
    oppilaitos = Some(oppilaitos),
    suoritukset = List(ympäristöalanPerustutkintoValmis(toimipiste)),
    lisätiedot = None,
    tila = AmmatillinenOpiskeluoikeudenTila(
      List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
        AmmatillinenOpiskeluoikeusjakso(date(2016, 5, 31), opiskeluoikeusValmistunut, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
      )
    )
  )

  def perustutkintoOpiskeluoikeusKesken(oppilaitos: Oppilaitos = stadinAmmattiopisto, toimipiste: OrganisaatioWithOid = stadinToimipiste) = AmmatillinenOpiskeluoikeus(
    alkamispäivä = Some(date(2012, 9, 1)),
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    oppilaitos = Some(oppilaitos),
    suoritukset = List(ympäristöalanPerustutkintoKesken(toimipiste)),
    tila = AmmatillinenOpiskeluoikeudenTila(
      List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
      )
    ),
    lisätiedot = Some(opiskeluoikeudenLisätiedot)
  )

  val lisätietoMuutosArviointiasteikossa = AmmatillisenTutkinnonOsanLisätieto(Koodistokoodiviite("muutosarviointiasteikossa", "ammatillisentutkinnonosanlisatieto"),
    "Tutkinnon osa on koulutuksen järjestäjän päätöksellä arvioitu asteikolla hyväksytty/hylätty.")

  val lisätietoOsaamistavoitteet = AmmatillisenTutkinnonOsanLisätieto(Koodistokoodiviite("mukautettu", "ammatillisentutkinnonosanlisatieto"),
    "Tutkinnon osan ammattitaitovaatimuksia tai osaamistavoitteita ja osaamisen arviointia on mukautettu ammatillisesta peruskoulutuksesta annetun lain (630/1998, muutos 246/2015) 19 a tai 21 §:n perusteella"
  )

  val työssäoppiminenSorttiAsemalla = Some(List(
    Työssäoppimisjakso(date(2014, 1, 1), Some(date(2014, 3, 15)), Some("Sortti-asema"), jyväskylä, suomi, Some(LocalizedString.finnish("Toimi harjoittelijana Sortti-asemalla")), LaajuusOsaamispisteissä(5))
  ))

  def ympäristöalanPerustutkintoValmis(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = {
    AmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = AmmatillinenTutkintoKoulutus(
        Koodistokoodiviite("361902", Some("Luonto- ja ympäristöalan perustutkinto"), "koulutus", None),
        Some("62/011/2014")
      ),
      tutkintonimike = Some(List(Koodistokoodiviite("10083", Some("Ympäristönhoitaja"), "tutkintonimikkeet", None))),
      osaamisala = Some(List(Koodistokoodiviite("1590", Some("Ympäristöalan osaamisala"), "osaamisala", None))),
      suoritustapa = Some(suoritustapaOps),
      järjestämismuoto = Some(järjestämismuotoOppilaitos),
      suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      vahvistus = vahvistusPaikkakunnalla(date(2016, 5, 31), stadinAmmattiopisto, helsinki),
      ryhmä = Some("YMP14SN"),
      osasuoritukset = Some(List(
        tutkinnonOsanSuoritus("100431", "Kestävällä tavalla toimiminen", ammatillisetTutkinnonOsat, k3, 40).copy(työssäoppimisjaksot = työssäoppiminenSorttiAsemalla),
        tutkinnonOsanSuoritus("100432", "Ympäristön hoitaminen", ammatillisetTutkinnonOsat, k3, 35).copy(näyttö = Some(
          näyttö(date(2016, 2, 1), "Muksulan päiväkodin ympäristövaikutusten arvioiminen ja ympäristön kunnostustöiden\ntekeminen sekä mittauksien tekeminen ja näytteiden ottaminen", "Muksulan päiväkoti, Kaarinan kunta", Some(näytönArviointi)))
        ),
        tutkinnonOsanSuoritus("100439", "Uusiutuvien energialähteiden hyödyntäminen", ammatillisetTutkinnonOsat, k3, 15),
        tutkinnonOsanSuoritus("100442", "Ulkoilureittien rakentaminen ja hoitaminen", ammatillisetTutkinnonOsat, k3, 15),
        tutkinnonOsanSuoritus("100443", "Kulttuuriympäristöjen kunnostaminen ja hoitaminen", ammatillisetTutkinnonOsat, k3, 15).copy(näyttö = Some(
          näyttö(date(2016, 3, 1), "Sastamalan kunnan kulttuuriympäristöohjelmaan liittyvän Wanhan myllyn lähiympäristön\nkasvillisuuden kartoittamisen sekä ennallistamisen suunnittelu ja toteutus", "Sastamalan kunta", Some(näytönArviointi)))
        ),
        tutkinnonOsanSuoritus("100447", "Vesistöjen kunnostaminen ja hoitaminen", ammatillisetTutkinnonOsat, hyväksytty, 15).copy(
          näyttö = Some(näyttö(date(2016, 4, 1), "Uimarin järven tilan arviointi ja kunnostus", "Vesipojat Oy", Some(näytönArviointi))),
          lisätiedot = Some(List(lisätietoMuutosArviointiasteikossa)),
          osasuoritukset = Some(List(
            AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
              AmmatillisenTutkinnonOsaaPienempiKokonaisuus(PaikallinenKoodi("htm", "Hoitotarpeen määrittäminen"), "Hoitotarpeen määrittäminen"),
              arviointi = Some(List(arviointiHyväksytty)),
              tila = tilaValmis
            )
          ))
        ),
        yhteisenTutkinnonOsanSuoritus("101053", "Viestintä- ja vuorovaikutusosaaminen", k3, 11).copy(
          osasuoritukset = Some(List(
            AmmatillisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(5))), tila = tilaValmis, arviointi = Some(List(arviointiKiitettävä))),
            AmmatillisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = false, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(3))), tila = tilaValmis, arviointi = Some(List(arviointiKiitettävä))),
            AmmatillisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("TK1", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(1))), tila = tilaValmis, arviointi = Some(List(arviointiKiitettävä))),
            AmmatillisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(Koodistokoodiviite("VK", "ammatillisenoppiaineet"), Koodistokoodiviite("EN", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(2))), tila = tilaValmis, arviointi = Some(List(arviointiKiitettävä)))
          ))
        ),
        yhteisenTutkinnonOsanSuoritus("101054", "Matemaattis-luonnontieteellinen osaaminen", k3, 9).copy(
          lisätiedot = Some(List(lisätietoOsaamistavoitteet)),
          osasuoritukset = Some(List(
            AmmatillisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = PaikallinenAmmatillisenTutkinnonOsanOsaAlue(PaikallinenKoodi("MA", "Matematiikka"), "Matematiikan opinnot", pakollinen = true, Some(LaajuusOsaamispisteissä(3))), tila = tilaValmis, arviointi = Some(List(arviointiKiitettävä))),
            AmmatillisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("FK", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(3))), tila = tilaValmis, arviointi = Some(List(arviointiKiitettävä))),
            AmmatillisenTutkinnonOsanOsaAlueenSuoritus(
              koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("TVT", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(3))),
              tila = tilaValmis,
              arviointi = Some(List(arviointiKiitettävä)),
              alkamispäivä = Some(date(2014, 1, 1)),
              tunnustettu = Some(tunnustettu),
              lisätiedot = Some(List(lisätietoOsaamistavoitteet))
            )
          ))
        ),
        yhteisenTutkinnonOsanSuoritus("101055", "Yhteiskunnassa ja työelämässä tarvittava osaaminen", k3, 8),
        yhteisenTutkinnonOsanSuoritus("101056", "Sosiaalinen ja kulttuurinen osaaminen", k3, 7),

        paikallisenTutkinnonOsanSuoritus("enkku3", "Matkailuenglanti", yksilöllisestiLaajentavatTutkinnonOsat, k3, 5),
        paikallisenTutkinnonOsanSuoritus("soskultos1", "Sosiaalinen ja kulttuurinen osaaminen", vapaavalintaisetTutkinnonOsat, k3, 5)
      ).map(_.toimipisteellä(toimipiste)))
    )
  }

  def ympäristöalanPerustutkintoKesken(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = {
    AmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = AmmatillinenTutkintoKoulutus(
        Koodistokoodiviite("361902", Some("Luonto- ja ympäristöalan perustutkinto"), "koulutus", None),
        Some("62/011/2014")
      ),
      tutkintonimike = Some(List(Koodistokoodiviite("10083", Some("Ympäristönhoitaja"), "tutkintonimikkeet", None))),
      osaamisala = Some(List(Koodistokoodiviite("1590", Some("Ympäristöalan osaamisala"), "osaamisala", None))),
      suoritustapa = Some(suoritustapaOps),
      järjestämismuoto = Some(järjestämismuotoOppilaitos),
      suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
      tila = tilaKesken,
      alkamispäivä = None,
      toimipiste = toimipiste,
      työssäoppimisjaksot = työssäoppiminenSorttiAsemalla,
      osasuoritukset = Some(List(
        tutkinnonOsanSuoritus("100431", "Kestävällä tavalla toimiminen", ammatillisetTutkinnonOsat, k3, 40),
        tutkinnonOsanSuoritus("100432", "Ympäristön hoitaminen", ammatillisetTutkinnonOsat, k3, 35),
        tutkinnonOsanSuoritus("100439", "Uusiutuvien energialähteiden hyödyntäminen", ammatillisetTutkinnonOsat, k3, 15),
        tutkinnonOsanSuoritusTilassa("100442", "Ulkoilureittien rakentaminen ja hoitaminen", ammatillisetTutkinnonOsat, tilaKesken, None),
        tutkinnonOsanSuoritusTilassa("100443", "Kulttuuriympäristöjen kunnostaminen ja hoitaminen", None, tilaKeskeytynyt, None),
        paikallisenTutkinnonOsanSuoritus("enkku3", "Matkailuenglanti", None, k3, 5)
      ).map(_.toimipisteellä(toimipiste)))
    )
  }


  def ammatillisenTutkinnonOsittainenSuoritus = AmmatillisenTutkinnonOsittainenSuoritus(
    koulutusmoduuli = AmmatillinenTutkintoKoulutus(
      Koodistokoodiviite("361902", Some("Luonto- ja ympäristöalan perustutkinto"), "koulutus", None),
      Some("62/011/2014")
    ),
    tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
    toinenTutkintonimike = true,
    osaamisala = Some(List(Koodistokoodiviite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None))),
    toinenOsaamisala = true,
    järjestämismuoto = Some(järjestämismuotoOppilaitos),
    suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
    tila = tilaValmis,
    alkamispäivä = None,
    toimipiste = stadinToimipiste,
    osasuoritukset = Some(List(
      tutkinnonOsanSuoritus("100432", "Ympäristön hoitaminen", None, k3, 35)
    )),
    todistuksellaNäkyvätLisätiedot = Some("Suorittaa toista osaamisalaa")
  )

}
