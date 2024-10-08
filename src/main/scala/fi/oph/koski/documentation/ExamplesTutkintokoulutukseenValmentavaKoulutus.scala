package fi.oph.koski.documentation

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, MockOppijat}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

import java.time.LocalDate
import java.time.LocalDate.{of => date}

object ExamplesTutkintokoulutukseenValmentavaKoulutus {

  def tuvaSanallinenArviointi(
    arviointiPäivä: Option[LocalDate]
  ): Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = {
    arviointiPäivä.map(d =>
      List(
        SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
          arvosana = Koodistokoodiviite("Hyväksytty", "arviointiasteikkotuva"),
          kuvaus = Some(
            finnish("Hyväksytty tutkintokoulutukseen valmentavan koulutuksen osasuoritus.")
          ),
          päivä = d
        )
      )
    )
  }

  def tuvaSanallinenArviointiHylätty(
    arviointiPäivä: Option[LocalDate]
  ): Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = {
    arviointiPäivä.map(d =>
      List(
        SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
          arvosana = Koodistokoodiviite("Hylätty", "arviointiasteikkotuva"),
          kuvaus = Some(
            finnish("Hylätty tutkintokoulutukseen valmentavan koulutuksen osasuoritus.")
          ),
          päivä = d
        )
      )
    )
  }

  def tuvaOpiskeluOikeusjakso(d: LocalDate, koodistokoodiviite: String) = TutkintokoulutukseenValmentavanOpiskeluoikeusjakso(
    alku = d,
    tila = Koodistokoodiviite(koodistokoodiviite, "koskiopiskeluoikeudentila"),
    opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)
  )

  def tuvaPäätasonSuoritus(laajuus: Option[Double], toimipiste: Oppilaitos = stadinAmmattiopisto) = TutkintokoulutukseenValmentavanKoulutuksenSuoritus(
    toimipiste = toimipiste,
    koulutusmoduuli = TutkintokoulutukseenValmentavanKoulutus(
      laajuus = laajuus.map(l => LaajuusViikoissa(l))
    ),
    vahvistus = Some(
      HenkilövahvistusValinnaisellaPaikkakunnalla(
        päivä = date(2021, 12, 31),
        paikkakunta = Some(
          ExampleData.helsinki
        ),
        myöntäjäOrganisaatio = toimipiste,
        myöntäjäHenkilöt = List(
          Organisaatiohenkilö("Reijo Reksi", "rehtori", toimipiste)
        )
      )
    ),
    suorituskieli = suomenKieli,
    osasuoritukset = None
  )

  def tuvaKoulutuksenMuunOsanSuoritus(
    koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenMuuOsa,
    arviointiPäivä: Option[LocalDate] = None,
    koodistoviite: String
  ) = TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(
    koulutusmoduuli = koulutusmoduuli,
    arviointi = tuvaSanallinenArviointi(arviointiPäivä),
    suorituskieli = Some(suomenKieli),
    tyyppi = Koodistokoodiviite(koodistoviite, "suorituksentyyppi"),
    tunnustettu = None
  )

  def tuvaKoulutuksenValinnaisenOsanSuoritus(
    laajuus: Option[Double],
    arviointiPäivä: Option[LocalDate] = None
  ) = TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus(
    koulutusmoduuli = TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa(
      laajuus = laajuus.map(LaajuusViikoissa(_))
    ),
    arviointi = tuvaSanallinenArviointi(arviointiPäivä),
    suorituskieli = Some(suomenKieli),
    tyyppi = Koodistokoodiviite("tutkintokoulutukseenvalmentava", "suorituksentyyppi"),
    tunnustettu = None,
    osasuoritukset = None
  )

  def tuvaOpiskeluJaUrasuunnittelutaidot(laajuus: Option[Double]) = TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot(
    laajuus = laajuus.map(l => LaajuusViikoissa(l))
  )

  def tuvaPerustaitojenVahvistaminen(laajuus: Option[Double]) = TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen(
    laajuus = laajuus.map(l => LaajuusViikoissa(l))
  )

  def tuvaAmmatillisenKoulutuksenOpinnot(laajuus: Option[Double]) = TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot(
    laajuus = laajuus.map(l => LaajuusViikoissa(l))
  )

  def tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus: Option[Double]) = TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(
    laajuus = laajuus.map(l => LaajuusViikoissa(l))
  )

  def tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus: Option[Double]) = TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot(
    laajuus = laajuus.map(l => LaajuusViikoissa(l))
  )

  def tuvaLukiokoulutuksenOpinnot(laajuus: Option[Double]) = TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot(
    laajuus = laajuus.map(l => LaajuusViikoissa(l))
  )

  lazy val tuvaTilaLäsnä = TutkintokoulutukseenValmentavanOpiskeluoikeudenTila(
    opiskeluoikeusjaksot = List(
      tuvaOpiskeluOikeusjakso(date(2021, 8, 1), "lasna")
    )
  )
  lazy val tuvaTilaValmistunut = TutkintokoulutukseenValmentavanOpiskeluoikeudenTila(
    opiskeluoikeusjaksot = List(
      tuvaOpiskeluOikeusjakso(date(2021, 8, 1), "lasna"),
      tuvaOpiskeluOikeusjakso(date(2022, 8, 1), "valmistunut")
    )
  )
  lazy val tuvaTilaKatsotaanEronneeksi= TutkintokoulutukseenValmentavanOpiskeluoikeudenTila(
    opiskeluoikeusjaksot = List(
      tuvaOpiskeluOikeusjakso(date(2021, 8, 1), "lasna"),
      tuvaOpiskeluOikeusjakso(date(2022, 8, 1), "katsotaaneronneeksi")
    )
  )
  lazy val tuvaTilaLoma = TutkintokoulutukseenValmentavanOpiskeluoikeudenTila(
    opiskeluoikeusjaksot = List(
      tuvaOpiskeluOikeusjakso(date(2021, 8, 1), "lasna"),
      tuvaOpiskeluOikeusjakso(date(2022, 8, 1), "loma")
    )
  )

  lazy val stadinAmmattiopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto, Some(Koodistokoodiviite("10105", None, "oppilaitosnumero", None)), Some("Stadin ammattiopisto"))
  lazy val lähdePrimus = Koodistokoodiviite("primus", Some("Primus"), "lahdejarjestelma", Some(1))
  lazy val opiskeluoikeudenOrganisaatioHistoria = List(
    OpiskeluoikeudenOrganisaatiohistoria(
      muutospäivä = date(2021, 8, 1),
      oppilaitos = Some(Oppilaitos(
        oid = MockOrganisaatiot.stadinAmmattiopisto,
        nimi = Some(Finnish(fi = "Stadin ammatti- ja aikuisopisto"))
      )),
      koulutustoimija = Some(Koulutustoimija(
        oid = MockOrganisaatiot.helsinginKaupunki,
        nimi = Some(Finnish(fi = "Helsingin kaupunki"))
      ))
    )
  )

  lazy val tuvaOpiskeluOikeusValmistunut = TutkintokoulutukseenValmentavanOpiskeluoikeus(
    lähdejärjestelmänId = None,
    oppilaitos = Some(stadinAmmattiopisto),
    koulutustoimija = Some(
      Koulutustoimija(
        oid = MockOrganisaatiot.helsinginKaupunki,
        nimi = Some(Finnish(fi = "Helsingin kaupunki"))
      )
    ),
    tila = tuvaTilaValmistunut,
    järjestämislupa = Koodistokoodiviite("ammatillinen", "tuvajarjestamislupa"),
    lisätiedot = Some(TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot(
      maksuttomuus = Some(
        List(
          Maksuttomuus(
            alku = date(2021, 8, 1),
            loppu = None,
            maksuton = true
          )
        )
      ),
      erityinenTuki = Some(List(Aikajakso(alku = date(2021, 8, 1), loppu = None)))
    )),
    organisaatiohistoria = Some(opiskeluoikeudenOrganisaatioHistoria),
    suoritukset = List(
      tuvaPäätasonSuoritus(laajuus = Some(12)).copy(
        osasuoritukset = Some(
          List(
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
              arviointiPäivä = Some(date(2021, 9, 1)),
              koodistoviite = "tutkintokoulutukseenvalmentava"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaPerustaitojenVahvistaminen(laajuus = Some(1)),
              arviointiPäivä = Some(date(2021, 9, 1)),
              koodistoviite = "tuvaperusopetus"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaAmmatillisenKoulutuksenOpinnot(laajuus = Some(1)),
              arviointiPäivä = Some(date(2021, 10, 1)),
              koodistoviite = "tuvaammatillinenkoulutus"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(1)),
              arviointiPäivä = Some(date(2021, 10, 1)),
              koodistoviite = "tutkintokoulutukseenvalmentava"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(1)),
              arviointiPäivä = Some(date(2021, 11, 1)),
              koodistoviite = "tutkintokoulutukseenvalmentava"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaLukiokoulutuksenOpinnot(laajuus = Some(1)),
              arviointiPäivä = Some(date(2021, 11, 1)),
              koodistoviite = "tuvalukiokoulutus"
            ).copy(
              tunnustettu = Some(
                OsaamisenTunnustaminen(
                  osaaminen = Some(
                    LukioExampleData.kurssisuoritus(
                      LukioExampleData.valtakunnallinenKurssi("ENA1")
                    ).copy(arviointi = LukioExampleData.numeerinenArviointi(8))
                  ),
                  selite = finnish("Tunnustettu lukion kurssi")
                )
              )
            ),
            tuvaKoulutuksenValinnaisenOsanSuoritus(
              laajuus = Some(5),
              arviointiPäivä = Some(date(2021, 12, 1))
            ).copy(
              osasuoritukset = Some(
                List(
                  tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                    kurssinNimi = "Valinnainen kurssi 1",
                    paikallinenKoodi = "VALKU1",
                    paikallisenKoodinNimi = "Paikallinen kurssisuoritus",
                    laajuusViikoissa = 2
                  ),
                  tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                    kurssinNimi = "Valinnainen kurssi 2",
                    paikallinenKoodi = "VALKU2",
                    paikallisenKoodinNimi = "Paikallinen kurssisuoritus",
                    laajuusViikoissa = 3
                  )
                )
              )
            )
          )
        )
      )
    )
  )

  lazy val tuvaOpiskeluOikeusLoma = TutkintokoulutukseenValmentavanOpiskeluoikeus(
    lähdejärjestelmänId = None,
    oppilaitos = Some(stadinAmmattiopisto),
    koulutustoimija = Some(
      Koulutustoimija(
        oid = MockOrganisaatiot.helsinginKaupunki,
        nimi = Some(Finnish(fi = "Helsingin kaupunki"))
      )
    ),
    tila = tuvaTilaLäsnä.copy(opiskeluoikeusjaksot = List(tuvaOpiskeluOikeusjakso(LocalDate.of(2020, 1, 1), "loma"))),
    järjestämislupa = Koodistokoodiviite("lukio", "tuvajarjestamislupa"),
    organisaatiohistoria = Some(opiskeluoikeudenOrganisaatioHistoria),
    suoritukset = List(
      tuvaPäätasonSuoritus(laajuus = Some(12)).copy(
        osasuoritukset = Some(
          List(
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
              arviointiPäivä = Some(date(2021, 9, 1)),
              koodistoviite = "tutkintokoulutukseenvalmentava"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaPerustaitojenVahvistaminen(laajuus = Some(1)),
              arviointiPäivä = Some(date(2021, 9, 1)),
              koodistoviite = "tuvaperusopetus"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaAmmatillisenKoulutuksenOpinnot(laajuus = Some(1)),
              arviointiPäivä = Some(date(2021, 10, 1)),
              koodistoviite = "tuvaammatillinenkoulutus"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(1)),
              arviointiPäivä = Some(date(2021, 10, 1)),
              koodistoviite = "tutkintokoulutukseenvalmentava"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(1)),
              arviointiPäivä = Some(date(2021, 11, 1)),
              koodistoviite = "tutkintokoulutukseenvalmentava"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaLukiokoulutuksenOpinnot(laajuus = Some(1)),
              arviointiPäivä = Some(date(2021, 11, 1)),
              koodistoviite = "tuvalukiokoulutus"
            ).copy(
              tunnustettu = Some(
                OsaamisenTunnustaminen(
                  osaaminen = Some(
                    LukioExampleData.kurssisuoritus(
                      LukioExampleData.valtakunnallinenKurssi("ENA1")
                    ).copy(arviointi = LukioExampleData.numeerinenArviointi(8))
                  ),
                  selite = finnish("Tunnustettu lukion kurssi")
                )
              )
            ),
            tuvaKoulutuksenValinnaisenOsanSuoritus(
              laajuus = Some(5),
              arviointiPäivä = Some(date(2021, 12, 1))
            ).copy(
              osasuoritukset = Some(
                List(
                  tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                    kurssinNimi = "Valinnainen kurssi 1",
                    paikallinenKoodi = "VALKU1",
                    paikallisenKoodinNimi = "Paikallinen kurssisuoritus",
                    laajuusViikoissa = 2
                  ),
                  tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                    kurssinNimi = "Valinnainen kurssi 2",
                    paikallinenKoodi = "VALKU2",
                    paikallisenKoodinNimi = "Paikallinen kurssisuoritus",
                    laajuusViikoissa = 3
                  )
                )
              )
            )
          )
        )
      )
    )
  )

  def tuvaKoulutuksenValinnaisenOsanOsasuoritus(
    kurssinNimi: String,
    paikallinenKoodi: String,
    paikallisenKoodinNimi: String,
    laajuusViikoissa: Int
  ): TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus = {
    TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus(
      koulutusmoduuli = TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus(
        nimi = finnish(kurssinNimi),
        tunniste = PaikallinenKoodi(paikallinenKoodi, finnish(paikallisenKoodinNimi)),
        laajuus = Some(
          LaajuusViikoissa(laajuusViikoissa)
        )
      ),
      arviointi = tuvaSanallinenArviointi(Some(date(2021, 12, 1))),
      tunnustettu = None,
      suorituskieli = Some(suomenKieli)
    )
  }

  lazy val tuvaOpiskeluOikeusEiValmistunut = TutkintokoulutukseenValmentavanOpiskeluoikeus(
    lähdejärjestelmänId = None,
    oppilaitos = Some(stadinAmmattiopisto),
    koulutustoimija = Some(
      Koulutustoimija(
        oid = MockOrganisaatiot.helsinginKaupunki,
        nimi = Some(Finnish(fi = "Helsingin kaupunki"))
      )
    ),
    tila = tuvaTilaLäsnä,
    järjestämislupa = Koodistokoodiviite("perusopetus", "tuvajarjestamislupa"),
    lisätiedot = Some(TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
      maksuttomuus = Some(
        List(
          Maksuttomuus(
            alku = date(2021, 8, 1),
            loppu = None,
            maksuton = true
          )
        )
      ),
      vammainen = Some(List(
        Aikajakso(
          alku = date(2021, 8, 1),
          loppu = None
        )
      )),
      erityisenTuenPäätökset = Some(List(TuvaErityisenTuenPäätös(
        alku = Some(date(2021, 8, 1)),
        loppu = None
      ))),
      majoitusetu = Some(
        Aikajakso(
          alku = date(2021, 9, 1),
          loppu = None
        )
      ),
      kuljetusetu = Some(
        Aikajakso(
          alku = date(2021, 9, 1),
          loppu = None
        )
      ),
      sisäoppilaitosmainenMajoitus = Some(List(
        Aikajakso(
          alku = date(2021, 9, 1),
          loppu = None
        )
      )),
      koulukoti = Some(List(
        Aikajakso(
          alku = date(2021, 9, 1),
          loppu = None
        )
      )),
    )),
    organisaatiohistoria = Some(opiskeluoikeudenOrganisaatioHistoria),
    suoritukset = List(
      tuvaPäätasonSuoritus(laajuus = None).copy(
        vahvistus = None,
        osasuoritukset = Some(
          List(
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = None),
              koodistoviite = "tutkintokoulutukseenvalmentava"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaPerustaitojenVahvistaminen(laajuus = None),
              koodistoviite = "tuvaperusopetus"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = None),
              koodistoviite = "tutkintokoulutukseenvalmentava"
            ),
          )
        )
      )
    )
  )

  val tuvaHenkilöValmis = MockOppijat.asUusiOppija(KoskiSpecificMockOppijat.tuva)
  val tuvaHenkilöEiValmis = MockOppijat.asUusiOppija(KoskiSpecificMockOppijat.tuvaPerus)
  val tuvaHenkilöLoma = MockOppijat.asUusiOppija(KoskiSpecificMockOppijat.tuvaLoma)

  lazy val tuvaOppijaValmistunut = Oppija(
    henkilö = tuvaHenkilöValmis,
    opiskeluoikeudet = List(
      tuvaOpiskeluOikeusValmistunut
    )
  )

  lazy val tuvaOppijaEiValmis = Oppija(
    henkilö = tuvaHenkilöEiValmis,
    opiskeluoikeudet = List(
      tuvaOpiskeluOikeusEiValmistunut
    )
  )

  lazy val examples = List(
    Example("tutkintokoulutukseen valmentava koulutus - valmistunut", "Oppija on suorittanut tutkintokoulutukseen valmentavan koulutuksen.", tuvaOppijaValmistunut),
    Example("tutkintokoulutukseen valmentava koulutus - ei valmistunut", "Oppija on aloittanut tutkintokoulutukseen valmentavan koulutuksen.", tuvaOppijaEiValmis),
  )
}
