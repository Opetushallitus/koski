import { NonEmptyArray } from "fp-ts/lib/NonEmptyArray"
import { By, WebElement } from "selenium-webdriver"
import { Oid } from "../../src/state/common"
import {
  kunnalleIlmoitetutPathWithOrg,
  oppijaPath,
} from "../../src/state/paths"
import { fromEntries, objectEntry } from "../../src/utils/objects"
import {
  clickElement,
  expectElementEventuallyNotVisible,
  expectElementEventuallyVisible,
} from "../integrationtests-env/browser/content"
import {
  $,
  $$,
  getOptionalText,
  getText,
  goToLocation,
  pathToUrl,
  scrollIntoView,
  testIdIs,
  urlIsEventually,
} from "../integrationtests-env/browser/core"
import { dataTableEventuallyEquals } from "../integrationtests-env/browser/datatable"
import { fillField } from "../integrationtests-env/browser/forms"
import { loginAs } from "../integrationtests-env/browser/reset"
import {
  defaultAnimationSleepTime,
  shortTimeout,
} from "../integrationtests-env/browser/timeouts"
import { eventually, sleep } from "../integrationtests-env/browser/utils"
import {
  hakutilannePath,
  jklNormaalikouluTableContent,
  kuntailmoitusRowSelector,
  oppijaRowSelector,
} from "./hakutilanne.shared"
import { jyväskylänNormaalikouluOid } from "./oids"

export type Oppija = {
  oid: Oid
  title: string
  prefill?: number
  fill?: OppijaFillValues
  expected: DisplayedIlmoitusData
}

export type OppijaFillValues = {
  asuinkunta?: string
  postinumero?: string
  postitoimipaikka?: string
  katuosoite?: string
  puhelinnumero?: string
  sähköposti?: string
}

export type DisplayedRequiredIlmoitusData = {
  kohde: string
  tekijä: string
}

export type DisplayedOptionalIlmoitusData = {
  lähiosoite?: string
  postitoimipaikka?: string
  maa?: string
  puhelin?: string
  email?: string
  muuHaku?: string
}

export type Tekijä = {
  nimi: string
  email: string
  puhelin: string
  organisaatio: string
  organisaatioOid: Oid
}

export type DisplayedIlmoitusData = DisplayedRequiredIlmoitusData &
  DisplayedOptionalIlmoitusData

export const hkiTableContent = `
  LukionAloittanutJaLopettanut-ilmo Valpas | 30.11.2021 | Jyväskylän normaalikoulu | 5.4.2005 | doneJyväskylän normaalikoulu, Lukiokoulutus
`

export const hkiTableContent_20211201 = `
  LukionAloittanutJaLopettanut-ilmo Valpas | 30.11.2021 | Jyväskylän normaalikoulu | 5.4.2005 | –
`

export const pyhtääTableContent = `
  Ilmoituksen-lisätiedot–poistettu Valpas         | 5.9.2021  | 1.2.246.562.10.14613773812  | 19.5.2005   | –
  Kahdella-oppija-oidilla-ilmo Valpas             | 5.9.2021  | Aapajoen koulu              | 4.6.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  Kahdella-oppija-oidilla-ilmo-2 Valpas           | 5.9.2021  | Jyväskylän normaalikoulu    | 3.6.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  KahdenKoulunYsi-ilmo Valpas	                    | 5.9.2021  | Jyväskylän normaalikoulu    | 21.11.2004  | –
  KasiinAstiToisessaKoulussaOllut-ilmo Valpas   	| 5.9.2021  | Jyväskylän normaalikoulu    | 2.5.2005    | –
  Oppivelvollisuus-keskeytetty-ei-opiskele Valpas | 20.5.2021 | Jyväskylän normaalikoulu    | 1.10.2005   | –
  Turvakielto Valpas                              | 5.9.2021  | Jyväskylän normaalikoulu    | 29.9.2004   | –
  Ysiluokka-valmis-keväällä-2021-ilmo Valpas      | 5.9.2021  | Jyväskylän normaalikoulu    | 26.8.2005   | –
`

