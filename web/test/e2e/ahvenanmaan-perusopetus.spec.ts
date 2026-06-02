import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Testit Ahvenanmaan perusopetuksen v2-editorille.
 *
 * Åländsk Alina (150510A0873) on Ahvenanmaan perusopetuksen oppilas, jolla on
 * vahvistettu 9. vuosiluokan suoritus ja oppimäärän suoritus (Avgångsbetyg).
 * Tabit on lajiteltu: 0 = Avgångsbetyg (oppimäärä), 1 = 9. vuosiluokka.
 *
 * Editori on aina päällä (ei feature-flagia), joten URL:ssä ei ole
 * `perusopetus-v2`-parametria. Erot manner-Suomen perusopetukseen:
 *  - Arvosteluasteikko 4-10 / G-D-U (ahvenanmaanarviointiasteikkoyleissivistava),
 *    sekä numeeriset että sanalliset arvosanat samassa pudotusvalikossa.
 *  - 9. luokan oppiaineita ei piiloteta (toisin kuin manner-Suomessa).
 *  - Käyttäytymisen sijaan "Ansvar och samarbete" (vastuuJaYhteistyöArvio),
 *    jonka ainoa sallittu arvo on G.
 *  - Lisätiedoissa vain kotiopetusjaksot.
 */

const oppijaOid = '1.2.246.562.24.00000000190'
const url = `${oppijaOid}?opiskeluoikeudenTyyppi=ahvenanmaanperusopetus`

const editButton = 'oo.0.opiskeluoikeus.edit'
const saveButton = 'oo.0.opiskeluoikeus.save'

const ensimmäisenOppiaineenArvosanaView =
  'oo.0.suoritukset.1.osasuoritukset.0.arvosana.value'
const ensimmäisenOppiaineenArvosanaEdit =
  'oo.0.suoritukset.1.osasuoritukset.0.arvosana.edit.input'

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

    // Oletustabi on 9. vuosiluokka, jonka oppiaineet näkyvät (9. luokkaa ei
    // piiloteta kuten manner-Suomessa).
    await expect(page.getByTestId('oo.0.suoritukset.1.koulutus')).toContainText(
      '9. vuosiluokka'
    )
    await expect(page.locator('.oppiaineet')).toBeVisible()

    // Ahvenanmaan arvosteluasteikon ohjeteksti.
    await expect(
      page.locator('.perusopetuksen-arvosteluasteikko')
    ).toContainText('G (godkänd)')

    // Numeerinen arvosana näkyy (Ruotsi = 9).
    await expect(
      page.getByTestId(ensimmäisenOppiaineenArvosanaView)
    ).toContainText('9')

    // Oppimäärän (Avgångsbetyg) tabilla suoritustapa ja arvosanat.
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suoritustapa.value')
    ).toContainText('Koulutus')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.value')
    ).toContainText('9')
  })

  test('Arvosanavalikko tarjoaa sekä numeeriset (4-10) että sanalliset (G/D/U) arvosanat', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(url)
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
    await page.getByTestId(editButton).click()

    await page.getByTestId(ensimmäisenOppiaineenArvosanaEdit).click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^G$/ })
      .first()
      .click()

    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({ timeout: 15000 })
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
    await page.getByTestId(editButton).click()

    // Lisätään arvio: ainoa sallittu arvo on G (godkänd).
    await page.getByTestId('oo.0.suoritukset.1.vastuuJaYhteistyo.lisaa').click()
    await expect(
      page.getByTestId('oo.0.suoritukset.1.vastuuJaYhteistyo.arvosana')
    ).toContainText('G')

    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({ timeout: 15000 })

    // Arvio säilyy tallennuksen ja sivun uudelleenlatauksen yli.
    await oppijaPage.goto(url)
    await expect(
      page.getByTestId('oo.0.suoritukset.1.vastuuJaYhteistyo.arvosana')
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
})
