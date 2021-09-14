import { NonEmptyArray } from "fp-ts/lib/NonEmptyArray"
import {
  createHakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg,
  createOppijaPath,
} from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyNotVisible,
  expectElementEventuallyVisible,
} from "../integrationtests-env/browser/content"
import {
  goToLocation,
  pathToUrl,
  urlIsEventually,
} from "../integrationtests-env/browser/core"
import { dataTableEventuallyEquals } from "../integrationtests-env/browser/datatable"
import { loginAs } from "../integrationtests-env/browser/reset"
import {
  hakutilannePath,
  jklNormaalikouluTableContent,
  kuntailmoitusRowSelector,
  oppijaRowSelector,
} from "./hakutilanne.shared"
import {
  fillTekijänTiedot,
  getCloseButton,
  getIlmoitusData,
  getIlmoitusForm,
  hkiTableContent_20211201,
  Oppija,
  teeKuntailmoitusOppijanäkymistä,
  Tekijä,
  täytäJaLähetäLomake,
} from "./kuntailmoitus.shared"
import { jyväskylänNormaalikouluOid } from "./oids"
import { suorittaminenHetuhakuPath } from "./suorittaminen.shared"

const opo: Tekijä = {
  nimi: "käyttäjä valpas-jkl-normaali",
  email: "integraatiotesti@oph.fi",
  puhelin: "0505050505050",
  organisaatio: "Jyväskylän normaalikoulu",
  organisaatioOid: "1.2.246.562.10.14613773812",
}

const koulutustoimijaHakeutumisenValvoja: Tekijä = {
  nimi: "käyttäjä valpas-jkl-yliopisto",
  email: "integraatiotesti@oph.fi",
  puhelin: "0505050505050",
  // koulutustoimijatasollakin ilmoitukset tehdään aina oppilaitoksen nimissä, siksi normaalikoulu eikä yliopisto:
  organisaatio: "Jyväskylän normaalikoulu",
  organisaatioOid: "1.2.246.562.10.14613773812",
}

const kuntakäyttäjä: Tekijä = {
  nimi: "käyttäjä valpas-helsinki",
  email: "integraatiotesti.kunta@oph.fi",
  puhelin: "04040404040",
  organisaatio: "Helsingin kaupunki",
  organisaatioOid: "1.2.246.562.10.346830761110",
}

const suorittamisenValvoja = {
  nimi: "käyttäjä valpas-pelkkä-suorittaminen",
  email: "integraatiotesti.suorittaminen@oph.fi",
  puhelin: "090909",
  organisaatio: "Jyväskylän normaalikoulu",
  organisaatioOid: "1.2.246.562.10.14613773812",
}

const nivelvaiheenValvoja = {
  nimi: "käyttäjä valpas-nivelvaihe",
  email: "integraatiotesti.nivelvaihe@oph.fi",
  puhelin: "1010",
  organisaatio: "Jyväskylän normaalikoulu",
  organisaatioOid: "1.2.246.562.10.14613773812",
}

const teeOppijat = (tekijä: Tekijä): NonEmptyArray<Oppija> => [
  {
    oid: "1.2.246.562.24.00000000001",
    title:
      "Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas (221105A3023)",
    prefill: 0,
    expected: {
      kohde: "Helsingin kaupunki",
      tekijä: [
        tekijä.nimi,
        tekijä.email,
        tekijä.puhelin,
        tekijä.organisaatio,
      ].join("\n"),
      lähiosoite: "Esimerkkikatu 123",
      postitoimipaikka: "99999 Helsinki",
      maa: "Suomi",
      puhelin: "0401234567",
      email: "Valpas.Oppivelvollinen-ysiluokka-kesken-keväällä-2021@gmail.com",
      muuHaku: "Ei",
    },
  },
  {
    oid: "1.2.246.562.24.00000000028",
    title: "Epäonninen Valpas (301005A336J)",
    fill: {
      asuinkunta: "Pyhtää",
      postinumero: "12345",
      postitoimipaikka: "Pyhtää",
      katuosoite: "Esimerkkikatu 1234",
    },
    expected: {
      kohde: "Pyhtään kunta",
      tekijä: [
        tekijä.nimi,
        tekijä.email,
        tekijä.puhelin,
        tekijä.organisaatio,
      ].join("\n"),
      lähiosoite: "Esimerkkikatu 1234",
      postitoimipaikka: "12345 Pyhtää",
      maa: "Suomi",
      puhelin: undefined,
      email: undefined,
      muuHaku: "Ei",
    },
  },
  {
    oid: "1.2.246.562.24.00000000014",
    title: "KasiinAstiToisessaKoulussaOllut Valpas (170805A613F)",
    fill: {
      asuinkunta: "Pyhtää",
    },
    expected: {
      kohde: "Pyhtään kunta",
      tekijä: [
        tekijä.nimi,
        tekijä.email,
        tekijä.puhelin,
        tekijä.organisaatio,
      ].join("\n"),
      lähiosoite: undefined,
      postitoimipaikka: undefined,
      maa: "Suomi",
      puhelin: undefined,
      email: undefined,
      muuHaku: "Ei",
    },
  },
]

