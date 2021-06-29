import { act, render, RenderResult, waitFor } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import fetchMock from "jest-fetch-mock"
import React from "react"
import { disableMissingTranslationWarnings } from "../../i18n/i18n"
import { Kieli, KoodistoKoodiviite, Maa } from "../../state/apitypes/koodistot"
import { KuntailmoitusKunta } from "../../state/apitypes/kuntailmoitus"
import { OppijanPohjatiedot } from "../../state/apitypes/kuntailmoituspohjatiedot"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../state/apitypes/oppija"
import { organisaatioWithOid } from "../../state/apitypes/organisaatiot"
import { IlmoitusForm, IlmoitusFormValues } from "./IlmoitusForm"

describe("IlmoitusForm", () => {
  test("Renderöityy virheittä", () => {
    createForm()
  })

  test("Pakollisten kenttien täyttäminen enabloi submit-nappulan", () => {
    const form = createForm()
    expectSubmitButtonIsEnabled(form, false)
    selectOption(form, "ilmoituslomake__asuinkunta *", 1)
    fillTextField(form, "ilmoituslomake__postinumero", "00150")
    fillTextField(form, "ilmoituslomake__postitoimipaikka", "Helsinki")
    fillTextField(form, "ilmoituslomake__katuosoite", "Katu 5")
    fillTextField(form, "ilmoituslomake__puhelinnumero", "04012345678")
    fillTextField(form, "ilmoituslomake__sähköposti", "valpas@gmail.com")
    expectSubmitButtonIsEnabled(form, true)

    fillTextField(form, "ilmoituslomake__postinumero", "00150")
    fillTextField(form, "ilmoituslomake__postitoimipaikka", "Helsinki")
    fillTextField(form, "ilmoituslomake__katuosoite", "Katu 5")
    fillTextField(form, "ilmoituslomake__puhelinnumero", "04012345678")
    fillTextField(form, "ilmoituslomake__sähköposti", "valpas@gmail.com")
    expectSubmitButtonIsEnabled(form, true)
  })

  test("Fokuksen siirtyminen pois pakollisesta täyttämättömästä kentästä tuo esille virheilmoituksen", () => {
    const form = createForm()

    expectFieldError(form, "ilmoituslomake__asuinkunta *", null)

    userEvent.click(form.getByText("ilmoituslomake__asuinkunta *"))
    userEvent.click(form.getByText("ilmoituslomake__maa"))

    expectFieldError(
      form,
      "ilmoituslomake__asuinkunta *",
      "ilmoituslomake__pakollinen_tieto"
    )
  })

  test("Lomake palauttaa täytetyt arvot", async () => {
    const callback = jest.fn()
    const form = createForm(callback)

    await submit(form)

    selectOption(form, "ilmoituslomake__asuinkunta *", 1)
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

    await submit(form, callback)

    expect(callback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenLastCalledWith({
      asuinkunta: "oid.jyvaskyla",
      hakenutOpiskelemaanYhteyshakujenUlkopuolella: true,
      lähiosoite: "Testitie 5",
      maa: "752",
      postinumero: "00010",
      postitoimipaikka: "Helsinki",
      puhelinnumero: "0401234567",
      email: "testi@gmail.com",
      yhteydenottokieli: "SV",
    })
  })

  test("Lomakkeen yhteystiedot täydentyvät esitäytöllä", async () => {
    const callback = jest.fn()
    const form = createForm(callback)

    userEvent.click(form.getByText("1) Yhteishaku kevät 2021"))
    await submit(form, callback)

    expect(callback).toHaveBeenLastCalledWith({
      asuinkunta: "oid.jyvaskyla",
      email: "osoite@email.com",
      hakenutOpiskelemaanYhteyshakujenUlkopuolella: false,
      lähiosoite: "Jytäraitti 83",
      maa: undefined,
      postinumero: "12345",
      postitoimipaikka: "Jyväskylä",
      puhelinnumero: "04012345678",
      yhteydenottokieli: "FI",
    })
  })

  test("Näytä varoitus, jos oppijalla on turvakielto", () => {
    const formEiTurvakieltoa = createForm(undefined, { turvakielto: false })
    expect(
      formEiTurvakieltoa.queryByText("ilmoituslomake__turvakielto_ohje")
    ).toBeNull()

    const formTurvakielto = createForm(undefined, { turvakielto: true })
    expect(
      formTurvakielto.queryByText("ilmoituslomake__turvakielto_ohje")
    ).not.toBeNull()
  })
})

function ignoreOnSubmit(_values: IlmoitusFormValues) {}

const createForm = (
  onSubmit?: (values: IlmoitusFormValues) => void,
  oppijanPohjatiedotPatch?: Partial<OppijanPohjatiedot>
) => {
  disableMissingTranslationWarnings()
  return render(
    <IlmoitusForm
      formIndex={0}
      numberOfForms={2}
      oppijaTiedot={mockOppija.oppija.henkilö}
      opiskeluoikeudet={mockOppija.oppija.opiskeluoikeudet}
      lisätiedot={mockOppija.lisätiedot}
      pohjatiedot={{ ...mockOppijanPohjatiedot, ...oppijanPohjatiedotPatch }}
      kunnat={mockAsuinkunnat}
      maat={mockMaat}
      kielet={mockYhteydenottokielet}
      tekijä={{ organisaatio: organisaatioWithOid("tekijäorganisaatio.oid") }}
      onSubmit={onSubmit || ignoreOnSubmit}
    />
  )
}

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
  const c = form
    .getByText(labelText)
    .parentElement?.getElementsByTagName("input")
    .item(0)
  userEvent.click(c!!)
}

