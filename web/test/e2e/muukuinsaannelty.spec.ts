import { test, expect } from './base'
import { virkailija } from './setup/auth'

test.describe('Muu kuin säännelty koulutus', () => {
  test.use({ storageState: virkailija('muks') })
  test.beforeEach(async ({ fixtures, page }) => {
    page.once('dialog', (dialog) => {
      dialog.accept()
    })
    await fixtures.reset()
  })

  test.describe('Uuden opiskeluoikeuden luonti', () => {
    test.beforeEach(async ({ uusiOppijaPage }) => {
      await uusiOppijaPage.goTo('260200A256M')
      await uusiOppijaPage.controls.opiskeluoikeus.setByLabel(
        'Muu kuin säännelty koulutus'
      )
    })

    test('Lisäys ei onnistu ilman valittua opiskeluoikeutta', async ({
      uusiOppijaPage
    }) => {
      await expect(uusiOppijaPage.controls.submit.button).toBeDisabled()
    })

    test('Vain oikeat opiskeluoikeuden tilat valittavissa', async ({
      uusiOppijaPage
    }) => {
      await expect(await uusiOppijaPage.controls.tila.options()).toEqual([
        'Hyväksytysti suoritettu',
        'Keskeytynyt',
        'Läsnä'
      ])
    })

    test('Vain jotpa-rahoitukset valittavissa', async ({ uusiOppijaPage }) => {
      await expect(
        await uusiOppijaPage.controls.opintojenRahoitus.options()
      ).toEqual([
        'Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus',
        'Jatkuvan oppimisen ja työllisyyden palvelukeskuksen rahoitus (RRF)'
      ])
    })

    test('Opiskeluoikeuden luonti onnistuu', async ({ uusiOppijaPage }) => {
      await uusiOppijaPage.fill({
        etunimet: 'Jonna',
        sukunimi: 'Muksunen',
        opintokokonaisuus: '2319 21VF2 Valaisu',
        jotpaAsianumero:
          '01/5848/2023 - TTS Kehitys Oy - Pientalojen kestävien energiaratkaisujen suunnitteluosaaminen'
      })
      await uusiOppijaPage.submitAndExpectSuccess()
    })
  })

  test.describe('Opiskeluoikeuden muokkaus', () => {
    test.beforeEach(async ({ virkailijaPage, oppijaHaku }) => {
      await virkailijaPage.goto()
      const hakutulokset = await oppijaHaku.search('200600A515B')
      await hakutulokset.clickOnFirst()
    })

    test('Näyttää oppijan tiedot oikein', async ({ muksOppijaPage }) => {
      await expect(muksOppijaPage.oppijaHeading).toContainText(
        'Jotpanen, Muksu (200600A515B)'
      )
      await expect(muksOppijaPage.hetu).toContainText('200600A515B')
      await expect(muksOppijaPage.koulutusmoduuli).toContainText(
        'Muu kuin säännelty koulutus'
      )
      await expect(muksOppijaPage.opintokokonaisuus).toContainText(
        'Kuvallisen ilmaisun perusteet ja välineet'
      )
    })

    test('Uuden osasuorituksen lisääminen', async ({ muksOppijaPage }) => {
      await muksOppijaPage.avaaMuokkausnäkymä()

      const osasuoritus = 'Maalausvälineet'
      await muksOppijaPage.lisääUusiOsasuoritus(0, osasuoritus)
      await muksOppijaPage.setOsasuorituksenLaajuus(0, 6)
      await muksOppijaPage.tallenna()
    })

    test('Osasuorituksen lisääminen ei onnistu ilman laajuutta', async ({
      muksOppijaPage
    }) => {
      await muksOppijaPage.avaaMuokkausnäkymä()

      const osasuoritus = 'Laajuus puuttuu'
      await muksOppijaPage.lisääUusiOsasuoritus(0, osasuoritus)

      await expect(muksOppijaPage.tallennusBtn).toBeDisabled()
      await muksOppijaPage.peruuta()
    })

    test('Alaosasuorituksen lisääminen onnistuu', async ({
      muksOppijaPage
    }) => {
      await muksOppijaPage.avaaMuokkausnäkymä()

      await muksOppijaPage.lisääUusiOsasuoritus(0, 'Hedelmäasetelmat')
      await muksOppijaPage.setOsasuorituksenLaajuus(0, 6)

      await muksOppijaPage.lisääUusiOsasuoritus(0, 'Fotorealistiset omenat')
      await muksOppijaPage.setOsasuorituksenLaajuus(1, 6)

      await muksOppijaPage.tallenna()
    })

    test('Tallennattessa virhe, jos alaosasuoritusten yhteislaajuus ei vastaa osasuorituksen laajuutta', async ({
      virkailijaPage,
      muksOppijaPage
    }) => {
      await muksOppijaPage.avaaMuokkausnäkymä()

      await muksOppijaPage.lisääUusiOsasuoritus(0, 'Hedelmäasetelmat')
      await muksOppijaPage.setOsasuorituksenLaajuus(0, 6)

      await muksOppijaPage.lisääUusiOsasuoritus(0, 'Fotorealistiset omenat')
      await muksOppijaPage.setOsasuorituksenLaajuus(1, 3)

      await muksOppijaPage.tallenna()
      expect(await virkailijaPage.virheilmoitus()).toEqual(
        'Suorituksen Hedelmäasetelmat (Hedelmäasetelmat) osasuoritusten laajuuksien summa 3.0 ei vastaa suorituksen laajuutta 6.0'
      )
    })

    test('Sama osasuoritus ei voi olla useampaa kertaa', async ({
      virkailijaPage,
      muksOppijaPage
    }) => {
      await muksOppijaPage.avaaMuokkausnäkymä()

      const nimi = 'Maalaaminen'
      await muksOppijaPage.lisääUusiOsasuoritus(0, nimi)
      await muksOppijaPage.setOsasuorituksenLaajuus(0, 6)

      await muksOppijaPage.tallenna()

      await muksOppijaPage.avaaMuokkausnäkymä()
      await muksOppijaPage.lisääTallennettuOsasuoritus(0, nimi)
      await muksOppijaPage.setOsasuorituksenLaajuus(1, 6)

      await muksOppijaPage.tallenna()
      expect(await virkailijaPage.virheilmoitus()).toEqual(
        `Osasuoritus ${nimi} (${nimi}) esiintyy useammin kuin kerran`
      )
    })

    test('Opiskeluoikeuden tilan lisäämisessä näytetään oikeat tilavaihtoehdot', async ({
      muksOppijaPage
    }) => {
      await muksOppijaPage.avaaMuokkausnäkymä()

      await muksOppijaPage.opiskeluoikeudenTila.avaa()
      await expect(
        muksOppijaPage.opiskeluoikeudenTila.valittavatTilat
      ).toHaveText(['Hyväksytysti suoritettu', 'Keskeytynyt', 'Läsnä'])
    })

    test('Suoritusta ei voi merkitä valmiiksi ilman ainakin yhtä osasuoritusta', async ({
      virkailijaPage,
      muksOppijaPage
    }) => {
      await muksOppijaPage.avaaMuokkausnäkymä()
      await muksOppijaPage.merkitseSuoritusValmiiksi()
      await muksOppijaPage.tallenna()

      expect(await virkailijaPage.virheilmoitus()).toEqual(
        'Suoritus koulutus/999951 on merkitty valmiiksi, mutta sillä on tyhjä osasuorituslista tai opiskeluoikeudelta puuttuu linkitys'
      )
    })
  })
})
