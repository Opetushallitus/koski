import { Oid } from "../../src/state/common"
import {
  createSuorittaminenHetuhakuPath,
  createSuorittaminenPath,
} from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyVisible,
} from "../integrationtests-env/browser/content"

export const jklNormaalikouluSuorittaminenTableHead = "Oppivelvolliset (12)"
export const jklNormaalikouluSuorittaminenTableContent = `
  Jkl-Lukio-Kulosaarelainen Valpas          |  1.1.2004  | Lukion oppimäärä          | Läsnä                   | Jyväskylän normaalikoulu |  1.8.2019  | –         | doneKulosaaren ala-aste, Perusopetus         |  1.1.2022 asti
  Jkl-Nivel-Kulosaarelainen Valpas          |  1.1.2004  | Perusopetuksen lisäopetus | Läsnä                   | Jyväskylän normaalikoulu | 15.8.2012  | –         | doneKulosaaren ala-aste, Perusopetus         |  1.1.2022 asti
  Kahdella-oppija-oidilla Valpas            | 15.2.2005  | Lukion oppimäärä          | Läsnä                   | Jyväskylän normaalikoulu |  1.8.2019  | –         | –                                            | 15.2.2023 asti
  Kahdella-oppija-oidilla-ilmo Valpas       |  4.6.2005  | Lukion oppimäärä          | Läsnä                   | Jyväskylän normaalikoulu |  1.8.2019  | –         | –                                            |  4.6.2023 asti
  Kahdella-oppija-oidilla-ilmo-2 Valpas     |  3.6.2005  | Lukion oppimäärä          | Läsnä                   | Jyväskylän normaalikoulu |  1.8.2019  | –         | –                                            |  3.6.2023 asti
  Kaksois-tutkinnosta-valmistunut Valpas    | 26.9.2005  | Lukion oppimäärä          | Valmistunut             | Jyväskylän normaalikoulu |  1.8.2019  | 2.9.2021  | –                                            | 26.9.2023 asti
  Lukio-opiskelija Valpas                   |  7.5.2004  | Lukion oppimäärä          | Läsnä                   | Jyväskylän normaalikoulu |  1.8.2019  | –         | –                                            |  7.5.2022 asti
  Lukio-opiskelija-valmistunut Valpas       | 27.11.2005 | Lukion oppimäärä          | Valmistunut             | Jyväskylän normaalikoulu |  1.8.2019  | 2.9.2021  | –                                            | 27.11.2023 asti
  LukionAloittanut Valpas                   | 29.4.2005  | Lukion oppimäärä          | Läsnä                   | Jyväskylän normaalikoulu | 15.8.2021  | –         | –                                            | 29.4.2023 asti
  LukionAloittanut-ilmo Valpas              | 11.4.2005  | Lukion oppimäärä          | Läsnä                   | Jyväskylän normaalikoulu | 15.8.2021  | –         | –                                            | 11.4.2023 asti
  LukionAloittanutJaLopettanut-ilmo Valpas  |  5.4.2005  | Lukion oppimäärä          | warningEronnut          | Jyväskylän normaalikoulu | 15.8.2021  | 19.9.2021 | –                                            |  5.4.2023 asti
  LukionLokakuussaAloittanut Valpas         | 18.4.2005  | Lukion oppimäärä          | Läsnä                   | Jyväskylän normaalikoulu |  3.10.2021 | –         | –                                            | 18.4.2023 asti
    `

export const suorittaminenHetuhakuPath = createSuorittaminenHetuhakuPath(
  "/virkailija"
)
export const suorittaminenListaPath = createSuorittaminenPath("/virkailija")

export const openSuorittaminenOppijaView = async (oppijaOid: Oid) => {
  const selector = `.suorittaminen .table__row[data-row*="${oppijaOid}"] td:first-child a`
  await expectElementEventuallyVisible(selector)
  await clickElement(selector)
}

export const openSuorittaminenAnyOppijaView = () =>
  clickElement(`.suorittaminen .table__row:first-child td:first-child a`)
