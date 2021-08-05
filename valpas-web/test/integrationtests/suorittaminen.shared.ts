import { Oid } from "../../src/state/common"
import {
  createSuorittaminenHetuhakuPath,
  createSuorittaminenPath,
} from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyVisible,
} from "../integrationtests-env/browser/content"

export const jklNormaalikouluSuorittaminenTableHead = "Oppivelvolliset (11)"
export const jklNormaalikouluSuorittaminenTableContent = `
  Jkl-Lukio-Kulosaarelainen Valpas          |  1.1.2004  | TODO lukiokoulutus            | Opiskeluoikeus voimassa |  1.8.2019  | –         | done2 opiskeluoikeutta                       |  1.1.2022
  Jkl-Nivel-Kulosaarelainen Valpas          |  1.1.2004  | TODO perusopetuksenlisaopetus | Opiskeluoikeus voimassa | 15.8.2012  | –         | done2 opiskeluoikeutta                       |  1.1.2022
  Kahdella-oppija-oidilla Valpas            | 15.2.2005  | TODO lukiokoulutus            | Opiskeluoikeus voimassa |  1.8.2019  | –         | doneJyväskylän normaalikoulu, Lukiokoulutus  | 15.2.2023
  Kahdella-oppija-oidilla-ilmo Valpas       |  4.6.2005  | TODO lukiokoulutus            | Opiskeluoikeus voimassa |  1.8.2019  | –         | doneJyväskylän normaalikoulu, Lukiokoulutus  |  4.6.2023
  Kahdella-oppija-oidilla-ilmo-2 Valpas     |  3.6.2005  | TODO lukiokoulutus            | Opiskeluoikeus voimassa |  1.8.2019  | –         | doneJyväskylän normaalikoulu, Lukiokoulutus  |  3.6.2023
  Kaksois-tutkinnosta-valmistunut Valpas    | 26.9.2005  | TODO lukiokoulutus            | Valmistunut             |  1.8.2019  | 2.9.2021  | –                                            | 26.9.2023
  Lukio-opiskelija Valpas                   |  7.5.2004  | TODO lukiokoulutus            | Opiskeluoikeus voimassa |  1.8.2019  | –         | doneJyväskylän normaalikoulu, Lukiokoulutus  |  7.5.2022
  Lukio-opiskelija-valmistunut Valpas       | 27.11.2005 | TODO lukiokoulutus            | Valmistunut             |  1.8.2019  | 2.9.2021  | –                                            | 27.11.2023
  LukionAloittanut Valpas                   | 29.4.2005  | TODO lukiokoulutus            | Opiskeluoikeus voimassa | 15.8.2021  | –         | doneJyväskylän normaalikoulu, Lukiokoulutus  | 29.4.2023
  LukionAloittanut-ilmo Valpas              | 11.4.2005  | TODO lukiokoulutus            | Opiskeluoikeus voimassa | 15.8.2021  | –         | doneJyväskylän normaalikoulu, Lukiokoulutus  | 11.4.2023
  LukionAloittanutJaLopettanut-ilmo Valpas  |  5.4.2005  | TODO lukiokoulutus            | Opiskeluoikeus voimassa | 15.8.2021  | 19.9.2021 | doneJyväskylän normaalikoulu, Lukiokoulutus  |  5.4.2023
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
