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
        opintokokonaisuus: '13435 4H Företagare',
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

      const osasuorituksenNimi = 'Maalausvälineet'
      const osasuoritus =
        await muksOppijaPage.lisääOsasuoritus(osasuorituksenNimi)
      await osasuoritus.syötäLaajuus(6)
      await osasuoritus.valitseArvosana('5')
      await osasuoritus.syötäArviointipäivä('1.2.2027')
      await muksOppijaPage.tallenna()
    })

    test('Osasuorituksen lisääminen ei onnistu ilman laajuutta', async ({
      muksOppijaPage
    }) => {
      await muksOppijaPage.avaaMuokkausnäkymä()

      const osasuorituksenNimi = 'Laajuus puuttuu'
      const osasuoritus =
        await muksOppijaPage.lisääOsasuoritus(osasuorituksenNimi)

      await osasuoritus.syötäLaajuus(6)
      await expect(muksOppijaPage.tallennusBtn).toBeEnabled()
      await osasuoritus.syötäLaajuus('')
      await expect(muksOppijaPage.tallennusBtn).toBeDisabled()
      await muksOppijaPage.peruuta()
    })

    test('Osasuoritustietojen lisääminen ja muokkaaminen kaikilla osasuoritustasoilla', async ({
      muksOppijaPage
    }) => {
      await muksOppijaPage.avaaMuokkausnäkymä()

      const osasuorituksenNimi = 'Hedelmäasetelmat'
      const alaosasuorituksenNimi = 'Fotorealistiset omenat'
      const osasuoritus =
        await muksOppijaPage.lisääOsasuoritus(osasuorituksenNimi)
      await osasuoritus.syötäLaajuus(6)
      await osasuoritus.valitseArvosana('5')
      await expect(osasuoritus.arviointipäiväInput).toHaveValue('')
      await osasuoritus.syötäArviointipäivä('1.2.2027')

      const alaosasuoritus = await osasuoritus.lisääAlaosasuoritus(
        alaosasuorituksenNimi
      )
      await alaosasuoritus.avaa()
      await alaosasuoritus.syötäLaajuus(6)
      await alaosasuoritus.valitseArvosana('4')
      await expect(alaosasuoritus.arviointipäiväInput).toHaveValue('')
      await alaosasuoritus.syötäArviointipäivä('2.2.2027')

      await muksOppijaPage.tallenna()
      await osasuoritus.avaa()
      await alaosasuoritus.avaa()
      await expect(osasuoritus.laajuus).toHaveText('6 tuntia')
      await expect(osasuoritus.arvosana).toHaveText('5')
      await expect(osasuoritus.arviointipäivä).toHaveText('1.2.2027')
      await expect(alaosasuoritus.laajuus).toHaveText('6 tuntia')
      await expect(alaosasuoritus.arvosana).toHaveText('4')
      await expect(alaosasuoritus.arviointipäivä).toHaveText('2.2.2027')

      await muksOppijaPage.avaaMuokkausnäkymä()
      await osasuoritus.avaa()
      await osasuoritus.syötäLaajuus(7)
      await osasuoritus.valitseArvosana('3')
      await osasuoritus.syötäArviointipäivä('3.3.2027')

      await alaosasuoritus.avaa()
      await alaosasuoritus.syötäLaajuus(7)
      await alaosasuoritus.valitseArvosana('2')
      await alaosasuoritus.syötäArviointipäivä('4.4.2027')
      await muksOppijaPage.tallenna()

      await osasuoritus.avaa()
      await alaosasuoritus.avaa()
      await expect(osasuoritus.laajuus).toHaveText('7 tuntia')
      await expect(osasuoritus.arvosana).toHaveText('3')
      await expect(osasuoritus.arviointipäivä).toHaveText('3.3.2027')
      await expect(alaosasuoritus.laajuus).toHaveText('7 tuntia')
      await expect(alaosasuoritus.arvosana).toHaveText('2')
      await expect(alaosasuoritus.arviointipäivä).toHaveText('4.4.2027')
    })

    test('Tallennettaessa virhe, jos alaosasuoritusten yhteislaajuus ei vastaa osasuorituksen laajuutta', async ({
      virkailijaPage,
      muksOppijaPage
    }) => {
      await muksOppijaPage.avaaMuokkausnäkymä()

      const osasuorituksenNimi = 'Hedelmäasetelmat'
      const osasuoritus =
        await muksOppijaPage.lisääOsasuoritus(osasuorituksenNimi)
      await osasuoritus.syötäLaajuus(6)

      const alaosasuorituksenNimi = 'Fotorealistiset omenat'
      const alaosasuoritus = await osasuoritus.lisääAlaosasuoritus(
        alaosasuorituksenNimi
      )
      await alaosasuoritus.syötäLaajuus(3)

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

      const osasuorituksenNimi = 'Maalaaminen'
      const osasuoritus =
        await muksOppijaPage.lisääOsasuoritus(osasuorituksenNimi)
      await osasuoritus.syötäLaajuus(6)

      await muksOppijaPage.tallenna()

      await muksOppijaPage.avaaMuokkausnäkymä()
      const toinenOsasuoritus =
        await muksOppijaPage.lisääOsasuoritus(osasuorituksenNimi)
      await toinenOsasuoritus.syötäLaajuus(6)

      await muksOppijaPage.tallenna()
      expect(await virkailijaPage.virheilmoitus()).toEqual(
        `Osasuoritus ${osasuorituksenNimi} (${osasuorituksenNimi}) esiintyy useammin kuin kerran`
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