export const pyhtääTableContent_kaikkiIlmoitukset = `
  Ilmoituksen-lisätiedot–poistettu Valpas                          | 5.9.2021  | 1.2.246.562.10.14613773812  | 19.5.2005   | –
  Kahdella-oppija-oidilla-ilmo Valpas                              | 5.9.2021  | Aapajoen koulu              | 4.6.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  scheduleKahdella-oppija-oidilla-ilmo Valpas                      | 5.9.2021  | Jyväskylän normaalikoulu    | 4.6.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  Kahdella-oppija-oidilla-ilmo-2 Valpas                            | 5.9.2021  | Jyväskylän normaalikoulu    | 3.6.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  KahdenKoulunYsi-ilmo Valpas                                      | 5.9.2021  | Jyväskylän normaalikoulu    | 21.11.2004  | –
  KasiinAstiToisessaKoulussaOllut-ilmo Valpas                      | 5.9.2021  | Jyväskylän normaalikoulu    | 2.5.2005    | –
  scheduleLukionAloittanut-ilmo Valpas                             | 15.6.2021 | Jyväskylän normaalikoulu    | 11.4.2005   | doneJyväskylän normaalikoulu, Lukiokoulutus
  scheduleLukionAloittanutJaLopettanut-ilmo Valpas                 | 20.9.2021 | Jyväskylän normaalikoulu    | 5.4.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  scheduleLukionAloittanutJaLopettanut-ilmo Valpas                 | 15.6.2021 | Jyväskylän normaalikoulu    | 5.4.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  Oppivelvollisuus-keskeytetty-ei-opiskele Valpas                  | 20.5.2021 | Jyväskylän normaalikoulu    | 1.10.2005   | –
  Turvakielto Valpas                                               | 5.9.2021  | Jyväskylän normaalikoulu    | 29.9.2004   | –
  scheduleYsiluokka-valmis-ja-ilmoitettu-ja-uusi-nivelvaihe Valpas | 27.6.2021 | Jyväskylän normaalikoulu    | 24.7.2006   | doneRessun lukio, Ammatillinen koulutus
  Ysiluokka-valmis-keväällä-2021-ilmo Valpas                       | 5.9.2021  | Jyväskylän normaalikoulu    | 26.8.2005   | –
`

export const teeKuntailmoitusOppijanäkymistä = async (
  oppijat: NonEmptyArray<Oppija>,
  tekijä: Tekijä,
) => {
  for (const oppija of oppijat) {
    // Tee ilmoitus oppijakohtaisesta näkymästä käsin, ja tarkista, että uuden ilmoituksen tiedot ilmestyvät näkyviin
    await goToLocation(
      oppijaPath.href("/virkailija", {
        oppijaOid: oppija.oid,
      }),
    )
    await urlIsEventually(
      pathToUrl(
        oppijaPath.href("/virkailija", {
          oppijaOid: oppija.oid,
        }),
      ),
    )

    await openKuntailmoitusOppijanäkymässä()
    await fillTekijänTiedot(tekijä)

    const forms = await getIlmoitusForm()
    expect(forms.length, "Lomakkeita näkyy vain yksi").toBe(1)
    const form = forms[0]!!
    expect(form.title).toBe(oppija.title)

    await täytäJaLähetäLomake(oppija, form)

    // Tarkista, että sulkemisnappulan teksti on vaihtunut
    const button = await getCloseButton()
    expect(await button.getText()).toBe("Valmis")

    // Sulje lomake
    await button.click()
    await expectElementEventuallyNotVisible(".modal__container")

    // Tarkista, että ilmoituksen tiedot ovat tulleet näkyviin (automaattinen reload lomakkeen sulkemisen jälkeen)

    expect(await getIlmoitusData()).toEqual(oppija.expected)
  }
}

const openKuntailmoitusOppijanäkymässä = async () => {
  await clickElement(".oppijaview__ilmoitusbuttonwithinfo button")
  await expectElementEventuallyVisible(".modal__container")
}

export const fillTekijänTiedot = async (tekijä: {
  puhelin: string
  email: string
}) => {
  const form = await getTekijänYhteystiedotForm()
  await fillField(form.email, tekijä.email)
  await fillField(form.puhelin, tekijä.puhelin)
  await form.submit.click()
}

const getTekijänYhteystiedotForm = async () => {
  const form = await $(".ilmoitusform__frame")
  return {
    organisaatio: await form.findElement(testIdIs("organisaatio")),
    email: await form.findElement(testIdIs("email")),
    puhelin: await form.findElement(testIdIs("puhelin")),
    submit: await form.findElement({ className: "ilmoitusform__continue" }),
  }
}

export type Form = {
  root: WebElement
  title: string
  subtitle: string
  prefills: WebElement[]
  asuinkuntaSelect: WebElement
  postinumeroInput: WebElement
  postitoimipaikkaInput: WebElement
  katuosoiteInput: WebElement
  puhelinnumeroInput: WebElement
  sähköpostiInput: WebElement
  submitButton: WebElement
}

