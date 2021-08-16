import {
  dropdownSelect,
  dropdownSelectAllOptionTexts,
  dropdownSelectContains,
} from "../integrationtests-env/browser/forms"

export const selectOrganisaatio = (index: number) =>
  dropdownSelect("#organisaatiovalitsin", index)

export const selectOrganisaatioByNimi = (text: string) =>
  dropdownSelectContains("#organisaatiovalitsin", text)

export const valitsimenOrganisaatiot = () =>
  dropdownSelectAllOptionTexts("#organisaatiovalitsin")
