import { Oid } from "../../src/state/common"
import { hakutilannePathWithoutOrg } from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyVisible,
} from "../integrationtests-env/browser/content"

export const jklNormaalikouluTableHead = "Hakeutumisvelvollisia oppijoita (47)"
export const jklNormaalikouluTableContent = `
  Amis-valmistunut-eronnut-valmasta Valpas                | 18.6.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | done3 opiskeluoikeutta                                                     |
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
  Keskeyttänyt-17-vuotta-täyttävä-8-luokkalainen Valpas   | 10.11.2004  | 8C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Kotiopetus-menneisyydessä Valpas                        | 6.2.2005    | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  LukioVanhallaOpsilla Valpas                             | 6.7.2004    | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  LukionAineopinnotAloittanut Valpas                      | 4.3.2005    | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  LukionAloittanut Valpas                                 | 29.4.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  LukionAloittanut-ilmo Valpas                            | 11.4.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  LukionAloittanutJaLopettanut-ilmo Valpas                | 5.4.2005    | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  LukionLokakuussaAloittanut Valpas                       | 18.4.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | hourglass_empty3.10.2021 alkaen: Jyväskylän normaalikoulu, Lukiokoulutus   |
  LuokallejäänytYsiluokkalainen Valpas                    | 2.8.2005    | 9A | –          | 2 hakua              | –                           | –                         | –                                                                          |
  LuokallejäänytYsiluokkalainenJatkaa Valpas              | 6.2.2005    | 9B | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Läsnä-17-vuotta-täyttävä-8-luokkalainen Valpas          | 10.11.2004  | 8C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Oppivelvollinen-hetullinen Valpas                       | 3.1.2005    | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas   | 22.11.2005  | 9C | –          | Hakenut open_in_new  | 2. Helsingin medialukio     | doneHelsingin medialukio  | –                                                                          |
  Oppivelvollinen-ysiluokka-kesken-keväällä-2021-rikkinäinen-7-luokka Valpas | 21.3.2005 | 9C | – | Ei hakemusta | –                           | –                         | –                                                                          |
  Oppivelvollinen-ysiluokka-kesken-vsop Valpas            | 24.3.2005   | vsop 9C | –     | Ei hakemusta         | –                           | –                         | –                                                                          |
  Oppivelvollisuus-keskeytetty-ei-opiskele Valpas         | 1.10.2005   | 9C | 15.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Oppivelvollisuus-keskeytetty-määräajaksi Valpas         | 18.10.2005  | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Oppivelvollisuus-keskeytetty-toistaiseksi Valpas        | 15.9.2005   | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Perusopetukseen-valmistautuva-17-vuotta-täyttävä Valpas | 6.1.2004    | valo  | –       | Ei hakemusta         | –                           | –                         | –                                                                          |
  Perusopetukseen-valmistavasta-eronnut-17-vuotta-täyttävä Valpas     | 21.6.2004 | valo  | – | Ei hakemusta         | –                           | –                         | –                                                                          |
  Perusopetukseen-valmistavasta-valmistunut-17-vuotta-täyttävä Valpas | 29.5.2004 | valo  | – | Ei hakemusta         | –                           | –                         | –                                                                          |
  Päällekkäisiä Oppivelvollisuuksia                       | 6.6.2005    | 9B | –          | Hakenut open_in_new  | Hyväksytty (2 hakukohdetta) | doneOmnia                 | –                                                                          |
  SuorittaaPreIB Valpas                                   | 19.7.2004   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, IB-tutkinto                                  |
  Turvakielto Valpas                                      | 29.9.2004   | 9C | –          | Hakenut open_in_new  | warningEi opiskelupaikkaa   | –                         | –                                                                          |
  TurvakieltoTyhjälläKotikunnalla Valpas                  | 28.7.2005   | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  UseampiYsiluokkaSamassaKoulussa Valpas                  | 25.8.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Valmistunut-ei-ysiluokkaa Valpas                        | 24.9.2005   | –  | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Valmistunut-kasiluokkalainen-alle-17-vuotias Valpas     | 9.6.2005    | 8C | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Ysiluokka-eronnut-syksyllä-2021 Valpas                  | 19.6.2004   | 9C | –  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Ysiluokka-valmis-ja-ilmoitettu-ja-uusi-nivelvaihe Valpas | 24.7.2006  | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | doneRessun lukio, Ammatillinen koulutus                                        |
  Ysiluokka-valmis-keväällä-2021 Valpas                   | 19.6.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Ysiluokka-valmis-keväällä-2021-ilmo Valpas              | 26.8.2005   | 9C | 30.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Ysiluokka-valmis-keväällä-2021-vsop Valpas              | 19.7.2005   | vsop 9C | 30.5.2021 | Ei hakemusta     | –                           | –                         | –                                                                          |
  Ysiluokka-valmis-syksyllä-2021 Valpas                   | 19.6.2004   | 9C | 1.9.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
`

