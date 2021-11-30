import { NonEmptyArray } from "fp-ts/lib/NonEmptyArray"
import { By, WebElement } from "selenium-webdriver"
import { Oid } from "../../src/state/common"
import { oppijaPath } from "../../src/state/paths"
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
import { fillField } from "../integrationtests-env/browser/forms"
import {
  defaultAnimationSleepTime,
  shortTimeout,
} from "../integrationtests-env/browser/timeouts"
import { eventually, sleep } from "../integrationtests-env/browser/utils"

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
  Oppivelvollisuus-keskeytetty-ei-opiskele Valpas | 20.5.2021 | Jyväskylän normaalikoulu    | 1.10.2005  | –
  Ysiluokka-valmis-keväällä-2021-ilmo Valpas      | 5.9.2021  | Jyväskylän normaalikoulu    | 26.8.2005   | –
`

export const pyhtääTableContent_kaikkiIlmoitukset = `
  Ilmoituksen-lisätiedot–poistettu Valpas           | 5.9.2021  | 1.2.246.562.10.14613773812  | 19.5.2005   | –
  Kahdella-oppija-oidilla-ilmo Valpas               | 5.9.2021  | Aapajoen koulu              | 4.6.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  scheduleKahdella-oppija-oidilla-ilmo Valpas       | 5.9.2021  | Jyväskylän normaalikoulu    | 4.6.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  Kahdella-oppija-oidilla-ilmo-2 Valpas             | 5.9.2021  | Jyväskylän normaalikoulu    | 3.6.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  KahdenKoulunYsi-ilmo Valpas	                      | 5.9.2021  | Jyväskylän normaalikoulu    | 21.11.2004  | –
  KasiinAstiToisessaKoulussaOllut-ilmo Valpas	      | 5.9.2021  | Jyväskylän normaalikoulu    | 2.5.2005    | –
  scheduleLukionAloittanut-ilmo Valpas              | 15.6.2021 | Jyväskylän normaalikoulu    | 11.4.2005   | doneJyväskylän normaalikoulu, Lukiokoulutus
  scheduleLukionAloittanutJaLopettanut-ilmo Valpas  | 20.9.2021 | Jyväskylän normaalikoulu    | 5.4.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  scheduleLukionAloittanutJaLopettanut-ilmo Valpas  | 15.6.2021 | Jyväskylän normaalikoulu    | 5.4.2005    | doneJyväskylän normaalikoulu, Lukiokoulutus
  Oppivelvollisuus-keskeytetty-ei-opiskele Valpas   | 20.5.2021 | Jyväskylän normaalikoulu    | 1.10.2005   | –
  Ysiluokka-valmis-keväällä-2021-ilmo Valpas        | 5.9.2021  | Jyväskylän normaalikoulu    | 26.8.2005   | –
`

export const teeKuntailmoitusOppijanäkymistä = async (
  oppijat: NonEmptyArray<Oppija>,
  tekijä: Tekijä
) => {
  for (const oppija of oppijat) {
    // Tee ilmoitus oppijakohtaisesta näkymästä käsin, ja tarkista, että uuden ilmoituksen tiedot ilmestyvät näkyviin
    await goToLocation(
      oppijaPath.href("/virkailija", {
        oppijaOid: oppija.oid,
      })
    )
    await urlIsEventually(
      pathToUrl(
        oppijaPath.href("/virkailija", {
          oppijaOid: oppija.oid,
        })
      )
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
        By.css(".ilmoitusform__prefilllist li")
      ),
      asuinkuntaSelect: await form.findElement(testIdIs("asuinkunta")),
      postinumeroInput: await form.findElement(testIdIs("postinumero")),
      postitoimipaikkaInput: await form.findElement(
        testIdIs("postitoimipaikka")
      ),
      katuosoiteInput: await form.findElement(testIdIs("katuosoite")),
      puhelinnumeroInput: await form.findElement(testIdIs("puhelinnumero")),
      sähköpostiInput: await form.findElement(testIdIs("sähköposti")),
      submitButton: await form.findElement({
        className: "ilmoitusform__submit",
      }),
    }))
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
    "Aktiivisia ilmoituksia tulisi näkyä tasan yksi"
  ).toEqual(1)
  const ilmoitus = ilmoitukset[0]!!

  return Promise.all([
    ...requiredTestIds.map(async (id) =>
      objectEntry(id, await ilmoitus.findElement(testIdIs(id)).then(getText))
    ),
    ...optionalTestIds.map(async (id) =>
      objectEntry(
        id,
        await ilmoitus.findElements(testIdIs(id)).then(getOptionalText)
      )
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
        By.css('[data-testid="submitted"]')
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
