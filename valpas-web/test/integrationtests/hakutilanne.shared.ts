import { Oid } from "../../src/state/common"
import { createHakutilannePathWithoutOrg } from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyVisible,
} from "../integrationtests-env/browser/content"

export const jklNormaalikouluTableHead = "Hakeutumisvelvollisia oppijoita (32)"
export const jklNormaalikouluTableContent = `
  Epäonninen Valpas                                       | 30.10.2005  | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Eroaja-myöhemmin Valpas                                 | 29.9.2005   | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Eronnut-elokuussa-17-vuotta-täyttävä-8-luokkalainen Valpas | 11.9.2004 | 8C | –         | Ei hakemusta         | –                           | –                         | –                                                                          |
  Eronnut-kevään-valmistumisjaksolla-17-vuotta-täyttävä-8-luokkalainen Valpas | 12.10.2004 | 8C | – | Ei hakemusta | –                         | –                         | –                                                                          |
  Hetuton Valpas                                          | 1.1.2005    | 9C |–           | Ei hakemusta         | –                           | –                         | –                                                                          |
  Ilmoituksen-lisätiedot–poistettu Valpas                 | 19.5.2005   | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Kahdella-oppija-oidilla Valpas                          | 15.2.2005   | 9C | 30.5.2021  | Hakenut open_in_new  | Varasija: Ressun lukio      | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  Kahdella-oppija-oidilla-ilmo Valpas                     | 4.6.2005    | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  Kahdella-oppija-oidilla-ilmo-2 Valpas                   | 3.6.2005    | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  KahdenKoulunYsi-ilmo Valpas                             | 21.11.2004  | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  KasiinAstiToisessaKoulussaOllut Valpas                  | 17.8.2005   | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  KasiinAstiToisessaKoulussaOllut-ilmo Valpas             | 2.5.2005    | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Kotiopetus-menneisyydessä Valpas                        | 6.2.2005    | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  LukionAineopinnotAloittanut Valpas                      | 4.3.2005    | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  LukionAloittanut Valpas                                 | 29.4.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  LukionAloittanut-ilmo Valpas                            | 11.4.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  LukionAloittanutJaLopettanut-ilmo Valpas                | 5.4.2005    | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  LukionLokakuussaAloittanut Valpas                       | 18.4.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | hourglass_empty3.10.2021 alkaen: Jyväskylän normaalikoulu, Lukiokoulutus   |
  LuokallejäänytYsiluokkalainen Valpas                    | 2.8.2005    | 9A | –          | 2 hakua              | –                           | –                         | –                                                                          |
  LuokallejäänytYsiluokkalainenJatkaa Valpas              | 6.2.2005    | 9B | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Oppivelvollinen-hetullinen Valpas                       | 3.1.2005    | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas   | 22.11.2005  | 9C | –          | Hakenut open_in_new  | 2. Helsingin medialukio     | doneHelsingin medialukio  | –                                                                          |
  Oppivelvollinen-ysiluokka-kesken-vsop Valpas            | 24.3.2005   | vsop 9C | –     | Ei hakemusta         | –                           | –                         | –                                                                          |
  Oppivelvollisuus-keskeytetty-määräajaksi Valpas         | 18.10.2005  | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Oppivelvollisuus-keskeytetty-toistaiseksi Valpas        | 15.9.2005   | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Päällekkäisiä Oppivelvollisuuksia                       | 6.6.2005    | 9B | –          | Hakenut open_in_new  | Hyväksytty (2 hakukohdetta) | doneOmnia                 | –                                                                          |
  Turvakielto Valpas                                      | 29.9.2004   | 9C | –          | Hakenut open_in_new  | warningEi opiskelupaikkaa   | –                         | –                                                                          |
  UseampiYsiluokkaSamassaKoulussa Valpas                  | 25.8.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Valmistunut-kasiluokkalainen-alle-17-vuotias Valpas     | 9.6.2005    | 8C | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Ysiluokka-valmis-keväällä-2021 Valpas                   | 19.6.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Ysiluokka-valmis-keväällä-2021-ilmo Valpas              | 26.8.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Ysiluokka-valmis-keväällä-2021-vsop Valpas              | 19.7.2005   | vsop 9C | 30.5.2021 | Ei hakemusta     | –                           | –                         | –                                                                          |
`

export const hakutilannePath = createHakutilannePathWithoutOrg("/virkailija")

export const openOppijaView = async (oppijaOid: Oid) => {
  const selector = `.hakutilanne .table__row[data-row*="${oppijaOid}"] td:first-child a`
  await expectElementEventuallyVisible(selector)
  await clickElement(selector)
}

export const openAnyOppijaView = () =>
  clickElement(`.hakutilanne .table__row:first-child td:first-child a`)
