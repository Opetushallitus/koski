/* eslint-disable mocha/no-identical-title */
import { foreachAsync, repeatAsync } from '../util/iterating'
import { expect as _expect, test } from './base'
import { KoskiKansalainenPage } from './pages/kansalainen/KoskiKansalainenPage'
import { KoskiVSTOppijaPage } from './pages/oppija/KoskiVSTOppijaPage'
import { kansalainen, virkailija } from './setup/auth'
import { Page } from '@playwright/test'

const kotoutumiskoulutus2022 = '1.2.246.562.24.00000000135'
const oppivelvollisilleSuunnattu = '1.2.246.562.24.00000000143'
const jotpaKoulutus = '1.2.246.562.24.00000000140'
const lukutaitokoulutus = '1.2.246.562.24.00000000107'
const kansanopisto = '1.2.246.562.24.00000000105'
const vapaatavoitteinenKoulutus = '1.2.246.562.24.00000000108'
const jotpaKoulutusTiedonsiirto = '1.2.246.562.24.00000000058'
const osaamismerkki = '1.2.246.562.24.00000000164'
const vstMaksuttomuus = '1.2.246.562.24.00000000170'

const openOppijaPage =
  (oppijaOid: string, edit: boolean) =>
  async ({ vstOppijaPage }: { vstOppijaPage: KoskiVSTOppijaPage }) => {
    await vstOppijaPage.goto(oppijaOid)
    if (edit) {
      await vstOppijaPage.edit()
    }
  }

