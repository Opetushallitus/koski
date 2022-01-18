import { oppijaPath } from "../../src/state/paths"
import { formatDate, today } from "../../src/utils/date"
import {
  clickElement,
  contentEventuallyEquals,
  expectElementNotVisible,
  textEventuallyEquals,
} from "../integrationtests-env/browser/content"
import { $$, goToLocation } from "../integrationtests-env/browser/core"
import {
  allowNetworkError,
  FORBIDDEN,
} from "../integrationtests-env/browser/fail-on-console"
import { loginAs, resetMockData } from "../integrationtests-env/browser/reset"

const ysiluokkaKeskenKeväälläPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000001",
})
const päällekkäisiäOppivelvollisuuksiaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000003",
})
const ysiluokkaValmisKeväälläPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000011",
})
const lukionAloittanutPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000015",
})
const lukionLokakuussaAloittanutPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000016",
})
const kahdellaOppijaOidillaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000017",
})
const turvakiellollinenOppijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000024",
})
const epäonninenOppijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000028",
})
const montaHakuaJoistaYksiPäättynytOppijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000009",
})
const lukionAineopinnotAloittanutPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000033",
})
const lukioOpiskelijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000004",
})
const vsopPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000046",
})
const oppivelvollisuusKeskeytettyMääräajaksiPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000056",
  }
)
const oppivelvollisuusKeskeytettyToistaiseksiPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000057",
  }
)
const eiOppivelvollisuudenSuorittamiseenKelpaaviaOpiskeluoikeuksiaPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000058",
  }
)
const hetutonPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000059",
})

const opiskeluoikeusKeskeytettyMääräajaksiPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000077",
  }
)

const opiskeluoikeusLomaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000078",
})

const opiskeluoikeusValmaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000062",
})

const opiskeluoikeusIntSchoolPerusopetusPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000094",
})

const maksuttomuuttaPidennettyPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000127",
})

const perusopetukseenValmistautuvaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000131",
})

const mainHeadingEquals = (expected: string) =>
  textEventuallyEquals("h1.heading--primary", expected)
const secondaryHeadingEquals = (expected: string) =>
  textEventuallyEquals(".oppijaview__secondaryheading", expected)
const cardBodyEquals = (id: string, innerSelector?: string) => (
  expected: string
) =>
  contentEventuallyEquals(
    `#${id} .card__body ${innerSelector || ""}`.trim(),
    expected
  )
const oppivelvollisuustiedotEquals = cardBodyEquals(
  "oppivelvollisuustiedot",
  ".infotable"
)
const opiskeluhistoriaEquals = cardBodyEquals("opiskeluhistoria")
const hautEquals = cardBodyEquals("haut")
const ilmoitetutYhteystiedotEquals = (expected: string) =>
  contentEventuallyEquals("#ilmoitetut-yhteystiedot", expected)
const virallisetYhteystiedotEquals = (expected: string) =>
  contentEventuallyEquals("#viralliset-yhteystiedot", expected)
const turvakieltoVaroitusEquals = (expected: string) =>
  contentEventuallyEquals("#turvakielto-varoitus", expected)
const turvakieltoVaroitusNotVisible = () =>
  expectElementNotVisible("#turvakielto-varoitus")
const expectEiKuntailmoituksiaNotVisible = () =>
  expectElementNotVisible(".oppijaview__eiilmoituksia")

const rivi = (label: string, value?: string) =>
  value ? [label + ":", value] : []

const oppivelvollisuustiedot = (p: {
  opiskelutilanne: string
  oppivelvollisuus: string
  oppivelvollisuudenKeskeytykset?: string[]
  maksuttomuusoikeus: string
  kuntailmoitusBtn?: true
  oppivelvollisuudenKeskeytysBtn?: true
}) =>
  [
    ...rivi("Opiskelutilanne", p.opiskelutilanne),
    ...rivi("Oppivelvollisuus", p.oppivelvollisuus),
    ...(p.oppivelvollisuudenKeskeytykset || []).map((d) => `Keskeytetty ${d}`),
    ...rivi("Oikeus opintojen maksuttomuuteen", p.maksuttomuusoikeus),
    ...(p.oppivelvollisuudenKeskeytysBtn ? ["Keskeytä oppivelvollisuus"] : []),
    ...(p.kuntailmoitusBtn
      ? ["Tee ilmoitus valvontavastuusta", "info_outline"]
      : []),
  ].join("\n")

