import { render, RenderResult } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import React from "react"
import { disableMissingTranslationWarnings } from "../../../i18n/i18n"
import { KoodistoKoodiviite } from "../../../state/apitypes/koodistot"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../../state/apitypes/oppija"
import { expectToMatchSnapshot } from "../../../utils/tests"
import {
  IlmoitusForm,
  IlmoitusFormValues,
  PrefilledIlmoitusFormValues,
} from "./IlmoitusForm"

describe("IlmoitusForm", () => {
  test("Renderöityy virheittä", () => {
    disableMissingTranslationWarnings()
    expectToMatchSnapshot(createForm())
  })

  test("Pakollisten kenttien täyttäminen enabloi submit-nappulan", () => {
    const form = createForm()
    expectSubmitButtonIsEnabled(form, false)
    selectOption(form, "ilmoituslomake__asuinkunta", 1)
    expectSubmitButtonIsEnabled(form, true)
  })

  test("Fokuksen siirtyminen pois pakollisesta täyttämättömästä kentästä tuo esille virheilmoituksen", () => {
    const form = createForm()

    expectFieldError(form, "ilmoituslomake__asuinkunta", null)

    userEvent.click(form.getByText("ilmoituslomake__asuinkunta"))
    userEvent.click(form.getByText("ilmoituslomake__maa"))

    expectFieldError(
      form,
      "ilmoituslomake__asuinkunta",
      "ilmoituslomake__pakollinen_tieto"
    )
  })

  test("Lomake palauttaa täytetyt arvot", () => {
    const callback = jest.fn()
    const form = createForm(callback)

    userEvent.click(getSubmitButton(form))

    selectOption(form, "ilmoituslomake__asuinkunta", 1)
    selectOption(form, "ilmoituslomake__yhteydenottokieli", 1)
    selectOption(form, "ilmoituslomake__maa", 1)
    fillTextField(form, "ilmoituslomake__postinumero", "00010")
    fillTextField(form, "ilmoituslomake__postitoimipaikka", "Helsinki")
    fillTextField(form, "ilmoituslomake__katuosoite", "Testitie 5")
    fillTextField(form, "ilmoituslomake__puhelinnumero", "0401234567")
    fillTextField(form, "ilmoituslomake__sähköposti", "testi@gmail.com")
    toggleCheckbox(
      form,
      "ilmoituslomake__hakenut_opiskelemaan_yhteishakujen_ulkopuolella"
    )

    userEvent.click(getSubmitButton(form))

    expect(callback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenLastCalledWith({
      asuinkunta: "272",
      hakenutOpiskelemaanYhteyshakujenUlkopuolella: true,
      katuosoite: "Testitie 5",
      maa: "752",
      postinumero: "00010",
      postitoimipaikka: "Helsinki",
      puhelinnumero: "0401234567",
      sähköposti: "testi@gmail.com",
      yhteydenottokieli: "SV",
    })
  })

  test("Lomakkeen yhteystiedot täydentyvät esitäytöllä", () => {
    const callback = jest.fn()
    const form = createForm(callback)

    userEvent.click(form.getByText("1) Yhteishaku kevät 2021"))
    userEvent.click(getSubmitButton(form))

    expect(callback).toHaveBeenLastCalledWith({
      hakenutOpiskelemaanYhteyshakujenUlkopuolella: false,
      maa: "246",
      puhelinnumero: "",
      sähköposti: "",
      yhteydenottokieli: "FI",
      ...mockPrefilledYhteishakuValues.values,
    })
  })
})

const createForm = (onSubmit?: (values: IlmoitusFormValues) => void) =>
  render(
    <IlmoitusForm
      formIndex={0}
      numberOfForms={2}
      oppija={mockOppija}
      kunnat={mockAsuinkunnat}
      maat={mockMaat}
      kielet={mockYhteydenottokielet}
      prefilledValues={[mockPrefilledYhteishakuValues, mockPrefilledDvvValues]}
      onSubmit={onSubmit || (() => {})}
    />
  )

const selectOption = (form: RenderResult, labelText: string, index: number) => {
  const s = getInputContainer(form, labelText)
    ?.getElementsByTagName("select")
    .item(0)
  userEvent.selectOptions(s!!, index.toString())
}

const fillTextField = (form: RenderResult, labelText: string, text: string) => {
  const f = getInputContainer(form, labelText)
    ?.getElementsByTagName("input")
    .item(0)
  userEvent.type(f!!, text)
}

const toggleCheckbox = (form: RenderResult, labelText: string) => {
  const c = form.getByText(labelText).getElementsByTagName("input").item(0)
  userEvent.click(c!!)
}

const expectSubmitButtonIsEnabled = (form: RenderResult, enabled: boolean) => {
  const disabled = getSubmitButton(form)?.disabled
  expect(disabled).not.toBe(enabled)
}

const getSubmitButton = (form: RenderResult) => {
  const button = form
    .getByText("ilmoituslomake__ilmoita_asuinkunnalle")
    .closest("button")
  expect(button).not.toBeNull()
  return button!!
}

const expectFieldError = (
  form: RenderResult,
  labelText: string,
  errorText: string | null
) => {
  const errorElement = getInputContainer(form, labelText)
    .getElementsByClassName("dropdown__error")
    .item(0)

  if (errorText === null) {
    expect(errorElement).toBeNull()
  } else {
    expect(errorElement?.textContent).toBe(errorText)
  }
}

const getInputContainer = (
  form: RenderResult,
  labelText: string
): HTMLElement => {
  const container = form.getByText(labelText).parentElement
  expect(container).not.toBeNull()
  return container!!
}

const mockOppija: OppijaHakutilanteillaSuppeatTiedot = {
  oppija: {
    henkilö: {
      oid: "1.2.246.562.24.00000000001",
      etunimet: "Valpas",
      sukunimi: "Testi-Ukkeli",
    },
    opiskeluoikeudet: [],
    opiskelee: true,
  },
  hakutilanteet: [],
}

const mockKoodisto = (
  uri: string,
  arvot: Record<string, string>
): Array<KoodistoKoodiviite> =>
  Object.entries(arvot).map(([arvo, nimi]) => ({
    koodistoUri: uri,
    koodiarvo: arvo,
    nimi: {
      fi: nimi,
    },
  }))

const mockAsuinkunnat = mockKoodisto("kunta", {
  "091": "Helsinki",
  "179": "Jyväskylä",
  "272": "Kokkola",
})

const mockMaat = mockKoodisto("maatjavaltiot2", {
  "004": "Afganistan",
  "246": "Suomi",
  "752": "Ruotsi",
  "840": "Yhdysvallat (USA)",
})

const mockYhteydenottokielet = mockKoodisto("kielivalikoima", {
  FI: "suomi",
  SV: "ruotsi",
  EN: "englanti",
  AR: "arabia",
})

const mockPrefilledDvvValues: PrefilledIlmoitusFormValues = {
  label: "DVV yhteystiedot",
  values: {
    asuinkunta: "272",
    postinumero: "67100",
    postitoimipaikka: "Kokkola",
    katuosoite: "Esimerkkikatu 123",
  },
}

const mockPrefilledYhteishakuValues: PrefilledIlmoitusFormValues = {
  label: "Yhteishaku kevät 2021",
  values: {
    asuinkunta: "179",
    postinumero: "12345",
    postitoimipaikka: "Jyväskylä",
    katuosoite: "Jytäraitti 83",
  },
}
