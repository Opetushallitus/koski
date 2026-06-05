import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Testit Ahvenanmaan perusopetuksen v2-editorille.
 *
 * Åländsk Alina (150510A0873) on valmistunut Ahvenanmaan perusopetuksen
 * oppilas. Esimerkkidata noudattaa manner-Suomen mallia: päättövuoden (9. lk)
 * vuosiluokan suoritus on tyhjä ja lopulliset arvosanat ovat päättötodistuksella
 * (Avgångsbetyg). Edellinen lukuvuositodistus (8. lk) sisältää oppiaineet
 * arvosanoineen.
 *
 * Suoritukset lajitellaan: 0 = Avgångsbetyg (oppimäärä), 1 = 9. vuosiluokka
 * (tyhjä, oletustabi), 2 = 8. vuosiluokka (läsårsbetyg arvosanoineen).
 *
 * Editori on aina päällä (ei feature-flagia), joten URL:ssä ei ole
 * `perusopetus-v2`-parametria. Erot manner-Suomen perusopetukseen:
 *  - Arvosteluasteikko 4-10 / G-D-U (ahvenanmaanarviointiasteikkoyleissivistava),
 *    sekä numeeriset että sanalliset arvosanat samassa pudotusvalikossa.
 *  - Käyttäytymisen sijaan "Ansvar och samarbete" (vastuuJaYhteistyöArvio),
 *    jonka ainoa sallittu arvo on G.
 *  - Lisätiedoissa vain kotiopetusjaksot.
 */

const oppijaOid = '1.2.246.562.24.00000000190'
const url = `${oppijaOid}?opiskeluoikeudenTyyppi=ahvenanmaanperusopetus`

const editButton = 'oo.0.opiskeluoikeus.edit'
const saveButton = 'oo.0.opiskeluoikeus.save'

// 8. vuosiluokan läsårsbetyg sisältää oppiaineet arvosanoineen (ei oletustabi).
const avgångsbetygTab = 'oo.0.suoritusTabs.0.tab'
const vuosiluokkaTab = 'oo.0.suoritusTabs.2.tab'

const ensimmäisenOppiaineenArvosanaView =
  'oo.0.suoritukset.2.osasuoritukset.0.arvosana.value'
const ensimmäisenOppiaineenArvosanaEdit =
  'oo.0.suoritukset.2.osasuoritukset.0.arvosana.edit.input'