const historiaOpintoikeus = (p: {
  otsikko: string
  tila: string
  maksuttomuus?: string[]
  toimipiste: string
  ryhmä?: string
  vuosiluokkiinSitomatonOpetus?: boolean
  alkamispäivä: string
  päättymispäivä?: string
  perusopetusSuoritettu?: string
}) =>
  [
    "school",
    p.otsikko,
    ...rivi("Tila", p.tila),
    ...rivi("Maksuttomuus", p.maksuttomuus?.join("\n")),
    ...rivi("Toimipiste", p.toimipiste),
    ...rivi("Ryhmä", p.ryhmä),
    ...rivi(
      "Muuta",
      p.vuosiluokkiinSitomatonOpetus
        ? "Vuosiluokkiin sitomaton opetus"
        : undefined
    ),
    ...rivi("Opiskeluoikeuden alkamispäivä", p.alkamispäivä),
    ...rivi("Opiskeluoikeuden päättymispäivä", p.päättymispäivä),
    ...rivi("Perusopetus suoritettu", p.perusopetusSuoritettu),
  ].join("\n")

const historiaOppivelvollisuudenKeskeytys = (range: string) =>
  ["schedule", "Oppivelvollisuus", `Keskeytetty ${range}`].join("\n")

const historiaOppivelvollisuudenKeskeytysToistaiseksi = (
  alkamispäivä: string
) =>
  [
    "schedule",
    "Oppivelvollisuus",
    `Keskeytetty toistaiseksi ${alkamispäivä} alkaen`,
  ].join("\n")

const ilmoitetutYhteystiedot = (p: {
  pvm: string
  lähiosoite?: string
  postitoimipaikka?: string
  maa?: string
  matkapuhelin?: string
  sähköposti?: string
  lähde?: string
}) =>
  [
    "Ilmoitetut yhteystiedot",
    `keyboard_arrow_rightYhteystiedot – ${p.pvm}`,
    ...rivi("Lähiosoite", p.lähiosoite),
    ...rivi("Postitoimipaikka", p.postitoimipaikka),
    ...rivi("Maa", p.maa),
    ...rivi("Matkapuhelin", p.matkapuhelin),
    ...rivi("Sähköposti", p.sähköposti),
    ...(p.lähde ? [`Lähde: ${p.lähde}`] : []),
  ].join("\n")

const virallisetYhteystiedot = (p: {
  lähiosoite?: string
  postitoimipaikka?: string
  maa?: string
  puhelin?: string
  sähköposti?: string
}) =>
  [
    "Viralliset yhteystiedot",
    "keyboard_arrow_rightVTJ: Kotiosoite",
    ...rivi("Lähiosoite", p.lähiosoite),
    ...rivi("Postitoimipaikka", p.postitoimipaikka),
    ...rivi("Maa", p.maa),
    ...rivi("Puhelin", p.puhelin),
    ...rivi("Sähköposti", p.sähköposti),
  ].join("\n")

const merge = (...items: string[]) => items.join("\n")

