import { By, WebElement } from "selenium-webdriver"
import { Oid } from "../../src/state/common"
import {
  clickElement,
  expectElementEventuallyVisible,
  expectElementNotVisible,
} from "../integrationtests-env/browser/content"
import { $, $$, testIdIs } from "../integrationtests-env/browser/core"
import { dataTableEventuallyEquals } from "../integrationtests-env/browser/datatable"
import { loginAs } from "../integrationtests-env/browser/reset"
import { eventually } from "../integrationtests-env/browser/utils"
import {
  hakutilannePath,
  jklNormaalikouluTableContent,
} from "./hakutilanne.shared"

type Oppija = {
  oid: Oid
  title: string
  prefill?: number
  asuinkunta?: string
  postinumero?: string
  postitoimipaikka?: string
  katuosoite?: string
  puhelinnumero?: string
  sähköposti?: string
}

const oppijat: Oppija[] = [
  {
    oid: "1.2.246.562.24.00000000001",
    title:
      "Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas (221105A3023)",
    prefill: 0,
  },
  {
    oid: "1.2.246.562.24.00000000028",
    title: "Epäonninen Valpas (301005A336J)",
    asuinkunta: "Helsinki",
    postinumero: "00150",
    postitoimipaikka: "Helsinki",
    katuosoite: "Esimerkkikatu 1234",
  },
  {
    oid: "1.2.246.562.24.00000000014",
    title: "KasiinAstiToisessaKoulussaOllut Valpas (170805A613F)",
    asuinkunta: "Pyhtää",
  },
]

describe("Kuntailmoituksen tekeminen", () => {
  it("happy path", async () => {
    await openHakutilanneView()

    for (const oppija of oppijat) {
      await selectOppija(oppija.oid)
    }

    await openKuntailmoitus()

    const forms = await getForms()
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

      // Esitäytä lomake
      if (oppija.prefill !== undefined) {
        form.prefills[oppija.prefill]!!.click()
      }

      // Täytä lomake
      if (oppija.asuinkunta !== undefined) {
        await selectOption(form.asuinkuntaSelect, oppija.asuinkunta)
      }
      if (oppija.postinumero !== undefined) {
        await fillField(form.postinumeroInput, oppija.postinumero)
      }
      if (oppija.postitoimipaikka !== undefined) {
        await fillField(form.postitoimipaikkaInput, oppija.postitoimipaikka)
      }
      if (oppija.katuosoite !== undefined) {
        await fillField(form.katuosoiteInput, oppija.katuosoite)
      }
      if (oppija.puhelinnumero !== undefined) {
        await fillField(form.puhelinnumeroInput, oppija.puhelinnumero)
      }
      if (oppija.sähköposti !== undefined) {
        await fillField(form.sähköpostiInput, oppija.sähköposti)
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

    // Tarkista, että sulkemisnappulan teksti on vaihtunut
    const button = await getCloseButton()
    expect(await button.getText()).toBe("Valmis")

    // Sulje lomake
    await button.click()
    await expectElementNotVisible(".modal__container")

    // TODO: Tarkista kuntailmoituslistasta, että oppijat ovat nyt siellä. Tämä odottaa listan toteutusta.
  })
})

const openHakutilanneView = async () => {
  await loginAs(hakutilannePath, "valpas-jkl-normaali")
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
  await clickElement(".ilmoitusform__continue")
}

const getForms = async () => {
  const forms = await $$(".ilmoitusform__frame")
  return Promise.all(
    forms.map(async (form) => ({
      root: form,
      title: await form
        .findElement({ className: "ilmoitusform__titletext" })
        .then((e) => e.getText()),
      subtitle: await form
        .findElement({ className: "ilmoitusform__subtitle" })
        .then((e) => e.getText()),
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
  await input.clear()
  await input.sendKeys(text)
}

const getCloseButton = () => $(".modalbuttongroup button")
