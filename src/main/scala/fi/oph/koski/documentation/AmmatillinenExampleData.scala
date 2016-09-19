package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

object AmmatillinenExampleData {
  val exampleHenkilö = MockOppijat.ammattilainen.vainHenkilötiedot

  def tutkintoSuoritus(tutkintoKoulutus: AmmatillinenTutkintoKoulutus,
    tutkintonimike: Option[List[Koodistokoodiviite]] = None,
    osaamisala: Option[List[Koodistokoodiviite]] = None,
    suoritustapa: Option[Koodistokoodiviite] = None,
    järjestämismuoto: Option[Järjestämismuoto] = None,
    suorituskieli: Option[Koodistokoodiviite] = None,
    tila: Koodistokoodiviite,
    alkamisPäivä: Option[LocalDate] = None,
    toimipiste: OrganisaatioWithOid,
    vahvistus: Option[Henkilövahvistus] = None,
    osasuoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None): AmmatillisenTutkinnonSuoritus =

    AmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = tutkintoKoulutus,
      tutkintonimike,
      osaamisala = osaamisala,
      suoritustapa = suoritustapa,
      järjestämismuoto = järjestämismuoto,
      suorituskieli = suorituskieli,
      tila = tila,
      alkamispäivä = alkamisPäivä,
      toimipiste = toimipiste,
      vahvistus = vahvistus,
      osasuoritukset = osasuoritukset)

  val autoalanPerustutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", "koulutus"), Some("39/011/2014"))
  val parturikampaaja: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("381301", "koulutus"), None)
  val puutarhuri: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("361255", "koulutus"), None)

  def autoalanPerustutkinnonSuoritus(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = ammatillisenPerustutkinnonSuoritus(autoalanPerustutkinto, toimipiste)

  def ammatillisenPerustutkinnonSuoritus(tutkinto: AmmatillinenTutkintoKoulutus, toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = tutkintoSuoritus(
    tutkintoKoulutus = tutkinto,
    tutkintonimike = None,
    osaamisala = None,
    suoritustapa = None,
    järjestämismuoto = None,
    suorituskieli = None,
    tila = tilaKesken,
    alkamisPäivä = Some(date(2016, 9, 1)),
    toimipiste = toimipiste,
    vahvistus = None,
    osasuoritukset = None
  )

  lazy val h2: Koodistokoodiviite = Koodistokoodiviite("2", Some("H2"), "arviointiasteikkoammatillinent1k3", None)
  lazy val k3: Koodistokoodiviite = Koodistokoodiviite("3", Some("K3"), "arviointiasteikkoammatillinent1k3", None)
  lazy val näytönArviointi = NäytönArviointi(List(
    NäytönArviointikohde(Koodistokoodiviite("1", Some("Työprosessin hallinta"), "ammatillisennaytonarviointikohde", None), k3),
    NäytönArviointikohde(Koodistokoodiviite("2", Some("Työmenetelmien, -välineiden ja materiaalin hallinta"), "ammatillisennaytonarviointikohde", None), h2),
    NäytönArviointikohde(Koodistokoodiviite("3", Some("Työn perustana olevan tiedon hallinta"), "ammatillisennaytonarviointikohde", None), h2),
    NäytönArviointikohde(Koodistokoodiviite("4", Some("Elinikäisen oppimisen avaintaidot"), "ammatillisennaytonarviointikohde", None), k3)),
    Koodistokoodiviite("1", Some("Opettaja"), "ammatillisennaytonarvioinnistapaattaneet", None),
    Koodistokoodiviite("1", Some("Opiskelija ja opettaja"), "ammatillisennaytonarviointikeskusteluunosallistuneet", None)
  )

  def näyttö(kuvaus: String, paikka: String, arviointi: Option[NäytönArviointi] = None) = Näyttö(
    kuvaus,
    NäytönSuorituspaikka(Koodistokoodiviite("1", Some("työpaikka"), "ammatillisennaytonsuorituspaikka", Some(1)), paikka),
    arviointi,
    työssäoppimisenYhteydessä = false
  )

  lazy val tavoiteTutkinto = Koodistokoodiviite("ammatillinentutkinto", "suorituksentyyppi")
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
  lazy val arviointiHyväksytty: Some[List[AmmatillinenArviointi]] = Some(List(AmmatillinenArviointi(
    arvosana = hyväksytty, date(2013, 3, 20),
    arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen"))))))


  lazy val paikallisenOsanSuoritus = AmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = PaikallinenTutkinnonOsa(PaikallinenKoodi("123456789", "Pintavauriotyöt"), "Opetellaan korjaamaan pinnallisia vaurioita", false, None),
    tunnustettu = None,
    näyttö = Some(näyttö("Pintavaurioiden korjausta", "Autokorjaamo Oy, Riihimäki")),
    lisätiedot = None,
    suorituskieli = None,
    tila = tilaValmis,
    alkamispäivä = None,
    toimipiste = Some(stadinToimipiste),
    arviointi = arviointiHyväksytty,
    vahvistus = vahvistus(date(2013, 5, 31), stadinAmmattiopisto, helsinki)
  )

  lazy val arviointiKiitettävä = Some(
    List(
      AmmatillinenArviointi(
        arvosana = k3,
        date(2014, 10, 20)
      )
    )
  )

  def opiskeluoikeus(oppilaitos: Oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto),
                     tutkinto: AmmatillisenTutkinnonSuoritus = autoalanPerustutkinnonSuoritus(stadinToimipiste),
                     osat: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None): AmmatillinenOpiskeluoikeus = {
    AmmatillinenOpiskeluoikeus(
      alkamispäivä = Some(date(2016, 9, 1)),
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(date(2016, 9, 1), opiskeluoikeusLäsnä, None))),
      oppilaitos = oppilaitos,
      suoritukset = List(tutkinto.copy(osasuoritukset = osat)),
      tavoite = tavoiteTutkinto
    )
  }

  def oppija( henkilö: Henkilö = exampleHenkilö,
    opiskeluOikeus: Opiskeluoikeus = opiskeluoikeus()) = {
    Oppija(
      henkilö,
      List(opiskeluOikeus)
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
      vahvistus = vahvistus(date(2016, 5, 31), stadinAmmattiopisto, helsinki)
    )
  }
}