const kunnanOppijat: NonEmptyArray<Oppija> = teeOppijat(kuntakäyttäjä)

const suorittamisenValvojanOppijat: NonEmptyArray<Oppija> = [
  {
    oid: "1.2.246.562.24.00000000004",
    title: "Lukio-opiskelija Valpas (070504A717P)",
    prefill: 0,
    expected: {
      kohde: "Helsingin kaupunki",
      tekijä: [
        "käyttäjä valpas-pelkkä-suorittaminen",
        suorittamisenValvoja.email,
        suorittamisenValvoja.puhelin,
        "Jyväskylän normaalikoulu",
      ].join("\n"),
      lähiosoite: "Esimerkkitie 10",
      postitoimipaikka: "99999 Helsinki",
      maa: "Costa Rica",
      puhelin: "0401122334",
      email: "valpas@gmail.com",
      muuHaku: "Ei",
    },
  },
]

const nivelvaiheenValvojanOppijat: NonEmptyArray<Oppija> = [
  {
    oid: "1.2.246.562.24.00000000004",
    title: "Lukio-opiskelija Valpas (070504A717P)",
    prefill: 0,
    expected: {
      kohde: "Helsingin kaupunki",
      tekijä: [
        "käyttäjä valpas-nivelvaihe",
        nivelvaiheenValvoja.email,
        nivelvaiheenValvoja.puhelin,
        "Jyväskylän normaalikoulu",
      ].join("\n"),
      lähiosoite: "Esimerkkitie 10",
      postitoimipaikka: "99999 Helsinki",
      maa: "Costa Rica",
      puhelin: "0401122334",
      email: "valpas@gmail.com",
      muuHaku: "Ei",
    },
  },
]

describe("Kuntailmoituksen tekeminen", () => {
  it("happy path hakeutumisen valvojana", async () => {
    await teeKuntailmoitusHakutilannenäkymästä("valpas-jkl-normaali", opo)
  })

  it("happy path hakeutumisen valvojana koulutustoimijana", async () => {
    await teeKuntailmoitusHakutilannenäkymästä(
      "valpas-jkl-yliopisto",
      koulutustoimijaHakeutumisenValvoja
    )
  })

  it("happy path oppijanäkymistä hakeutumisen valvojana", async () => {
    const oppijat = teeOppijat(opo)

    await openHakutilanneView("valpas-jkl-normaali")

    await teeKuntailmoitusOppijanäkymistä(oppijat, opo)
  })

  it("happy path oppijanäkymistä hakeutumisen valvojana koulutustoimijana", async () => {
    const oppijat = teeOppijat(koulutustoimijaHakeutumisenValvoja)

    await openHakutilanneView("valpas-jkl-yliopisto")

    await teeKuntailmoitusOppijanäkymistä(
      oppijat,
      koulutustoimijaHakeutumisenValvoja
    )
  })

  it("happy path kunnan käyttäjänä", async () => {
    await loginAs(hakutilannePath, "valpas-helsinki", true, "2021-12-01")
    await dataTableEventuallyEquals(
      ".kuntailmoitus",
      hkiTableContent_20211201,
      "|"
    )

    await teeKuntailmoitusOppijanäkymistä(kunnanOppijat, kuntakäyttäjä)
  })

  it("happy path suorittamisen valvojana", async () => {
    await loginAs(
      suorittaminenHetuhakuPath,
      "valpas-pelkkä-suorittaminen",
      true
    )
    await teeKuntailmoitusOppijanäkymistä(
      suorittamisenValvojanOppijat,
      suorittamisenValvoja
    )
  })

  it("happy path hakeutumisen ja suorittamisen valvojana", async () => {
    await loginAs(suorittaminenHetuhakuPath, "valpas-nivelvaihe", true)
    await teeKuntailmoitusOppijanäkymistä(
      nivelvaiheenValvojanOppijat,
      nivelvaiheenValvoja
    )
  })
})

