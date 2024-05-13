package fi.oph.koski.documentation

import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, MockOppijat}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

import java.time.LocalDate

object ExamplesVapaaSivistystyöKotoutuskoulutus2022 {
  lazy val varsinaisSuomenKansanopisto: Oppilaitos = Oppilaitos(
    MockOrganisaatiot.varsinaisSuomenKansanopisto,
    Some(Koodistokoodiviite("01694", None, "oppilaitosnumero", None)),
    Some("Varsinais-Suomen kansanopisto")
  )

  lazy val varsinaisSuomenKansanopistoToimipiste: OidOrganisaatio =
    OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste)

  lazy val englanninKieli: Koodistokoodiviite =
    Koodistokoodiviite("EN", "kieli")

  def suoritusArviointi(päivä: LocalDate): Option[List[VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022]] =
    Some(List(VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022(päivä = päivä)))

  object Opiskeluoikeus {
    lazy val juuriAloittanut: VapaanSivistystyönOpiskeluoikeus =
      VapaanSivistystyönOpiskeluoikeus(
        oppilaitos = Some(varsinaisSuomenKansanopisto),
        tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
          OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(LocalDate.of(2022, 8, 1), opiskeluoikeusLäsnä)
        )),
        suoritukset = List(PäätasonSuoritus.juuriAloittanut)
      )

    lazy val keskeneräinen: VapaanSivistystyönOpiskeluoikeus =
      juuriAloittanut.copy(suoritukset = List(PäätasonSuoritus.keskeneräinen))

    lazy val suoritettu: VapaanSivistystyönOpiskeluoikeus =
      juuriAloittanut.copy(
        tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
          OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(LocalDate.of(2022, 8, 1), opiskeluoikeusLäsnä),
          OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(LocalDate.of(2023, 5, 1), opiskeluoikeusValmistunut)
        )),
        suoritukset = List(PäätasonSuoritus.suoritettu),
      )

    val opiskeluoikeusHyväksytystiSuoritettu: Koodistokoodiviite =
      Koodistokoodiviite("hyvaksytystisuoritettu", Some("Hyväksytysti suoritettu"), "koskiopiskeluoikeudentila", Some(1))
  }

  object PäätasonSuoritus {
    lazy val juuriAloittanut: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =
      OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
        toimipiste = varsinaisSuomenKansanopistoToimipiste,
        koulutusmoduuli = VSTKotoutumiskoulutus2022(),
        suorituskieli = englanninKieli,
      )

    lazy val keskeneräinen: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =
      juuriAloittanut.copy(
        osasuoritukset = Some(List(
          KieliJaViestintä.keskeneräinen,
          YhteiskuntaJaTyöelämä.keskeneräinen,
          Ohjaus.keskeneräinen,
          Valinnaiset.keskeneräinen,
        ))
      )

    lazy val suoritettu: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =
      juuriAloittanut.copy(
        osasuoritukset = Some(List(
          KieliJaViestintä.suoritettu,
          YhteiskuntaJaTyöelämä.suoritettu,
          Ohjaus.suoritettu,
          Valinnaiset.suoritettu,
        )),
        vahvistus = Some(HenkilövahvistusValinnaisellaPaikkakunnalla(
          päivä = LocalDate.of(2023, 5, 1),
          myöntäjäOrganisaatio = varsinaisSuomenKansanopisto,
          myöntäjäHenkilöt = List(
            Organisaatiohenkilö("Reijo Reksi", LocalizedString.finnish("rehtori"), varsinaisSuomenKansanopisto),
          ),
        )),
      )
  }

  object KieliJaViestintä {
    lazy val tyhjä: VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 =
      VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022()

    lazy val keskeneräinen: VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 =
      VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(
        osasuoritukset = Some(List(
          Osasuoritukset.kuullunYmmärtäminen,
          Osasuoritukset.luetunYmmärtäminen,
        )),
      )

    lazy val suoritettu: VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 =
      VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(
        osasuoritukset = Some(List(
          Osasuoritukset.kuullunYmmärtäminen,
          Osasuoritukset.luetunYmmärtäminen,
          Osasuoritukset.puhuminen,
          Osasuoritukset.kirjoittaminen,
        )),
        arviointi = suoritusArviointi(LocalDate.of(2023, 1, 1))
      )

    object Osasuoritukset {
      lazy val kuullunYmmärtäminen: VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus =
        osasuoritus(
          koulutusmoduuli = koulutusmoduuli("kuullunymmartaminen", 10),
          arviointi = arviointi("A1.1", LocalDate.of(2022, 10, 1)),
        )

      lazy val luetunYmmärtäminen: VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus =
        osasuoritus(
          koulutusmoduuli = koulutusmoduuli("luetunymmartaminen", 10),
          arviointi = arviointi("B1.2", LocalDate.of(2022, 11, 1)),
        )

      lazy val puhuminen: VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus =
        osasuoritus(
          koulutusmoduuli = koulutusmoduuli("puhuminen", 10),
          arviointi = arviointi("A2.2", LocalDate.of(2022, 12, 1)),
        )

      lazy val kirjoittaminen: VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus =
        osasuoritus(
          koulutusmoduuli = koulutusmoduuli("kirjoittaminen", 10),
          arviointi = arviointi("A2.2", LocalDate.of(2023, 1, 1)),
        )

      def osasuoritus(
        koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022,
        arviointi: Option[List[VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi]]
      ): VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus =
        VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus(
          tyyppi = Koodistokoodiviite("vstmaahanmuuttajienkotoutumiskoulutuksenkielitaitojensuoritus", "suorituksentyyppi"),
          koulutusmoduuli = koulutusmoduuli,
          arviointi = arviointi,
        )

      def koulutusmoduuli(tunniste: String, laajuus: Double): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 =
        VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022(
          tunniste = Koodistokoodiviite(tunniste, "vstkoto2022kielijaviestintakoulutus"),
          laajuus = LaajuusOpintopisteissä(laajuus),
        )

      def kielitaso(taso: String): Koodistokoodiviite =
        Koodistokoodiviite(taso, "arviointiasteikkokehittyvankielitaidontasot")

      def arviointi(taso: String, päivä: LocalDate): Option[List[VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi]] =
        Some(List(VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi(kielitaso(taso), Some(päivä))))
    }
  }

  object YhteiskuntaJaTyöelämä {
    lazy val tyhjä: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 =
      VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022()

    lazy val keskeneräinen: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 =
      VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(
        osasuoritukset = Some(List(
          Alaosasuoritukset.yhteiskunnanPerusrakenteet,
          Alaosasuoritukset.yhteyskunnanPeruspalvelut,
          Alaosasuoritukset.ammattiJaKoulutustietous,
          Alaosasuoritukset.työssäoppimisjakso4op,
        ))
      )

    lazy val suoritettu: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 =
      VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(
        osasuoritukset = Some(List(
          Alaosasuoritukset.yhteiskunnanPerusrakenteet,
          Alaosasuoritukset.yhteyskunnanPeruspalvelut,
          Alaosasuoritukset.ammattiJaKoulutustietous,
          Alaosasuoritukset.työelämätietous,
          Alaosasuoritukset.työssäoppimisjakso4op,
          Alaosasuoritukset.työssäoppimisjakso4op,
        )),
        arviointi = suoritusArviointi(LocalDate.of(2023,3,1)),
      )

    object Alaosasuoritukset {
      lazy val yhteiskunnanPerusrakenteet: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
        alaosasuoritus("yhteiskunnanperusrakenteet", 3)
      lazy val yhteyskunnanPeruspalvelut: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
        alaosasuoritus("yhteiskunnanperuspalvelut", 3)
      lazy val ammattiJaKoulutustietous: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
        alaosasuoritus("ammattijakoulutus", 2)
      lazy val työelämätietous: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
        alaosasuoritus("tyoelamatietous", 4)
      lazy val työssäoppimisjakso4op: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
        alaosasuoritus("tyossaoppiminen", 4)

      def alaosasuoritus(tunniste: String, laajuus: Double): VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
        VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus(
          koulutusmoduuli = VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022(
            tunniste = Koodistokoodiviite(tunniste, "vstkoto2022yhteiskuntajatyoosaamiskoulutus"),
            laajuus = LaajuusOpintopisteissä(laajuus),
          ),
        )
    }
  }

  object Ohjaus {
    lazy val tyhjä: VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 =
      VSTKotoutumiskoulutuksenOhjauksenSuoritus2022()

    lazy val keskeneräinen: VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 =
      VSTKotoutumiskoulutuksenOhjauksenSuoritus2022(
        koulutusmoduuli = VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022(laajuus = Some(LaajuusOpintopisteissä(4))),
      )

    lazy val suoritettu: VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 =
      VSTKotoutumiskoulutuksenOhjauksenSuoritus2022(
        koulutusmoduuli = VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022(laajuus = Some(LaajuusOpintopisteissä(7))),
      )
  }

  object Valinnaiset {
    lazy val tyhjä: VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 =
      VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022()

    lazy val keskeneräinen: VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 =
      VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(
        osasuoritukset = osasuoritukset(Seq(("Studiotekniikka", "KOTO0023", 3)))
      )

    lazy val suoritettu: VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 =
      VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(
        koulutusmoduuli = VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022(laajuus = Some(LaajuusOpintopisteissä(7))),
        osasuoritukset = osasuoritukset(Seq(("Studiotekniikka", "KOTO0023", 3), ("Somemarkkinointi", "KOTO0055", 5))),
        arviointi = suoritusArviointi(LocalDate.of(2023, 4, 1)),
      )

    def osasuoritukset(laajuudet: Seq[(String, String, Double)]): Option[List[VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus]] =
      Some(laajuudet.map { case (kuvaus, koodi, laajuus) => osasuoritus(kuvaus, koodi, laajuus) }.toList)

    def osasuoritus(kuvaus: String, koodi: String, laajuus: Double): VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus =
      VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus(
        koulutusmoduuli = VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022(
          kuvaus = finnish(kuvaus),
          tunniste = PaikallinenKoodi(koodi, nimi = finnish(kuvaus)),
          laajuus = LaajuusOpintopisteissä(laajuus),
        ),
      )
  }

  object Examples {
    val vstKoto2022HenkilöValmis: UusiHenkilö = MockOppijat.asUusiOppija(KoskiSpecificMockOppijat.vstKoto2022Suorittanut)
    val vstKoto2022HenkilöKesken: UusiHenkilö = MockOppijat.asUusiOppija(KoskiSpecificMockOppijat.vstKoto2022Kesken)

    lazy val vstKoto2022Valmis: Oppija = Oppija(
      henkilö = vstKoto2022HenkilöValmis,
      opiskeluoikeudet = List(Opiskeluoikeus.suoritettu),
    )

    lazy val vstKoto2022Kesken: Oppija = Oppija(
      henkilö = vstKoto2022HenkilöKesken,
      opiskeluoikeudet = List(Opiskeluoikeus.keskeneräinen),
    )

    lazy val examples = List(
      Example("vst-koto 2022 -koulutus - valmistunut", "Oppija on suorittanut vapaan sivistystyön kotoutumisenkoulutuksen.", vstKoto2022Valmis),
      Example("vst-koto 2022 -koulutus - ei valmistunut", "Oppija on aloittanut vapaan sivistystyön kotoutumisenkoulutuksen.", vstKoto2022Kesken),
    )
  }
}