export const internationalSchoolTableHead =
  "Hakeutumisvelvollisia oppijoita (13)"
export const internationalSchoolTableContent = `
  Int-school-10-luokalla-ilman-alkamispäivää Valpas                                 | 14.3.2005  | 10B | 30.5.2021           | Ei hakemusta | – | – | International School of Helsinki, International school                                   |
  Int-school-9-luokalta-kesken-eroaja-myöhemmin Valpas                              | 21.4.2005  | 9B  | –                   | Ei hakemusta | – | – | –                                                                                        |
  Int-school-9-luokalta-valmistumisen-jälkeen-eronnut-myöhemmin Valpas              | 10.7.2005  | 9B  | –                   | Ei hakemusta | – | – | –                                                                                        |
  Int-school-9-luokan-jälkeen-int-schoolin-10-luokalla-aloittanut Valpas            | 22.2.2005  | 10B | 30.5.2021           | Ei hakemusta | – | – | doneInternational School of Helsinki, International school                               |
  Int-school-9-luokan-jälkeen-int-schoolin-10-luokalla-lokakuussa-aloittanut Valpas | 8.4.2005   | 10B | 30.5.2021           | Ei hakemusta | – | – | hourglass_empty3.10.2021 alkaen: International School of Helsinki, International school  |
  Int-school-9-luokan-jälkeen-lukion-aloittanut Valpas                              | 12.5.2005  | 9B  | 30.5.2021           | Ei hakemusta | – | – | doneJyväskylän normaalikoulu, Lukiokoulutus                                              |
  Int-school-9-luokan-jälkeen-lukion-lokakuussa-aloittanut Valpas                   | 7.1.2005   | 9B  | 30.5.2021           | Ei hakemusta | – | – | hourglass_empty3.10.2021 alkaen: Jyväskylän normaalikoulu, Lukiokoulutus                 |
  Int-school-9-vahvistettu-lokakuussa Valpas                                        | 22.11.2005 | 10B | Valmistuu 1.10.2021 | Ei hakemusta | – | – | hourglass_empty15.10.2021 alkaen: International School of Helsinki, International school |
  Int-school-eronnut-elokuussa-17-vuotta-täyttävä-8-luokkalainen Valpas             | 22.8.2004  | 8B  | –                   | Ei hakemusta | – | – | –                                                                                        |
  Int-school-yli-2kk-aiemmin-9-valmistunut Valpas                                   | 23.10.2005 | 9B  | 4.7.2021            | Ei hakemusta | – | – | –                                                                                        |
  Int-school-yli-2kk-aiemmin-9-valmistunut-10-jatkanut Valpas                       | 11.11.2005 | 10B | 4.7.2021            | Ei hakemusta | – | – | doneInternational School of Helsinki, International school                               |
  Inter-valmistunut-9-2021 Valpas                                                   | 20.4.2005  | 9B  | 30.5.2021           | Ei hakemusta | – | – | –                                                                                        |
  Oppivelvollinen-int-school-kesken-keväällä-2021 Valpas                            | 18.2.2005  | 9B  | –                   | Ei hakemusta | – | – | –                                                                                        |
`

export const hakutilannePath = hakutilannePathWithoutOrg.href("/virkailija")

export const oppijaRowSelector = (oppijaOid: Oid) =>
  `.hakutilanne .table__row[data-row*="${oppijaOid}"] td:first-child a`

export const openOppijaView = async (oppijaOid: Oid) => {
  const selector = oppijaRowSelector(oppijaOid)
  await expectElementEventuallyVisible(selector)
  await clickElement(selector)
}

export const openAnyOppijaView = () =>
  clickElement(`.hakutilanne .table__row:first-child td:first-child a`)

export const kuntailmoitusRowSelector = (oppijaOid: Oid) =>
  `.kuntailmoitukset .table__row[data-row*="${oppijaOid}"] td:first-child a`
