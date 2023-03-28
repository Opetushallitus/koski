import { kunnanHetuhakuPath } from "../../src/state/paths"
import {
  clickElement,
  textEventuallyEquals,
} from "../integrationtests-env/browser/content"
import {
  cleanupDownloads,
  expectDownloadExists,
} from "../integrationtests-env/browser/downloads"
import {
  inputIsEnabled,
  setTextArea,
} from "../integrationtests-env/browser/forms"
import { loginAs } from "../integrationtests-env/browser/reset"

const heturouhintaPath = kunnanHetuhakuPath.href("/virkailija")

const input = `
  Etunimi Sukunimi (161004A404E)
  Toinen Tyyppi (011005A115P)
  Kolmas Hetuilija (110405A6951)
  Neljäs Ei Näytä Hetulta (404040-4040)
`.trim()

describe("Hetulistalla rouhinta", () => {
  beforeAll(cleanupDownloads)

  it("Submit-nappi ei toimi ilman hetuja", async () => {
    await loginAs(heturouhintaPath, "valpas-monta")
    await expectSubmitButtonIsEnabled(false)

    await fillForm(input)
    await expectSubmitButtonIsEnabled(true)
  })

  it("Henkilötunnusten parsinta selaimessa", async () => {
    await loginAs(heturouhintaPath, "valpas-monta")
    await fillForm(input)
    await expectSubmitButtonTextToEqual("Lataa raportti (3 henkilötunnusta)")
  })

  it.skip("Lomakkeen lähettäminen palauttaa tiedoston", async () => {
    await loginAs(heturouhintaPath, "valpas-monta")
    await fillForm(input)
    await submit()

    await expectDownloadExists("oppijahaku-hetulista-2021-09-05.xlsx")
  })
})

const fillForm = async (text: string) => {
  await setTextArea(".kuntahetulista .textfield__input", text)
}

const expectSubmitButtonTextToEqual = async (expected: string) => {
  await textEventuallyEquals(
    ".kuntahetulista .button--primary .button__content",
    expected
  )
}

const expectSubmitButtonIsEnabled = async (isEnabled: boolean) => {
  const state = await inputIsEnabled(".kuntahetulista .button--primary")
  expect(state).toEqual(isEnabled)
}

const submit = async () => {
  await clickElement(".kuntahetulista .button--primary")
}
