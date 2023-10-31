import { foreachAsync, repeatAsync } from '../util/iterating'
import { expect as _expect, test } from './base'
import { KoskiVSTOppijaPage } from './pages/oppija/KoskiVSTOppijaPage'
import { virkailija } from './setup/auth'

const kotoutumiskoulutus2022 = '1.2.246.562.24.00000000135'
const oppivelvollisilleSuunnattu = '1.2.246.562.24.00000000143'
const jotpaKoulutus = '1.2.246.562.24.00000000140'
const lukutaitokoulutus = '1.2.246.562.24.00000000107'
const kansanopisto = '1.2.246.562.24.00000000105'
const vstKoulutus = '1.2.246.562.24.00000000108'

const openOppijaPage =
  (oppijaOid: string, edit: boolean) =>
  async ({ vstOppijaPage }: { vstOppijaPage: KoskiVSTOppijaPage }) => {
    await vstOppijaPage.gotoWithQueryParams(oppijaOid, {
      newVSTUI: 'true'
    })
    if (edit) {
      await vstOppijaPage.edit()
    }
  }

test.describe('Vapaa sivistystyö', () => {
  test.describe('Katselunäkymä', () => {
    test.use({ storageState: virkailija('kalle') })
    test.beforeAll(async ({ fixtures }) => {
      await fixtures.reset()
    })
    test.describe('VST JOTPA', () => {
      test.beforeEach(openOppijaPage(jotpaKoulutus, false))
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
            'suoritukset.0.osasuoritukset.0.properties.arviointi.0.value.arvosana'
          )
        ).toHaveText('9')
        await expect(
          page.getByTestId(
            'suoritukset.0.osasuoritukset.0.properties.arviointi.0.value.päivä'
          )
        ).toHaveText('1.2.2023')
        await expect(
          page.getByTestId(
            'suoritukset.0.osasuoritukset.0.properties.arviointi.1.value.arvosana'
          )
        ).toHaveText('Hylätty')
        await expect(
          page.getByTestId(
            'suoritukset.0.osasuoritukset.0.properties.arviointi.1.value.päivä'
          )
        ).toHaveText('1.1.2023')
        await page.getByTestId('suoritukset.0.osasuoritukset.1.expand').click()
        await expect(
          page.getByTestId(
            'suoritukset.0.osasuoritukset.1.properties.arviointi.0.value.arvosana'
          )
        ).toHaveText('Hyväksytty')
        await expect(
          page.getByTestId(
            'suoritukset.0.osasuoritukset.1.properties.arviointi.0.value.päivä'
          )
        ).toHaveText('1.3.2023')
        await page.getByTestId('suoritukset.0.osasuoritukset.2.expand').click()
        await expect(
          page.getByTestId('suoritukset.0.yhteensa.value')
        ).toHaveText('3 op')
      })
    })
    test.describe('VST lukutaitokoulutus', () => {
      test.beforeEach(openOppijaPage(lukutaitokoulutus, false))
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
          page.getByTestId('suoritukset.0.laajuus.value')
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
              `suoritukset.0.osasuoritukset.${i}.properties.arviointi.0.value.arvosana`
            )
          ).toHaveText(osasuoritus.expander.arvosana)
          await expect(
            page.getByTestId(
              `suoritukset.0.osasuoritukset.${i}.properties.arviointi.0.value.päivä`
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
      test.beforeEach(openOppijaPage(kansanopisto, false))
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
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.value.arvosana`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.arvosana)
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.value.päivä`
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
      test.beforeEach(openOppijaPage(oppivelvollisilleSuunnattu, false))
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
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.value.arvosana`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.arvosana)
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.value.päivä`
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
      test.beforeEach(openOppijaPage(kotoutumiskoulutus2022, false))
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
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.value.arvosana`
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

        await expect(
          page.getByTestId('suoritukset.0.yhteensa.value')
        ).toHaveText('39 op')
      })
    })
    test.describe('Vapaatavoitteinen vst-koulutus', () => {
      test.beforeEach(openOppijaPage(vstKoulutus, false))
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
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.value.arvosana`
                      )
                    ).toHaveText(alaosasuoritus.extra.arviointi.arvosana)
                    await expect(
                      page.getByTestId(
                        `suoritukset.0.osasuoritukset.${i}.properties.osasuoritukset.${a}.properties.arviointi.0.value.päivä`
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

  test.describe('Muokkausnäkymä', () => {
    const expect = _expect.configure({
      timeout: 2000
    })
    test.use({ storageState: virkailija('kalle') })
    test.beforeEach(async ({ fixtures }) => {
      await fixtures.reset()
    })

    test.describe('Vapaatavoitteinen', () => {
      test.beforeEach(openOppijaPage(vstKoulutus, true))

      test('Uuden osasuorituksen lisäys', async ({ vstOppijaPage }) => {
        const nimi = 'Lopeta turha kiihkoilu'
        await vstOppijaPage.addNewOsasuoritus(nimi)
        await expect(await vstOppijaPage.osasuoritusOptions()).toEqual([
          'Lisää osasuoritus',
          nimi
        ])
        expect(await vstOppijaPage.suorituksenLaajuus()).toEqual('6 op')

        await vstOppijaPage.tallenna()
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
      })

      test('Vaihda arvostelun päivämäärää', async ({ vstOppijaPage }) => {
        const osasuoritus = vstOppijaPage.osasuoritus(0)
        await osasuoritus.expand()
        await osasuoritus.setArvostelunPvm('1.1.2022')
        expect(await osasuoritus.arvostelunPvm()).toEqual('1.1.2022')

        await vstOppijaPage.tallenna()
      })

      test('Lisää alaosasuoritus', async ({ vstOppijaPage }) => {
        const nimi = 'Sienien tunnistaminen 2'
        const osasuoritus = vstOppijaPage.osasuoritus(0)
        await osasuoritus.expand()
        await osasuoritus.addNewAlaosasuoritus(nimi)

        await expect(await osasuoritus.alaosasuoritusOptions()).toEqual([
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
        // Lisää suoritukseen toinen osasuoritus
        const nimi = 'Testikurssi'
        await vstOppijaPage.addNewOsasuoritus(nimi)

        // Poista ensimmäinen osasuoritus
        const osasuoritus = vstOppijaPage.osasuoritus(0)
        await osasuoritus.delete()

        // Kärkeen noussut osasuoritus pitäisi olla sama mikä luotiin hetki sitten
        const osasuoritus2 = vstOppijaPage.osasuoritus(0)
        expect(await osasuoritus.nimi()).toEqual(nimi)

        await vstOppijaPage.tallennaVirheellisenä(
          'Vapaatavoitteisella vapaan sivistystyön koulutuksella tulee olla vähintään yksi arvioitu osasuoritus'
        )
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
        expect(await tila.items(0).date.value()).toEqual('1.1.2022')
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
        ).toBeTruthy()
        await vstOppijaPage.$.opiskeluoikeus.tila.edit.items(0).remove.click()
        await vahvistaminen.edit.merkitseKeskeneräiseksi.click()

        // Merkitse takaisin valmiiksi
        await vstOppijaPage.vahvistaSuoritusUudellaHenkilöllä(
          'Reijo',
          'Rehtori',
          '1.1.2023'
        )

        await vstOppijaPage.tallennaVirheellisenä(
          'lessThanMinimumNumberOfItems: opiskeluoikeudet.0.tila.opiskeluoikeusjaksot'
        )
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
        await expect(await vstOppijaPage.osasuoritusOptions()).toEqual(
          Object.values(kokonaisuudet)
        )
        await poistaKaikkiKokonaisuudet(vstOppijaPage)
        for (const koodi of Object.keys(kokonaisuudet)) {
          await vstOppijaPage.addOsasuoritus(koodi)
        }
        await foreachAsync(Object.values(kokonaisuudet))(async (nimi, i) => {
          const kokonaisuus = vstOppijaPage.osasuoritus(i)
          expect(await kokonaisuus.nimi()).toEqual(nimi)
          await kokonaisuus.setLaajuus(2)
          expect(await kokonaisuus.laajuus()).toEqual('2')
        })
        expect(await vstOppijaPage.suorituksenLaajuus()).toEqual(
          `${Object.values(kokonaisuudet).length * 2} op`
        )

        await vstOppijaPage.tallenna()
      })

      test('Vaihda laajuutta, arvosanaa ja taitotasoa', async ({
        vstOppijaPage
      }) => {
        await poistaKaikkiKokonaisuudet(vstOppijaPage)
        await vstOppijaPage.addOsasuoritus(
          'vstlukutaitokoulutuksenvuorovaikutustilannekokonaisuudensuoritus'
        )

        const osasuoritus = vstOppijaPage.osasuoritus(0)
        expect(await osasuoritus.nimi()).toEqual(
          'Vuorovaikutustilanteissa toimiminen'
        )
        expect(await osasuoritus.laajuus()).toEqual('1')
        expect(await osasuoritus.arvosana()).toEqual('')
        expect(await osasuoritus.taitotaso()).toEqual('')

        await osasuoritus.setLaajuus(20)
        await osasuoritus.setSuoritusarvosana(true)
        await osasuoritus.setKielenTaitotaso('C1.1')

        expect(await osasuoritus.laajuus()).toEqual('20')
        expect(await osasuoritus.arvosana()).toEqual('Hyväksytty')
        expect(await osasuoritus.taitotaso()).toEqual('C1.1')

        await vstOppijaPage.tallenna()
      })

      test('Vaihda arvostelun päivämäärää', async ({ vstOppijaPage }) => {
        const osasuoritus = vstOppijaPage.osasuoritus(0)
        await osasuoritus.expand()
        await osasuoritus.setArvostelunPvm('1.1.2022')
        expect(await osasuoritus.arvostelunPvm()).toEqual('1.1.2022')

        await vstOppijaPage.tallenna()
      })

      test('Tilan muokkaaminen', async ({ vstOppijaPage }) => {
        const tila = vstOppijaPage.$.opiskeluoikeus.tila.edit
        await tila.add.click()
        await tila.modal.date.set('1.1.2023')
        await tila.modal.tila.set('valmistunut')
        await tila.modal.submit.click()
        expect(await tila.items(0).date.value()).toEqual('1.9.2021')
        expect(await tila.items(0).tila.value()).toEqual('Läsnä')
        expect(await tila.items(1).date.value()).toEqual('1.1.2023')
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
          'Rehtori',
          '1.1.2023'
        )

        await vstOppijaPage.tallennaVirheellisenä(
          'lessThanMinimumNumberOfItems: opiskeluoikeudet.0.tila.opiskeluoikeusjaksot'
        )
      })
    })

    test.describe('Oppivelvollisille suunnattu vst-koulutus', () => {
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
        await expect(await vstOppijaPage.osasuoritusOptions()).toEqual(
          Object.values(osaamiskokonaisuudet)
        )
        for (const koodi of Object.keys(osaamiskokonaisuudet)) {
          await vstOppijaPage.addOsasuoritus(koodi)
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
        await expect(await vstOppijaPage.suuntautumisopinnotOptions()).toEqual([
          'Valinnaiset suuntautumisopinnot'
        ])

        await vstOppijaPage.addSuuntautumisopinto()
        const suuntautumisopinto = vstOppijaPage.osasuoritus(0)

        await suuntautumisopinto.expand()
        await foreachAsync(Object.keys(muuallaSuoritutetutOpinnot))((koodi) =>
          suuntautumisopinto.addAlaosasuoritus(koodi)
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
        await expect(await vstOppijaPage.suuntautumisopinnotOptions()).toEqual([
          'Valinnaiset suuntautumisopinnot'
        ])

        await vstOppijaPage.addSuuntautumisopinto()
        const suuntautumisopinto = vstOppijaPage.osasuoritus(0)

        await suuntautumisopinto.expand()
        suuntautumisopinto.addPaikallinenOpintokokonaisuus(
          'Työelämäharjoittelu'
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
        await osaamiskokonaisuus.addPaikallinenOpintokokonaisuus('Testi')

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

        expect(await tila.items(0).date.value()).toEqual('1.9.2021')
        expect(await tila.items(0).tila.value()).toEqual('Läsnä')
        expect(await tila.items(1).date.value()).toEqual('1.1.2023')
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
          'Rehtori',
          '1.1.2023'
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

      test('Osasuorituksien lisäys sekä laajuuksien ja arvosanan asettaminen', async ({
        vstOppijaPage
      }) => {
        await poistaKaikkiOsasuoritukset(vstOppijaPage)
        await expect(await vstOppijaPage.osasuoritusOptions()).toEqual(
          Object.values(osasuoritukset)
        )
        await foreachAsync(Object.entries(osasuoritukset))(
          async ([koodi, nimi], index) => {
            await vstOppijaPage.addOsasuoritus(koodi)
            const osasuoritus = vstOppijaPage.osasuoritus(index)
            expect(await osasuoritus.nimi()).toEqual(nimi)

            await osasuoritus.setLaajuus(1 + index)
            expect(await osasuoritus.laajuus()).toEqual(`${1 + index}`)

            if (koodi !== 'ohjaus') {
              await osasuoritus.setSuoritusarvosana(true)
              expect(await osasuoritus.arvosana()).toEqual('Hyväksytty')
            }
          }
        )
        expect(await vstOppijaPage.suorituksenLaajuus()).toEqual('10 op')

        await vstOppijaPage.tallennaVirheellisenä(
          'Kielten ja viestinnän osasuoritusta ei voi hyväksyä ennen kuin kaikki pakolliset alaosasuoritukset on arvioitu',
          'Oppiaineen laajuus puuttuu',
          'Oppiaineen laajuus puuttuu'
        )
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
        expect(await tila.items(0).date.value()).toEqual('1.8.2022')
        expect(await tila.items(0).tila.value()).toEqual('Läsnä')
        expect(await tila.items(1).date.value()).toEqual('1.1.2023')
        expect(await tila.items(1).tila.value()).toEqual('Valmistunut')
        expect(await tila.add.isVisible()).toBeFalsy()

        await vstOppijaPage.tallennaVirheellisenä(
          'Suoritukselta koulutus/999910 puuttuu vahvistus, vaikka opiskeluoikeus on tilassa Valmistunut',
          'Kielten ja viestinnän osasuoritusta ei voi hyväksyä ennen kuin kaikki pakolliset alaosasuoritukset on arvioitu',
          "Oppiaineen 'Yhteiskunta- ja työelämäosaaminen' suoritettu laajuus liian suppea (12.0 op, pitäisi olla vähintään 20.0 op)",
          "Oppiaineen 'Työssäoppiminen' suoritettu laajuus liian suppea (4.0 op, pitäisi olla vähintään 8.0 op)"
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
          'Rehtori',
          '1.1.2023'
        )

        await vstOppijaPage.tallennaVirheellisenä(
          'Kielten ja viestinnän osasuoritusta ei voi hyväksyä ennen kuin kaikki pakolliset alaosasuoritukset on arvioitu',
          "Oppiaineen 'Yhteiskunta- ja työelämäosaaminen' suoritettu laajuus liian suppea (12.0 op, pitäisi olla vähintään 20.0 op)",
          "Oppiaineen 'Työssäoppiminen' suoritettu laajuus liian suppea (4.0 op, pitäisi olla vähintään 8.0 op)",
          "Oppiaineen 'Ohjaus' suoritettu laajuus liian suppea (4.0 op, pitäisi olla vähintään 7.0 op)"
        )
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

          await osasuoritus.setLaajuus(1 + i)
          expect(await osasuoritus.laajuus()).toEqual(`${i + 1}`)

          await osasuoritus.setJotpaArvosana(4 + i)
          expect(await osasuoritus.arvosana()).toEqual(`${4 + i}`)

          await osasuoritus.setSuoritusarvosana(true, true)
          expect(await osasuoritus.arvosana()).toEqual('Hyväksytty')
        })

        expect(await vstOppijaPage.osasuoritusOptions()).toEqual([
          'Lisää osasuoritus',
          ...osasuoritukset
        ])
        expect(await vstOppijaPage.laajuudetYhteensä()).toEqual('6 op')

        await vstOppijaPage.tallenna()
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

        expect(await tila.items(0).date.value()).toEqual('1.1.2023')
        expect(await tila.items(0).tila.value()).toEqual('Läsnä')
        expect(await tila.items(0).rahoitus.value()).toEqual(
          '(Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus)'
        )
        expect(await tila.items(1).date.value()).toEqual('1.3.2023')
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
          'Rehtori',
          '1.1.2023'
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
  })
})
