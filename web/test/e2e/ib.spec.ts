import { expect, test } from './base'
import { virkailija } from './setup/auth'

test.describe('IB', () => {
  test.use({ storageState: virkailija('kalle') })
  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test.describe('Pre-IB 2015', () => {
    const oppijaOid = '1.2.246.562.24.00000000060'

    test.beforeEach(async ({ oppijaPage }) => {
      await oppijaPage.goto(oppijaOid)
    })

    test('Opiskeluoikeuden tiedot näytetään oikein', async ({
      ibOppijaPage
    }) => {
      const tiedot = ibOppijaPage.$.opiskeluoikeus

      await expect(tiedot.voimassaoloaika.elem).toHaveText(
        'Opiskeluoikeuden voimassaoloaika: 1.9.2012 – 4.6.2016'
      )
      await expect(tiedot.tila.value.items(0).tila.elem).toHaveText(
        'Valmistunut'
      )
      await expect(tiedot.tila.value.items(0).rahoitus.elem).toHaveText(
        'Valtionosuusrahoitteinen koulutus'
      )
      await expect(tiedot.tila.value.items(1).tila.elem).toHaveText('Läsnä')
      await expect(tiedot.tila.value.items(1).rahoitus.elem).toHaveText(
        'Valtionosuusrahoitteinen koulutus'
      )
    })

    test('Suorituksen tiedot näkyvissä', async ({ ibOppijaPage }) => {
      const suoritus = ibOppijaPage.$.suoritukset(0)
      await expect(suoritus.koulutus.elem).toHaveText('Pre-IB')
      await expect(await suoritus.organisaatio.value()).toEqual('Ressun lukio')
      await expect(await suoritus.ryhmä.value()).toEqual('AH')
      await expect(await suoritus.suorituskieli.value()).toEqual('englanti')

      const vahvistus = suoritus.suorituksenVahvistus.value
      await expect(vahvistus.status.elem).toHaveText('Suoritus valmis')
      await expect(vahvistus.henkilö(0).elem).toHaveText(
        'Reijo Reksi (rehtori)'
      )
    })

    test('Oppiaineiden ja kurssien arvosanat näytetään', async ({
      ibOppijaPage
    }) => {
      await ibOppijaPage.testOppiaineryhmät({
        oppiaineet: [
          {
            nimi: 'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus',
            arvosana: '8',
            kurssit: [
              { tunniste: 'ÄI1', arvosana: '8', laajuus: 1 },
              { tunniste: 'ÄI2', arvosana: '8', laajuus: 1 },
              { tunniste: 'ÄI3', arvosana: '8', laajuus: 1 }
            ]
          },
          {
            nimi: 'A1-kieli, englanti',
            arvosana: '10',
            kurssit: [
              { tunniste: 'ENA1', arvosana: '10', laajuus: 1 },
              { tunniste: 'ENA2', arvosana: '10', laajuus: 1 },
              { tunniste: 'ENA5', arvosana: '10', laajuus: 1 }
            ]
          },
          {
            nimi: 'B1-kieli, ruotsi',
            arvosana: '7',
            kurssit: [
              { tunniste: 'RUB11', arvosana: '8', laajuus: 1 },
              { tunniste: 'RUB12', arvosana: '7', laajuus: 1 }
            ]
          },
          {
            nimi: 'B2-kieli, ranska',
            arvosana: '9',
            kurssit: [
              {
                tunniste: 'RAN3',
                paikallinen: true,
                arvosana: '9',
                laajuus: 1
              }
            ]
          },
          {
            nimi: 'B3-kieli, espanja',
            arvosana: '6',
            kurssit: [
              {
                tunniste: 'ES1',
                paikallinen: true,
                arvosana: 'S',
                laajuus: 1
              }
            ]
          },
          {
            nimi: 'Matematiikka, pitkä oppimäärä',
            arvosana: '7',
            kurssit: [
              { tunniste: 'MAA2', arvosana: '7', laajuus: 1 },
              { tunniste: 'MAA11', arvosana: '7', laajuus: 1 },
              { tunniste: 'MAA12', arvosana: '7', laajuus: 1 },
              { tunniste: 'MAA13', arvosana: '7', laajuus: 1 }
            ]
          },
          {
            nimi: 'Biologia',
            arvosana: '8',
            kurssit: [
              { tunniste: 'BI1', arvosana: '8', laajuus: 1 },
              {
                tunniste: 'BI10',
                paikallinen: true,
                arvosana: 'S',
                laajuus: 1
              }
            ]
          },
          {
            nimi: 'Maantieto',
            arvosana: '10',
            kurssit: [{ tunniste: 'GE2', arvosana: '10', laajuus: 1 }]
          },
          {
            nimi: 'Fysiikka',
            arvosana: '7',
            kurssit: [{ tunniste: 'FY1', arvosana: '7', laajuus: 1 }]
          },
          {
            nimi: 'Kemia',
            arvosana: '8',
            kurssit: [{ tunniste: 'KE1', arvosana: '8', laajuus: 1 }]
          },
          {
            nimi: 'Uskonto/Elämänkatsomustieto',
            arvosana: '10',
            kurssit: [{ tunniste: 'UK4', arvosana: '10', laajuus: 1 }]
          },
          {
            nimi: 'Filosofia',
            arvosana: '7',
            kurssit: [{ tunniste: 'FI1', arvosana: 'S', laajuus: 1 }]
          },
          {
            nimi: 'Psykologia',
            arvosana: '8',
            kurssit: [{ tunniste: 'PS1', arvosana: '8', laajuus: 1 }]
          },
          {
            nimi: 'Historia',
            arvosana: '8',
            kurssit: [
              { tunniste: 'HI3', arvosana: '9', laajuus: 1 },
              { tunniste: 'HI4', arvosana: '8', laajuus: 1 },
              {
                tunniste: 'HI10',
                paikallinen: true,
                arvosana: 'S',
                laajuus: 1
              }
            ]
          },
          {
            nimi: 'Yhteiskuntaoppi',
            arvosana: '8',
            kurssit: [{ tunniste: 'YH1', arvosana: '8', laajuus: 1 }]
          },
          {
            nimi: 'Liikunta',
            arvosana: '8',
            kurssit: [{ tunniste: 'LI1', arvosana: '8', laajuus: 1 }]
          },
          {
            nimi: 'Musiikki',
            arvosana: '8',
            kurssit: [{ tunniste: 'MU1', arvosana: '8', laajuus: 1 }]
          },
          {
            nimi: 'Kuvataide',
            arvosana: '9',
            kurssit: [{ tunniste: 'KU1', arvosana: '9', laajuus: 1 }]
          },
          {
            nimi: 'Terveystieto',
            arvosana: '7',
            kurssit: [{ tunniste: 'TE1', arvosana: '7', laajuus: 1 }]
          },
          {
            nimi: 'Opinto-ohjaus',
            arvosana: '7',
            kurssit: [{ tunniste: 'OP1', arvosana: 'S', laajuus: 1 }]
          },
          {
            nimi: 'Teemaopinnot',
            arvosana: 'S',
            kurssit: [
              {
                tunniste: 'MTA',
                paikallinen: true,
                arvosana: 'S',
                laajuus: 1
              }
            ]
          }
        ]
      })

      await expect(
        ibOppijaPage.$.suoritukset(0).suoritettujaKurssejaYhteensä.elem
      ).toHaveText('32')
    })

    test.describe('Tietojen muokkaaminen', () => {
      test.beforeEach(async ({ ibOppijaPage }) => {
        await ibOppijaPage.edit()
      })

      test.describe('Suoritusten tiedot', () => {
        test.describe('Oppiaineiden muokkaus', () => {
          test('Pre-IB 2015:n arvosana-asteikko on oikea', async ({
            ibOppijaPage
          }) => {
            const oppiaine = ibOppijaPage.oppiaineryhmä().oppiaineet(0)
            expect(await oppiaine.arvosana.options()).toEqual([
              '4',
              '5',
              '6',
              '7',
              '8',
              '9',
              '10',
              'H',
              'O',
              'S'
            ])
          })

          test('Arvosanan muuttaminen', async ({ ibOppijaPage }) => {
            const oppiaine = ibOppijaPage.oppiaineryhmä().oppiaineet(0)
            await oppiaine.arvosana.set('arviointiasteikkoyleissivistava_10')
            await ibOppijaPage.tallenna()
            await expect(oppiaine.arvosana.viewer).toHaveText('10')
          })

          test.describe('Kurssit', () => {
            test('Pre-IB 2015:n arvosanan muuttaminen', async ({
              ibOppijaPage
            }) => {
              const kurssi = ibOppijaPage
                .oppiaineryhmä()
                .oppiaineet(0)
                .kurssit(0)
              await kurssi.arvosana.set('arviointiasteikkoyleissivistava_10')
              await ibOppijaPage.tallenna()
              await expect(kurssi.arvosana.viewer).toHaveText('10')
            })

            test('Lukion valtakunnallinen kurssi', async ({ ibOppijaPage }) => {
              const oppiaine = ibOppijaPage.oppiaineryhmä().oppiaineet(0)
              await oppiaine.addKurssi.button.click()

              const modal = oppiaine.modal
              await expect(modal.submit.button).toBeDisabled()

              await modal.tunniste.setByLabel('ÄI5 Teksti ja konteksti')
              await expect(modal.submit.button).toBeDisabled()

              await modal.tyyppi.setByLabel('Pakollinen')
              await modal.submit.button.click()

              const kurssi = oppiaine.kurssit(3)
              await expect(kurssi.tunniste.button).toHaveText('ÄI5')

              await kurssi.delete.button.click()
              await expect(kurssi.tunniste.button).not.toBeAttached()
            })

            test('Lukion paikallinen kurssi', async ({ ibOppijaPage }) => {
              const oppiaine = ibOppijaPage.oppiaineryhmä().oppiaineet(0)
              await oppiaine.addKurssi.button.click()

              const modal = oppiaine.modal
              await expect(modal.submit.button).toBeDisabled()

              await modal.tunniste.set(
                'Paikalliset lukion kurssit.__uusi_paikallinen_lukio__'
              )
              await expect(modal.submit.button).toBeDisabled()

              await modal.paikallinenKoulutus.nimi.set(
                'Sosiaalisen median lukutaito'
              )
              await expect(modal.submit.button).toBeDisabled()

              await modal.paikallinenKoulutus.koodiarvo.set('ÄI11')
              await expect(modal.submit.button).toBeDisabled()

              await modal.paikallinenKoulutus.kuvaus.set(
                'Tutustutaan sosiaalisen median vaikutuskeinoihin'
              )
              await expect(modal.submit.button).toBeDisabled()

              await modal.tyyppi.setByLabel('Pakollinen')
              await modal.submit.button.click()

              const kurssi = oppiaine.kurssit(3)
              await expect(kurssi.tunniste.button).toHaveText('ÄI11 *')

              await kurssi.delete.button.click()
              await expect(kurssi.tunniste.button).not.toBeAttached()
            })
          })
        })

        test.describe('Oppiaineen lisäys', () => {
          test('IB-oppiaine', async ({ ibOppijaPage }) => {
            const suoritus = ibOppijaPage.$.suoritukset(0)
            await suoritus.addOppiaine.button.click()

            const modal = suoritus.modal
            await modal.tunniste.setByLabel('DAN Dance')

            await expect(modal.submit.button).toBeDisabled()
            await expect(modal.kieli.input).not.toBeAttached()
            await expect(modal.äidinkielenKieli.input).not.toBeAttached()
            await expect(modal.matematiikanOppimäärä.input).not.toBeAttached()

            await modal.aineryhmä.setByLabel('The arts')
            await modal.submit.button.click()

            const uusiAineryhmä = ibOppijaPage.oppiaineryhmä(0, 1)
            await expect(uusiAineryhmä.nimi.elem).toHaveText('The arts')

            const uusiOppiaine = uusiAineryhmä.oppiaineet(0)
            await expect(uusiOppiaine.nimi.elem).toHaveText('Dance')

            await uusiOppiaine.delete.button.click()
            await expect(uusiOppiaine.nimi.elem).not.toBeAttached()
          })

          test('Lukion kieliaine', async ({ ibOppijaPage }) => {
            const suoritus = ibOppijaPage.$.suoritukset(0)
            await suoritus.addOppiaine.button.click()

            const modal = suoritus.modal
            await modal.tunniste.setByLabel('A2 A2-kieli')

            await expect(modal.submit.button).toBeDisabled()
            await expect(modal.aineryhmä.input).not.toBeAttached()
            await expect(modal.äidinkielenKieli.input).not.toBeAttached()
            await expect(modal.matematiikanOppimäärä.input).not.toBeAttached()

            await modal.kieli.setByLabel('albania')
            await modal.submit.button.click()

            const uusiOppiaine = ibOppijaPage.oppiaineryhmä(0, 0).oppiaineet(21)
            await expect(uusiOppiaine.nimi.elem).toHaveText('A2-kieli, albania')

            await uusiOppiaine.delete.button.click()
            await expect(uusiOppiaine.nimi.elem).not.toBeAttached()
          })

          test('Lukion matematiikka', async ({ ibOppijaPage }) => {
            const suoritus = ibOppijaPage.$.suoritukset(0)

            await suoritus.oppiaineryhmät(0).oppiaineet(5).delete.button.click() // Poista opiskeluoikeudella jo oleva matematiikka

            await suoritus.addOppiaine.button.click()

            const modal = suoritus.modal
            await modal.tunniste.setByLabel('MA Matematiikka')

            await expect(modal.submit.button).toBeDisabled()
            await expect(modal.aineryhmä.input).not.toBeAttached()
            await expect(modal.kieli.input).not.toBeAttached()
            await expect(modal.äidinkielenKieli.input).not.toBeAttached()

            await modal.matematiikanOppimäärä.setByLabel(
              'Matematiikka, pitkä oppimäärä'
            )
            await modal.submit.button.click()

            const uusiOppiaine = ibOppijaPage.oppiaineryhmä(0, 0).oppiaineet(20)
            await expect(uusiOppiaine.nimi.elem).toHaveText(
              'Matematiikka, pitkä oppimäärä'
            )

            await uusiOppiaine.delete.button.click()
            await expect(uusiOppiaine.nimi.elem).not.toBeAttached()
          })

          test('Lukion äidinkieli', async ({ ibOppijaPage }) => {
            const suoritus = ibOppijaPage.$.suoritukset(0)

            await suoritus.oppiaineryhmät(0).oppiaineet(0).delete.button.click() // Poista opiskeluoikeudella jo oleva äidinkielen oppiaine

            await suoritus.addOppiaine.button.click()

            const modal = suoritus.modal
            await modal.tunniste.setByLabel('AI Äidinkieli ja kirjallisuus')

            await expect(modal.submit.button).toBeDisabled()
            await expect(modal.aineryhmä.input).not.toBeAttached()
            await expect(modal.kieli.input).not.toBeAttached()
            await expect(modal.matematiikanOppimäärä.input).not.toBeAttached()

            await modal.äidinkielenKieli.setByLabel('Suomi viittomakielisille')
            await modal.submit.button.click()

            const uusiOppiaine = ibOppijaPage.oppiaineryhmä(0, 0).oppiaineet(20)
            await expect(uusiOppiaine.nimi.elem).toHaveText(
              'Äidinkieli ja kirjallisuus, Suomi viittomakielisille'
            )

            await uusiOppiaine.delete.button.click()
            await expect(uusiOppiaine.nimi.elem).not.toBeAttached()
          })

          test('Paikallinen oppiaine', async ({ ibOppijaPage }) => {
            const suoritus = ibOppijaPage.$.suoritukset(0)

            await suoritus.addOppiaine.button.click()

            const modal = suoritus.modal
            await modal.tunniste.set(
              'Paikallinen oppiaine.__uusi_paikallinen_oppiaine__'
            )

            await expect(modal.submit.button).toBeDisabled()
            await expect(modal.aineryhmä.input).not.toBeAttached()
            await expect(modal.kieli.input).not.toBeAttached()
            await expect(modal.matematiikanOppimäärä.input).not.toBeAttached()
            await expect(modal.äidinkielenKieli.input).not.toBeAttached()

            await modal.paikallinenKoulutus.nimi.set('Elektroniikka')
            await expect(modal.submit.button).toBeDisabled()

            await modal.paikallinenKoulutus.koodiarvo.set('ELE')
            await expect(modal.submit.button).toBeDisabled()

            await modal.paikallinenKoulutus.kuvaus.set('Kolvi tutuksi')
            await modal.submit.button.click()

            const uusiOppiaine = ibOppijaPage.oppiaineryhmä(0, 0).oppiaineet(21)
            await expect(uusiOppiaine.nimi.elem).toHaveText('Elektroniikka')

            await uusiOppiaine.delete.button.click()
            await expect(uusiOppiaine.nimi.elem).not.toBeAttached()
          })
        })
      })
    })
  })

  test.describe('Pre-IB 2019', () => {
    const oppijaOid = '1.2.246.562.24.00000000062'

    test.beforeEach(async ({ oppijaPage }) => {
      await oppijaPage.goto(oppijaOid)
    })

    test.describe('Tietojen näyttäminen', () => {
      test('Pre-IB 2019:n opiskeluoikeuden tiedot näytetään oikein', async ({
        ibOppijaPage
      }) => {
        const tiedot = ibOppijaPage.$.opiskeluoikeus

        await expect(tiedot.voimassaoloaika.elem).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.9.2012 – 4.6.2016'
        )
        await expect(tiedot.tila.value.items(0).tila.elem).toHaveText(
          'Valmistunut'
        )
        await expect(tiedot.tila.value.items(0).rahoitus.elem).toHaveText(
          'Valtionosuusrahoitteinen koulutus'
        )
        await expect(tiedot.tila.value.items(1).tila.elem).toHaveText('Läsnä')
        await expect(tiedot.tila.value.items(1).rahoitus.elem).toHaveText(
          'Valtionosuusrahoitteinen koulutus'
        )
      })

      test('Pre-IB 2019:n suorituksen tiedot näkyvissä', async ({
        ibOppijaPage
      }) => {
        const suoritus = ibOppijaPage.$.suoritukset(0)
        await expect(suoritus.koulutus.elem).toHaveText('Pre-IB 2019')
        await expect(await suoritus.organisaatio.value()).toEqual(
          'Ressun lukio'
        )
        await expect(await suoritus.ryhmä.value()).toEqual('AH')
        await expect(await suoritus.suorituskieli.value()).toEqual('englanti')
        await expect(
          await suoritus.todistuksellaNäkyvätLisätiedot.value()
        ).toEqual('Suorittanut etäopetuskokeiluna')

        const vahvistus = suoritus.suorituksenVahvistus.value
        await expect(vahvistus.status.elem).toHaveText('Suoritus valmis')
        await expect(vahvistus.henkilö(0).elem).toHaveText(
          'Reijo Reksi (rehtori)'
        )
      })

      test('Pre-IB 2019:n oppiaineiden ja kurssien arvosanat näytetään', async ({
        ibOppijaPage
      }) => {
        await ibOppijaPage.testOppiaineryhmät({
          oppiaineet: [
            {
              nimi: 'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus',
              arvosana: '9',
              kurssit: [
                { tunniste: 'ÄI1', arvosana: '8', laajuus: 2 },
                { tunniste: 'ÄI2', arvosana: '8', laajuus: 2 }
              ]
            },
            {
              nimi: 'Matematiikka, lyhyt oppimäärä',
              arvosana: '10',
              kurssit: [
                { tunniste: 'MAB2', arvosana: '10', laajuus: 2 },
                { tunniste: 'MAB3', arvosana: '10', laajuus: 2 }
              ]
            },
            {
              nimi: 'Uskonto/Elämänkatsomustieto',
              arvosana: '9',
              kurssit: [{ tunniste: 'UK1', arvosana: '9', laajuus: 2 }]
            },
            {
              nimi: 'Liikunta',
              arvosana: '8',
              kurssit: [
                { tunniste: 'LI2', arvosana: '8', laajuus: 2 },
                {
                  tunniste: 'LITT1',
                  paikallinen: true,
                  arvosana: 'S',
                  laajuus: 1
                }
              ]
            },
            {
              nimi: 'Fysiikka',
              arvosana: '8',
              kurssit: []
            },
            {
              nimi: 'Kemia',
              arvosana: '7',
              kurssit: [{ tunniste: 'KE1', arvosana: '6', laajuus: 2 }]
            },
            {
              nimi: 'A-kieli, englanti',
              arvosana: '9',
              kurssit: [
                { tunniste: 'ENA1', arvosana: '10', laajuus: 2 },
                { tunniste: 'ENA2', arvosana: '9', laajuus: 2 }
              ]
            },
            {
              nimi: 'A-kieli, espanja',
              arvosana: '6',
              kurssit: [
                { tunniste: 'VKA1', arvosana: '6', laajuus: 2 },
                { tunniste: 'VKA2', arvosana: '7', laajuus: 2 }
              ]
            },
            {
              nimi: 'Tanssi ja liike',
              arvosana: '6',
              kurssit: [
                {
                  tunniste: 'ITT234',
                  paikallinen: true,
                  arvosana: '6',
                  laajuus: 1
                },
                {
                  tunniste: 'ITT235',
                  paikallinen: true,
                  arvosana: '7',
                  laajuus: 1
                }
              ]
            },
            {
              nimi: 'Muut suoritukset',
              kurssit: [
                { tunniste: 'RUB11', arvosana: '6', laajuus: 2 },
                { tunniste: 'VKAAB31', arvosana: '6', laajuus: 2 },
                { tunniste: 'ÄI1', arvosana: '7', laajuus: 2 }
              ]
            },
            {
              nimi: 'Lukiodiplomit',
              kurssit: [{ tunniste: 'KULD2', arvosana: '9', laajuus: 2 }]
            },
            {
              nimi: 'Teemaopinnot',
              kurssit: [
                {
                  tunniste: 'HAI765',
                  paikallinen: true,
                  arvosana: 'S',
                  laajuus: 1
                }
              ]
            }
          ]
        })
      })
    })

    test.describe('Tietojen muokkaaminen', () => {
      test.beforeEach(async ({ ibOppijaPage }) => {
        await ibOppijaPage.edit()
      })

      test.describe('Oppiaineiden muokkaus', () => {
        test.describe('Oppiaineen arvosana', () => {
          test('Arvosana-asteikko on oikea', async ({ ibOppijaPage }) => {
            const oppiaine = ibOppijaPage.oppiaineryhmä().oppiaineet(0)
            expect(await oppiaine.arvosana.options()).toEqual([
              '4',
              '5',
              '6',
              '7',
              '8',
              '9',
              '10',
              'H',
              'S'
            ])
          })

          test('Pre-IB 2019:n oppiaineen arvosanan muuttaminen', async ({
            ibOppijaPage
          }) => {
            const oppiaine = ibOppijaPage.oppiaineryhmä().oppiaineet(0)
            await oppiaine.arvosana.set('arviointiasteikkoyleissivistava_10')
            await ibOppijaPage.tallenna()
            await expect(oppiaine.arvosana.viewer).toHaveText('10')
          })
        })

        test.describe('Kurssien muokkaus', () => {
          test('Pre-IB 2019:n kurssin arvosanan muuttaminen', async ({
            ibOppijaPage
          }) => {
            const kurssi = ibOppijaPage.oppiaineryhmä().oppiaineet(0).kurssit(0)
            await kurssi.arvosana.set('arviointiasteikkoyleissivistava_10')
            await ibOppijaPage.tallenna()
            await expect(kurssi.arvosana.viewer).toHaveText('10')
          })

          test('Lukion moduulin suoritus oppiaineissa', async ({
            ibOppijaPage
          }) => {
            const oppiaine = ibOppijaPage.oppiaineryhmä().oppiaineet(0)
            await oppiaine.addKurssi.button.click()

            const modal = oppiaine.modal
            await expect(modal.submit.button).toBeDisabled()

            await modal.tunniste.setByLabel('ÄI9 Vuorovaikutus 3')
            await expect(modal.submit.button).toBeDisabled()

            await modal.laajuus.set('2')
            await modal.submit.button.click()

            const kurssi = oppiaine.kurssit(2)
            await expect(kurssi.tunniste.button).toHaveText('ÄI9')

            await kurssi.delete.button.click()
            await expect(kurssi.tunniste.button).not.toBeAttached()
          })

          test('Lukion paikallisen opintojakson suoritus', async ({
            ibOppijaPage
          }) => {
            const oppiaine = ibOppijaPage.oppiaineryhmä().oppiaineet(0)
            await oppiaine.addKurssi.button.click()

            const modal = oppiaine.modal
            await expect(modal.submit.button).toBeDisabled()

            await modal.tunniste.set(
              'Paikalliset opintojaksot.__uusi_paikallinen_lukio__'
            )
            await expect(modal.submit.button).toBeDisabled()

            await modal.paikallinenKoulutus.nimi.set(
              'Sosiaalisen median lukutaito'
            )
            await expect(modal.submit.button).toBeDisabled()

            await modal.paikallinenKoulutus.koodiarvo.set('ÄI11')
            await expect(modal.submit.button).toBeDisabled()

            await modal.paikallinenKoulutus.kuvaus.set(
              'Tutustutaan sosiaalisen median vaikutuskeinoihin'
            )
            await expect(modal.submit.button).toBeDisabled()

            await modal.laajuus.set('2')
            await modal.submit.button.click()

            const kurssi = oppiaine.kurssit(2)
            await expect(kurssi.tunniste.button).toHaveText('ÄI11 *')

            await kurssi.delete.button.click()
            await expect(kurssi.tunniste.button).not.toBeAttached()
          })
        })

        test.describe('Oppiaineen lisäys', () => {
          test('Pre-IB 2019:n lukion kieliaine', async ({ ibOppijaPage }) => {
            const suoritus = ibOppijaPage.$.suoritukset(0)
            await suoritus.addOppiaine.button.click()

            const modal = suoritus.modal
            await modal.tunniste.setByLabel('B2 B2-kieli')

            await expect(modal.submit.button).toBeDisabled()
            await expect(modal.äidinkielenKieli.input).not.toBeAttached()
            await expect(modal.matematiikanOppimäärä.input).not.toBeAttached()

            await modal.kieli.setByLabel('albania')
            await modal.submit.button.click()

            const uusiOppiaine = ibOppijaPage.oppiaineryhmä(0, 0).oppiaineet(12)
            await expect(uusiOppiaine.nimi.elem).toHaveText('B2-kieli, albania')

            await uusiOppiaine.delete.button.click()
            await expect(uusiOppiaine.nimi.elem).not.toBeAttached()
          })

          test('Pre-IB 2019:n lukion matematiikka', async ({
            ibOppijaPage
          }) => {
            const suoritus = ibOppijaPage.$.suoritukset(0)

            await suoritus.oppiaineryhmät(0).oppiaineet(1).delete.button.click() // Poista opiskeluoikeudella jo oleva matematiikka

            await suoritus.addOppiaine.button.click()

            const modal = suoritus.modal
            await modal.tunniste.setByLabel('MA Matematiikka')

            await expect(modal.submit.button).toBeDisabled()
            await expect(modal.kieli.input).not.toBeAttached()
            await expect(modal.äidinkielenKieli.input).not.toBeAttached()

            await modal.matematiikanOppimäärä.setByLabel(
              'Matematiikka, pitkä oppimäärä'
            )
            await modal.submit.button.click()

            const uusiOppiaine = ibOppijaPage.oppiaineryhmä(0, 0).oppiaineet(11)
            await expect(uusiOppiaine.nimi.elem).toHaveText(
              'Matematiikka, pitkä oppimäärä'
            )

            await uusiOppiaine.delete.button.click()
            await expect(uusiOppiaine.nimi.elem).not.toBeAttached()
          })

          test('Pre-IB 2019:n lukion äidinkieli', async ({ ibOppijaPage }) => {
            const suoritus = ibOppijaPage.$.suoritukset(0)

            await suoritus.oppiaineryhmät(0).oppiaineet(0).delete.button.click() // Poista opiskeluoikeudella jo oleva äidinkielen oppiaine

            await suoritus.addOppiaine.button.click()

            const modal = suoritus.modal
            await modal.tunniste.setByLabel('AI Äidinkieli ja kirjallisuus')

            await expect(modal.submit.button).toBeDisabled()
            await expect(modal.aineryhmä.input).not.toBeAttached()
            await expect(modal.kieli.input).not.toBeAttached()
            await expect(modal.matematiikanOppimäärä.input).not.toBeAttached()

            await modal.äidinkielenKieli.setByLabel('Suomi viittomakielisille')
            await modal.submit.button.click()

            const uusiOppiaine = ibOppijaPage.oppiaineryhmä(0, 0).oppiaineet(11)
            await expect(uusiOppiaine.nimi.elem).toHaveText(
              'Äidinkieli ja kirjallisuus, Suomi viittomakielisille'
            )

            await uusiOppiaine.delete.button.click()
            await expect(uusiOppiaine.nimi.elem).not.toBeAttached()
          })

          test('Pre-IB 2019:n paikallinen oppiaine', async ({
            ibOppijaPage
          }) => {
            const suoritus = ibOppijaPage.$.suoritukset(0)

            await suoritus.addOppiaine.button.click()

            const modal = suoritus.modal
            await modal.tunniste.set(
              'Paikallinen oppiaine.__uusi_paikallinen_oppiaine__'
            )

            await expect(modal.submit.button).toBeDisabled()
            await expect(modal.aineryhmä.input).not.toBeAttached()
            await expect(modal.kieli.input).not.toBeAttached()
            await expect(modal.matematiikanOppimäärä.input).not.toBeAttached()
            await expect(modal.äidinkielenKieli.input).not.toBeAttached()

            await modal.paikallinenKoulutus.nimi.set('Elektroniikka')
            await expect(modal.submit.button).toBeDisabled()

            await modal.paikallinenKoulutus.koodiarvo.set('ELE')
            await expect(modal.submit.button).toBeDisabled()

            await modal.paikallinenKoulutus.kuvaus.set('Kolvi tutuksi')
            await modal.submit.button.click()

            const uusiOppiaine = ibOppijaPage.oppiaineryhmä(0, 0).oppiaineet(12)
            await expect(uusiOppiaine.nimi.elem).toHaveText('Elektroniikka')

            await uusiOppiaine.delete.button.click()
            await expect(uusiOppiaine.nimi.elem).not.toBeAttached()
          })
        })
      })
    })
  })

  test.describe('IB-tutkinto', () => {
    test.describe('Tietojen näyttäminen', () => {
      const oppijaOid = '1.2.246.562.24.00000000060'

      test.beforeEach(async ({ oppijaPage, ibOppijaPage }) => {
        await oppijaPage.goto(oppijaOid)
        await ibOppijaPage.selectSuoritus(1)
      })

      test('IB-tutkinnon opiskeluoikeuden tiedot näytetään oikein', async ({
        ibOppijaPage
      }) => {
        const tiedot = ibOppijaPage.$.opiskeluoikeus

        await expect(tiedot.voimassaoloaika.elem).toHaveText(
          'Opiskeluoikeuden voimassaoloaika: 1.9.2012 – 4.6.2016'
        )
        await expect(tiedot.tila.value.items(0).tila.elem).toHaveText(
          'Valmistunut'
        )
        await expect(tiedot.tila.value.items(0).rahoitus.elem).toHaveText(
          'Valtionosuusrahoitteinen koulutus'
        )
        await expect(tiedot.tila.value.items(1).tila.elem).toHaveText('Läsnä')
        await expect(tiedot.tila.value.items(1).rahoitus.elem).toHaveText(
          'Valtionosuusrahoitteinen koulutus'
        )
      })

      test('IB-tutkinnon suorituksen tiedot näkyvissä', async ({
        ibOppijaPage
      }) => {
        const suoritus = ibOppijaPage.$.suoritukset(1)
        await expect(suoritus.koulutus.elem).toHaveText(
          'IB-tutkinto (International Baccalaureate)'
        )
        await expect(await suoritus.organisaatio.value()).toEqual(
          'Ressun lukio'
        )
        await expect(await suoritus.ryhmä.value()).toEqual('AH')
        await expect(await suoritus.suorituskieli.value()).toEqual('englanti')
        await expect(suoritus.theoryOfKnowledge.arvosana.viewer).toHaveText(
          'Excellent'
        )
        await expect(suoritus.theoryOfKnowledge.pakollinen.viewer).toHaveText(
          'Kyllä'
        )
        await expect(
          suoritus.theoryOfKnowledge.kurssit(0).tunniste.button
        ).toHaveText('TOK1')
        await expect(
          suoritus.theoryOfKnowledge.kurssit(0).arvosana.viewer
        ).toHaveText('S')
        await expect(
          suoritus.theoryOfKnowledge.kurssit(1).tunniste.button
        ).toHaveText('TOK2')
        await expect(
          suoritus.theoryOfKnowledge.kurssit(1).arvosana.viewer
        ).toHaveText('S')
        await expect(suoritus.extendedEssay.oppiaine.elem).toHaveText(
          'Language A: language and literature'
        )
        await expect(suoritus.extendedEssay.kieli.elem).toHaveText('englanti')
        await expect(suoritus.extendedEssay.taso.elem).toHaveText(
          'Higher Level'
        )
        await expect(suoritus.extendedEssay.ryhma.elem).toHaveText(
          'Studies in language and literature'
        )
        await expect(suoritus.extendedEssay.pakollinen.elem).toHaveText(
          'Pakollinen'
        )
        await expect(suoritus.extendedEssay.aihe.elem).toHaveText(
          "How is the theme of racial injustice treated in Harper Lee's To Kill a Mockingbird and Solomon Northup's 12 Years a Slave"
        )
        await expect(suoritus.extendedEssay.arvosana.elem).toHaveText('B Good')
        await expect(suoritus.creativityActionService.elem).toHaveText('pass')
        await expect(suoritus.lisäpisteet.viewer).toHaveText('3')

        const vahvistus = suoritus.suorituksenVahvistus.value
        await expect(vahvistus.status.elem).toHaveText('Suoritus valmis')
        await expect(vahvistus.henkilö(0).elem).toHaveText(
          'Reijo Reksi (rehtori)'
        )
      })

      test('IB-tutkinnon oppiaineiden ja kurssien arvosanat näytetään', async ({
        ibOppijaPage
      }) => {
        await ibOppijaPage.testSuorituksenOppiaineryhmät(
          1,
          {
            aineryhmä: 'Studies in language and literature',
            oppiaineet: [
              {
                nimi: 'Language A: literature, suomi',
                arvosana: '4',
                predictedGrade: '4',
                kurssit: [
                  { tunniste: 'FIN_S1', arvosana: '4' },
                  { tunniste: 'FIN_S2', arvosana: '4' },
                  { tunniste: 'FIN_S3', arvosana: 'S' },
                  { tunniste: 'FIN_S4', arvosana: '5' },
                  { tunniste: 'FIN_S5', arvosana: '6' },
                  { tunniste: 'FIN_S6', arvosana: '5' },
                  { tunniste: 'FIN_S7', arvosana: '5' },
                  { tunniste: 'FIN_S8', arvosana: 'S' },
                  { tunniste: 'FIN_S9', arvosana: '5' }
                ]
              },
              {
                nimi: 'Language A: language and literature, englanti',
                arvosana: '7',
                predictedGrade: '6',
                kurssit: [
                  { tunniste: 'ENG_B_H1', arvosana: '6' },
                  { tunniste: 'ENG_B_H2', arvosana: '7' },
                  { tunniste: 'ENG_B_H4', arvosana: 'S' },
                  { tunniste: 'ENG_B_H5', arvosana: '6' },
                  { tunniste: 'ENG_B_H6', arvosana: '6' },
                  { tunniste: 'ENG_B_H8', arvosana: '5' }
                ]
              }
            ]
          },
          {
            aineryhmä: 'Individuals and societies',
            oppiaineet: [
              {
                nimi: 'History',
                arvosana: '6',
                predictedGrade: '6',
                kurssit: [
                  { tunniste: 'HIS_H3', arvosana: '6' },
                  { tunniste: 'HIS_H4', arvosana: '6' },
                  { tunniste: 'HIS_H5', arvosana: '7' },
                  { tunniste: 'HIS_H6', arvosana: '6' },
                  { tunniste: 'HIS_H7', arvosana: '1' },
                  { tunniste: 'HIS_H9', arvosana: 'S' }
                ]
              },
              {
                nimi: 'Psychology',
                arvosana: '7',
                predictedGrade: '7',
                kurssit: [
                  { tunniste: 'PSY_S1', arvosana: '6' },
                  { tunniste: 'PSY_S2', arvosana: '6' },
                  { tunniste: 'PSY_S3', arvosana: '6' },
                  { tunniste: 'PSY_S4', arvosana: '5' },
                  { tunniste: 'PSY_S5', arvosana: 'S' },
                  { tunniste: 'PSY_S6', arvosana: '6' },
                  { tunniste: 'PSY_S7', arvosana: '5' },
                  { tunniste: 'PSY_S8', arvosana: '2' },
                  { tunniste: 'PSY_S9', arvosana: 'S' }
                ]
              }
            ]
          },
          {
            aineryhmä: 'Experimental sciences',
            oppiaineet: [
              {
                nimi: 'Biology',
                arvosana: '5',
                predictedGrade: '5',
                kurssit: [
                  { tunniste: 'BIO_H1', arvosana: '5' },
                  { tunniste: 'BIO_H2', arvosana: '4' },
                  { tunniste: 'BIO_H3', arvosana: 'S' },
                  { tunniste: 'BIO_H4', arvosana: '5' },
                  { tunniste: 'BIO_H5', arvosana: '5' },
                  { tunniste: 'BIO_H6', arvosana: '2' },
                  { tunniste: 'BIO_H7', arvosana: '3' },
                  { tunniste: 'BIO_H8', arvosana: '4' },
                  { tunniste: 'BIO_H9', arvosana: '1' }
                ]
              }
            ]
          },
          {
            aineryhmä: 'Mathematics',
            oppiaineet: [
              {
                nimi: 'Mathematical studies',
                arvosana: '5',
                predictedGrade: '5',
                kurssit: [
                  { tunniste: 'MATST_S1', arvosana: '5' },
                  { tunniste: 'MATST_S2', arvosana: '7' },
                  { tunniste: 'MATST_S3', arvosana: '6' },
                  { tunniste: 'MATST_S4', arvosana: '6' },
                  { tunniste: 'MATST_S5', arvosana: '4' },
                  { tunniste: 'MATST_S6', arvosana: 'S' }
                ]
              }
            ]
          }
        )
      })
    })

    test.describe('Tietojen muokkaaminen', () => {
      const oppijaOid = '1.2.246.562.24.00000000060'

      test.beforeEach(async ({ oppijaPage, ibOppijaPage }) => {
        await oppijaPage.goto(oppijaOid)
        await ibOppijaPage.selectSuoritus(1)
      })

      test.beforeEach(async ({ ibOppijaPage }) => {
        await ibOppijaPage.edit()
      })

      test.describe('Oppiaineiden muokkaus', () => {
        test.describe('Oppiaineen arvosana', () => {
          test('IB-tutkinnon arvosana-asteikko on oikea', async ({
            ibOppijaPage
          }) => {
            const oppiaine = ibOppijaPage.oppiaineryhmä(1).oppiaineet(0)
            expect(await oppiaine.arvosana.options()).toEqual([
              '1',
              '2',
              '3',
              '4',
              '5',
              '6',
              '7',
              'F',
              'O',
              'S'
            ])
          })

          test('IB-tutkinnon  arvosanan muuttaminen', async ({
            ibOppijaPage
          }) => {
            const oppiaine = ibOppijaPage.oppiaineryhmä(1).oppiaineet(0)
            await oppiaine.arvosana.set('arviointiasteikkoib_7')
            await ibOppijaPage.tallenna()
            await expect(oppiaine.arvosana.viewer).toHaveText('7')
          })
        })

        test.describe('Kurssien muokkaus', () => {
          test('IB-tutkinnon kurssin arvosanan muuttaminen', async ({
            ibOppijaPage
          }) => {
            const kurssi = ibOppijaPage
              .oppiaineryhmä(1)
              .oppiaineet(0)
              .kurssit(0)
            await kurssi.arvosana.set('arviointiasteikkoib_7')
            await ibOppijaPage.tallenna()
            await expect(kurssi.arvosana.viewer).toHaveText('7')
          })

          test('Kurssin lisäys ja poisto', async ({ ibOppijaPage }) => {
            const oppiaine = ibOppijaPage.oppiaineryhmä(1).oppiaineet(0)
            await oppiaine.addKurssi.button.click()

            const modal = oppiaine.modal
            await expect(modal.submit.button).toBeDisabled()

            await modal.tunniste.set('__uusi_paikallinen_ib__')
            await expect(modal.submit.button).toBeDisabled()

            await modal.paikallinenKoulutus.nimi.set(
              'Sosiaalisen median lukutaito'
            )
            await expect(modal.submit.button).toBeDisabled()

            await modal.paikallinenKoulutus.koodiarvo.set('ÄI11')
            await expect(modal.submit.button).toBeDisabled()

            await modal.paikallinenKoulutus.kuvaus.set(
              'Tutustutaan sosiaalisen median vaikutuskeinoihin'
            )
            await expect(modal.submit.button).not.toBeDisabled()

            await modal.laajuus.set('2')
            await modal.suorituskieli.setByLabel('englanti')
            await modal.submit.button.click()

            const kurssi = oppiaine.kurssit(9)
            await expect(kurssi.tunniste.button).toHaveText('ÄI11')

            await kurssi.delete.button.click()
            await expect(kurssi.tunniste.button).not.toBeAttached()
          })
        })

        test.describe('Oppiaineen lisäys', () => {
          test('Kieliaine', async ({ ibOppijaPage }) => {
            const suoritus = ibOppijaPage.$.suoritukset(1)
            await suoritus.addOppiaine.button.click()
            const modal = suoritus.modal

            await modal.tunniste.setByLabel('B-language')
            await expect(modal.submit.button).toBeDisabled()

            await modal.kieli.setByLabel('albania')
            await expect(modal.submit.button).toBeDisabled()

            await modal.taso.setByLabel('Standard level')
            await expect(modal.submit.button).toBeDisabled()

            await modal.aineryhmä.setByLabel('Language acquisition')
            await modal.submit.button.click()

            const uusiRyhmä = ibOppijaPage.oppiaineryhmä(1, 4)
            await expect(uusiRyhmä.nimi.elem).toHaveText('Language acquisition')
            const uusiOppiaine = uusiRyhmä.oppiaineet(0)
            await expect(uusiOppiaine.nimi.elem).toHaveText(
              'B-language, albania'
            )

            await uusiOppiaine.delete.button.click()
            await expect(uusiOppiaine.nimi.elem).not.toBeAttached()
          })

          test('Muu aine', async ({ ibOppijaPage }) => {
            const suoritus = ibOppijaPage.$.suoritukset(1)
            await suoritus.addOppiaine.button.click()
            const modal = suoritus.modal

            await modal.tunniste.setByLabel('Chemistry')
            await expect(modal.submit.button).toBeDisabled()

            await expect(modal.kieli.input).not.toBeAttached()

            await modal.taso.setByLabel('Standard level')
            await expect(modal.submit.button).toBeDisabled()

            await modal.aineryhmä.setByLabel('Experimental sciences')
            await modal.submit.button.click()

            const vanhaRyhmä = ibOppijaPage.oppiaineryhmä(1, 2)
            await expect(vanhaRyhmä.nimi.elem).toHaveText(
              'Experimental sciences'
            )
            const uusiOppiaine = vanhaRyhmä.oppiaineet(1)
            await expect(uusiOppiaine.nimi.elem).toHaveText('Chemistry')

            await uusiOppiaine.delete.button.click()
            await expect(uusiOppiaine.nimi.elem).not.toBeAttached()
          })
        })
      })

      test.describe('Päätason suorituksen poistaminen', () => {
        test('Päätason suorituksen voi poistaa', async({
          ibOppijaPage
        }) => {
          await ibOppijaPage.getByTestId(`suoritukset.${ibOppijaPage.suoritusIndex}.button`).click()
          await ibOppijaPage.getByTestId(`suoritukset.${ibOppijaPage.suoritusIndex}.confirm`).click()
          await ibOppijaPage.tallenna()
        })
      })
    })

    test.describe('Laajuuden yksikön vaihtuminen', () => {
      const oppijaOid = '1.2.246.562.24.00000000178'

      test.beforeEach(async ({ oppijaPage, ibOppijaPage }) => {
        await oppijaPage.goto(oppijaOid)
        await ibOppijaPage.edit()
      })

      test('Kursseja ennen 1.8.2025 alkaneelle opiskeluoikeudelle', async ({
        ibOppijaPage
      }) => {
        const kurssi = ibOppijaPage.oppiaineryhmä(0).oppiaineet(0).kurssit(0)
        await kurssi.tunniste.click()
        await expect(kurssi.modal.laajuus.unit).toHaveText('kurssia')
      })

      test('Opintopisteitä 1.8.2025 alkaneelle opiskeluoikeudelle', async ({
        ibOppijaPage,
        page
      }) => {
        await ibOppijaPage.$.opiskeluoikeus.tila.edit
          .items(0)
          .date.set('1.8.2025')
        await ibOppijaPage.tallenna()

        await page.reload()
        await ibOppijaPage.edit()

        const kurssi = ibOppijaPage.oppiaineryhmä(0).oppiaineet(0).kurssit(0)
        await kurssi.tunniste.click()
        await expect(kurssi.modal.laajuus.unit).toHaveText('op')
      })
    })

    test.describe('Suorituksen ryhmän vaihtuminen', () => {
      const oppijaOid = '1.2.246.562.24.00000000178'

      test.beforeEach(async ({ oppijaPage, ibOppijaPage }) => {
        await oppijaPage.goto(oppijaOid)
        await ibOppijaPage.edit()
      })

      test('Voi lisätä ryhmän', async ({
        ibOppijaPage,
        page
      }) => {
        await ibOppijaPage.$.suoritukset(0).ryhmä.set('foo')
        await ibOppijaPage.tallenna()
        await expect(await ibOppijaPage.$.suoritukset(0).ryhmä.value()).toEqual('foo')
      })

      test('Voi tyhjentää ryhmän', async ({
        ibOppijaPage
      }) => {
        await ibOppijaPage.$.suoritukset(0).ryhmä.set('')
        await ibOppijaPage.tallenna()
        await expect(
          await ibOppijaPage.$.suoritukset(0).ryhmä.elem.isHidden()
        ).toBeTruthy()
      })
    })

    test.describe('Suorituksen todistuksellä näkyvien lisätietojen vaihtuminen', () => {
      const oppijaOid = '1.2.246.562.24.00000000178'

      test.beforeEach(async ({ oppijaPage, ibOppijaPage }) => {
        await oppijaPage.goto(oppijaOid)
        await ibOppijaPage.edit()
      })

      test('Voi lisätä todistuksella näkyvän lisätiedon', async ({
        ibOppijaPage,
        page
      }) => {
        await ibOppijaPage.$.suoritukset(0).todistuksellaNäkyvätLisätiedot.set('foo')
        await ibOppijaPage.tallenna()
        await expect(
          await ibOppijaPage.$.suoritukset(0).todistuksellaNäkyvätLisätiedot.value()
        ).toEqual('foo')
      })

      test('Voi tyhjentää todistuksella näkyvän lisätiedon', async ({
        ibOppijaPage
      }) => {
        await ibOppijaPage.$.suoritukset(0).todistuksellaNäkyvätLisätiedot.set('')
        await ibOppijaPage.tallenna()
        await expect(
          await ibOppijaPage.$.suoritukset(0).todistuksellaNäkyvätLisätiedot.elem.isHidden()
        ).toBeTruthy()
      })
    })
  })
})