const teeKuntailmoitusHakutilannenäkymästä = async (
  username: string,
  tekijä: Tekijä
) => {
  const oppijat = teeOppijat(tekijä)

  await openHakutilanneView(username)

  for (const oppija of oppijat) {
    await selectOppija(oppija.oid)
  }

  await openKuntailmoitus()
  await fillTekijänTiedot(tekijä)

  const forms = await getIlmoitusForm()
  expect(
    forms.length,
    "Lomakkeita näkyy yhtä monta kuin valittuja oppijoita"
  ).toBe(oppijat.length)

  for (const form of forms) {
    // Tarkista että valitut oppijat ja lomakkeet mäppäytyvät toisiinsa
    const oppija = oppijat.find((o) => form.subtitle.includes(o.oid))!!
    expect(
      oppija,
      `Lomakkeen oppija "${form.title}" "${form.subtitle}" on valittujen oppijoiden joukossa`
    ).toBeDefined()
    expect(form.title).toBe(oppija.title)

    await täytäJaLähetäLomake(oppija, form)
  }

  // Tarkista, että sulkemisnappulan teksti on vaihtunut
  const button = await getCloseButton()
  expect(await button.getText()).toBe("Valmis")

  // Sulje lomake
  await button.click()
  await expectElementEventuallyNotVisible(".modal__container")

  // Tarkista että oppijat ovat poistuneet listasta
  for (const oppija of oppijat) {
    await expectElementEventuallyNotVisible(oppijaRowSelector(oppija.oid))
  }

  // Tarkista että oppijat ovat ilmestyneet "kunnalle tehdyt ilmoitukset" -tauluun
  for (const oppija of oppijat) {
    const ilmotuksetPath = createHakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg(
      "/virkailija",
      {
        organisaatioOid: tekijä.organisaatioOid,
      }
    )
    await goToLocation(ilmotuksetPath)
    await urlIsEventually(pathToUrl(ilmotuksetPath))
    await expectElementEventuallyVisible(kuntailmoitusRowSelector(oppija.oid))
  }

  // Tarkista oppijakohtaisista näkymistä, että ilmoituksen tiedot ovat siellä
  for (const oppija of oppijat) {
    const oppijaPath = createOppijaPath("/virkailija", {
      oppijaOid: oppija.oid,
      hakutilanneRef: jyväskylänNormaalikouluOid,
    })
    await goToLocation(oppijaPath)
    await urlIsEventually(pathToUrl(oppijaPath))
    expect(await getIlmoitusData()).toEqual(oppija.expected)
  }
}

const openHakutilanneView = async (username: string) => {
  await loginAs(hakutilannePath, username, true)
  await dataTableEventuallyEquals(
    ".hakutilanne",
    jklNormaalikouluTableContent,
    "|"
  )
}

const selectOppija = async (oppijaOid: string) => {
  const checkboxSelector = `.table__body .table__row[data-row*="${oppijaOid}"]  td:first-child .checkbox`
  await clickElement(checkboxSelector)
}

const openKuntailmoitus = async () => {
  await clickElement(".hakutilannedrawer__ilmoittaminen button")
  await expectElementEventuallyVisible(".modal__container")
}
