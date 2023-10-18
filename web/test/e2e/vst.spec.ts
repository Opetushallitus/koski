import { test, expect as _expect } from './base'
import { virkailija } from './setup/auth'
import { Page } from '@playwright/test'

const kotoutumiskoulutus = '1.2.246.562.24.00000000106'
const kotoutumiskoulutusOppivelvollisille = '1.2.246.562.24.00000000135'
const oppivelvollisilleSuunnattu = '1.2.246.562.24.00000000143'
const jotpaKoulutus = '1.2.246.562.24.00000000140'
const lukutaitokoulutus = '1.2.246.562.24.00000000107'
const kansanopisto = '1.2.246.562.24.00000000105'
const vstKoulutus = '1.2.246.562.24.00000000108'

const testaaUusiPaikallinenKoodistoJääTalteen = async (
  testIdPrefix: string,
  page: Page
) => {
  await page.getByTestId(testIdPrefix + '.select.input').click()
  await page.getByTestId(testIdPrefix + '.select.options.__NEW__.item').click()
  await page
    .getByTestId(testIdPrefix + '.modal.nimi.edit.input')
    .fill('Playwright-opinnot')
  await page.getByTestId(testIdPrefix + '.modal.submit').click()
  await page.getByTestId(testIdPrefix + '.select.input').click()
  await expect(
    // Köyhän miehen tapa hakea paikallinen koodi
    page.locator('.Removable__content').getByText('Playwright-opinnot')
  ).toBeVisible()
}