export const getIlmoitusForm = async (): Promise<Form[]> => {
  const forms = await $$(".ilmoitusform__frame")
  return Promise.all(
    forms.map(async (form) => ({
      root: form,
      title: await form
        .findElement({ className: "ilmoitusform__titletext" })
        .then(getText),
      subtitle: await form
        .findElement({ className: "ilmoitusform__subtitle" })
        .then(getText),
      prefills: await form.findElements(
        By.css(".ilmoitusform__prefilllist li"),
      ),
      asuinkuntaSelect: await form.findElement(testIdIs("asuinkunta")),
      postinumeroInput: await form.findElement(testIdIs("postinumero")),
      postitoimipaikkaInput: await form.findElement(
        testIdIs("postitoimipaikka"),
      ),
      katuosoiteInput: await form.findElement(testIdIs("katuosoite")),
      puhelinnumeroInput: await form.findElement(testIdIs("puhelinnumero")),
      sähköpostiInput: await form.findElement(testIdIs("sähköposti")),
      submitButton: await form.findElement({
        className: "ilmoitusform__submit",
      }),
    })),
  )
}

export const getIlmoitusData = async (): Promise<DisplayedIlmoitusData> => {
  const requiredTestIds: Array<keyof DisplayedRequiredIlmoitusData> = [
    "kohde",
    "tekijä",
  ]
  const optionalTestIds: Array<keyof DisplayedOptionalIlmoitusData> = [
    "lähiosoite",
    "postitoimipaikka",
    "maa",
    "puhelin",
    "email",
    "muuHaku",
  ]

  const ilmoitukset = await $$(".kuntailmoitus__frame")
  expect(
    ilmoitukset.length,
    "Aktiivisia ilmoituksia tulisi näkyä tasan yksi",
  ).toEqual(1)
  const ilmoitus = ilmoitukset[0]!!

  return Promise.all([
    ...requiredTestIds.map(async (id) =>
      objectEntry(id, await ilmoitus.findElement(testIdIs(id)).then(getText)),
    ),
    ...optionalTestIds.map(async (id) =>
      objectEntry(
        id,
        await ilmoitus.findElements(testIdIs(id)).then(getOptionalText),
      ),
    ),
  ]).then((x) => fromEntries(x) as DisplayedIlmoitusData)
}

export const täytäJaLähetäLomake = async (oppija: Oppija, form: Form) => {
  // Esitäytä lomake
  if (oppija.prefill !== undefined) {
    await form.prefills[oppija.prefill]!!.click()
  }

  // Täytä lomake
  if (oppija.fill?.asuinkunta !== undefined) {
    await selectOption(form.asuinkuntaSelect, oppija.fill?.asuinkunta)
  }
  if (oppija.fill?.postinumero !== undefined) {
    await fillField(form.postinumeroInput, oppija.fill?.postinumero)
  }
  if (oppija.fill?.postitoimipaikka !== undefined) {
    await fillField(form.postitoimipaikkaInput, oppija.fill?.postitoimipaikka)
  }
  if (oppija.fill?.katuosoite !== undefined) {
    await fillField(form.katuosoiteInput, oppija.fill?.katuosoite)
  }
  if (oppija.fill?.puhelinnumero !== undefined) {
    await fillField(form.puhelinnumeroInput, oppija.fill?.puhelinnumero)
  }
  if (oppija.fill?.sähköposti !== undefined) {
    await fillField(form.sähköpostiInput, oppija.fill?.sähköposti)
  }

  // Lähetä
  await eventually(async () => {
    await scrollIntoView(form.submitButton)
    await form.submitButton.click()
    await eventually(async () => {
      const submitted = await form.root.findElement(
        By.css('[data-testid="submitted"]'),
      )
      expect(submitted).toBeDefined()
    }, shortTimeout)
  })

  // Odota, että animaatiot ovat päättyneet. Myöhemmät operaatiot, erityisesti buttonien painaminen voivat epäonnistua,
  // jos animaatiot ovat käynnissä. Tähän ei oikein ole muuta helppoa keinoa kuin sleep: vaihtoehto voisi olla
  // jotenkin tutkia jonkin siirtyvän elementin koordinaatteja, ja jatkaa vasta kun koordinaatit ovat stabiloituneet.
  await sleep(defaultAnimationSleepTime)
}

const selectOption = async (select: WebElement, text: string) => {
  const options = await select.findElements({ tagName: "option" })
  const optionTexts = await Promise.all(options.map((o) => o.getText()))
  const index = optionTexts.findIndex((o) => o === text)
  expect(index >= 0, `Valinta "${text}" löytyy valikosta`).toBeTruthy()
  await options[index]!!.click()
}

export const getCloseButton = () => $(".modalbuttongroup button")