test.describe('Ahvenanmaan perusopetuksen käyttöliittymä', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Näyttää oppimäärän ja vuosiluokan suoritukset arvosanoineen', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(url)

    await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toContainText(
      'Avgångsbetyg'
    )
    await expect(page.getByTestId('oo.0.suoritusTabs.1.tab')).toContainText(
      '9. vuosiluokka'
    )
    await expect(page.getByTestId('oo.0.suoritusTabs.2.tab')).toContainText(
      '8. vuosiluokka'
    )

    // Oletustabi on 9. vuosiluokka. Sen oppiaineita ei näytetä, koska oppilas ei
    // jää luokalle: päättövuoden arvosanat ovat päättötodistuksella (kuten
    // manner-Suomessa).
    await expect(page.getByTestId('oo.0.suoritukset.1.koulutus')).toContainText(
      '9. vuosiluokka'
    )
    await expect(page.locator('.oppiaineet')).toHaveCount(0)

    // 8. luokan läsårsbetyg sen sijaan sisältää oppiaineet arvosanoineen
    // (Ruotsi = 9).
    await page.getByTestId(vuosiluokkaTab).click()
    await expect(page.locator('.oppiaineet')).toBeVisible()
    await expect(
      page.locator('.perusopetuksen-arvosteluasteikko')
    ).toContainText('G (godkänd)')
    await expect(
      page.getByTestId(ensimmäisenOppiaineenArvosanaView)
    ).toContainText('9')

    // Avgångsbetyg (oppimäärä) sisältää lopulliset arvosanat.
    await page.getByTestId(avgångsbetygTab).click()
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suoritustapa.value')
    ).toContainText('Koulutus')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.value')
    ).toContainText('9')

    // Peruste näkyy linkkinä Ahvenanmaan opsiin (laroplan.ax).
    await expect(
      page.locator('a[href="https://www.laroplan.ax/laroplan-grundskolan"]')
    ).toContainText('ÅLR2020/9841')
  })

  test('Arvosanavalikko tarjoaa sekä numeeriset (4-10) että sanalliset (G/D/U) arvosanat', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(url)
    await page.getByTestId(vuosiluokkaTab).click()
    await page.getByTestId(editButton).click()

    await page.getByTestId(ensimmäisenOppiaineenArvosanaEdit).click()
    const options = page.locator('.Select__optionLabel')
    await expect(options.filter({ hasText: /^10$/ })).toBeVisible()
    await expect(options.filter({ hasText: /^4$/ })).toBeVisible()
    await expect(options.filter({ hasText: /^G$/ })).toBeVisible()
    await expect(options.filter({ hasText: /^U$/ })).toBeVisible()
  })

  test('Sanallisen arvosanan (G) valinta ja tallennus', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(url)
    await page.getByTestId(vuosiluokkaTab).click()
    await page.getByTestId(editButton).click()

    await page.getByTestId(ensimmäisenOppiaineenArvosanaEdit).click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^G$/ })
      .first()
      .click()

    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({ timeout: 15000 })
    await page.getByTestId(vuosiluokkaTab).click()
    await expect(
      page.getByTestId(ensimmäisenOppiaineenArvosanaView)
    ).toContainText('G')
  })

  test('Vastuu ja yhteistyö (Ansvar och samarbete) -arvion lisäys ja tallennus', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(url)
    // Ansvar och samarbete annetaan lukuvuositodistuksella (8. lk), ei
    // päättötodistuksella.
    await page.getByTestId(vuosiluokkaTab).click()
    await page.getByTestId(editButton).click()

    // Lisätään arvio: ainoa sallittu arvo on G (godkänd).
    await page.getByTestId('oo.0.suoritukset.2.vastuuJaYhteistyo.lisaa').click()
    await expect(
      page.getByTestId('oo.0.suoritukset.2.vastuuJaYhteistyo.arvosana')
    ).toContainText('G')

    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({ timeout: 15000 })

    // Arvio säilyy tallennuksen ja sivun uudelleenlatauksen yli.
    await oppijaPage.goto(url)
    await page.getByTestId(vuosiluokkaTab).click()
    await expect(
      page.getByTestId('oo.0.suoritukset.2.vastuuJaYhteistyo.arvosana')
    ).toContainText('G')
  })

  test('Lisätiedot: kotiopetusjakson lisäys ja tallennus', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(url)
    await page.getByTestId(editButton).click()

    const kotiopetusRivi = page
      .locator('.EditorContainer__lisatiedot .KeyValueRow')
      .filter({ hasText: 'Kotiopetusjaksot' })
    await kotiopetusRivi
      .getByRole('button', { name: 'Lisää', exact: true })
      .click()

    await page
      .getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.kotiopetusjaksot.0.aikajakso.alku.input'
      )
      .fill('1.1.2020')
    await page
      .getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.kotiopetusjaksot.0.aikajakso.loppu.input'
      )
      .fill('31.5.2020')

    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({ timeout: 15000 })

    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.kotiopetusjaksot.0.alku')
    ).toContainText('1.1.2020')
  })

  test('Uuden vuosiluokan lisäys esitäyttää luokka-asteen oppiaineet', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(url)
    await page.getByTestId(editButton).click()

    // Esimerkkioppija on valmistunut (terminaalitila), jolloin vuosiluokkaa ei
    // voi lisätä. Poistetaan valmistunut-jakso, jolloin lisäys-välilehti tulee
    // näkyviin.
    await page
      .getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove')
      .click()

    // "lisää vuosiluokan suoritus" on viimeinen välilehti (Avgångsbetyg=0,
    // 9. vuosiluokka=1, 8. vuosiluokka=2, lisäys=3).
    await page.getByTestId('oo.0.suoritusTabs.3.tab').click()

    // Luokka-aste on oletuksena 1. vuosiluokka; täytetään luokka ja
    // alkamispäivä (toimipiste peritään viimeisimmästä suorituksesta).
    await page
      .getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.luokka.input')
      .fill('1A')
    await page
      .getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.alkamispäivä.edit.input')
      .fill('15.8.2016')
    await page.getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.submit').click()

    // Uusi 1. vuosiluokka lajitellaan viimeiseksi (indeksi 3). Oppiaineet on
    // esitäytetty 1.–2. luokan mallilla (8 ainetta), arvosanoja ei vielä ole.
    await expect(
      page.getByTestId('oo.0.suoritukset.3.osasuoritukset.0.nimi')
    ).toContainText('Ruotsi')
    await expect(
      page.locator(
        '[data-testid^="oo.0.suoritukset.3.osasuoritukset."][data-testid$=".nimi"]'
      )
    ).toHaveCount(8)
  })

  test('Tyhjän vuosiluokan muokkauksessa näytetään sekä pakolliset että valinnaiset oppiaineet', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(url)
    await page.getByTestId(editButton).click()

    // Oletustabi on tyhjä 9. vuosiluokka. Merkitään oppilas jäämään luokalle,
    // jolloin (tyhjän) vuosiluokan oppiaineet tulevat näkyviin.
    await page.getByTestId('oo.0.suoritukset.1.jääLuokalle.edit.input').click()

    // Tyhjällekin vuosiluokalle näytetään molemmat ryhmät, jotta sekä pakollisen
    // että valinnaisen oppiaineen lisäys on mahdollista (ei vain valinnaisen).
    await expect(page.getByTestId('oppiaineet-pakolliset')).toBeVisible()
    await expect(page.getByTestId('oppiaineet-valinnaiset')).toBeVisible()
  })
})