const submit = async (form: RenderResult, onSubmitMock?: jest.Mock) => {
  const numberOfCalls = fetchMock.mock.calls.length
  act(() => userEvent.click(getSubmitButton(form)))
  if (onSubmitMock) {
    await waitFor(() => expect(onSubmitMock).toHaveBeenCalledTimes(1))
    expect(fetchMock).toHaveBeenCalledTimes(numberOfCalls + 1)
  }
}

const expectSubmitButtonIsEnabled = (form: RenderResult, enabled: boolean) => {
  const button = getSubmitButton(form)
  const disabled =
    button?.disabled || button?.classList.contains("button--disabled")
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
  lisätiedot: [],
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

const mockAsuinkunnat: KuntailmoitusKunta[] = [
  {
    oid: "oid.helsinki",
    nimi: { fi: "Helsingin kaupunki" },
    kotipaikka: {
      koodistoUri: "kunta",
      koodiarvo: "123",
      nimi: { fi: "Helsinki" },
    },
  },
  {
    oid: "oid.jyvaskyla",
    nimi: { fi: "Jyväskylän kaupunki" },
    kotipaikka: {
      koodistoUri: "kunta",
      koodiarvo: "321",
      nimi: { fi: "Jyväskylä" },
    },
  },
  {
    oid: "oid.kokkola",
    nimi: { fi: "Kokkolan kaupunki" },
    kotipaikka: {
      koodistoUri: "kunta",
      koodiarvo: "222",
      nimi: { fi: "Kokkola" },
    },
  },
]

const mockMaat = mockKoodisto("maatjavaltiot2", {
  "004": "Afganistan",
  "246": "Suomi",
  "752": "Ruotsi",
  "840": "Yhdysvallat (USA)",
}) as Maa[]

const mockYhteydenottokielet = mockKoodisto("kielivalikoima", {
  FI: "suomi",
  SV: "ruotsi",
}) as Kieli[]

const mockOppijanPohjatiedot: OppijanPohjatiedot = {
  oppijaOid: "123",
  mahdollisetTekijäOrganisaatiot: [],
  yhteydenottokieli: { koodistoUri: "kieli", koodiarvo: "FI" },
  turvakielto: false,
  yhteystiedot: [
    {
      yhteystietojenAlkuperä: {
        hakemusOid: "13232",
        hakuNimi: { fi: "Yhteishaku kevät 2021" },
        hakuOid: "2321",
        haunAlkamispaivämäärä: "2021-03-01",
      },
      yhteystiedot: {
        postinumero: "12345",
        postitoimipaikka: "Jyväskylä",
        lähiosoite: "Jytäraitti 83",
        email: "osoite@email.com",
        puhelinnumero: "04012345678",
      },
      kunta: mockAsuinkunnat[1],
    },
    {
      yhteystietojenAlkuperä: {
        alkuperä: {
          koodiarvo: "alkupera1",
          nimi: {
            fi: "VTJ",
          },
          koodistoUri: "yhteystietojenalkupera",
          koodistoVersio: 1,
        },
        tyyppi: {
          koodistoUri: "yhteystietotyypit",
          koodiarvo: "",
        },
      },
      yhteystiedot: {
        postinumero: "67100",
        postitoimipaikka: "KOKKOLA",
        lähiosoite: "Esimerkkikatu 123",
      },
      kunta: mockAsuinkunnat[2],
    },
  ],
}