export const teeKuntailmoitusHakutilannenäkymästä = async (
  username: string,
  tekijä: Tekijä,
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
    "Lomakkeita näkyy yhtä monta kuin valittuja oppijoita",
  ).toBe(oppijat.length)

  for (const form of forms) {
    // Tarkista että valitut oppijat ja lomakkeet mäppäytyvät toisiinsa
    const oppija = oppijat.find((o) => form.subtitle.includes(o.oid))!!
    expect(
      oppija,
      `Lomakkeen oppija "${form.title}" "${form.subtitle}" on valittujen oppijoiden joukossa`,
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

  // Tarkista että oppijat ovat ilmestyneet "kunnalle tehdyt ilmoitukset" -tauluun ja niitä klikkaamalla näkee ilmoitukset tiedot
  for (const oppija of oppijat) {
    const ilmotuksetPath = kunnalleIlmoitetutPathWithOrg.href("/virkailija", {
      organisaatioOid: tekijä.organisaatioOid,
    })
    await goToLocation(ilmotuksetPath)
    await urlIsEventually(pathToUrl(ilmotuksetPath))
    const ilmoitusSelector = kuntailmoitusRowSelector(oppija.oid)
    await expectElementEventuallyVisible(ilmoitusSelector)

    await (await $(ilmoitusSelector)).click()
    expect(await getIlmoitusData()).toEqual(oppija.expected)
    await (await $(".modal__closebutton")).click()
  }

  // Tarkista oppijakohtaisista näkymistä, että ilmoituksen tiedot ovat siellä
  for (const oppija of oppijat) {
    const path = oppijaPath.href("/virkailija", {
      oppijaOid: oppija.oid,
      hakutilanneRef: jyväskylänNormaalikouluOid,
    })
    await goToLocation(path)
    await urlIsEventually(pathToUrl(path))
    expect(await getIlmoitusData()).toEqual(oppija.expected)
  }
}

export const openHakutilanneView = async (username: string) => {
  await loginAs(hakutilannePath, username, true)
  await dataTableEventuallyEquals(
    ".hakutilanne",
    jklNormaalikouluTableContent,
    "|",
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

export const opo: Tekijä = {
  nimi: "käyttäjä valpas-jkl-normaali",
  email: "integraatiotesti@oph.fi",
  puhelin: "0505050505050",
  organisaatio: "Jyväskylän normaalikoulu",
  organisaatioOid: "1.2.246.562.10.14613773812",
}

export const koulutustoimijaHakeutumisenValvoja: Tekijä = {
  nimi: "käyttäjä valpas-jkl-yliopisto",
  email: "integraatiotesti@oph.fi",
  puhelin: "0505050505050",
  // koulutustoimijatasollakin ilmoitukset tehdään aina oppilaitoksen nimissä, siksi normaalikoulu eikä yliopisto:
  organisaatio: "Jyväskylän normaalikoulu",
  organisaatioOid: "1.2.246.562.10.14613773812",
}

export const kuntakäyttäjä: Tekijä = {
  nimi: "käyttäjä valpas-helsinki",
  email: "integraatiotesti.kunta@oph.fi",
  puhelin: "04040404040",
  organisaatio: "Helsingin kaupunki",
  organisaatioOid: "1.2.246.562.10.346830761110",
}

export const suorittamisenValvoja = {
  nimi: "käyttäjä valpas-pelkkä-suorittaminen",
  email: "integraatiotesti.suorittaminen@oph.fi",
  puhelin: "090909",
  organisaatio: "Jyväskylän normaalikoulu",
  organisaatioOid: "1.2.246.562.10.14613773812",
}

export const nivelvaiheenValvoja = {
  nimi: "käyttäjä valpas-nivelvaihe",
  email: "integraatiotesti.nivelvaihe@oph.fi",
  puhelin: "1010",
  organisaatio: "Jyväskylän normaalikoulu",
  organisaatioOid: "1.2.246.562.10.14613773812",
}

export const teeOppijat = (tekijä: Tekijä): NonEmptyArray<Oppija> => [
  {
    oid: "1.2.246.562.24.00000000001",
    title:
      "Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas (221105A3023)",
    prefill: 0,
    expected: {
      kohde: "Helsinki",
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
      kohde: "Pyhtää",
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
      kohde: "Pyhtää",
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

export const kunnanOppijat: NonEmptyArray<Oppija> = teeOppijat(kuntakäyttäjä)

export const suorittamisenValvojanOppijat: NonEmptyArray<Oppija> = [
  {
    oid: "1.2.246.562.24.00000000004",
    title: "Lukio-opiskelija Valpas (070504A717P)",
    prefill: 0,
    expected: {
      kohde: "Helsinki",
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

export const nivelvaiheenValvojanOppijat: NonEmptyArray<Oppija> = [
  {
    oid: "1.2.246.562.24.00000000004",
    title: "Lukio-opiskelija Valpas (070504A717P)",
    prefill: 0,
    expected: {
      kohde: "Helsinki",
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
