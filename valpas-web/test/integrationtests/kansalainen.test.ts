import { kansalainenOmatTiedotPath } from "../../src/state/kansalainenPaths"
import { contentEventuallyEquals } from "../integrationtests-env/browser/content"
import { $$ } from "../integrationtests-env/browser/core"
import { dropdownSelectContains } from "../integrationtests-env/browser/forms"
import { loginKansalainenAs } from "../integrationtests-env/browser/resetKansalainen"
import { getIlmoitusData } from "./kuntailmoitus.shared"
import {
  hautEquals,
  historiaEiOpiskeluhistoriaa,
  historiaOpintoOikeus,
  historiaOppivelvollisuudenKeskeytys,
  historiaOppivelvollisuudenKeskeytysToistaiseksi,
  historiaVastuuilmoitus,
  ilmoitetutYhteystiedot,
  ilmoitetutYhteystiedotEquals,
  merge,
  opiskeluhistoriaEquals,
  oppivelvollisuustiedot,
  oppivelvollisuustiedotEquals,
  turvakieltoVaroitusEquals,
  virallisetYhteystiedot,
  virallisetYhteystiedotEquals,
} from "./oppija.shared"

const omatTiedotPath = kansalainenOmatTiedotPath.href()

const hetut = {
  huoltaja: "240470-621T",
  huollettavaOppivelvollinen: "221105A3023",
  huollettavaTurvakielto: "290904A4030",
  huollettavaEiTietoja: "060488-681S",
  eiKoskessaOppivelvollinen: "240105A7049",
  eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksia: "260705A1119",
}

describe("Kansalaisen näkymä", () => {
  it("Näyttää oppijan omat tiedot", async () => {
    await loginKansalainenAs(
      omatTiedotPath,
      hetut.huollettavaOppivelvollinen,
      true
    )
    await expectHuollettavaOppivelvollinenKaikkiTiedot()
  })

  it("Näyttää vain oppijanumerorekisterissä olevan kansalaisen omat Valpas-tiedot, jos niitä on tallennettu", async () => {
    await loginKansalainenAs(
      omatTiedotPath,
      hetut.eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksia
    )
    await expectEiKoskessaOppivelvollinenKeskeytyksiäJaIlmoituksiaKaikkiTiedot()
  })

  it("Näyttää vain oppijanumerorekisterissä olevan kansalaisen omat Valpas-tiedot, jos niitä on tallennettu, vaikka olisi ohitettu vuosi, jolloin kansalainen täyttää 25 vuotta", async () => {
    await loginKansalainenAs(
      omatTiedotPath,
      hetut.eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksia,
      false,
      "2031-01-01"
    )
    await expectEiKoskessaOppivelvollinenKeskeytyksiäJaIlmoituksiaKaikkiTiedot()
  })

  it("Näyttää vain oppijanumerorekisterissä olevan kansalaisen tiedot", async () => {
    await loginKansalainenAs(omatTiedotPath, hetut.eiKoskessaOppivelvollinen)
    await expectEiKoskessaOppivelvollinenKaikkiTiedot()
  })

  it("Näyttää vain oppijanumerorekisterissä olevan kansalaisen tiedot, vaikka olisi yli 18-vuotias", async () => {
    await loginKansalainenAs(
      omatTiedotPath,
      hetut.eiKoskessaOppivelvollinen,
      false,
      "2024-01-01"
    )
    await expectEiKoskessaOppivelvollinenKaikkiTiedot()
  })

  it("Ei näytä vain ONR:ssä olevan kansalaisen tietoja, jos on ohitettu vuosi, jolloin kansalainen täyttää 25 vuotta", async () => {
    await loginKansalainenAs(
      omatTiedotPath,
      hetut.eiKoskessaOppivelvollinen,
      false,
      "2031-01-01"
    )
    await expectNotValpasOppija()
  })

  it("Näyttää turvakiellolliselle henkilölle hänen kaikki tietonsa", async () => {
    await loginKansalainenAs(omatTiedotPath, hetut.huollettavaTurvakielto)
    await expectHuollettavaTurvakieltoPerustiedot()
    await expectHuollettavaTurvakieltoTäysiOpiskeluhistoria()
    await expectOmaTurvakiellollinenKuntailmoitus()
  })

  it("Näyttää tiedon siitä, että henkilöstä ei löydy tietoja Valppaasta", async () => {
    await loginKansalainenAs(omatTiedotPath, hetut.huoltaja)
    await expectNotValpasOppija()
  })

  it("Huoltaja voi katsoa huollettavansa tietoja", async () => {
    await loginKansalainenAs(omatTiedotPath, hetut.huoltaja)
    await selectOppija(hetut.huollettavaOppivelvollinen)
    await expectHuollettavaOppivelvollinenKaikkiTiedot()
  })

  it("Huoltaja näkee turvakiellollisesta huollettavastaan tiedot vain osittain", async () => {
    await loginKansalainenAs(omatTiedotPath, hetut.huoltaja)
    await selectOppija(hetut.huollettavaTurvakielto)
    await expectHuollettavaTurvakieltoPerustiedot()
    await expectHuollettavaTurvakieltoRajattuOpiskeluhistoria()
    await expectHuollettavanTurvakiellollinenKuntailmoitus()
  })

  it("Näyttää huollettavasta tiedon siitä, ettei löydy tietoja Valppaasta", async () => {
    await loginKansalainenAs(omatTiedotPath, hetut.huoltaja)
    await selectOppija(hetut.huollettavaEiTietoja)
    await expectNotValpasOppija()
  })
})