describe("Oppijakohtainen näkymä", () => {
  it("Näyttää oppijan tiedot, johon käyttäjällä on lukuoikeus", async () => {
    await loginAs(ysiluokkaKeskenKeväälläPath, "valpas-jkl-normaali", true)
    await mainHeadingEquals(
      "Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas (221105A3023)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000001")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "21.11.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
        }),
        historiaOpintoikeus({
          otsikko: "Esiopetuksen suoritus 2010 – 2011",
          tila: "Valmistunut",
          toimipiste: "Jyväskylän normaalikoulu",
          alkamispäivä: "13.8.2010",
          päättymispäivä: "3.6.2011",
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
  })

  it("Näyttää oppijan, jolla monta hakua, kaikki haut, myös päättyneet", async () => {
    await loginAs(
      montaHakuaJoistaYksiPäättynytOppijaPath,
      "valpas-jkl-normaali"
    )
    await mainHeadingEquals(
      "LuokallejäänytYsiluokkalainen Valpas (020805A5625)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000009")
    await hautEquals(`
      list_alt
      Yhteishaku 2021 Hakenut open_in_new
      Hakukohde
      Valinta
      Pisteet
      Alin pistemäärä
      1. Helsingin medialukio, Lukio – – –
      list_alt
      Yhteishaku 2021 Hakenut open_in_new
      Hakukohde
      Valinta
      Pisteet
      Alin pistemäärä
      1. Varsinais-Suomen kansanopisto, Vapaan sivistystyön koulutus oppivelvollisille 2021-2022 – – –
      list_alt
      Päättynyt: Yhteishaku 2019 Hakenut open_in_new
      Hakukohde
      Valinta
      Pisteet
      Alin pistemäärä
      1. Varsinais-Suomen kansanopisto, Vapaan sivistystyön koulutus oppivelvollisille 2019-2020 – – –
    `)
  })

  it("Näyttää oppijan tiedot valmistuneelle ysiluokkalaiselle", async () => {
    await loginAs(ysiluokkaValmisKeväälläPath, "valpas-jkl-normaali")
    await mainHeadingEquals(
      "Ysiluokka-valmis-keväällä-2021 Valpas (190605A006K)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000011")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei opiskelupaikkaa",
        oppivelvollisuus: "18.6.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoikeus({
          otsikko: "Perusopetus 2012 – 2021",
          tila: "Valmistunut",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
          päättymispäivä: "30.5.2021",
        })
      )
    )
  })

  it("Näyttää oppijan tiedot int schoolissa olevalle ysiluokkalaiselle", async () => {
    await loginAs(opiskeluoikeusIntSchoolPerusopetusPath, "valpas-jkl-normaali")
    await mainHeadingEquals(
      "Int-school-9-luokan-jälkeen-lukion-aloittanut Valpas (120505A3434)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000094")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "11.5.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoikeus({
          otsikko: "Lukion oppimäärä 2021 –",
          tila: "Läsnä",
          maksuttomuus: [
            "15.8.2021–16.8.2021 maksuton",
            "17.8.2021–18.8.2021 maksullinen",
            "19.8.2021– maksuton",
          ],
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "AH",
          alkamispäivä: "15.8.2021",
        }),
        historiaOpintoikeus({
          otsikko: "International school 2004 –",
          tila: "Läsnä",
          maksuttomuus: ["Ei"],
          toimipiste: "International School of Helsinki",
          ryhmä: "9B",
          alkamispäivä: "15.8.2004",
          perusopetusSuoritettu: "30.5.2021",
        })
      )
    )
  })

  it("Näyttää oppijan vsop-tiedon", async () => {
    await loginAs(vsopPath, "valpas-jkl-normaali", true)
    await mainHeadingEquals(
      "Ysiluokka-valmis-keväällä-2021-vsop Valpas (190705A575R)"
    )
    await opiskeluhistoriaEquals(
      historiaOpintoikeus({
        otsikko: "Perusopetus 2012 – 2021",
        tila: "Valmistunut",
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "9C",
        vuosiluokkiinSitomatonOpetus: true,
        alkamispäivä: "15.8.2012",
        päättymispäivä: "30.5.2021",
      })
    )
  })

  it("Näyttää oppijan muut tiedot vaikka hakukoostekysely epäonnistuu", async () => {
    await loginAs(epäonninenOppijaPath, "valpas-jkl-normaali")
    await mainHeadingEquals("Epäonninen Valpas (301005A336J)")
    await hautEquals("Virhe oppijan hakuhistorian hakemisessa")
  })

  it("Ei näytä oppijan tietoja, johon käyttäjällä ei ole lukuoikeutta", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(ysiluokkaKeskenKeväälläPath, "valpas-pelkkä-suorittaminen")

    await mainHeadingEquals("Oppijan tiedot")
    await secondaryHeadingEquals(
      "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000001"
    )
    await expectEiKuntailmoituksiaNotVisible()
  })

  it("Ei näytä oppijan tietoja, johon käyttäjällä ei ole lukuoikeutta vaihdetun tarkastelupäivän jälkeen", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(ysiluokkaValmisKeväälläPath, "valpas-jkl-normaali-perus")
    await mainHeadingEquals(
      "Ysiluokka-valmis-keväällä-2021 Valpas (190605A006K)"
    )
    await resetMockData("2021-10-05")
    await goToLocation(ysiluokkaValmisKeväälläPath)

    await mainHeadingEquals("Oppijan tiedot")
    await secondaryHeadingEquals(
      "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000011"
    )
    await expectEiKuntailmoituksiaNotVisible()
  })

  it("Näyttää oppijalta, jolla on useampia päällekäisiä opiskeluoikeuksia kaikki opiskeluoikeudet", async () => {
    await loginAs(päällekkäisiäOppivelvollisuuksiaPath, "valpas-jkl-normaali")
    await mainHeadingEquals("Päällekkäisiä Oppivelvollisuuksia (060605A083N)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000003")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "5.6.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9B",
          alkamispäivä: "15.8.2012",
        }),
        historiaOpintoikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Kulosaaren ala-aste",
          ryhmä: "8A",
          alkamispäivä: "15.8.2012",
        })
      )
    )
  })

  it("Näyttää oppijalta, jolla on useampia peräkkäisiä opiskeluoikeuksia kaikki opiskeluoikeudet", async () => {
    await loginAs(lukionAloittanutPath, "valpas-jkl-normaali")
    await mainHeadingEquals("LukionAloittanut Valpas (290405A871A)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000015")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "28.4.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoikeus({
          otsikko: "Lukion oppimäärä 2021 –",
          tila: "Läsnä",
          maksuttomuus: [
            "15.8.2021–16.8.2021 maksuton",
            "17.8.2021–18.8.2021 maksullinen",
            "19.8.2021– maksuton",
          ],
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "AH",
          alkamispäivä: "15.8.2021",
        }),
        historiaOpintoikeus({
          otsikko: "Perusopetus 2012 – 2021",
          tila: "Valmistunut",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
          päättymispäivä: "30.5.2021",
        })
      )
    )
  })

  it("Näyttää oppijalta, jolla opiskeluoikeus alkaa tulevaisuudessa oikeat tiedot", async () => {
    await loginAs(lukionLokakuussaAloittanutPath, "valpas-jkl-normaali")
    await mainHeadingEquals("LukionLokakuussaAloittanut Valpas (180405A819J)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000016")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei opiskelupaikkaa",
        oppivelvollisuus: "17.4.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoikeus({
          otsikko: "Lukion oppimäärä 2021 –",
          tila: "Opiskeluoikeus alkaa 3.10.2021",
          maksuttomuus: ["3.10.2021– maksuton"],
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "AH",
          alkamispäivä: "3.10.2021",
        }),
        historiaOpintoikeus({
          otsikko: "Perusopetus 2012 – 2021",
          tila: "Valmistunut",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
          päättymispäivä: "30.5.2021",
        })
      )
    )
  })

  it("Näyttää oppijan yhteystiedot ilman turvakieltovaroitusta", async () => {
    await loginAs(ysiluokkaKeskenKeväälläPath, "valpas-jkl-normaali")

    const pvm = "9.4.2020"
    await ilmoitetutYhteystiedotEquals(
      ilmoitetutYhteystiedot({
        pvm,
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

    // Klikkaukset kääntävät näkyvät ja piilotetut arvot päinvastaiseen tilaan
    const labels = await $$("#yhteystiedot .accordion__label")
    await Promise.all(labels.map((label) => label.click()))

    await ilmoitetutYhteystiedotEquals(ilmoitetutYhteystiedot({ pvm }))

    await turvakieltoVaroitusNotVisible()
  })

  it("Näyttää oppijalta uusimman muokatun hakemuksen yhteystiedot", async () => {
    await loginAs(
      montaHakuaJoistaYksiPäättynytOppijaPath,
      "valpas-jkl-normaali"
    )

    await ilmoitetutYhteystiedotEquals(
      ilmoitetutYhteystiedot({
        pvm: "10.4.2020",
        lähiosoite: "Uudempi esimerkkikatu 987",
        postitoimipaikka: "99999 Helsinki",
        matkapuhelin: "0401234567",
        sähköposti: "Valpas.LuokallejäänytYsiluokkalainen@gmail.com",
        lähde: "Hakulomake – Yhteishaku 2021",
      })
    )
  })

  it("Näyttää oppijalta ruotsissa olevan ilmotukselta tulevan osoitteen", async () => {
    await loginAs(päällekkäisiäOppivelvollisuuksiaPath, "valpas-jkl-normaali")

    await ilmoitetutYhteystiedotEquals(
      ilmoitetutYhteystiedot({
        pvm: "9.4.2020",
        lähiosoite: "Kungsgatan 123",
        postitoimipaikka: "99999 STOCKHOLM",
        maa: "Ruotsi",
        matkapuhelin: "0401234567",
        sähköposti: "Oppivelvollisuuksia.Päällekkäisiä@gmail.com",
        lähde: "Hakulomake – Yhteishaku 2021",
      })
    )
  })

  it("Yhteystietoja ei näytetä, jos oppijalla on turvakielto", async () => {
    await loginAs(turvakiellollinenOppijaPath, "valpas-jkl-normaali")

    await turvakieltoVaroitusEquals(`
      warning
      Oppijalla on turvakielto. Yhteystietoja saa käyttää ainoastaan oppivelvollisuuden valvontaan.
    `)

    await virallisetYhteystiedotEquals(`
      Viralliset yhteystiedot
      Henkilöllä on turvakielto
    `)
  })

  it("Näyttää varasijan hakutuloksissa", async () => {
    await loginAs(kahdellaOppijaOidillaPath, "valpas-jkl-normaali")
    await mainHeadingEquals("Kahdella-oppija-oidilla Valpas (150205A490C)")
    await hautEquals(`
      list_alt
      Yhteishaku 2021 Hakenut open_in_new
        Hakukohde
        Valinta
        Pisteet
        Alin pistemäärä
        1. Ressun lukio, Lukio 3. varasija 9,00 8,99
    `)
  })

  it("Näyttää harkinnanvaraisen haun", async () => {
    await loginAs(päällekkäisiäOppivelvollisuuksiaPath, "valpas-jkl-normaali")
    await mainHeadingEquals("Päällekkäisiä Oppivelvollisuuksia (060605A083N)")
    await hautEquals(`
      list_alt
      Yhteishaku 2021 Hakenut open_in_new
        Hakukohde
        Valinta
        Pisteet
        Alin pistemäärä
        1. Omnia, Peruuntumisala  Perunut –	–
        2. Helsingin medialukio, Lukio	Hylätty	7,50	8,20
        3. Omnia, Leipomoala1
        Otettu vastaan ehdollisesti
        Hyväksytty	–	–
        4. Omnia, Puhtaus- ja kiinteistöpalveluala	Hyväksytty	–	–
        1) Hakenut harkinnanvaraisesti
    `)
  })

  it("Oppivelvollisuuden suorittamiseen kelpaamattomia opintoja ei näytetä", async () => {
    await loginAs(lukionAineopinnotAloittanutPath, "valpas-jkl-normaali")
    await mainHeadingEquals("LukionAineopinnotAloittanut Valpas (040305A559A)")
    await opiskeluhistoriaEquals(
      historiaOpintoikeus({
        otsikko: "Perusopetus 2012 – 2021",
        tila: "Valmistunut",
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "9C",
        alkamispäivä: "15.8.2012",
        päättymispäivä: "30.5.2021",
      })
    )
  })

  it("Näyttää detaljisivun maksuttomuuskäyttäjälle lukio-oppijasta", async () => {
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-maksuttomuus")

    await mainHeadingEquals("Lukio-opiskelija Valpas (070504A717P)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000004")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "6.5.2022 asti",
        maksuttomuusoikeus: "31.12.2024 asti",
      })
    )
    await opiskeluhistoriaEquals(
      historiaOpintoikeus({
        otsikko: "Lukion oppimäärä 2019 –",
        tila: "Läsnä",
        maksuttomuus: ["Ei"],
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "AH",
        alkamispäivä: "1.8.2019",
      })
    )
  })

  it("Näyttää detaljisivun maksuttomuuskäyttäjälle lukio-oppijasta oppivelvollisuuden päättymisen jälkeenkin", async () => {
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-maksuttomuus")

    await resetMockData("2022-08-10")

    await mainHeadingEquals("Lukio-opiskelija Valpas (070504A717P)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000004")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "6.5.2022 asti",
        maksuttomuusoikeus: "31.12.2024 asti",
      })
    )
    await opiskeluhistoriaEquals(
      historiaOpintoikeus({
        otsikko: "Lukion oppimäärä 2019 –",
        tila: "Läsnä",
        maksuttomuus: ["Ei"],
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "AH",
        alkamispäivä: "1.8.2019",
      })
    )
  })

  it("Ei näytä detaljisivua maksuttomuuskäyttäjälle maksuttomuuden päättymisen jälkeen", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-maksuttomuus")

    await resetMockData("2025-01-01")
    await goToLocation(lukioOpiskelijaPath)

    await mainHeadingEquals("Oppijan tiedot")
    await secondaryHeadingEquals(
      "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000004"
    )
    await expectEiKuntailmoituksiaNotVisible()
  })

  it("Näyttää detaljisivun kuntakäyttäjälle lukio-oppijasta", async () => {
    await loginAs(lukioOpiskelijaPath, "valpas-helsinki")

    await mainHeadingEquals("Lukio-opiskelija Valpas (070504A717P)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000004")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "6.5.2022 asti",
        maksuttomuusoikeus: "31.12.2024 asti",
        kuntailmoitusBtn: true,
        oppivelvollisuudenKeskeytysBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      historiaOpintoikeus({
        otsikko: "Lukion oppimäärä 2019 –",
        tila: "Läsnä",
        maksuttomuus: ["Ei"],
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "AH",
        alkamispäivä: "1.8.2019",
      })
    )
  })

  it("Näyttää detaljisivun suorittamisen valvojalle lukio-oppijasta", async () => {
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-suorittaminen")

    await mainHeadingEquals("Lukio-opiskelija Valpas (070504A717P)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000004")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "6.5.2022 asti",
        maksuttomuusoikeus: "31.12.2024 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      historiaOpintoikeus({
        otsikko: "Lukion oppimäärä 2019 –",
        tila: "Läsnä",
        maksuttomuus: ["Ei"],
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "AH",
        alkamispäivä: "1.8.2019",
      })
    )
  })

  it("Näyttää detaljisivun maksuttomuuden oppijasta, jolla ei ole oppivelvollisuuden suorittamiseen kelpaavaa opiskeluoikeutta", async () => {
    await loginAs(
      eiOppivelvollisuudenSuorittamiseenKelpaaviaOpiskeluoikeuksiaPath,
      "valpas-maksuttomuus-hki"
    )
    await mainHeadingEquals(
      "Ei-oppivelvollisuuden-suorittamiseen-kelpaavia-opiskeluoikeuksia Valpas (061005A671V)"
    )
  })

  it("Näyttää detaljisivun hetuttomasta oppijasta", async () => {
    await loginAs(hetutonPath, "valpas-maksuttomuus-hki")
    await mainHeadingEquals("Hetuton Valpas")
  })

  it("Ei näytä detaljisivua kuntakäyttäjälle lukio-oppijasta oppivelvollisuuden päätyttyä", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(lukioOpiskelijaPath, "valpas-helsinki")

    await resetMockData("2022-08-10")
    await goToLocation(lukioOpiskelijaPath)

    await mainHeadingEquals("Oppijan tiedot")
    await secondaryHeadingEquals(
      "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000004"
    )
    await expectEiKuntailmoituksiaNotVisible()
  })

  it("Ei näytä detaljisivua suorittamisen valvojalle lukio-oppijasta oppivelvollisuuden päätyttyä", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-suorittaminen")

    await resetMockData("2022-08-10")
    await goToLocation(lukioOpiskelijaPath)

    await mainHeadingEquals("Oppijan tiedot")
    await secondaryHeadingEquals(
      "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000004"
    )
    await expectEiKuntailmoituksiaNotVisible()
  })

  it("Näyttää oppijan oppivelvollisuuden määräaikaisen keskeytyksen", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyMääräajaksiPath,
      "valpas-jkl-normaali"
    )
    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)"
    )
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "17.10.2023 asti",
        oppivelvollisuudenKeskeytykset: ["1.3.2021 – 30.9.2021"],
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOppivelvollisuudenKeskeytys("1.3.2021 – 30.9.2021"),
        historiaOppivelvollisuudenKeskeytys("1.1.2020 – 30.1.2020"),
        historiaOpintoikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
        })
      )
    )
  })

  it("Näyttää oppijan oppivelvollisuuden keskeytyksen toistaiseksi", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyToistaiseksiPath,
      "valpas-jkl-normaali"
    )
    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-toistaiseksi Valpas (150905A1823)"
    )
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "Keskeytetty toistaiseksi 1.1.2021 alkaen",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOppivelvollisuudenKeskeytysToistaiseksi("1.1.2021"),
        historiaOpintoikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
        })
      )
    )
  })

  it("Näyttää oppijan oppivelvollisuuden umpeutuneen määräaikaisen keskeytyksen oikein", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyMääräajaksiPath,
      "valpas-jkl-normaali"
    )

    await resetMockData("2022-10-01")
    await goToLocation(oppivelvollisuusKeskeytettyMääräajaksiPath)

    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)"
    )
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "17.10.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOppivelvollisuudenKeskeytys("1.3.2021 – 30.9.2021"),
        historiaOppivelvollisuudenKeskeytys("1.1.2020 – 30.1.2020"),
        historiaOpintoikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
        })
      )
    )
  })

  it("Oppivelvollisuuden keskeytys toimii oikein", async () => {
    await loginAs(oppivelvollisuusKeskeytettyMääräajaksiPath, "valpas-helsinki")

    await resetMockData("2022-11-11")
    await goToLocation(oppivelvollisuusKeskeytettyMääräajaksiPath)
    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)"
    )

    // Avaa ov-keskeytysmodaali
    await clickElement("#ovkeskeytys-btn")
    await textEventuallyEquals(
      ".modal__container .heading--secondary",
      "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)"
    )

    // Valitse "Oppivelvollisuus keskeytetään toistaiseksi", säilytä alkupäivänä nykyinen päivä, hyväksy ehto
    await clickElement(
      ".ovkeskeytys__option:nth-child(2) .radiobutton__container"
    )
    await clickElement(".ovkeskeytys__option:nth-child(2) .checkbox__labeltext")
    await clickElement("#ovkeskeytys-submit")

    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: `Keskeytetty toistaiseksi ${formatDate(
          today()
        )} alkaen`,
        maksuttomuusoikeus: "31.12.2025 asti",
        oppivelvollisuudenKeskeytysBtn: true,
        kuntailmoitusBtn: true,
      })
    )
  })

  it("Näytä väliaikaisesti keskeytynyt opiskeluoikeus", async () => {
    await loginAs(
      opiskeluoikeusKeskeytettyMääräajaksiPath,
      "valpas-jkl-yliopisto-suorittaminen"
    )

    await resetMockData("2021-08-15")

    await opiskeluhistoriaEquals(
      historiaOpintoikeus({
        otsikko: "Lukion oppimäärä 2021 –",
        tila: "Väliaikaisesti keskeytynyt 2.8.2021",
        maksuttomuus: ["Ei"],
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "AH",
        alkamispäivä: "1.8.2021",
      })
    )
  })

  it("Näytä lomailevan ammattikoululaisen opiskeluoikeus", async () => {
    await loginAs(opiskeluoikeusLomaPath, "valpas-pelkkä-suorittaminen-amis")

    await resetMockData("2021-08-15")

    await opiskeluhistoriaEquals(
      historiaOpintoikeus({
        otsikko: "Ammatillinen tutkinto 2021 –",
        tila: "Loma",
        maksuttomuus: ["1.8.2021– maksuton"],
        toimipiste:
          "Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka",
        alkamispäivä: "1.8.2021",
      })
    )
  })

  it("Näytä koulutustyyppi oikein", async () => {
    await loginAs(opiskeluoikeusValmaPath, "valpas-pelkkä-suorittaminen-amis")

    await resetMockData("2021-08-15")

    await opiskeluhistoriaEquals(
      historiaOpintoikeus({
        otsikko: "VALMA 2012 –",
        tila: "Läsnä",
        maksuttomuus: ["Ei"],
        toimipiste: "Stadin ammatti- ja aikuisopisto",
        alkamispäivä: "1.9.2012",
      })
    )
  })

  it("Näyttää maksuttomuuden pidennyksen", async () => {
    await loginAs(maksuttomuuttaPidennettyPath, "valpas-monta")
    await mainHeadingEquals("Maksuttomuutta-pidennetty Valpas (070604A200U)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000127")
    await opiskeluhistoriaEquals(
      historiaOpintoikeus({
        otsikko: "Ammatillinen tutkinto 2021 –",
        tila: "Läsnä",
        maksuttomuus: [
          "Oikeutta maksuttomuuteen pidennetty 1.9.2021–31.12.2023",
          "1.9.2021– maksuton",
        ],
        toimipiste: "Omnia Koulutus, Arbetarinstitut",
        alkamispäivä: "1.9.2021",
      })
    )
  })

  it("Näyttää perusopetukseen valmistavan opetuksen opiskeluhistoriassa", async () => {
    await loginAs(perusopetukseenValmistautuvaPath, "valpas-jkl-normaali", true)
    await mainHeadingEquals(
      "Perusopetukseen-valmistautuva Valpas (151011A1403)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000131")
    await opiskeluhistoriaEquals(
      historiaOpintoikeus({
        otsikko: "Perusopetukseen valmistava opetus 2021 –",
        tila: "Läsnä",
        toimipiste: "Jyväskylän normaalikoulu",
        alkamispäivä: "1.5.2021",
      })
    )
  })
})
