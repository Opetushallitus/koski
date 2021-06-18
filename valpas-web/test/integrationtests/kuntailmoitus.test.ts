import { NonEmptyArray } from "fp-ts/lib/NonEmptyArray"
import { By, WebElement } from "selenium-webdriver"
import { Oid } from "../../src/state/common"
import { createOppijaPath } from "../../src/state/paths"
import { fromEntries, objectEntry } from "../../src/utils/objects"
import {
  clickElement,
  expectElementEventuallyVisible,
  expectElementNotVisible,
} from "../integrationtests-env/browser/content"
import {
  $,
  $$,
  goToLocation,
  pathToUrl,
  testIdIs,
  urlIsEventually,
} from "../integrationtests-env/browser/core"
import { dataTableEventuallyEquals } from "../integrationtests-env/browser/datatable"
import { clearTextInputElement } from "../integrationtests-env/browser/forms"
import { loginAs } from "../integrationtests-env/browser/reset"
import { eventually } from "../integrationtests-env/browser/utils"
import {
  hakutilannePath,
  jklNormaalikouluTableContent,
  openOppijaView,
} from "./hakutilanne.shared"
import { jyväskylänNormaalikouluOid } from "./oids"

type Oppija = {
  oid: Oid
  title: string
  prefill?: number
  fill?: OppijaFillValues
  expected: DisplayedIlmoitusData
}

type OppijaFillValues = {
  asuinkunta?: string
  postinumero?: string
  postitoimipaikka?: string
  katuosoite?: string
  puhelinnumero?: string
  sähköposti?: string
}

type DisplayedRequiredIlmoitusData = {
  kohde: string
  tekijä: string
}

type DisplayedOptionalIlmoitusData = {
  lähiosoite?: string
  postitoimipaikka?: string
  maa?: string
  puhelin?: string
  email?: string
  muuHaku?: string
}

type DisplayedIlmoitusData = DisplayedRequiredIlmoitusData &
  DisplayedOptionalIlmoitusData

const opo = {
  email: "integraatiotesti@oph.fi",
  puhelin: "0505050505050",
}

const oppijat: NonEmptyArray<Oppija> = [
  {
    oid: "1.2.246.562.24.00000000001",
    title:
      "Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas (221105A3023)",
    prefill: 0,
    expected: {
      kohde: "Helsingin kaupunki",
      tekijä: [
        "käyttäjä valpas-jkl-normaali",
        opo.email,
        opo.puhelin,
        "Jyväskylän normaalikoulu",
      ].join("\n"),
      lähiosoite: "Esimerkkikatu 123",
      postitoimipaikka: "00000 Helsinki",
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
        "käyttäjä valpas-jkl-normaali",
        opo.email,
        opo.puhelin,
        "Jyväskylän normaalikoulu",
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
        "käyttäjä valpas-jkl-normaali",
        opo.email,
        opo.puhelin,
        "Jyväskylän normaalikoulu",
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

async function täytäJaLähetäLomake(oppija: Oppija, form: any) {
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
  await form.submitButton.click()
  await eventually(async () => {
    const submitted = await form.root.findElement(
      By.css('[data-testid="submitted"]')
    )
    expect(submitted).toBeDefined()
  })
}

describe("Kuntailmoituksen tekeminen", () => {
  it("happy path hakeutumisen valvojana", async () => {
    await openHakutilanneView()

    for (const oppija of oppijat) {
      await selectOppija(oppija.oid)
    }

    await openKuntailmoitus()
    await fillTekijänTiedot()

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
    await expectElementNotVisible(".modal__container")

    // Tarkista oppijakohtaisista näkymistä, että ilmoituksen tiedot ovat siellä
    for (const oppija of oppijat) {
      await goToLocation(hakutilannePath)
      await openOppijaView(oppija.oid)
      await urlIsEventually(
        pathToUrl(
          createOppijaPath("/virkailija", {
            oppijaOid: oppija.oid,
            hakutilanneRef: jyväskylänNormaalikouluOid,
          })
        )
      )

      expect(await getIlmoitusData()).toEqual(oppija.expected)
    }
  })

  it("happy path oppijanäkymistä hakeutumisen valvojana", async () => {
    await openHakutilanneView()

    for (const oppija of oppijat) {
      // Tee ilmoitus oppijakohtaisesta näkymästä käsin, ja tarkista, että uuden ilmoituksen tiedot ilmestyvät näkyviin
      await goToLocation(
        createOppijaPath("/virkailija", {
          oppijaOid: oppija.oid,
        })
      )
      await urlIsEventually(
        pathToUrl(
          createOppijaPath("/virkailija", {
            oppijaOid: oppija.oid,
          })
        )
      )

      await openKuntailmoitusOppijanäkymässä()
      await fillTekijänTiedot()

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
      await expectElementNotVisible(".modal__container")

      // Tarkista, että ilmoituksen tiedot ovat tulleet näkyviin (automaattinen reload lomakkeen sulkemisen jälkeen)

      expect(await getIlmoitusData()).toEqual(oppija.expected)
    }
  })
})

const openHakutilanneView = async () => {
  await loginAs(hakutilannePath, "valpas-jkl-normaali", true)
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

const openKuntailmoitusOppijanäkymässä = async () => {
  await clickElement(".oppijaview__ilmoitusbuttonwithinfo button")
  await expectElementEventuallyVisible(".modal__container")
}

const fillTekijänTiedot = async (
  tekijä: { puhelin: string; email: string } = opo
) => {
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

const getIlmoitusForm = async () => {
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

const selectOption = async (select: WebElement, text: string) => {
  const options = await select.findElements({ tagName: "option" })
  const optionTexts = await Promise.all(options.map((o) => o.getText()))
  const index = optionTexts.findIndex((o) => o === text)
  expect(index >= 0, `Valinta "${text}" löytyy valikosta`).toBeTruthy()
  await options[index]!!.click()
}

const fillField = async (input: WebElement, text: string) => {
  await clearTextInputElement(input)
  await input.sendKeys(text)
}

const getCloseButton = () => $(".modalbuttongroup button")

const getIlmoitusData = async (): Promise<DisplayedIlmoitusData> => {
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

const getText = (webElement: WebElement) => webElement.getText()
const getOptionalText = async (webElements: WebElement[]) =>
  webElements[0]?.getText()