test.describe('Vapaa sivistystyö', () => {
  test.describe('Katselunäkymä', () => {
    test.use({ storageState: virkailija('kalle') })
    test.beforeAll(async ({ fixtures }) => {
      await fixtures.reset()
    })
    test.describe('VST JOTPA', () => {
      test.beforeEach(async ({ vstOppijaPage }) => {
        await vstOppijaPage.gotoWithQueryParams(jotpaKoulutus, {
          newVSTUI: 'true'
        })
      })
      test('Näyttää tiedot oikein', async ({ page }) => {
        const expect = _expect.configure({
          timeout: 2000
        })
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('1.1.2023')
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText(
          'Läsnä (Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus)'
        )
        await expect(
          page.getByTestId('opiskeluoikeus.voimassaoloaika')
        ).toHaveText('Opiskeluoikeuden voimassaoloaika: 1.1.2023 –')
        await expect(
          page.getByTestId('suoritukset.0.toimipiste.value')
        ).toHaveText('Varsinais-Suomen kansanopisto')
        await expect(
          page.getByTestId('suoritukset.0.koulutusmoduuli.tunniste.value')
        ).toHaveText('Vapaan sivistystyön koulutus')
        await expect(
          page.getByTestId(
            'suoritukset.0.koulutusmoduuli.tunniste.koodiarvo.value'
          )
        ).toHaveText('099999')
        await expect(
          page.getByTestId('suoritukset.0.osasuoritukset.0.nimi.value')
        ).toHaveText('Kuvantekemisen perusvälineistö')

        await expect(
          page.getByTestId('suoritukset.0.osasuoritukset.0.laajuus.value')
        ).toHaveText('1 op')
        await expect(
          page.getByTestId('suoritukset.0.osasuoritukset.0.arvosana.value')
        ).toHaveText('9')
        await expect(
          page.getByTestId('suoritukset.0.osasuoritukset.1.nimi.value')
        ).toHaveText('Kuvallisen viestinnän perusteet')
        await expect(
          page.getByTestId('suoritukset.0.osasuoritukset.1.laajuus.value')
        ).toHaveText('1 op')
        await expect(
          page.getByTestId('suoritukset.0.osasuoritukset.1.arvosana.value')
        ).toHaveText('Hyväksytty')
        await expect(
          page.getByTestId('suoritukset.0.osasuoritukset.2.nimi.value')
        ).toHaveText('Tussitekniikat I ja II')
        await expect(
          page.getByTestId('suoritukset.0.osasuoritukset.2.laajuus.value')
        ).toHaveText('1 op')
        await page.getByTestId('suoritukset.0.osasuoritukset.0.expand').click({
          timeout: 2000
        })
        await expect(
          page.getByTestId(
            'suoritukset.0.osasuoritukset.0.properties.arviointi.arvosana.value'
          )
        ).toHaveText('9')
        await expect(
          page.getByTestId(
            'suoritukset.0.osasuoritukset.0.properties.arviointi.päivä.value'
          )
        ).toHaveText('1.2.2023')
        await page.getByTestId('suoritukset.0.osasuoritukset.1.expand').click()
        await expect(
          page.getByTestId(
            'suoritukset.0.osasuoritukset.1.properties.arviointi.arvosana.value'
          )
        ).toHaveText('Hyväksytty')
        await expect(
          page.getByTestId(
            'suoritukset.0.osasuoritukset.1.properties.arviointi.päivä.value'
          )
        ).toHaveText('1.3.2023')
        await page.getByTestId('suoritukset.0.osasuoritukset.2.expand').click()
        await expect(
          page.getByTestId('suoritukset.0.yhteensa.value')
        ).toHaveText('3 op')
      })
    })
    test.describe('VST lukutaitokoulutus', () => {
      test.beforeEach(async ({ vstOppijaPage }) => {
        await vstOppijaPage.gotoWithQueryParams(lukutaitokoulutus, {
          newVSTUI: 'true'
        })
      })
      test('Näyttää tiedot oikein', async ({ page }) => {
        const expect = _expect.configure({
          timeout: 1000
        })
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('1.9.2021')
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Läsnä')
        await expect(
          page.getByTestId('opiskeluoikeus.voimassaoloaika')
        ).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.9.2021 – 31.5.2022 (Arvioitu päättymispäivä)'
        )
        await expect(
          page.getByTestId('suoritukset.0.toimipiste.value')
        ).toHaveText('Itä-Suomen yliopisto')
        await expect(
          page.getByTestId('suoritukset.0.koulutusmoduuli.tunniste.value')
        ).toHaveText('Lukutaitokoulutus oppivelvollisille')
        await expect(
          page.getByTestId(
            'suoritukset.0.koulutusmoduuli.tunniste.koodiarvo.value'
          )
        ).toHaveText('999911')
        await expect(
          page.getByTestId('suoritukset.0.peruste.value')
        ).toHaveText('OPH-2984-2017')
        await expect(
          page.getByTestId('suoritukset.0.koulutuksen-laajuus.value')
        ).toHaveText('80 op')

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
            page.getByTestId(`suoritukset.0.osasuoritukset.${i}.nimi.value`)
          ).toHaveText(osasuoritus.nimi)
          await expect(
            page.getByTestId(`suoritukset.0.osasuoritukset.${i}.laajuus.value`)
          ).toHaveText(osasuoritus.laajuus)
          await expect(
            page.getByTestId(`suoritukset.0.osasuoritukset.${i}.arvosana.value`)
          ).toHaveText(osasuoritus.arvosana)
          await expect(
            page.getByTestId(
              `suoritukset.0.osasuoritukset.${i}.taitotaso.value`
            )
          ).toHaveText(osasuoritus.taitotaso)
          await page
            .getByTestId(`suoritukset.0.osasuoritukset.${i}.expand`)
            .click()
          await expect(
            page.getByTestId(
              `suoritukset.0.osasuoritukset.${i}.properties.arviointi.arvosana.value`
            )
          ).toHaveText(osasuoritus.expander.arvosana)
          await expect(
            page.getByTestId(
              `suoritukset.0.osasuoritukset.${i}.properties.arviointi.päivä.value`
            )
          ).toHaveText(osasuoritus.expander.arvosanaPvm)
          i++
        }
        await expect(
          page.getByTestId('suoritukset.0.yhteensa.value')
        ).toHaveText('80 op')
      })
    })
    test.describe('VST kansanopisto', () => {
      test.beforeEach(async ({ vstOppijaPage }) => {
        await vstOppijaPage.gotoWithQueryParams(kansanopisto, {
          newVSTUI: 'true'
        })
      })
      test('Näyttää tiedot oikein', async ({ page }) => {
        const expect = _expect.configure({
          timeout: 2000
        })
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('1.9.2021')
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Läsnä')
        await expect(
          page.getByTestId('opiskeluoikeus.voimassaoloaika')
        ).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.9.2021 – 31.5.2022 (Arvioitu päättymispäivä)'
        )
        await expect(
          page.getByTestId('suoritukset.0.toimipiste.value')
        ).toHaveText('Varsinais-Suomen kansanopisto')
        await expect(
          page.getByTestId('suoritukset.0.koulutusmoduuli.tunniste.value')
        ).toHaveText(
          'Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille'
        )
        await expect(
          page.getByTestId(
            'suoritukset.0.koulutusmoduuli.tunniste.koodiarvo.value'
          )
        ).toHaveText('999909')
        await expect(
          page.getByTestId('suoritukset.0.peruste.value')
        ).toHaveText('OPH-58-2021')

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
                    },
                    tunnustettu: '–'
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
                    },
                    tunnustettu: '–'
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
                      'Oman opiskelutyylin analysointi ja tavoitteiden asettaminen',
                    tunnustettu: '–'
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
                    kuvaus: 'Matematiikan jokapäiväinen käyttö',
                    tunnustettu: '–'
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
                    kuvaus: 'Geometrian perusteet',
                    tunnustettu: '–'
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
                    kuvaus: 'Trigonometrian perusteet',
                    tunnustettu: '–'
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
                      'Kansalaisuuden merkitys moniarvoisess yhteiskunnasa',
                    tunnustettu: '–'
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
                    kuvaus: 'CV:n laadinta ja käyttö työnhaussa',
                    tunnustettu: '–'
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
                      'Nykyaikaisen tietokoneen tyypilliset huoltotoimenpiteet',
                    tunnustettu: '–'
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
                    kuvaus: 'Valaisinlähteet ja niiden toiminta',
                    tunnustettu: '–'
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
                    kuvaus: 'Taiteen käyttö työkaluna',
                    tunnustettu: '–'
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
            page.getByTestId(`suoritukset.0.osasuoritukset.${i}.nimi.value`)
          ).toHaveText(osasuoritus.nimi)
          await expect(
            page.getByTestId(`suoritukset.0.osasuoritukset.${i}.laajuus.value`)
          ).toHaveText(osasuoritus.laajuus)
          // Expander
          if (osasuoritus.expander !== undefined) {
            await page
              .getByTestId(`suoritukset.0.osasuoritukset.${i}.expand`)
              .click()
            if (osasuoritus.expander.osasuoritukset !== undefined) {
              let a = 0
              for (const alaosasuoritus of osasuoritus.expander
                .osasuoritukset) {
                await expect(
                  page.getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.arvosana.value`
                  )
                ).toHaveText(alaosasuoritus.arvosana)
                await expect(
                  page.getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.laajuus.value`
                  )
                ).toHaveText(alaosasuoritus.laajuus)
                await expect(
                  page.getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.nimi.value`
                  )
                ).toHaveText(alaosasuoritus.nimi)
                if (alaosasuoritus.extra !== undefined) {
                  await page
                    .getByTestId(
                      `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.expand`
                    )
                    .click()
                  if (alaosasuoritus.extra.kuvaus !== undefined) {
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.kuvaus.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.kuvaus)
                  }
                  if (alaosasuoritus.extra.tunnustettu !== undefined) {
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.tunnustettu.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.tunnustettu)
                  }
                  if (alaosasuoritus.extra.arviointi !== undefined) {
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.arvosana.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.arvosana)
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.päivä.value`
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

        await expect(
          page.getByTestId('suoritukset.0.yhteensa.value')
        ).toHaveText('55 op')
      })
    })
    test.describe('VST Oppivelvollisille suunnattu', () => {
      test.beforeEach(async ({ vstOppijaPage }) => {
        await vstOppijaPage.gotoWithQueryParams(oppivelvollisilleSuunnattu, {
          newVSTUI: 'true'
        })
      })
      test('Näyttää tiedot oikein', async ({ page }) => {
        const expect = _expect.configure({
          timeout: 2000
        })
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('1.9.2021')
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Läsnä')
        await expect(
          page.getByTestId('opiskeluoikeus.voimassaoloaika')
        ).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.9.2021 – 31.5.2022 (Arvioitu päättymispäivä)'
        )
        await expect(
          page.getByTestId('suoritukset.0.toimipiste.value')
        ).toHaveText('Varsinais-Suomen kansanopisto')
        await expect(
          page.getByTestId('suoritukset.0.koulutusmoduuli.tunniste.value')
        ).toHaveText(
          'Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille'
        )
        await expect(
          page.getByTestId(
            'suoritukset.0.koulutusmoduuli.tunniste.koodiarvo.value'
          )
        ).toHaveText('999909')
        await expect(
          page.getByTestId('suoritukset.0.peruste.value')
        ).toHaveText('OPH-58-2021')

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
                    },
                    tunnustettu: '–'
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
                    },
                    tunnustettu: '–'
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
                      'Oman opiskelutyylin analysointi ja tavoitteiden asettaminen',
                    tunnustettu: '–'
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
                    kuvaus: 'Matematiikan jokapäiväinen käyttö',
                    tunnustettu: '–'
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
                    kuvaus: 'Geometrian perusteet',
                    tunnustettu: '–'
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
                    kuvaus: 'Trigonometrian perusteet',
                    tunnustettu: '–'
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
                      'Kansalaisuuden merkitys moniarvoisess yhteiskunnasa',
                    tunnustettu: '–'
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
                    kuvaus: 'CV:n laadinta ja käyttö työnhaussa',
                    tunnustettu: '–'
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
                      'Nykyaikaisen tietokoneen tyypilliset huoltotoimenpiteet',
                    tunnustettu: '–'
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
                    kuvaus: 'Valaisinlähteet ja niiden toiminta',
                    tunnustettu: '–'
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
                    kuvaus: 'Taiteen käyttö työkaluna',
                    tunnustettu: '–'
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
            page.getByTestId(`suoritukset.0.osasuoritukset.${i}.nimi.value`)
          ).toHaveText(osasuoritus.nimi)
          await expect(
            page.getByTestId(`suoritukset.0.osasuoritukset.${i}.laajuus.value`)
          ).toHaveText(osasuoritus.laajuus)
          // Expander
          if (osasuoritus.expander !== undefined) {
            await page
              .getByTestId(`suoritukset.0.osasuoritukset.${i}.expand`)
              .click()
            if (osasuoritus.expander.osasuoritukset !== undefined) {
              let a = 0
              for (const alaosasuoritus of osasuoritus.expander
                .osasuoritukset) {
                await expect(
                  page.getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.arvosana.value`
                  )
                ).toHaveText(alaosasuoritus.arvosana)
                await expect(
                  page.getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.laajuus.value`
                  )
                ).toHaveText(alaosasuoritus.laajuus)
                await expect(
                  page.getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.nimi.value`
                  )
                ).toHaveText(alaosasuoritus.nimi)
                if (alaosasuoritus.extra !== undefined) {
                  await page
                    .getByTestId(
                      `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.expand`
                    )
                    .click()
                  if (alaosasuoritus.extra.kuvaus !== undefined) {
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.kuvaus.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.kuvaus)
                  }
                  if (alaosasuoritus.extra.tunnustettu !== undefined) {
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.tunnustettu.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.tunnustettu)
                  }
                  if (alaosasuoritus.extra.arviointi !== undefined) {
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.arvosana.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.arvosana)
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.päivä.value`
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

        await expect(
          page.getByTestId('suoritukset.0.yhteensa.value')
        ).toHaveText('55 op')
      })
    })
    test.describe('VST Kotoutumiskoulutus oppivelvollisille', () => {
      test.beforeEach(async ({ vstOppijaPage }) => {
        await vstOppijaPage.gotoWithQueryParams(
          kotoutumiskoulutusOppivelvollisille,
          {
            newVSTUI: 'true'
          }
        )
      })
      test('Näyttää tiedot oikein', async ({ page }) => {
        const expect = _expect.configure({
          timeout: 2000
        })
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('1.8.2022')
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Läsnä')
        await expect(
          page.getByTestId('opiskeluoikeus.voimassaoloaika')
        ).toHaveText('Opiskeluoikeuden voimassaoloaika: 1.8.2022 –')
        await expect(
          page.getByTestId('suoritukset.0.toimipiste.value')
        ).toHaveText('Varsinais-Suomen kansanopisto')
        await expect(
          page.getByTestId('suoritukset.0.koulutusmoduuli.tunniste.value')
        ).toHaveText('Kotoutumiskoulutus oppivelvollisille')
        await expect(
          page.getByTestId(
            'suoritukset.0.koulutusmoduuli.tunniste.koodiarvo.value'
          )
        ).toHaveText('999910')
        await expect(
          page.getByTestId('suoritukset.0.peruste.value')
        ).toHaveText('OPH-649-2022')

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
            page.getByTestId(`suoritukset.0.osasuoritukset.${i}.nimi.value`)
          ).toHaveText(osasuoritus.nimi)
          await expect(
            page.getByTestId(`suoritukset.0.osasuoritukset.${i}.laajuus.value`)
          ).toHaveText(osasuoritus.laajuus)
          // Expander
          if (osasuoritus.expander !== undefined) {
            await page
              .getByTestId(`suoritukset.0.osasuoritukset.${i}.expand`)
              .click()
            if (osasuoritus.expander.osasuoritukset !== undefined) {
              let a = 0
              for (const alaosasuoritus of osasuoritus.expander
                .osasuoritukset) {
                if (alaosasuoritus.arvosana) {
                  await expect(
                    page.getByTestId(
                      `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.arvosana.value`
                    )
                  ).toHaveText(alaosasuoritus.arvosana)
                }
                if (alaosasuoritus.laajuus) {
                  await expect(
                    page.getByTestId(
                      `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.laajuus.value`
                    )
                  ).toHaveText(alaosasuoritus.laajuus)
                }
                await expect(
                  page.getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.nimi.value`
                  )
                ).toHaveText(alaosasuoritus.nimi)
                if (alaosasuoritus.extra !== undefined) {
                  await page
                    .getByTestId(
                      `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.expand`
                    )
                    .click()
                  if (alaosasuoritus.extra.kuvaus !== undefined) {
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.kuvaus.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.kuvaus)
                  }
                  if (alaosasuoritus.extra.tunnustettu !== undefined) {
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.tunnustettu.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.tunnustettu)
                  }
                  if (alaosasuoritus.extra.arviointi !== undefined) {
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.arvosana.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.arvosana)
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.päivä.value`
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

        await expect(
          page.getByTestId('suoritukset.0.yhteensa.value')
        ).toHaveText('39 op')
      })
    })
    test.describe('Vapaatavoitteinen vst-koulutus', () => {
      test.beforeEach(async ({ vstOppijaPage }) => {
        await vstOppijaPage.gotoWithQueryParams(vstKoulutus, {
          newVSTUI: 'true'
        })
      })
      test('Näyttää tiedot oikein', async ({ page }) => {
        const expect = _expect.configure({
          timeout: 2000
        })
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.date')
        ).toHaveText('31.5.2022')
        await expect(
          page.getByTestId('opiskeluoikeus.tila.value.items.0.tila')
        ).toHaveText('Hyväksytysti suoritettu')
        await expect(
          page.getByTestId('opiskeluoikeus.voimassaoloaika')
        ).toHaveText('Opiskeluoikeuden voimassaoloaika: 31.5.2022 – 31.5.2022')
        await expect(
          page.getByTestId('suoritukset.0.toimipiste.value')
        ).toHaveText('Varsinais-Suomen kansanopisto')
        await expect(
          page.getByTestId('suoritukset.0.koulutusmoduuli.tunniste.value')
        ).toHaveText('Vapaan sivistystyön koulutus')
        await expect(
          page.getByTestId(
            'suoritukset.0.koulutusmoduuli.tunniste.koodiarvo.value'
          )
        ).toHaveText('099999')
        await expect(
          page.getByTestId('suoritukset.0.opintokokonaisuus.value')
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
            page.getByTestId(`suoritukset.0.osasuoritukset.${i}.nimi.value`)
          ).toHaveText(osasuoritus.nimi)
          await expect(
            page.getByTestId(`suoritukset.0.osasuoritukset.${i}.laajuus.value`)
          ).toHaveText(osasuoritus.laajuus)
          // Expander
          if (osasuoritus.expander !== undefined) {
            await page
              .getByTestId(`suoritukset.0.osasuoritukset.${i}.expand`)
              .click()
            if (osasuoritus.expander.osasuoritukset !== undefined) {
              let a = 0
              for (const alaosasuoritus of osasuoritus.expander
                .osasuoritukset) {
                await expect(
                  page.getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.arvosana.value`
                  )
                ).toHaveText(alaosasuoritus.arvosana)
                await expect(
                  page.getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.laajuus.value`
                  )
                ).toHaveText(alaosasuoritus.laajuus)
                await expect(
                  page.getByTestId(
                    `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.nimi.value`
                  )
                ).toHaveText(alaosasuoritus.nimi)
                if (alaosasuoritus.extra !== undefined) {
                  await page
                    .getByTestId(
                      `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.expand`
                    )
                    .click()
                  if (alaosasuoritus.extra.kuvaus !== undefined) {
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.kuvaus.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.kuvaus)
                  }
                  if (alaosasuoritus.extra.arviointi !== undefined) {
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.arvosana.value`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.arvosana)
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.päivä.value`
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

        await expect(
          page.getByTestId('suoritukset.0.yhteensa.value')
        ).toHaveText('5 op')
      })
    })
  })
})