// ---

const oppijaHeaderEquals = (nimi: string, syntymäpäivä: string) =>
  contentEventuallyEquals(
    ".omattiedot__header",
    [nimi, `s. ${syntymäpäivä}`].join("\n")
  )

const expectNotValpasOppija = () =>
  contentEventuallyEquals(
    ".card__header",
    "Henkilöstä ei ole tietoja Valpas-palvelussa. Valpas-palvelussa ovat nähtävissä tiedot vain henkilöistä, jotka ovat olleet oppivelvollisia 1.1.2021 tai sen jälkeen. Tietoja ei ole enää saatavilla, kun henkilön oikeus maksuttomaan koulutukseen on päättynyt yli viisi vuotta sitten."
  )

const selectOppija = (hetu: string) =>
  dropdownSelectContains("#oppija-select", hetu)

const expectHuollettavaOppivelvollinenKaikkiTiedot = async () => {
  await oppijaHeaderEquals(
    "Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas",
    "22.11.2005"
  )

  await oppivelvollisuustiedotEquals(
    oppivelvollisuustiedot({
      opiskelutilanne: "Opiskelemassa",
      oppivelvollisuus: "21.11.2023 asti",
      maksuttomuusoikeus: "31.12.2025 asti",
    })
  )

  await ilmoitetutYhteystiedotEquals(
    ilmoitetutYhteystiedot({
      pvm: "9.4.2020",
      lähiosoite: "Esimerkkikatu 123",
      postitoimipaikka: "99999 Helsinki",
      matkapuhelin: "0401234567",
      sähköposti:
        "Valpas.Oppivelvollinen-ysiluokka-kesken-keväällä-2021@gmail.com",
      lähde: "Hakulomake – Yhteishaku 2021",
    })
  )

  await virallisetYhteystiedotEquals(
    virallisetYhteystiedot({
      lähiosoite: "Esimerkkitie 10",
      postitoimipaikka: "99999 Helsinki",
      maa: "Costa rica",
      puhelin: "0401122334",
      sähköposti: "valpas@gmail.com",
    })
  )

  await opiskeluhistoriaEquals(
    merge(
      historiaOpintoOikeus({
        otsikko: "Perusopetus 2012 –",
        tila: "Läsnä",
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "9C",
        alkamispäivä: "15.8.2012",
      }),
      historiaOpintoOikeus({
        otsikko: "Esiopetuksen suoritus 2010 – 2015",
        tila: "Valmistunut",
        toimipiste: "Jyväskylän normaalikoulu",
        alkamispäivä: "13.8.2010",
        päättymispäivä: "1.1.2015",
      })
    )
  )

  await hautEquals(`
    list_alt
    Yhteishaku 2021 Hakenut open_in_new
      Hakukohde
      Valinta
      Pisteet
      Alin pistemäärä
      1. Ressun lukio, Lukio Hylätty 9,00 9,01
      2. Helsingin medialukio, Lukio
      Otettu vastaan
      Hyväksytty 9,00 8,20
      3. Omnia, Leipomoala Peruuntunut – –
      4. Omnia, Puhtaus- ja kiinteistöpalveluala Peruuntunut – –
      5. Varsinais-Suomen kansanopisto, Vapaan sivistystyön koulutus oppivelvollisille 2021-2022 Peruuntunut – –
  `)
}

const expectEiKoskessaOppivelvollinenKaikkiTiedot = async () => {
  await oppijaHeaderEquals("Kosketon Valpas", "24.1.2005")

  await oppivelvollisuustiedotEquals(
    oppivelvollisuustiedot({
      opiskelutilanne: "Ei opiskelupaikkaa",
      oppivelvollisuus: "23.1.2023 asti",
      maksuttomuusoikeus: "31.12.2025 asti",
    })
  )

  await virallisetYhteystiedotEquals(
    virallisetYhteystiedot({
      lähiosoite: "Esimerkkitie 10",
      postitoimipaikka: "99999 Helsinki",
      maa: "Costa rica",
      puhelin: "0401122334",
      sähköposti: "valpas@gmail.com",
    })
  )

  await opiskeluhistoriaEquals(merge(historiaEiOpiskeluhistoriaa()))

  await hautEquals(`
    list_alt
    Yhteishaku 2021 Hakenut open_in_new
      Hakukohde
      Valinta
      Pisteet
      Alin pistemäärä
      1. Ressun lukio, Lukio 3. varasija 9,00 8,99
  `)
}