test.describe('Vapaa sivistystyö', () => {
  const expect = _expect.configure({
    timeout: 2000
  })

  test.describe('Katselunäkymä', () => {
    test.use({ storageState: virkailija('kalle') })
    test.beforeAll(async ({ fixtures }) => {
      await fixtures.reset()
    })
    test.describe('VST JOTPA', () => {
      test.beforeEach(openOppijaPage(jotpaKoulutus, false))
      test('Näyttää tiedot oikein', async ({ page }) => {
        const getByTestId = (id: string) => page.getByTestId(`oo.0.${id}`)
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('1.1.2023')
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Läsnä')
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.rahoitus')
        ).toHaveText(
          'Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus'
        )
        await expect(getByTestId('opiskeluoikeus.voimassaoloaika')).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.1.2023 –'
        )
        await expect(getByTestId('suoritukset.0.toimipiste.value')).toHaveText(
          'Varsinais-Suomen kansanopisto'
        )
        await expect(getByTestId('suoritukset.0.tunniste.nimi')).toHaveText(
          'Vapaan sivistystyön koulutus'
        )
        await expect(
          getByTestId('suoritukset.0.tunniste.koodiarvo')
        ).toHaveText('099999')
        await expect(
          getByTestId('suoritukset.0.osasuoritukset.0.nimi.value')
        ).toHaveText('Kuvantekemisen perusvälineistö')

        await expect(
          getByTestId('suoritukset.0.osasuoritukset.0.laajuus.value')
        ).toHaveText('1 op')
        await expect(
          getByTestId('suoritukset.0.osasuoritukset.0.arvosana.value')
        ).toHaveText('9')
        await expect(
          getByTestId('suoritukset.0.osasuoritukset.1.nimi.value')
        ).toHaveText('Kuvallisen viestinnän perusteet')
        await expect(
          getByTestId('suoritukset.0.osasuoritukset.1.laajuus.value')
        ).toHaveText('1 op')
        await expect(
          getByTestId('suoritukset.0.osasuoritukset.1.arvosana.value')
        ).toHaveText('Hyväksytty')
        await expect(
          getByTestId('suoritukset.0.osasuoritukset.2.nimi.value')
        ).toHaveText('Tussitekniikat I ja II')
        await expect(
          getByTestId('suoritukset.0.osasuoritukset.2.laajuus.value')
        ).toHaveText('1 op')
        await getByTestId('suoritukset.0.osasuoritukset.0.expand').click({
          timeout: 2000
        })
        await expect(
          getByTestId(
            'suoritukset.0.osasuoritukset.0.properties.arviointi.0.arvosana.value'
          )
        ).toHaveText('9')
        await expect(
          getByTestId(
            'suoritukset.0.osasuoritukset.0.properties.arviointi.0.date.value'
          )
        ).toHaveText('1.2.2023')
        await expect(
          getByTestId(
            'suoritukset.0.osasuoritukset.0.properties.arviointi.1.arvosana.value'
          )
        ).toHaveText('Hylätty')
        await expect(
          getByTestId(
            'suoritukset.0.osasuoritukset.0.properties.arviointi.1.date.value'
          )
        ).toHaveText('1.1.2023')
        await getByTestId('suoritukset.0.osasuoritukset.1.expand').click()
        await expect(
          getByTestId(
            'suoritukset.0.osasuoritukset.1.properties.arviointi.0.arvosana.value'
          )
        ).toHaveText('Hyväksytty')
        await expect(
          getByTestId(
            'suoritukset.0.osasuoritukset.1.properties.arviointi.0.date.value'
          )
        ).toHaveText('1.3.2023')
        await getByTestId('suoritukset.0.osasuoritukset.2.expand').click()
        await expect(getByTestId('suoritukset.0.yhteensa')).toHaveText('3 op')
      })
    })
    test.describe('VST lukutaitokoulutus', () => {
      test.beforeEach(openOppijaPage(lukutaitokoulutus, false))
      test('Näyttää tiedot oikein', async ({ page }) => {
        const getByTestId = (id: string) => page.getByTestId(`oo.0.${id}`)
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('1.9.2021')
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Läsnä')
        await expect(getByTestId('opiskeluoikeus.voimassaoloaika')).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.9.2021 – 31.5.2022 (Arvioitu päättymispäivä)'
        )
        await expect(getByTestId('suoritukset.0.toimipiste.value')).toHaveText(
          'Itä-Suomen yliopisto'
        )
        await expect(getByTestId('suoritukset.0.tunniste.nimi')).toHaveText(
          'Lukutaitokoulutus oppivelvollisille'
        )
        await expect(
          getByTestId('suoritukset.0.tunniste.koodiarvo')
        ).toHaveText('999911')
        await expect(getByTestId('suoritukset.0.peruste.value')).toHaveText(
          'OPH-2984-2017'
        )
        await expect(getByTestId('suoritukset.0.laajuus.value')).toHaveText(
          '80 op'
        )

        const osasuoritukset = [
          {
            nimi: 'Vuorovaikutustilanteissa toimiminen',
            laajuus: '20 op',
            arvosana: 'Hyväksytty',
            taitotaso: 'C2.2',
            expander: {
              arvosana: 'Hyväksytty',
              arvosanaPvm: '30.10.2021'
            }
          },
          {
            nimi: 'Tekstien lukeminen ja tulkitseminen',
            laajuus: '20 op',
            arvosana: 'Hyväksytty',
            taitotaso: 'C2.2',
            expander: {
              arvosana: 'Hyväksytty',
              arvosanaPvm: '30.10.2021'
            }
          },
          {
            nimi: 'Tekstien kirjoittaminen ja tuottaminen',
            laajuus: '20 op',
            arvosana: 'Hyväksytty',
            taitotaso: 'C2.2',
            expander: {
              arvosana: 'Hyväksytty',
              arvosanaPvm: '30.10.2021'
            }
          },
          {
            nimi: 'Numeeriset taidot',
            laajuus: '20 op',
            arvosana: 'Hyväksytty',
            taitotaso: 'C2.2',
            expander: {
              arvosana: 'Hyväksytty',
              arvosanaPvm: '30.10.2021'
            }
          }
        ]
        let i = 0
        for (const osasuoritus of osasuoritukset) {
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.nimi.value`)
          ).toHaveText(osasuoritus.nimi)
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.laajuus.value`)
          ).toHaveText(osasuoritus.laajuus)
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.arvosana.value`)
          ).toHaveText(osasuoritus.arvosana)
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.taitotaso.value`)
          ).toHaveText(osasuoritus.taitotaso)
          await getByTestId(`suoritukset.0.osasuoritukset.${i}.expand`).click()
          await expect(
            getByTestId(
              `suoritukset.0.osasuoritukset.${i}.properties.arviointi.0.arvosana.value`
            )
          ).toHaveText(osasuoritus.expander.arvosana)
          await expect(
            getByTestId(
              `suoritukset.0.osasuoritukset.${i}.properties.arviointi.0.date.value`
            )
          ).toHaveText(osasuoritus.expander.arvosanaPvm)
          i++
        }
        await expect(getByTestId('suoritukset.0.yhteensa')).toHaveText('80 op')
      })
    })
    test.describe('VST kansanopisto', () => {
      test.beforeEach(openOppijaPage(kansanopisto, false))
      test('Näyttää tiedot oikein', async ({ page }) => {
        const getByTestId = (id: string) => page.getByTestId(`oo.0.${id}`)
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('1.9.2021')
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Läsnä')
        await expect(getByTestId('opiskeluoikeus.voimassaoloaika')).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.9.2021 – 31.5.2022 (Arvioitu päättymispäivä)'
        )
        await expect(getByTestId('suoritukset.0.toimipiste.value')).toHaveText(
          'Varsinais-Suomen kansanopisto'
        )
        await expect(getByTestId('suoritukset.0.tunniste.nimi')).toHaveText(
          'Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille'
        )
        await expect(
          getByTestId('suoritukset.0.tunniste.koodiarvo')
        ).toHaveText('999909')
        await expect(getByTestId('suoritukset.0.peruste.value')).toHaveText(
          'OPH-58-2021'
        )

        type Osasuoritus = {
          nimi: string
          laajuus: string
          expander?: {
            osasuoritukset?: Array<{
              nimi: string
              laajuus: string
              arvosana: string
              extra?: {
                arviointi?: {
                  arvosana: string
                  pvm: string
                }
                kuvaus?: string
                tunnustettu?: string
              }
            }>
          }
        }

        const osasuoritukset: Array<Osasuoritus> = [
          {
            nimi: 'Arjen taidot ja elämänhallinta',
            laajuus: '4 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Arjen rahankäyttö',
                  laajuus: '2 op',
                  arvosana: 'Hyväksytty',
                  extra: {
                    kuvaus: 'Arjen rahankäyttö',
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    }
                  }
                },
                {
                  nimi: 'Mielen liikkeet',
                  laajuus: '2 op',
                  arvosana: 'Hyväksytty',
                  extra: {
                    kuvaus: 'Mielen liikkeet ja niiden havaitseminen',
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '2.11.2021'
                    }
                  }
                }
              ]
            }
          },
          {
            nimi: 'Opiskelu-, itsetuntemus- ja työelämätaidot',
            laajuus: '4 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Oman opiskelutyyli',
                  laajuus: '4 op',
                  arvosana: 'Hyväksytty',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus:
                      'Oman opiskelutyylin analysointi ja tavoitteiden asettaminen'
                  }
                }
              ]
            }
          },
          {
            nimi: 'Matemaattiset perustaidot ja ongelmanratkaisutaidot',
            laajuus: '8 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Matematiikka arjessa',
                  arvosana: 'Hyväksytty',
                  laajuus: '2 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus: 'Matematiikan jokapäiväinen käyttö'
                  }
                },
                {
                  nimi: 'Geometria',
                  arvosana: 'Hyväksytty',
                  laajuus: '4 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus: 'Geometrian perusteet'
                  }
                },
                {
                  nimi: 'Trigonometria',
                  arvosana: 'Hyväksytty',
                  laajuus: '2 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '10.12.2021'
                    },
                    kuvaus: 'Trigonometrian perusteet'
                  }
                }
              ]
            }
          },
          {
            nimi: 'Aktiivinen kansalaisuus',
            laajuus: '4 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Kansalaisuus',
                  arvosana: 'Hyväksytty',
                  laajuus: '4 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus:
                      'Kansalaisuuden merkitys moniarvoisess yhteiskunnasa'
                  }
                }
              ]
            }
          },
          {
            nimi: 'Opiskelu- ja urasuunnittelutaidot',
            laajuus: '4 op',
            expander: {
              osasuoritukset: [
                {
                  arvosana: 'Hyväksytty',
                  laajuus: '4 op',
                  nimi: 'CV:n laadinta',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus: 'CV:n laadinta ja käyttö työnhaussa'
                  }
                }
              ]
            }
          },
          {
            nimi: 'Valinnaiset suuntautumisopinnot',
            laajuus: '31 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Tietokoneen huolto',
                  arvosana: 'Hyväksytty',
                  laajuus: '5 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '12.11.2021'
                    },
                    kuvaus:
                      'Nykyaikaisen tietokoneen tyypilliset huoltotoimenpiteet'
                  }
                },
                {
                  nimi: 'Valaisintekniikka',
                  arvosana: 'Hyväksytty',
                  laajuus: '6 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus: 'Valaisinlähteet ja niiden toiminta'
                  }
                },
                {
                  nimi: 'Taide työkaluna',
                  arvosana: 'Hyväksytty',
                  laajuus: '15 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus: 'Taiteen käyttö työkaluna'
                  }
                },
                {
                  nimi: 'Lukio-opinnot',
                  arvosana: 'Hyväksytty',
                  laajuus: '5 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '12.11.2021'
                    },
                    kuvaus: 'Lukion lyhyen matematiikan kurssi M02',
                    tunnustettu:
                      'Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta'
                  }
                }
              ]
            }
          }
        ]
        let i = 0
        for (const osasuoritus of osasuoritukset) {
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.nimi.value`)
          ).toHaveText(osasuoritus.nimi)
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.laajuus.value`)
          ).toHaveText(osasuoritus.laajuus)
          // Expander
          if (osasuoritus.expander !== undefined) {
            await getByTestId(
              `suoritukset.0.osasuoritukset.${i}.expand`
            ).click()
            if (osasuoritus.expander.osasuoritukset !== undefined) {
              let a = 0
              for (const alaosasuoritus of osasuoritus.expander
                .osasuoritukset) {
                await expect(
                  getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.arvosana.value`
                  )
                ).toHaveText(alaosasuoritus.arvosana)
                await expect(
                  getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.laajuus.value`
                  )
                ).toHaveText(alaosasuoritus.laajuus)
                await expect(
                  getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.nimi.value`
                  )
                ).toHaveText(alaosasuoritus.nimi)
                if (alaosasuoritus.extra !== undefined) {
                  await page
                  getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.expand`
                  ).click()
                  if (alaosasuoritus.extra.kuvaus !== undefined) {
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.kuvaus.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.kuvaus)
                  }
                  if (alaosasuoritus.extra.tunnustettu !== undefined) {
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.tunnustettu.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.tunnustettu)
                  }
                  if (alaosasuoritus.extra.arviointi !== undefined) {
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.arvosana.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.arvosana)
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.date.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.pvm)
                  }
                }
                a++
              }
            }
          }
          i++
        }

        await expect(getByTestId('suoritukset.0.yhteensa')).toHaveText('55 op')
      })
    })
    test.describe('VST Oppivelvollisille suunnattu', () => {
      test.beforeEach(openOppijaPage(oppivelvollisilleSuunnattu, false))
      test('Näyttää tiedot oikein', async ({ page }) => {
        const getByTestId = (id: string) => page.getByTestId(`oo.0.${id}`)
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('1.9.2021')
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Läsnä')
        await expect(getByTestId('opiskeluoikeus.voimassaoloaika')).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.9.2021 – 31.5.2022 (Arvioitu päättymispäivä)'
        )
        await expect(getByTestId('suoritukset.0.toimipiste.value')).toHaveText(
          'Varsinais-Suomen kansanopisto'
        )
        await expect(getByTestId('suoritukset.0.tunniste.nimi')).toHaveText(
          'Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille'
        )
        await expect(
          getByTestId('suoritukset.0.tunniste.koodiarvo')
        ).toHaveText('999909')
        await expect(getByTestId('suoritukset.0.peruste.value')).toHaveText(
          'OPH-58-2021'
        )

        type Osasuoritus = {
          nimi: string
          laajuus: string
          expander?: {
            osasuoritukset?: Array<{
              nimi: string
              laajuus: string
              arvosana: string
              extra?: {
                arviointi?: {
                  arvosana: string
                  pvm: string
                }
                kuvaus?: string
                tunnustettu?: string
              }
            }>
          }
        }

        const osasuoritukset: Array<Osasuoritus> = [
          {
            nimi: 'Arjen taidot ja elämänhallinta',
            laajuus: '4 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Arjen rahankäyttö',
                  laajuus: '2 op',
                  arvosana: 'Hyväksytty',
                  extra: {
                    kuvaus: 'Arjen rahankäyttö',
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    }
                  }
                },
                {
                  nimi: 'Mielen liikkeet',
                  laajuus: '2 op',
                  arvosana: 'Hyväksytty',
                  extra: {
                    kuvaus: 'Mielen liikkeet ja niiden havaitseminen',
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '2.11.2021'
                    }
                  }
                }
              ]
            }
          },
          {
            nimi: 'Opiskelu-, itsetuntemus- ja työelämätaidot',
            laajuus: '4 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Oman opiskelutyyli',
                  laajuus: '4 op',
                  arvosana: 'Hyväksytty',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus:
                      'Oman opiskelutyylin analysointi ja tavoitteiden asettaminen'
                  }
                }
              ]
            }
          },
          {
            nimi: 'Matemaattiset perustaidot ja ongelmanratkaisutaidot',
            laajuus: '8 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Matematiikka arjessa',
                  arvosana: 'Hyväksytty',
                  laajuus: '2 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus: 'Matematiikan jokapäiväinen käyttö'
                  }
                },
                {
                  nimi: 'Geometria',
                  arvosana: 'Hyväksytty',
                  laajuus: '4 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus: 'Geometrian perusteet'
                  }
                },
                {
                  nimi: 'Trigonometria',
                  arvosana: 'Hyväksytty',
                  laajuus: '2 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '10.12.2021'
                    },
                    kuvaus: 'Trigonometrian perusteet'
                  }
                }
              ]
            }
          },
          {
            nimi: 'Aktiivinen kansalaisuus',
            laajuus: '4 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Kansalaisuus',
                  arvosana: 'Hyväksytty',
                  laajuus: '4 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus:
                      'Kansalaisuuden merkitys moniarvoisess yhteiskunnasa'
                  }
                }
              ]
            }
          },
          {
            nimi: 'Opiskelu- ja urasuunnittelutaidot',
            laajuus: '4 op',
            expander: {
              osasuoritukset: [
                {
                  arvosana: 'Hyväksytty',
                  laajuus: '4 op',
                  nimi: 'CV:n laadinta',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus: 'CV:n laadinta ja käyttö työnhaussa'
                  }
                }
              ]
            }
          },
          {
            nimi: 'Valinnaiset suuntautumisopinnot',
            laajuus: '31 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Tietokoneen huolto',
                  arvosana: 'Hyväksytty',
                  laajuus: '5 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '12.11.2021'
                    },
                    kuvaus:
                      'Nykyaikaisen tietokoneen tyypilliset huoltotoimenpiteet'
                  }
                },
                {
                  nimi: 'Valaisintekniikka',
                  arvosana: 'Hyväksytty',
                  laajuus: '6 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus: 'Valaisinlähteet ja niiden toiminta'
                  }
                },
                {
                  nimi: 'Taide työkaluna',
                  arvosana: 'Hyväksytty',
                  laajuus: '15 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '30.10.2021'
                    },
                    kuvaus: 'Taiteen käyttö työkaluna'
                  }
                },
                {
                  nimi: 'Lukio-opinnot',
                  arvosana: 'Hyväksytty',
                  laajuus: '5 op',
                  extra: {
                    arviointi: {
                      arvosana: 'Hyväksytty',
                      pvm: '12.11.2021'
                    },
                    kuvaus: 'Lukion lyhyen matematiikan kurssi M02',
                    tunnustettu:
                      'Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta'
                  }
                }
              ]
            }
          }
        ]
        let i = 0
        for (const osasuoritus of osasuoritukset) {
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.nimi.value`)
          ).toHaveText(osasuoritus.nimi)
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.laajuus.value`)
          ).toHaveText(osasuoritus.laajuus)
          // Expander
          if (osasuoritus.expander !== undefined) {
            await getByTestId(
              `suoritukset.0.osasuoritukset.${i}.expand`
            ).click()
            if (osasuoritus.expander.osasuoritukset !== undefined) {
              let a = 0
              for (const alaosasuoritus of osasuoritus.expander
                .osasuoritukset) {
                await expect(
                  getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.arvosana.value`
                  )
                ).toHaveText(alaosasuoritus.arvosana)
                await expect(
                  getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.laajuus.value`
                  )
                ).toHaveText(alaosasuoritus.laajuus)
                await expect(
                  getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.nimi.value`
                  )
                ).toHaveText(alaosasuoritus.nimi)
                if (alaosasuoritus.extra !== undefined) {
                  await getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.expand`
                  ).click()
                  if (alaosasuoritus.extra.kuvaus !== undefined) {
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.kuvaus.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.kuvaus)
                  }
                  if (alaosasuoritus.extra.tunnustettu !== undefined) {
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.tunnustettu.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.tunnustettu)
                  }
                  if (alaosasuoritus.extra.arviointi !== undefined) {
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.arvosana.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.arvosana)
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.date.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.pvm)
                  }
                }
                a++
              }
            }
          }
          i++
        }

        await expect(getByTestId('suoritukset.0.yhteensa')).toHaveText('55 op')
      })
    })
    test.describe('VST Kotoutumiskoulutus oppivelvollisille', () => {
      test.beforeEach(openOppijaPage(kotoutumiskoulutus2022, false))
      test('Näyttää tiedot oikein', async ({ page }) => {
        const getByTestId = (id: string) => page.getByTestId(`oo.0.${id}`)
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('1.8.2022')
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Läsnä')
        await expect(getByTestId('opiskeluoikeus.voimassaoloaika')).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.8.2022 –'
        )
        await expect(getByTestId('suoritukset.0.toimipiste.value')).toHaveText(
          'Varsinais-Suomen kansanopisto'
        )
        await expect(getByTestId('suoritukset.0.tunniste.nimi')).toHaveText(
          'Kotoutumiskoulutus oppivelvollisille'
        )
        await expect(
          getByTestId('suoritukset.0.tunniste.koodiarvo')
        ).toHaveText('999910')
        await expect(getByTestId('suoritukset.0.peruste.value')).toHaveText(
          'OPH-649-2022'
        )

        type Osasuoritus = {
          nimi: string
          laajuus: string
          expander?: {
            osasuoritukset?: Array<{
              nimi: string
              laajuus: string
              arvosana?: string
              extra?: {
                arviointi?: {
                  arvosana: string
                  pvm: string
                }
                kuvaus?: string
                tunnustettu?: string
              }
            }>
          }
        }

        const osasuoritukset: Array<Osasuoritus> = [
          {
            nimi: 'Kieli- ja viestintäosaaminen',
            laajuus: '20 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Kuullun ymmärtäminen (suullinen vastaanottaminen)',
                  laajuus: '10 op',
                  arvosana: 'A1.1',
                  extra: {
                    arviointi: {
                      arvosana: 'A1.1',
                      pvm: '–'
                    }
                  }
                },
                {
                  nimi: 'Luetun ymmärtäminen (kirjallinen vastaanottaminen)',
                  laajuus: '10 op',
                  arvosana: 'B1.2',
                  extra: {
                    arviointi: {
                      arvosana: 'B1.2',
                      pvm: '–'
                    }
                  }
                }
              ]
            }
          },
          {
            nimi: 'Yhteiskunta- ja työelämäosaaminen',
            laajuus: '12 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Yhteiskunnan perusrakenteet ja maantuntemus',
                  laajuus: '3 op'
                },
                {
                  nimi: 'Yhteiskunnan peruspalvelut',
                  laajuus: '3 op'
                },
                {
                  nimi: 'Ammatti- ja koulutuspalvelut',
                  laajuus: '2 op'
                },
                {
                  nimi: 'Työssäoppiminen',
                  laajuus: '4 op'
                }
              ]
            }
          },
          {
            nimi: 'Ohjaus',
            laajuus: '4 op'
          },
          {
            nimi: 'Valinnaiset opinnot',
            laajuus: '3 op',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Studiotekniikka',
                  laajuus: '3 op',
                  extra: {
                    kuvaus: 'Studiotekniikka'
                  }
                }
              ]
            }
          }
        ]
        let i = 0
        for (const osasuoritus of osasuoritukset) {
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.nimi.value`)
          ).toHaveText(osasuoritus.nimi)
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.laajuus.value`)
          ).toHaveText(osasuoritus.laajuus)
          // Expander
          if (osasuoritus.expander !== undefined) {
            await getByTestId(
              `suoritukset.0.osasuoritukset.${i}.expand`
            ).click()
            if (osasuoritus.expander.osasuoritukset !== undefined) {
              let a = 0
              for (const alaosasuoritus of osasuoritus.expander
                .osasuoritukset) {
                if (alaosasuoritus.arvosana) {
                  await expect(
                    getByTestId(
                      `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.arvosana.value`
                    )
                  ).toHaveText(alaosasuoritus.arvosana)
                }
                if (alaosasuoritus.laajuus) {
                  await expect(
                    getByTestId(
                      `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.laajuus.value`
                    )
                  ).toHaveText(alaosasuoritus.laajuus)
                }
                await expect(
                  getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.nimi.value`
                  )
                ).toHaveText(alaosasuoritus.nimi)
                if (alaosasuoritus.extra !== undefined) {
                  await getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.expand`
                  ).click()
                  if (alaosasuoritus.extra.kuvaus !== undefined) {
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.kuvaus.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.kuvaus)
                  }
                  if (alaosasuoritus.extra.tunnustettu !== undefined) {
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.tunnustettu.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.tunnustettu)
                  }
                  if (alaosasuoritus.extra.arviointi !== undefined) {
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.arvosana.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.arvosana)
                  }
                }
                a++
              }
            }
          }
          i++
        }

        await expect(getByTestId('suoritukset.0.yhteensa')).toHaveText('39 op')
      })
    })
    test.describe('Vapaatavoitteinen vst-koulutus', () => {
      test.beforeEach(openOppijaPage(vapaatavoitteinenKoulutus, false))
      test('Näyttää tiedot oikein', async ({ page }) => {
        const getByTestId = (id: string) => page.getByTestId(`oo.0.${id}`)
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('31.5.2022')
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Hyväksytysti suoritettu')
        await expect(getByTestId('opiskeluoikeus.voimassaoloaika')).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 31.5.2022 – 31.5.2022'
        )
        await expect(getByTestId('suoritukset.0.toimipiste.value')).toHaveText(
          'Varsinais-Suomen kansanopisto'
        )
        await expect(getByTestId('suoritukset.0.tunniste.nimi')).toHaveText(
          'Vapaan sivistystyön koulutus'
        )
        await expect(
          getByTestId('suoritukset.0.tunniste.koodiarvo')
        ).toHaveText('099999')
        await expect(
          getByTestId('suoritukset.0.opintokokonaisuus.value')
        ).toHaveText('1138 Kuvallisen ilmaisun perusteet ja välineet')

        type Osasuoritus = {
          nimi: string
          laajuus: string
          arvosana: string
          expander?: {
            osasuoritukset?: Array<{
              nimi: string
              laajuus: string
              arvosana: string
              extra?: {
                arviointi?: {
                  arvosana: string
                  pvm: string
                }
                kuvaus?: string
              }
            }>
          }
        }

        const osasuoritukset: Array<Osasuoritus> = [
          {
            nimi: 'Sienestämisen kokonaisuus',
            laajuus: '5 op',
            arvosana: '2',
            expander: {
              osasuoritukset: [
                {
                  nimi: 'Sienien tunnistaminen 1',
                  laajuus: '5 op',
                  arvosana: '2',
                  extra: {
                    kuvaus: 'Sienien tunnistaminen 1',
                    arviointi: {
                      arvosana: '2',
                      pvm: '30.10.2021'
                    }
                  }
                }
              ]
            }
          }
        ]
        let i = 0
        for (const osasuoritus of osasuoritukset) {
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.nimi.value`)
          ).toHaveText(osasuoritus.nimi)
          await expect(
            getByTestId(`suoritukset.0.osasuoritukset.${i}.laajuus.value`)
          ).toHaveText(osasuoritus.laajuus)
          // Expander
          if (osasuoritus.expander !== undefined) {
            await getByTestId(
              `suoritukset.0.osasuoritukset.${i}.expand`
            ).click()
            if (osasuoritus.expander.osasuoritukset !== undefined) {
              let a = 0
              for (const alaosasuoritus of osasuoritus.expander
                .osasuoritukset) {
                await expect(
                  getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.arvosana.value`
                  )
                ).toHaveText(alaosasuoritus.arvosana)
                await expect(
                  getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.laajuus.value`
                  )
                ).toHaveText(alaosasuoritus.laajuus)
                await expect(
                  getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.nimi.value`
                  )
                ).toHaveText(alaosasuoritus.nimi)
                if (alaosasuoritus.extra !== undefined) {
                  await getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.expand`
                  ).click()
                  if (alaosasuoritus.extra.kuvaus !== undefined) {
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.kuvaus.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.kuvaus)
                  }
                  if (alaosasuoritus.extra.arviointi !== undefined) {
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.arvosana.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.arvosana)
                    await expect(
                      getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.date.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.pvm)
                  }
                }
                a++
              }
            }
          }
          i++
        }

        await expect(getByTestId('suoritukset.0.yhteensa')).toHaveText('5 op')
      })
    })
    test.describe('VST osaamismerkki', () => {
      test.beforeEach(openOppijaPage(osaamismerkki, false))
      test('Näyttää tiedot oikein', async ({ page }) => {
        const getByTestId = (id: string) => page.getByTestId(`oo.0.${id}`)
        await expect(getByTestId('opiskeluoikeus.kuva')).toBeVisible()
        await expect(getByTestId('opiskeluoikeus.nimi')).toHaveText(
          'Varsinais-Suomen kansanopisto, oma osaamispolku(2024, hyväksytysti suoritettu)'
        )
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('1.1.2024')
        await expect(
          getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Hyväksytysti suoritettu')
        await expect(getByTestId('opiskeluoikeus.voimassaoloaika')).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.1.2024 – 1.1.2024'
        )
        await expect(getByTestId('suoritusTabs.0.tab')).toHaveText(
          'Vapaan sivistystyön osaamismerkki'
        )
        await expect(getByTestId('suoritukset.0.toimipiste.value')).toHaveText(
          'Varsinais-Suomen kansanopisto'
        )
        await expect(
          getByTestId('suoritukset.0.tunniste.koodiarvo-ja-nimi')
        ).toHaveText('1001 Oma osaamispolku')
        await expect(
          getByTestId('suoritukset.0.arviointi.0.arvosana.value')
        ).toHaveText('Hyväksytty')
        await expect(
          getByTestId('suoritukset.0.arviointi.0.date.value')
        ).toHaveText('1.1.2024')
        await expect(
          getByTestId('suoritukset.0.suorituksenVahvistus.value.status')
        ).toHaveText('Suoritus valmis')
        await expect(
          getByTestId('suoritukset.0.suorituksenVahvistus.value.details')
        ).toHaveText('Vahvistus: 1.1.2024 Jyväskylän normaalikoulu')
        await expect(
          getByTestId('suoritukset.0.suorituksenVahvistus.value.henkilö.0')
        ).toHaveText('Reijo Reksi (rehtori)')
      })
    })
    test.describe('VST maksuttomuus', () => {
      test.beforeEach(openOppijaPage(vstMaksuttomuus, false))
      test('Lisätiedot nappi näkyvissä', async ({ page }) => {
        await expect(
          page.getByTestId('oo.0.opiskeluoikeus.lisätiedotButton')
        ).toBeVisible()
      })
    })
  })

  test.describe('Muokkausnäkymä', () => {
    test.use({ storageState: virkailija('kalle') })
    test.beforeEach(async ({ fixtures }) => {
      await fixtures.reset()
    })

    test.describe('Vapaatavoitteinen', () => {
      test.beforeEach(openOppijaPage(vapaatavoitteinenKoulutus, true))

      test('Uuden osasuorituksen lisäys', async ({ vstOppijaPage }) => {
        const nimi = 'Lopeta turha kiihkoilu'
        await vstOppijaPage.addNewOsasuoritus(nimi)

        expect(await vstOppijaPage.osasuoritusOptions()).toEqual([
          'Lisää osasuoritus',
          nimi
        ])
        expect(await vstOppijaPage.suorituksenLaajuus()).toEqual('6 op')

        await vstOppijaPage.tallenna()

        expect(await vstOppijaPage.osasuoritus(1).nimi()).toEqual(nimi)
        expect(await vstOppijaPage.osasuoritus(1).laajuus()).toEqual('1 op')
        expect(await vstOppijaPage.suorituksenLaajuus()).toEqual('6 op')
      })

      test('Vaihda laajuutta ja arvosanaa', async ({ vstOppijaPage }) => {
        const osasuoritus = vstOppijaPage.osasuoritus(0)
        await osasuoritus.setLaajuus(5)
        await osasuoritus.setVapaatavoitteinenArvosana(4)

        expect(await osasuoritus.nimi()).toEqual('Sienestämisen kokonaisuus')
        expect(await osasuoritus.laajuus()).toEqual('5')
        expect(await osasuoritus.arvosana()).toEqual('4')

        await osasuoritus.setSuoritusarvosana(true, true)

        expect(await osasuoritus.nimi()).toEqual('Sienestämisen kokonaisuus')
        expect(await osasuoritus.laajuus()).toEqual('5')
        expect(await osasuoritus.arvosana()).toEqual('Hyväksytty')

        await vstOppijaPage.tallenna()

        const tallennettuOsasuoritus = vstOppijaPage.osasuoritus(0)
        expect(await tallennettuOsasuoritus.nimi()).toEqual(
          'Sienestämisen kokonaisuus'
        )
        expect(await tallennettuOsasuoritus.laajuus()).toEqual('5 op')
        expect(await tallennettuOsasuoritus.arvosana()).toEqual('Hyväksytty')
      })

      test('Vaihda arvostelun päivämäärää', async ({ vstOppijaPage }) => {
        const osasuoritus = vstOppijaPage.osasuoritus(0)
        await osasuoritus.expand()
        await osasuoritus.setArvostelunPvm('1.1.2022')
        expect(await osasuoritus.arvostelunPvm()).toEqual('1.1.2022')

        await vstOppijaPage.tallenna()

        const tallennettuOsasuoritus = vstOppijaPage.osasuoritus(0)
        expect(await tallennettuOsasuoritus.arvostelunPvm()).toEqual('1.1.2022')
      })

      test('Lisää alaosasuoritus', async ({ vstOppijaPage }) => {
        const nimi = 'Sienien tunnistaminen 2'
        const osasuoritus = vstOppijaPage.osasuoritus(0)
        await osasuoritus.expand()
        await osasuoritus.addNewAlaosasuoritus(nimi)

        expect(await osasuoritus.alaosasuoritusOptions()).toEqual([
          'Lisää osasuoritus',
          nimi
        ])
        await expect(await osasuoritus.alaosasuoritus(1).nimi()).toEqual(nimi)

        await vstOppijaPage.tallennaVirheellisenä(
          'Valmiiksi merkityllä suorituksella SK-K (Sienestämisen kokonaisuus) on keskeneräinen osasuoritus Sienien tunnistaminen 2 (Sienien tunnistaminen 2)',
          'Suorituksen SK-K (Sienestämisen kokonaisuus) osasuoritusten laajuuksien summa 6.0 ei vastaa suorituksen laajuutta 5.0'
        )
      })

      test('Osasuorituksen poisto', async ({ vstOppijaPage }) => {
        const ylinOsasuoritus = vstOppijaPage.osasuoritus(0)
        const alkuperäinenNimi = await ylinOsasuoritus.nimi()

        // Lisää suoritukseen toinen osasuoritus
        const nimi = 'Testikurssi'
        await vstOppijaPage.addNewOsasuoritus(nimi)

        // Poista ensimmäinen osasuoritus
        await ylinOsasuoritus.delete()

        // Kärkeen noussut osasuoritus pitäisi olla sama mikä luotiin hetki sitten
        expect(await ylinOsasuoritus.nimi()).toEqual(nimi)

        await vstOppijaPage.tallennaVirheellisenä(
          'Vapaatavoitteisella vapaan sivistystyön koulutuksella tulee olla vähintään yksi arvioitu osasuoritus'
        )

        // Poistu muokkaustilasta -> kärjessä pitäisi olla taas alkuperäinen osasuoritus
        await vstOppijaPage.cancelEdit()
        expect(await ylinOsasuoritus.nimi()).toEqual(alkuperäinenNimi)
      })

      test('Alaosasuorituksen poisto', async ({ vstOppijaPage }) => {
        // Lisää osasuoritukseen toinen alaosasuoritus
        const nimi = 'Testikurssi'
        const osasuoritus = vstOppijaPage.osasuoritus(0)

        await osasuoritus.expand()
        await osasuoritus.addNewAlaosasuoritus(nimi)

        // Poista ensimmäinen alaosasuoritus
        await osasuoritus.deleteAlaosasuoritus(0)

        // Kärkeen noussut alaosasuoritus pitäisi olla sama mikä luotiin hetki sitten
        expect(await osasuoritus.alaosasuoritus(0).nimi()).toEqual(nimi)

        await vstOppijaPage.tallennaVirheellisenä(
          'Valmiiksi merkityllä suorituksella SK-K (Sienestämisen kokonaisuus) on keskeneräinen osasuoritus Testikurssi (Testikurssi)',
          'Suorituksen SK-K (Sienestämisen kokonaisuus) osasuoritusten laajuuksien summa 1.0 ei vastaa suorituksen laajuutta 5.0'
        )
      })

      test('Tilan muokkaaminen', async ({ vstOppijaPage }) => {
        const tila = vstOppijaPage.$.opiskeluoikeus.tila.edit
        expect(await tila.add.isVisible()).toBeFalsy()
        await tila.items(0).remove.click()
        expect(await tila.add.isVisible()).toBeTruthy()
        await tila.add.click()
        await tila.modal.date.set('1.1.2022')
        await tila.modal.tila.set('keskeytynyt')
        await tila.modal.submit.click()
        expect(await tila.items(0).date.value(true)).toEqual('1.1.2022')
        expect(await tila.items(0).tila.value()).toEqual('Keskeytynyt')

        await vstOppijaPage.tallennaVirheellisenä(
          'suoritus.vahvistus.päivä (2022-05-31) oltava sama tai aiempi kuin päättymispäivä (2022-01-01)',
          "Vahvistetulla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Keskeytynyt'"
        )
      })

      test('Suorituksen vahvistus', async ({ vstOppijaPage }) => {
        const vahvistaminen =
          vstOppijaPage.$.suoritukset(0).suorituksenVahvistus

        // Palauta keskeneräiseksi
        expect(
          await vahvistaminen.edit.merkitseKeskeneräiseksi.isDisabled()
        ).toBeFalsy()
        await vstOppijaPage.$.opiskeluoikeus.tila.edit.items(0).remove.click()
        await vahvistaminen.edit.merkitseKeskeneräiseksi.click()

        // Merkitse takaisin valmiiksi
        await vstOppijaPage.vahvistaSuoritusUudellaHenkilöllä(
          'Reijo',
          '1.1.2023',
          'Rehtori'
        )

        await vstOppijaPage.tallennaVirheellisenä(
          'lessThanMinimumNumberOfItems: opiskeluoikeudet.0.tila.opiskeluoikeusjaksot'
        )
      })

      test('Suorituksen vahvistusta ilman titteliä ei voi lisätä', async ({
        vstOppijaPage
      }) => {
        const vahvistaminen =
          vstOppijaPage.$.suoritukset(0).suorituksenVahvistus

        // Palauta keskeneräiseksi
        expect(
          await vahvistaminen.edit.merkitseKeskeneräiseksi.isDisabled()
        ).toBeFalsy()
        await vstOppijaPage.$.opiskeluoikeus.tila.edit.items(0).remove.click()
        await vahvistaminen.edit.merkitseKeskeneräiseksi.click()

        // Syötä vahvistuksen tiedot, paitsi ei titteliä
        await vahvistaminen.edit.merkitseValmiiksi.click()
        await vahvistaminen.edit.modal.date.set('1.1.2023')
        const myöntäjät = vahvistaminen.edit.modal.organisaatiohenkilöt.edit
        await myöntäjät.add.set('__NEW__')
        const henkilö = myöntäjät.henkilö(0).newHenkilö
        await henkilö.nimi.set('Keijo')

        expect(vahvistaminen.edit.modal.submit.isDisabled()).toBeTruthy()
      })
    })

    test.describe('Lukutaitokoulutus', () => {
      test.beforeEach(openOppijaPage(lukutaitokoulutus, true))

      const kokonaisuudet = {
        vstlukutaitokoulutuksennumeeristentaitojenkokonaisuudensuoritus:
          'Numeeriset taidot',
        vstlukutaitokoulutuksentekstienkirjoittamisenkokonaisuudensuoritus:
          'Tekstien kirjoittaminen ja tuottaminen',
        vstlukutaitokoulutuksentekstienlukemisenkokonaisuudensuoritus:
          'Tekstien lukeminen ja tulkitseminen',
        vstlukutaitokoulutuksenvuorovaikutustilannekokonaisuudensuoritus:
          'Vuorovaikutustilanteissa toimiminen'
      }

      const poistaKaikkiKokonaisuudet = async (
        vstOppijaPage: KoskiVSTOppijaPage
      ) => repeatAsync(4)(() => vstOppijaPage.removeOsasuoritus(0))

      test('Uusien kokonaisuuksien lisäys', async ({ vstOppijaPage }) => {
        expect(await vstOppijaPage.osasuoritusOptions()).toEqual(
          Object.values(kokonaisuudet)
        )
        await poistaKaikkiKokonaisuudet(vstOppijaPage)
        for (const koodi of Object.keys(kokonaisuudet)) {
          await vstOppijaPage.addOsasuoritus(koodi)
        }
        await foreachAsync(Object.values(kokonaisuudet))(async (nimi, i) => {
          const kokonaisuus = vstOppijaPage.osasuoritus(i)
          expect(await kokonaisuus.nimi()).toEqual(nimi)
          await kokonaisuus.setLaajuus(20)
          expect(await kokonaisuus.laajuus()).toEqual('20')
        })
        expect(await vstOppijaPage.suorituksenLaajuus()).toEqual(
          `${Object.values(kokonaisuudet).length * 20} op`
        )

        await vstOppijaPage.tallenna()

        await foreachAsync(Object.values(kokonaisuudet))(async (nimi, i) => {
          const kokonaisuus = vstOppijaPage.osasuoritus(i)
          expect(await kokonaisuus.nimi()).toEqual(nimi)
          expect(await kokonaisuus.laajuus()).toEqual('20 op')
        })
      })

      test('Vaihda laajuutta, arvosanaa ja taitotasoa', async ({
        vstOppijaPage
      }) => {
        await poistaKaikkiKokonaisuudet(vstOppijaPage)
        for (const koodi of Object.keys(kokonaisuudet)) {
          await vstOppijaPage.addOsasuoritus(koodi)
        }
        await foreachAsync(Object.values(kokonaisuudet))(async (nimi, i) => {
          const kokonaisuus = vstOppijaPage.osasuoritus(i)
          expect(await kokonaisuus.nimi()).toEqual(nimi)
          expect(await kokonaisuus.laajuus()).toEqual('1')
          expect(await kokonaisuus.arvosana()).toEqual('')
          expect(await kokonaisuus.taitotaso()).toEqual('')
          await kokonaisuus.setLaajuus(20)
          expect(await kokonaisuus.laajuus()).toEqual('20')
        })

        const osasuoritus = vstOppijaPage.osasuoritus(3)
        expect(await osasuoritus.nimi()).toEqual(
          'Vuorovaikutustilanteissa toimiminen'
        )

        await osasuoritus.setSuoritusarvosana(true)
        await osasuoritus.setKielenTaitotaso('C1.1')

        expect(await osasuoritus.laajuus()).toEqual('20')
        expect(await osasuoritus.arvosana()).toEqual('Hyväksytty')
        expect(await osasuoritus.taitotaso()).toEqual('C1.1')

        await vstOppijaPage.tallenna()

        const tallennettu = vstOppijaPage.osasuoritus(3)
        expect(await tallennettu.laajuus()).toEqual('20 op')
        expect(await tallennettu.arvosana()).toEqual('Hyväksytty')
        expect(await tallennettu.taitotaso()).toEqual('C1.1')
      })

      test('Vaihda arvostelun päivämäärää', async ({ vstOppijaPage }) => {
        const osasuoritus = vstOppijaPage.osasuoritus(0)
        await osasuoritus.expand()
        await osasuoritus.setArvostelunPvm('1.1.2022')
        expect(await osasuoritus.arvostelunPvm()).toEqual('1.1.2022')

        await vstOppijaPage.tallenna()

        const tallennettu = vstOppijaPage.osasuoritus(0)
        expect(await tallennettu.arvostelunPvm()).toEqual('1.1.2022')
      })

      test('Tilan muokkaaminen', async ({ vstOppijaPage }) => {
        const tila = vstOppijaPage.$.opiskeluoikeus.tila.edit
        await tila.add.click()
        await tila.modal.date.set('1.1.2023')
        await tila.modal.tila.set('valmistunut')
        await tila.modal.submit.click()
        expect(await tila.items(0).date.value(true)).toEqual('1.9.2021')
        expect(await tila.items(0).tila.value()).toEqual('Läsnä')
        expect(await tila.items(1).date.value(true)).toEqual('1.1.2023')
        expect(await tila.items(1).tila.value()).toEqual('Valmistunut')

        await vstOppijaPage.tallenna()
      })

      test('Suorituksen vahvistus', async ({ vstOppijaPage }) => {
        const vahvistaminen =
          vstOppijaPage.$.suoritukset(0).suorituksenVahvistus

        // Palauta keskeneräiseksi
        await vstOppijaPage.$.opiskeluoikeus.tila.edit.items(0).remove.click()
        await vahvistaminen.edit.merkitseKeskeneräiseksi.click()

        // Merkitse takaisin valmiiksi
        await vstOppijaPage.vahvistaSuoritusUudellaHenkilöllä(
          'Reijo',
          '1.1.2023',
          'Rehtori'
        )

        await vstOppijaPage.tallennaVirheellisenä(
          'lessThanMinimumNumberOfItems: opiskeluoikeudet.0.tila.opiskeluoikeusjaksot'
        )
      })
    })

    test.describe('KOPS', () => {
      test.beforeEach(openOppijaPage(oppivelvollisilleSuunnattu, true))

      const osaamiskokonaisuudet = {
        1002: 'Arjen taidot ja elämänhallinta',
        1003: 'Opiskelu-, itsetuntemus- ja työelämätaidot',
        1004: 'Vuorovaikutus- ja viestintätaidot',
        1005: 'Matemaattiset perustaidot ja ongelmanratkaisutaidot',
        1006: 'Aktiivinen kansalaisuus',
        1007: 'Opiskelu- ja urasuunnittelutaidot',
        1008: 'Valinnaiset suuntautumisopinnot'
      }

      const poistaKaikkiOsasuoritukset = async (
        vstOppijaPage: KoskiVSTOppijaPage
      ) => repeatAsync(6)(() => vstOppijaPage.removeOsasuoritus(0))

      test('Osaamiskokonaisuuksien lisäys', async ({ vstOppijaPage }) => {
        await poistaKaikkiOsasuoritukset(vstOppijaPage)
        expect(await vstOppijaPage.osasuoritusOptions()).toEqual([
          'VST OSAAMISKOKONAISUUS',
          ...Object.values(osaamiskokonaisuudet).sort(),
          'VST MUUT OPINNOT',
          'Valinnaiset suuntautumisopinnot'
        ])
        for (const koodi of Object.keys(osaamiskokonaisuudet)) {
          await vstOppijaPage.addOsasuoritus(
            `VST osaamiskokonaisuus.vstosaamiskokonaisuus_${koodi}`
          )
        }
        await foreachAsync(Object.values(osaamiskokonaisuudet))(
          async (nimi, i) => {
            const osasuoritus = vstOppijaPage.osasuoritus(i)
            expect(await osasuoritus.nimi()).toEqual(nimi)
            await osasuoritus.setLaajuus(2)
            expect(await osasuoritus.laajuus()).toEqual('2')
          }
        )

        expect(await vstOppijaPage.laajuudetYhteensä()).toEqual(
          `${Object.values(osaamiskokonaisuudet).length * 2} op`
        )

        await vstOppijaPage.tallennaVirheellisenä(
          'Päätason suoritus koulutus/999909 on vahvistettu, mutta sillä ei ole 53 opintopisteen edestä hyväksytyksi arvioituja suorituksia',
          'Päätason suoritus koulutus/999909 on vahvistettu, mutta sillä on hyväksytyksi arvioituja osaamiskokonaisuuksia, joiden laajuus on alle 4 opintopistettä'
        )
      })

      test('Suuntautumisopintojen lisäys', async ({ vstOppijaPage }) => {
        const muuallaSuoritutetutOpinnot = {
          ammatillisentutkinnonosat: 'Ammatillisen tutkinnon osat',
          avoimetkorkeakouluopinnot: 'Avoimet korkeakouluopinnot',
          lukioopinnot: 'Lukio-opinnot',
          perusopetuksenarvosanankorottaminen:
            'Perusopetuksen arvosanan korottaminen',
          tyoelamaantutustuminen: 'Työelämään tutustuminen',
          vapaansivistystyonopinnot: 'Vapaan sivistystyön opinnot'
        }

        await poistaKaikkiOsasuoritukset(vstOppijaPage)

        await vstOppijaPage.addOsasuoritus(
          'VST muut opinnot.vstmuutopinnot_valinnaisetsuuntautumisopinnot'
        )
        const suuntautumisopinto = vstOppijaPage.osasuoritus(0)

        expect(await suuntautumisopinto.isExpanded()).toBeTruthy()
        await foreachAsync(Object.keys(muuallaSuoritutetutOpinnot))((koodi) =>
          suuntautumisopinto.addAlaosasuoritus(
            `VST muualla suoritetut opinnot.vstmuuallasuoritetutopinnot_${koodi}`
          )
        )
        await foreachAsync(Object.values(muuallaSuoritutetutOpinnot))(
          async (nimi, i) => {
            const kokonaisuus = suuntautumisopinto.alaosasuoritus(i)
            await kokonaisuus.setLaajuus(1 + i)
            await kokonaisuus.setSuoritusarvosana(true)

            expect(await kokonaisuus.nimi()).toEqual(nimi)
            expect(await kokonaisuus.arvosana()).toEqual('Hyväksytty')
            expect(await kokonaisuus.laajuus()).toEqual(`${i + 1}`)
          }
        )

        await vstOppijaPage.tallennaVirheellisenä(
          'Päätason suoritus koulutus/999909 on vahvistettu, mutta sillä ei ole 53 opintopisteen edestä hyväksytyksi arvioituja suorituksia'
        )
      })

      test('Paikallisen suuntautumisopinnon lisäys', async ({
        vstOppijaPage
      }) => {
        await poistaKaikkiOsasuoritukset(vstOppijaPage)

        await vstOppijaPage.addOsasuoritus(
          'VST muut opinnot.vstmuutopinnot_valinnaisetsuuntautumisopinnot'
        )
        const suuntautumisopinto = vstOppijaPage.osasuoritus(0)

        await suuntautumisopinto.expand()
        suuntautumisopinto.addNewAlaosasuoritus(
          'Työelämäharjoittelu',
          'paikallinen.__NEW__'
        )

        const paikallinen = suuntautumisopinto.alaosasuoritus(0)
        await paikallinen.setLaajuus(3)
        await paikallinen.setSuoritusarvosana(true)

        expect(await paikallinen.nimi()).toEqual('Työelämäharjoittelu')
        expect(await paikallinen.laajuus()).toEqual('3')
        expect(await paikallinen.arvosana()).toEqual('Hyväksytty')

        await vstOppijaPage.tallennaVirheellisenä(
          'Päätason suoritus koulutus/999909 on vahvistettu, mutta sillä ei ole 53 opintopisteen edestä hyväksytyksi arvioituja suorituksia'
        )
      })

      test('Osaamiskokonaisuuden lisäys olemassaolevaan opintokokonaisuuteen', async ({
        vstOppijaPage
      }) => {
        const osaamiskokonaisuus = vstOppijaPage.osasuoritus(0)
        await osaamiskokonaisuus.expand()
        await osaamiskokonaisuus.addNewAlaosasuoritus('Testi')

        const opintokokonaisuus = osaamiskokonaisuus.alaosasuoritus(0)
        await opintokokonaisuus.setLaajuus(3)
        expect(await opintokokonaisuus.laajuus()).toEqual('3')
        await opintokokonaisuus.setSuoritusarvosana(true)
        expect(await opintokokonaisuus.arvosana()).toEqual('Hyväksytty')

        await vstOppijaPage.tallenna()
      })

      test('Vaihda laajuutta ja arvosanaa', async ({ vstOppijaPage }) => {
        const osasuoritus = vstOppijaPage.osasuoritus(0)
        await osasuoritus.setLaajuus(3)

        expect(await osasuoritus.nimi()).toEqual(
          'Arjen taidot ja elämänhallinta'
        )

        await osasuoritus.expand()
        await osasuoritus.alaosasuoritus(0).setLaajuus(5)

        await vstOppijaPage.tallenna()

        const tallennettu = vstOppijaPage.osasuoritus(0)
        expect(await tallennettu.nimi()).toEqual(
          'Arjen taidot ja elämänhallinta'
        )
      })

      test('Alaosasuorituksen poisto', async ({ vstOppijaPage }) => {
        const kokonaisuus = vstOppijaPage.osasuoritus(0)
        await kokonaisuus.expand()
        const head = kokonaisuus.alaosasuoritus(0)
        expect(await head.nimi()).toEqual('Arjen rahankäyttö')
        await head.delete()
        expect(await head.nimi()).toEqual('Mielen liikkeet')

        await vstOppijaPage.tallennaVirheellisenä(
          'Päätason suoritus koulutus/999909 on vahvistettu, mutta sillä on hyväksytyksi arvioituja osaamiskokonaisuuksia, joiden laajuus on alle 4 opintopistettä'
        )
      })

      test('Tilan muokkaaminen', async ({ vstOppijaPage }) => {
        const tila = vstOppijaPage.$.opiskeluoikeus.tila.edit
        await tila.add.click()
        await tila.modal.date.set('1.1.2021')
        await tila.modal.date.set('1.1.2023')
        await tila.modal.tila.set('valmistunut')
        await tila.modal.submit.click()

        expect(await tila.items(0).date.value(true)).toEqual('1.9.2021')
        expect(await tila.items(0).tila.value()).toEqual('Läsnä')
        expect(await tila.items(1).date.value(true)).toEqual('1.1.2023')
        expect(await tila.items(1).tila.value()).toEqual('Valmistunut')
        expect(await tila.add.isVisible()).toBeFalsy()

        await vstOppijaPage.tallenna()
      })

      test('Suorituksen vahvistus', async ({ vstOppijaPage }) => {
        const vahvistaminen =
          vstOppijaPage.$.suoritukset(0).suorituksenVahvistus

        // Palauta keskeneräiseksi
        await vstOppijaPage.$.opiskeluoikeus.tila.edit.items(0).remove.click()
        await vahvistaminen.edit.merkitseKeskeneräiseksi.click()

        // Merkitse takaisin valmiiksi
        await vstOppijaPage.vahvistaSuoritusUudellaHenkilöllä(
          'Reijo',
          '1.1.2023',
          'Rehtori'
        )

        await vstOppijaPage.tallennaVirheellisenä(
          'lessThanMinimumNumberOfItems: opiskeluoikeudet.0.tila.opiskeluoikeusjaksot'
        )
      })
    })

    test.describe('Kotoutumiskoulutus 2022', () => {
      test.beforeEach(openOppijaPage(kotoutumiskoulutus2022, true))

      const osasuoritukset = {
        kielijaviestintaosaaminen: 'Kieli- ja viestintäosaaminen',
        ohjaus: 'Ohjaus',
        valinnaisetopinnot: 'Valinnaiset opinnot',
        yhteiskuntajatyoelamaosaaminen: 'Yhteiskunta- ja työelämäosaaminen'
      }

      const poistaKaikkiOsasuoritukset = async (
        vstOppijaPage: KoskiVSTOppijaPage
      ) => repeatAsync(4)(() => vstOppijaPage.removeOsasuoritus(0))

      test('Osasuorituksien lisäys sekä arvosanan asettaminen', async ({
        vstOppijaPage
      }) => {
        await poistaKaikkiOsasuoritukset(vstOppijaPage)
        expect(await vstOppijaPage.osasuoritusOptions()).toEqual(
          Object.values(osasuoritukset)
        )
        await foreachAsync(Object.entries(osasuoritukset))(
          async ([koodi, nimi], index) => {
            await vstOppijaPage.addOsasuoritus(koodi)
            const osasuoritus = vstOppijaPage.osasuoritus(index)
            expect(await osasuoritus.nimi()).toEqual(nimi)

            if (koodi !== 'ohjaus') {
              await osasuoritus.setSuoritusarvosana(true)
              expect(await osasuoritus.arvosana()).toEqual('Hyväksytty')
            }
          }
        )
        await vstOppijaPage.tallenna()
      })

      test('Kieli- ja viestintäosaamisen muokkaaminen', async ({
        vstOppijaPage
      }) => {
        const kieliopinnot = {
          kirjoittaminen: 'Kirjoittaminen (kirjallinen tuottaminen)',
          kuullunymmartaminen:
            'Kuullun ymmärtäminen (suullinen vastaanottaminen)',
          luetunymmartaminen:
            'Luetun ymmärtäminen (kirjallinen vastaanottaminen)',
          puhuminen: 'Puhuminen (suullinen tuottaminen)'
        }

        const kieliosaaminen = vstOppijaPage.osasuoritus(0)
        await kieliosaaminen.expand()
        await repeatAsync(2)(() => kieliosaaminen.deleteAlaosasuoritus(0))

        expect(await kieliosaaminen.alaosasuoritusOptions()).toEqual(
          Object.values(kieliopinnot)
        )

        await foreachAsync(Object.entries(kieliopinnot))(
          async ([koodi, nimi], i) => {
            await kieliosaaminen.addAlaosasuoritus(koodi)
            const alaosasuoritus = kieliosaaminen.alaosasuoritus(i)
            expect(await alaosasuoritus.nimi()).toEqual(nimi)
            await alaosasuoritus.setLaajuus(i + 1)
            expect(await alaosasuoritus.laajuus()).toEqual(`${i + 1}`)
            await alaosasuoritus.setArvosana(
              'arviointiasteikkokehittyvankielitaidontasot',
              'A1.1'
            )
            expect(await alaosasuoritus.arvosana()).toEqual('A1.1')
          }
        )

        await vstOppijaPage.tallenna()

        const tallennettu = vstOppijaPage.osasuoritus(0)
        await foreachAsync(Object.entries(kieliopinnot))(
          async ([koodi, nimi], i) => {
            const alaosasuoritus = tallennettu.alaosasuoritus(i)
            expect(await alaosasuoritus.nimi()).toEqual(nimi)
            expect(await alaosasuoritus.laajuus()).toEqual(`${i + 1} op`)
            expect(await alaosasuoritus.arvosana()).toEqual('A1.1')
          }
        )
      })

      test('Yhteiskunta- ja työelämäosaaminen', async ({ vstOppijaPage }) => {
        const yhteiskuntaopinnot = {
          ammattijakoulutus: 'Ammatti- ja koulutuspalvelut',
          tyoelamatietous: 'Työelämätietous',
          tyossaoppiminen: 'Työssäoppiminen',
          yhteiskunnanperuspalvelut: 'Yhteiskunnan peruspalvelut',
          yhteiskunnanperusrakenteet:
            'Yhteiskunnan perusrakenteet ja maantuntemus'
        }

        const yhteiskuntaosaaminen = vstOppijaPage.osasuoritus(1)
        await yhteiskuntaosaaminen.expand()
        await repeatAsync(4)(() => yhteiskuntaosaaminen.deleteAlaosasuoritus(0))

        expect(await yhteiskuntaosaaminen.alaosasuoritusOptions()).toEqual(
          Object.values(yhteiskuntaopinnot)
        )

        await foreachAsync(Object.entries(yhteiskuntaopinnot))(
          async ([koodi, nimi], i) => {
            await yhteiskuntaosaaminen.addAlaosasuoritus(koodi)
            const alaosasuoritus = yhteiskuntaosaaminen.alaosasuoritus(i)
            expect(await alaosasuoritus.nimi()).toEqual(nimi)
            await alaosasuoritus.setLaajuus(i + 1)
            expect(await alaosasuoritus.laajuus()).toEqual(`${i + 1}`)
          }
        )

        await vstOppijaPage.tallenna()

        const tallennettu = vstOppijaPage.osasuoritus(1)
        await foreachAsync(Object.values(yhteiskuntaopinnot))(
          async (nimi, i) => {
            const alaosasuoritus = tallennettu.alaosasuoritus(i)
            expect(await alaosasuoritus.nimi()).toEqual(nimi)
            expect(await alaosasuoritus.laajuus()).toEqual(`${i + 1} op`)
          }
        )
      })

      test('Valinnaiset opinnot', async ({ vstOppijaPage }) => {
        const valinnaiset = vstOppijaPage.osasuoritus(3)
        await valinnaiset.expand()
        await valinnaiset.deleteAlaosasuoritus(0)

        await valinnaiset.addNewAlaosasuoritus('Testaaminen')
        const alaosasuoritus = valinnaiset.alaosasuoritus(0)

        expect(await alaosasuoritus.nimi()).toEqual('Testaaminen')
        await alaosasuoritus.setLaajuus(10)
        expect(await alaosasuoritus.laajuus()).toEqual('10')

        await alaosasuoritus.expand()
        await alaosasuoritus.setKuvaus('Toimii')
        expect(await alaosasuoritus.kuvaus()).toEqual('Toimii')

        await vstOppijaPage.tallenna()

        const tallennettu = vstOppijaPage.osasuoritus(3).alaosasuoritus(0)
        expect(await tallennettu.nimi()).toEqual('Testaaminen')
        expect(await tallennettu.laajuus()).toEqual('10 op')
        expect(await tallennettu.kuvaus()).toEqual('Toimii')
      })

      test('Tilan muokkaaminen', async ({ vstOppijaPage }) => {
        await vstOppijaPage.osasuoritus(0).setSuoritusarvosana(true)
        await vstOppijaPage.osasuoritus(1).setSuoritusarvosana(true)
        await vstOppijaPage.osasuoritus(3).setSuoritusarvosana(true)

        const tila = vstOppijaPage.$.opiskeluoikeus.tila.edit
        await tila.add.click()
        await tila.modal.date.set('1.1.2023')
        await tila.modal.tila.set('valmistunut')
        await tila.modal.submit.click()
        expect(await tila.items(0).date.value(true)).toEqual('1.8.2022')
        expect(await tila.items(0).tila.value()).toEqual('Läsnä')
        expect(await tila.items(1).date.value(true)).toEqual('1.1.2023')
        expect(await tila.items(1).tila.value()).toEqual('Valmistunut')
        expect(await tila.add.isVisible()).toBeFalsy()

        await vstOppijaPage.tallennaVirheellisenä(
          'Suoritukselta koulutus/999910 puuttuu vahvistus, vaikka opiskeluoikeus on tilassa Valmistunut'
        )
      })

      test('Perusteen vaihtaminen', async ({ vstOppijaPage }) => {
        await vstOppijaPage.setPeruste('OPH-123-2021')
        expect(await vstOppijaPage.peruste()).toEqual(
          'OPH-123-2021 Aikuisten maahanmuuttajien kotoutumiskoulutuksen opetussuunnitelman perusteet 2012'
        )

        await vstOppijaPage.setPeruste('OPH-649-2022')
        expect(await vstOppijaPage.peruste()).toEqual(
          'OPH-649-2022 Aikuisten maahanmuuttajien kotoutumiskoulutuksen opetussuunnitelman perusteet 2012'
        )

        await vstOppijaPage.tallenna()
      })

      test('Suorituksen vahvistus', async ({ vstOppijaPage }) => {
        // Merkitse osasuoritukset suoritetuksi
        await vstOppijaPage.osasuoritus(0).setSuoritusarvosana(true)
        await vstOppijaPage.osasuoritus(1).setSuoritusarvosana(true)
        await vstOppijaPage.osasuoritus(3).setSuoritusarvosana(true)

        // Merkitse takaisin valmiiksi
        await vstOppijaPage.vahvistaSuoritusUudellaHenkilöllä(
          'Reijo',
          '1.1.2023',
          'Rehtori'
        )

        await vstOppijaPage.tallenna()
      })
    })

    test.describe('JOTPA', () => {
      test.beforeEach(openOppijaPage(jotpaKoulutus, true))

      const poistaKaikkiOsasuoritukset = async (
        vstOppijaPage: KoskiVSTOppijaPage
      ) => repeatAsync(3)(() => vstOppijaPage.removeOsasuoritus(0))

      test('Osasuoritusten muokkaus', async ({ vstOppijaPage }) => {
        await poistaKaikkiOsasuoritukset(vstOppijaPage)
        expect(await vstOppijaPage.osasuoritusOptions()).toEqual([
          'Lisää osasuoritus'
        ])

        const osasuoritukset = ['Esimerkki 1', 'Esimerkki 2', 'Esimerkki 3']
        await foreachAsync(osasuoritukset)(async (nimi, i) => {
          await vstOppijaPage.addNewOsasuoritus(nimi)

          const osasuoritus = vstOppijaPage.osasuoritus(i)
          expect(await osasuoritus.nimi()).toEqual(nimi)

          await osasuoritus.setLaajuus(20 + i)
          expect(await osasuoritus.laajuus()).toEqual(`${20 + i}`)

          await osasuoritus.setJotpaArvosana(4 + i)
          expect(await osasuoritus.arvosana()).toEqual(`${4 + i}`)

          await osasuoritus.setSuoritusarvosana(true, true)
          expect(await osasuoritus.arvosana()).toEqual('Hyväksytty')
        })

        expect(await vstOppijaPage.osasuoritusOptions()).toEqual([
          'Lisää osasuoritus',
          ...osasuoritukset
        ])
        expect(await vstOppijaPage.laajuudetYhteensä()).toEqual('63 op')

        await vstOppijaPage.tallenna()

        await foreachAsync(osasuoritukset)(async (nimi, i) => {
          const osasuoritus = vstOppijaPage.osasuoritus(i)
          expect(await osasuoritus.nimi()).toEqual(nimi)
          expect(await osasuoritus.laajuus()).toEqual(`${i + 20} op`)
          expect(await osasuoritus.arvosana()).toEqual('Hyväksytty')
        })
      })

      test('Tilan muokkaaminen', async ({ vstOppijaPage }) => {
        const tila = vstOppijaPage.$.opiskeluoikeus.tila.edit

        // Tsekkaa, ettei voi merkitä suoritetuksi puuttuvan arvosanan takia
        await tila.add.click()
        expect(
          await tila.modal.tila.isDisabled('hyvaksytystisuoritettu')
        ).toBeTruthy()
        await tila.modal.cancel.click()

        // Lisää arvosana ja yritä uudelleen
        await vstOppijaPage.osasuoritus(2).setSuoritusarvosana(true, true)

        await tila.add.click()
        await tila.modal.date.set('1.3.2023')
        await tila.modal.tila.set('hyvaksytystisuoritettu')
        await tila.modal.submit.click()

        expect(await tila.items(0).date.value(true)).toEqual('1.1.2023')
        expect(await tila.items(0).tila.value()).toEqual('Läsnä')
        expect(await tila.items(0).rahoitus.value()).toEqual(
          '(Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus)'
        )
        expect(await tila.items(1).date.value(true)).toEqual('1.3.2023')
        expect(await tila.items(1).tila.value()).toEqual(
          'Hyväksytysti suoritettu'
        )
        expect(await tila.items(1).rahoitus.value()).toEqual(
          '(Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus)'
        )
        expect(await tila.add.isVisible()).toBeFalsy()

        await vstOppijaPage.tallennaVirheellisenä(
          "Vahvistamattomalla jatkuvaan oppimiseen suunnatulla vapaan sivistystyön koulutuksella ei voi olla päättävänä tilana 'Hyväksytysti suoritettu'"
        )
      })

      test('Suorituksen vahvistus', async ({ vstOppijaPage }) => {
        expect(await vstOppijaPage.isMerkitseValmiiksiDisabled()).toBeTruthy()
        await vstOppijaPage.osasuoritus(2).setSuoritusarvosana(true, true)
        expect(await vstOppijaPage.isMerkitseValmiiksiDisabled()).toBeFalsy()

        // Merkitse takaisin valmiiksi
        await vstOppijaPage.vahvistaSuoritusUudellaHenkilöllä(
          'Reijo',
          '1.1.2023',
          'Rehtori'
        )

        expect(
          await vstOppijaPage.isMerkitseKeskeneräiseksiDisabled()
        ).toBeFalsy()

        const tila = vstOppijaPage.$.opiskeluoikeus.tila.edit
        await tila.add.click()
        await tila.modal.date.set('1.3.2023')
        await tila.modal.tila.set('hyvaksytystisuoritettu')
        await tila.modal.submit.click()

        expect(
          await vstOppijaPage.isMerkitseKeskeneräiseksiDisabled()
        ).toBeTruthy()

        await vstOppijaPage.tallenna()
      })
    })

    test.describe('Osaamismerkki', () => {
      test.beforeEach(openOppijaPage(osaamismerkki, true))

      test('Tilan muokkaaminen', async ({ vstOppijaPage }) => {
        const tila = vstOppijaPage.$.opiskeluoikeus.tila.edit
        expect(await tila.add.isVisible()).toBeFalsy()
        await tila.items(0).remove.click()
        expect(await tila.add.isVisible()).toBeTruthy()
        await tila.add.click()
        await tila.modal.date.set('1.1.2022')
        await tila.modal.tila.set('hyvaksytystisuoritettu')
        await tila.modal.submit.click()
        expect(await tila.items(0).date.value(true)).toEqual('1.1.2022')
        expect(await tila.items(0).tila.value()).toEqual(
          'Hyväksytysti suoritettu'
        )

        await vstOppijaPage.tallennaVirheellisenä(
          'suoritus.vahvistus.päivä (2024-01-01) oltava sama tai aiempi kuin päättymispäivä (2022-01-01)',
          'Osaamismerkin pitää olla arvioitu, vahvistettu ja päättynyt samana päivänä',
          'Osaamismerkin sisältävä opiskeluoikeus ei voi olla päättynyt ennen lain voimaantuloa 1.1.2024'
        )
      })

      test('Suorituksen vahvistus', async ({ vstOppijaPage }) => {
        const vahvistaminen =
          vstOppijaPage.$.suoritukset(0).suorituksenVahvistus

        // Palauta keskeneräiseksi
        expect(
          await vahvistaminen.edit.merkitseKeskeneräiseksi.isDisabled()
        ).toBeFalsy()
        await vstOppijaPage.$.opiskeluoikeus.tila.edit.items(0).remove.click()
        await vahvistaminen.edit.merkitseKeskeneräiseksi.click()

        // Merkitse takaisin valmiiksi
        await vstOppijaPage.vahvistaSuoritusUudellaHenkilöllä(
          'Reijo',
          '1.1.2023',
          'Rehtori'
        )

        await vstOppijaPage.tallennaVirheellisenä(
          'lessThanMinimumNumberOfItems: opiskeluoikeudet.0.tila.opiskeluoikeusjaksot'
        )
      })
    })
  })

  test.describe('Lähdejärjestelmästä siirretty opiskeluoikeus', () => {
    test.describe('Käyttäjä jolla ei ole yleistä mitätöintioikeutta', () => {
      test.use({ storageState: virkailija('kalle') })
      test.beforeEach(openOppijaPage(jotpaKoulutusTiedonsiirto, false))

      test('Muokkaus ja mitätöinti on estetty', async ({ vstOppijaPage }) => {
        expect(
          await vstOppijaPage.$.opiskeluoikeus.edit.isVisible()
        ).toBeFalsy()
        expect(
          await vstOppijaPage.$.opiskeluoikeus.invalidate.button.isVisible()
        ).toBeFalsy()
      })
    })

    test.describe('Oppilaitoksen pääkäyttäjä', () => {
      test.use({ storageState: virkailija('varsinaissuomi-oppilaitos-pää') })
      test.beforeEach(openOppijaPage(jotpaKoulutusTiedonsiirto, false))

      test('Muokkaus on estetty, mutta mitätöinti onnistuu', async ({
        vstOppijaPage
      }) => {
        expect(
          await vstOppijaPage.$.opiskeluoikeus.edit.isVisible()
        ).toBeFalsy()
      })
    })

    test.describe('Käyttäjä jolla on yleinen mitätöintioikeus', () => {
      test.use({ storageState: virkailija('pää') })
      test.beforeEach(openOppijaPage(jotpaKoulutusTiedonsiirto, false))

      test('Muokkaus on estetty, mutta mitätöinti onnistuu', async ({
        vstOppijaPage
      }) => {
        expect(
          await vstOppijaPage.$.opiskeluoikeus.edit.isVisible()
        ).toBeFalsy()
        await vstOppijaPage.mitätöi()
      })
    })
  })

  test.describe('Käyttöliittymässä luotu opiskeluoikeus', () => {
    test.describe('Oppilaitoksen pääkäyttäjä', () => {
      test.use({ storageState: virkailija('varsinaissuomi-oppilaitos-pää') })
      test.beforeEach(openOppijaPage(vapaatavoitteinenKoulutus, false))

      test('Muokkaus on estetty, mutta mitätöinti onnistuu', async ({
        vstOppijaPage
      }) => {
        expect(
          await vstOppijaPage.$.opiskeluoikeus.edit.isVisible()
        ).toBeFalsy()
        await vstOppijaPage.mitätöi()
      })
    })
  })

  test.describe('Kansalaisen näkymä', () => {
    test.beforeEach(async ({ fixtures, page }) => {
      await fixtures.apiLoginAsUser('kalle', 'kalle')
      await fixtures.reset()
      await fixtures.apiLogout()
      await page.goto('/koski/omattiedot')
    })

    test.describe('Osaamismerkki VST', () => {
      test.use({ storageState: kansalainen('050705A564B') })
      test('Kansalaisen tietoja voi katsella ja suoritusjakaa', async ({
        kansalainenPage,
        page
      }) => {
        const getByTestId = (id: string) =>
          page.getByTestId(`osaamismerkit.0.${id}`)
        await expect(getByTestId('opiskeluoikeus.kuva')).toBeAttached()
        await kansalainenPage.expandOsaamismerkki()
        await kansalainenPage.collapseOpiskeluoikeus()
        await expect(getByTestId('opiskeluoikeus.kuva')).toBeAttached()
        await kansalainenPage.expandOsaamismerkki()

        await tarkistaTiedot(page, kansalainenPage)

        // Tarkista ettei oppilaitoksen otsikko näy sivulla
        const title = await page.locator('.oppilaitos-title')
        await expect(title).toContainText('Osaamismerkit')

        // Tee suoritusjako, ja tarkista, että sen sisällössä on sama sisältö
        await kansalainenPage.openJaaSuoritustietoja()
        await kansalainenPage
          .suoritustietoLabel(
            '1.2.246.562.10.31915273374',
            'vstosaamismerkki',
            '1001'
          )
          .click()
        await kansalainenPage.jaaValitsemasiOpinnotButton().click()

        const page2Promise = page.waitForEvent('popup')
        await page
          .getByRole('link', { name: 'Katso, miltä suoritusote nä' })
          .click()
        const page2 = await page2Promise

        const kansalainenPage2 = KoskiKansalainenPage.create(page2)
        await kansalainenPage2.expandOsaamismerkki()

        await tarkistaTiedot(page2, kansalainenPage2)
      })

      const tarkistaTiedot = async (
        page: Page,
        kansalainenPage: KoskiKansalainenPage
      ) => {
        const getByTestId = (id: string) =>
          page.getByTestId(`osaamismerkit.0.${id}`)
        await expect(kansalainenPage.opiskeluoikeusTitle).toContainText(
          'Oma osaamispolku(2024, hyväksytysti suoritettu)Opiskeluoikeuden oid: 1.2.246.562.15'
        )
        await expect(getByTestId('opiskeluoikeus.voimassaoloaika')).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.1.2024 – 1.1.2024'
        )
        await expect(getByTestId('suoritukset.0.toimipiste.value')).toHaveText(
          'Varsinais-Suomen kansanopisto'
        )
        await expect(
          getByTestId('suoritukset.0.arviointi.0.arvosana.value')
        ).toHaveText('Hyväksytty')
        await expect(
          getByTestId('suoritukset.0.arviointi.0.date.value')
        ).toHaveText('1.1.2024')
        await expect(
          getByTestId('suoritukset.0.tunniste.koodiarvo-ja-nimi')
        ).toHaveText('1001 Oma osaamispolku')
        await expect(
          getByTestId('suoritukset.0.tunniste.koodiarvo-ja-nimi').locator('a')
        ).toBeAttached()
        await expect(
          getByTestId('suoritukset.0.suorituksenVahvistus.value.status')
        ).toHaveText('Suoritus valmis')
        await expect(
          getByTestId('suoritukset.0.suorituksenVahvistus.value.details')
        ).toHaveText('Vahvistus: 1.1.2024 Jyväskylän normaalikoulu')
        await expect(
          getByTestId('suoritukset.0.suorituksenVahvistus.value.henkilö.0')
        ).toHaveText('Reijo Reksi (rehtori)')
      }
    })

    test.describe('Suostumuksen peruutus', () => {
      test.describe('Vapaatavoitteinen VST', () => {
        test.use({ storageState: kansalainen('010917-156A') })
        test('Suostumuksen voi perua', async ({ kansalainenPage, page }) => {
          await page.getByTestId('oo.0.opiskeluoikeus.expand').click()
          await kansalainenPage.expectSuostumusPeruttavissa()
          await kansalainenPage.peruSuostumus()
        })

        test('Suostumusta ei voi perua, jos siitä on tehty suoritusjako', async ({
          kansalainenPage,
          page
        }) => {
          await kansalainenPage.openJaaSuoritustietoja()
          await kansalainenPage
            .suoritustietoLabel(
              '1.2.246.562.10.31915273374',
              'vstvapaatavoitteinenkoulutus',
              '099999'
            )
            .click()
          await kansalainenPage.jaaValitsemasiOpinnotButton().click()

          await page.reload() // TODO: Käli olisi hyvä fiksata niin, ettei tätä tarvita
          await kansalainenPage
            .getOpiskeluoikeusElementByTestId('opiskeluoikeus.expand')
            .click()
          await kansalainenPage.expectSuostumusEiPeruttavissa()
        })
      })

      test.describe('Lukutaitokoulutus', () => {
        test.use({ storageState: kansalainen('231158-467R') })
        test('Suostumusta ei voi perua, koska se ei ole peruttavaa tyyppiä', async ({
          kansalainenPage
        }) => {
          await kansalainenPage
            .getOpiskeluoikeusElementByTestId('opiskeluoikeus.expand')
            .click()
          await kansalainenPage.expectSuostumusEiPeruttavissa()
        })
      })

      test.describe('Osaamismerkki VST', () => {
        test.use({ storageState: kansalainen('050705A564B') })
        test('Suostumuksen voi perua', async ({ kansalainenPage }) => {
          await kansalainenPage.expandOsaamismerkki()
          await kansalainenPage.expectSuostumusPeruttavissa()
          await kansalainenPage.peruSuostumus('osaamismerkit.0')
        })
      })
    })
  })
})
