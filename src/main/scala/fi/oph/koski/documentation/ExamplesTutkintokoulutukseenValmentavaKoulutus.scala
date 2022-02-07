package fi.oph.koski.documentation

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, MockOppijat}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

import java.time.{LocalDate, LocalDateTime}
import java.time.LocalDate.{of => date}

object ExamplesTutkintokoulutukseenValmentavaKoulutus {

  def tuvaSanallinenArviointi(d: LocalDate): Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = Some(
    List(
      SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
        arvosana = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
        kuvaus = Some(
          finnish("Hyväksytty tutkintokoulutukseen valmentavan koulutuksen osasuoritus.")
        ),
        päivä = d
      )
    )
  )

  def tuvaNumeerinenArviointi(d: LocalDate): Option[List[NumeerinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]] = Some(
    List(
      NumeerinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
        arvosana = Koodistokoodiviite("10", "arviointiasteikkoyleissivistava"),
        kuvaus = None,
        päivä = date(2021, 12, 1)
      )
    )
  )

  def tuvaOpiskeluOikeusjakso(d: LocalDate, koodistokoodiviite: String) = TutkintokoulutukseenValmentavanOpiskeluoikeusjakso(
    alku = d,
    tila = Koodistokoodiviite(koodistokoodiviite, "koskiopiskeluoikeudentila")
  )

  def tuvaKoulutuksenMuunOsanSuoritus(
    koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenMuuOsa,
    arviointiPäivä: LocalDate,
    koodistoviite: String
  ) = TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(
    koulutusmoduuli = koulutusmoduuli,
    arviointi = tuvaSanallinenArviointi(arviointiPäivä),
    suorituskieli = Some(suomenKieli),
    tyyppi = Koodistokoodiviite(koodistoviite, "suorituksentyyppituva"),
    tunnustettu = None
  )

  def tuvaKoulutuksenValinnaisenOsanSuoritus(
    laajuus: Double,
    arviointiPäivä: LocalDate
  ) = TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus(
    koulutusmoduuli = TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa(
      laajuus = Some(LaajuusViikoissa(laajuus))
    ),
    arviointi = tuvaSanallinenArviointi(arviointiPäivä),
    suorituskieli = Some(suomenKieli),
    tyyppi = Koodistokoodiviite("tutkintokoulutukseenvalmentava", "suorituksentyyppituva"),
    tunnustettu = None,
    osasuoritukset = None
  )

  def tuvaOpiskeluJaUrasuunnittelutaidot(laajuus: Double) = TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot(
    laajuus = Some(LaajuusViikoissa(laajuus))
  )

  def tuvaPerustaitojenVahvistaminen(laajuus: Double) = TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen(
    laajuus = Some(LaajuusViikoissa(laajuus))
  )

  def tuvaAmmatillisenKoulutuksenOpinnot(laajuus: Double) = TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot(
    laajuus = Some(LaajuusViikoissa(laajuus))
  )

  def tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus: Double) = TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(
    laajuus = Some(LaajuusViikoissa(laajuus))
  )

  def tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus: Double) = TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot(
    laajuus = Some(LaajuusViikoissa(laajuus))
  )

  def tuvaLukiokoulutuksenOpinnot(laajuus: Double) = TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot(
    laajuus = Some(LaajuusViikoissa(laajuus))
  )

  lazy val tuvaTila = TutkintokoulutukseenValmentavanOpiskeluoikeudenTila(
    opiskeluoikeusjaksot = List(
      tuvaOpiskeluOikeusjakso(date(2021, 8, 1), "lasna"),
      tuvaOpiskeluOikeusjakso(date(2021, 12, 31), "valmistunut")
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
    tila = tuvaTila,
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
      )
    )),
    organisaatiohistoria = Some(opiskeluoikeudenOrganisaatioHistoria),
    suoritukset = List(
      TutkintokoulutukseenValmentavanKoulutuksenSuoritus(
        toimipiste = stadinAmmattiopisto,
        koulutusmoduuli = TutkintokoulutukseenValmentavanKoulutus(
          laajuus = Some(LaajuusViikoissa(12))
        ),
        vahvistus = Some(
          HenkilövahvistusValinnaisellaPaikkakunnalla(
            päivä = date(2021, 12, 31),
            paikkakunta = Some(
              ExampleData.helsinki
            ),
            myöntäjäOrganisaatio = stadinAmmattiopisto,
            myöntäjäHenkilöt = List(
              Organisaatiohenkilö("Reijo Reksi", "rehtori", stadinAmmattiopisto)
            )
          )
        ),
        suorituskieli = suomenKieli,
        osasuoritukset = Some(
          List(
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(2),
              arviointiPäivä = date(2021, 9, 1),
              koodistoviite = "tutkintokoulutukseenvalmentava"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaPerustaitojenVahvistaminen(1),
              arviointiPäivä = date(2021, 9, 1),
              koodistoviite = "perusopetus"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaAmmatillisenKoulutuksenOpinnot(1),
              arviointiPäivä = date(2021, 10, 1),
              koodistoviite = "ammatillinenkoulutus"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(1),
              arviointiPäivä = date(2021, 10, 1),
              koodistoviite = "tutkintokoulutukseenvalmentava"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(1),
              arviointiPäivä = date(2021, 11, 1),
              koodistoviite = "tutkintokoulutukseenvalmentava"
            ),
            tuvaKoulutuksenMuunOsanSuoritus(
              koulutusmoduuli = tuvaLukiokoulutuksenOpinnot(1),
              arviointiPäivä = date(2021, 11, 1),
              koodistoviite = "lukiokoulutus"
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
              laajuus = 5,
              arviointiPäivä = date(2021, 12, 1)
            ).copy(
              osasuoritukset = Some(
                List(
                  TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus(
                    koulutusmoduuli = TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus(
                      nimi = finnish("Valinnainen kurssi 1"),
                      tunniste = PaikallinenKoodi("VALKU1", finnish("Paikallinen kurssisuoritus")),
                      laajuus = Some(
                        LaajuusViikoissa(2)
                      )
                    ),
                    arviointi = tuvaNumeerinenArviointi(date(2021, 12, 1)),
                    tunnustettu = None,
                    suorituskieli = Some(suomenKieli)
                  ),
                  TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus(
                    koulutusmoduuli = TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus(
                      nimi = finnish("Valinnainen kurssi 2"),
                      tunniste = PaikallinenKoodi("VALKU2", finnish("Paikallinen kurssisuoritus")),
                      laajuus = Some(
                        LaajuusViikoissa(3)
                      )
                    ),
                    arviointi = tuvaNumeerinenArviointi(date(2021, 12, 1)),
                    tunnustettu = None,
                    suorituskieli = Some(suomenKieli)
                  )
                )
              )
            )
          )
        )
      )
    )
  )

  lazy val tuvaOppijaValmistunut = Oppija(
    MockOppijat.asUusiOppija(KoskiSpecificMockOppijat.tuva),
    List(
      tuvaOpiskeluOikeusValmistunut
    )
  )

  lazy val examples = List(
    Example("tutkintokoulutukseen valmentava koulutus - valmistunut", "Oppija on suorittanut tutkintokoulutukseen valmentavan koulutuksen.", tuvaOppijaValmistunut)
  )
}