const expectEiKoskessaOppivelvollinenKeskeytyksiäJaIlmoituksiaKaikkiTiedot =
  async () => {
    await oppijaHeaderEquals(
      "Kosketon-keskeytyksiä-ilmoituksia Valpas",
      "26.7.2005"
    )

    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei opiskelupaikkaa",
        oppivelvollisuus: "Keskeytetty toistaiseksi 1.9.2021 alkaen",
        oppivelvollisuudenKeskeytykset: ["1.1.2019 – 1.12.2019"],
        maksuttomuusoikeus: "31.12.2025 asti",
      })
    )

    await ilmoitetutYhteystiedotEquals(
      ilmoitetutYhteystiedot({
        pvm: "9.4.2020",
        lähiosoite: "Esimerkkikatu 123",
        postitoimipaikka: "99999 Helsinki",
        matkapuhelin: "0401234567",
        sähköposti: "Valpas.Kosketon-keskeytyksiä-ilmoituksia@gmail.com",
        lähde: "Hakulomake – Yhteishaku 2021",
      })
    )

    await virallisetYhteystiedotEquals(
      virallisetYhteystiedot({
        lähiosoite: "Esimerkkitie 10",
        postitoimipaikka: "99999 Helsinki",
        maa: "Costa rica",
        puhelin: "0401122334",
        sähköposti: "valpas@gmail.com",
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaEiOpiskeluhistoriaa(),
        historiaOppivelvollisuudenKeskeytysToistaiseksi("1.9.2021"),
        historiaVastuuilmoitus({
          päivämäärä: "15.8.2021",
          ilmoittaja: "Jyväskylän normaalikoulu",
          tahoJolleIlmoitettu: "Pyhtää",
        }),
        historiaVastuuilmoitus({
          päivämäärä: "8.4.2021",
          ilmoittaja: "Jyväskylän normaalikoulu",
          tahoJolleIlmoitettu: "Helsinki",
        }),
        historiaOppivelvollisuudenKeskeytys("1.1.2019 – 1.12.2019")
      )
    )

    await hautEquals(`
    list_alt
    Yhteishaku 2021 Hakenut open_in_new
      Hakukohde
      Valinta
      Pisteet
      Alin pistemäärä
      1. Ressun lukio, Lukio 3. varasija 9,00 8,99
  `)
  }

const expectHuollettavaTurvakieltoPerustiedot = async () => {
  await oppijaHeaderEquals("Turvakielto Valpas", "29.9.2004")

  await oppivelvollisuustiedotEquals(
    oppivelvollisuustiedot({
      opiskelutilanne: "Opiskelemassa",
      oppivelvollisuus: "28.9.2022 asti",
      maksuttomuusoikeus: "31.12.2024 asti",
    })
  )

  await turvakieltoVaroitusEquals(`
    warning
    Tietoja ei saatavilla, henkilöllä on turvakielto.
  `)

  await virallisetYhteystiedotEquals(`
    Viralliset yhteystiedot
    Henkilöllä on turvakielto
  `)
}

const expectHuollettavaTurvakieltoRajattuOpiskeluhistoria = async () => {
  await hautEquals("Oppijalle ei löytynyt hakuhistoriaa")

  await opiskeluhistoriaEquals(
    merge(
      historiaVastuuilmoitus({
        päivämäärä: "5.9.2021",
      }),
      historiaOpintoOikeus({
        otsikko: "Opiskeluoikeus 2012 –",
        tila: "Läsnä",
        alkamispäivä: "15.8.2012",
      })
    )
  )
}

const expectHuollettavaTurvakieltoTäysiOpiskeluhistoria = async () => {
  await hautEquals(`
    list_alt
    Yhteishaku 2021 Hakenut open_in_new
    Hakukohde
    Valinta
    Pisteet
    Alin pistemäärä
    1. Ressun lukio, Lukio Hylätty 9,00 9,01
  `)

  await opiskeluhistoriaEquals(
    merge(
      historiaVastuuilmoitus({
        päivämäärä: "5.9.2021",
        ilmoittaja: "Jyväskylän normaalikoulu",
        tahoJolleIlmoitettu: "Pyhtää",
      }),
      historiaOpintoOikeus({
        otsikko: "Perusopetus 2012 –",
        tila: "Läsnä",
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "9C",
        alkamispäivä: "15.8.2012",
      })
    )
  )
}

const expectOmaTurvakiellollinenKuntailmoitus = async () => {
  expect(await getIlmoitusData()).toEqual({
    email: "Veijo.Valpas@gmail.com",
    kohde: "Pyhtää",
    lähiosoite: "Esimerkkikatu 123",
    maa: "Suomi",
    muuHaku: "Ei",
    postitoimipaikka: "99999 Pyhtää",
    puhelin: "0401234567",
    tekijä: "Jyväskylän normaalikoulu",
  })
}

const expectHuollettavanTurvakiellollinenKuntailmoitus = async () => {
  const ilmoitukset = await $$(".kuntailmoitus__body")
  expect(
    ilmoitukset.length,
    "Aktiivisia ilmoituksia tulisi näkyä tasan yksi"
  ).toEqual(1)
  const ilmoitus = ilmoitukset[0]!!

  expect(await ilmoitus.getText()).toEqual(
    "Tarkemmat tiedot näkyvät ainoastaan ilmoituksen vastaanottajalle ja tekijälle"
  )
}
