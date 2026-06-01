import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Porttaa perusopetusSpec_2.js "Tietojen muuttaminen" -osan pienempiä
 * oppiaine- ja arviointikohtaisia testejä v2-editorille.
 *
 * Kaisan (220109-784L) päättötodistus on tab 0 ja sisältää kaikki oppiaineet
 * (pakolliset + valinnaiset). 8. vuosiluokka on tab 2, sisältää myös kaikki
 * oppiaineet. 7. vuosiluokka on tab 3 ja sisältää vain pakolliset oppiaineet,
 * koska luokkaa on jääty.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: oppiainemuutokset', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Pakollisen oppiaineen arvosana S tuo näkyviin sanallisen arvioinnin kentän', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // 8. vuosiluokka on tab 2
    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Äidinkieli (osasuoritukset.0) on pakollinen. Vaihda arvosanaksi S.
    await page
      .getByTestId('oo.0.suoritukset.2.osasuoritukset.0.arvosana.edit.input')
      .click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^S$/ })
      .first()
      .click()

    // Sanallinen arviointi -kenttä näkyy nyt myös pakolliselle oppiaineelle
    const sanallinen = page.getByTestId(
      'oo.0.suoritukset.2.osasuoritukset.0.sanallinenArviointi.edit.input'
    )
    await expect(sanallinen).toBeVisible()
    await sanallinen.fill('Hienoa työtä')

    // Tallenna
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // View-modessa sanallinen näkyy
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.2.osasuoritukset.0.sanallinenArviointi.value'
      )
    ).toContainText('Hienoa työtä')
    await expect(
      page.getByTestId('oo.0.suoritukset.2.osasuoritukset.0.arvosana.value')
    ).toContainText('S')
  })

  test('Sanallisen arvioinnin piilotus kun arvosana vaihdetaan numeroon (pakollinen)', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // Luo tila: äidinkielelle S + sanallinen arviointi
    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .getByTestId('oo.0.suoritukset.2.osasuoritukset.0.arvosana.edit.input')
      .click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^S$/ })
      .first()
      .click()
    await page
      .getByTestId(
        'oo.0.suoritukset.2.osasuoritukset.0.sanallinenArviointi.edit.input'
      )
      .fill('Kuvaus')
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Vaihda numeeriseen arvosanaan (8)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .getByTestId('oo.0.suoritukset.2.osasuoritukset.0.arvosana.edit.input')
      .click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^8$/ })
      .first()
      .click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Sanallinen arviointi ei enää näy
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.2.osasuoritukset.0.sanallinenArviointi.value'
      )
    ).not.toBeVisible()
    await expect(
      page.getByTestId('oo.0.suoritukset.2.osasuoritukset.0.arvosana.value')
    ).toContainText('8')
  })

  test('Sanallisen arvioinnin lisäys valinnaiselle oppiaineelle', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Päättötodistus on tab 0. Siirry muokkaustilaan.
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Varmista että kyseinen rivi on Liikunta (valinnainen, indeksi 20)
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.20.nimi')
    ).toContainText('Liikunta')

    // Sanallinen arviointi -kenttä näkyy (arviointi on jo S)
    const sanallinen = page.getByTestId(
      'oo.0.suoritukset.0.osasuoritukset.20.sanallinenArviointi.edit.input'
    )
    await expect(sanallinen).toBeVisible()

    // Syötä kuvaus
    await sanallinen.fill('Hienoa työtä')

    // Tallenna
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Sanallinen arviointi näkyy view-modessa
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.osasuoritukset.20.sanallinenArviointi.value'
      )
    ).toContainText('Hienoa työtä')
  })

  test('Arvosanoja ei näytetä kun päättötodistus on kesken', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Alkutila: päättötodistus valmis, äidinkielen arvosana näkyy
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.value')
    ).toContainText('9')

    // Merkitse päättötodistus keskeneräiseksi: poista valmistunut-tila ja
    // poista vahvistus
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()
    await page
      .getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
      )
      .click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Arvosanat eivät enää näy: kaikki .arvosana.value -elementit piilotettu
    // oppimäärän tabilla
    await expect(
      page.locator(
        '[data-testid^="oo.0.suoritukset.0.osasuoritukset."][data-testid$=".arvosana.value"]'
      )
    ).toHaveCount(0)

    // Vuosiluokkien suoritukset näyttävät edelleen arvosanat (8. vuosiluokka,
    // tab 2). Varmistaa, ettei piilotussääntö vaikuta vuosiluokkiin.
    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()
    await expect(
      page.getByTestId('oo.0.suoritukset.2.osasuoritukset.0.arvosana.value')
    ).toContainText('9')
  })

  test('Arvosanat näytetään kun vahvistus on tulevaisuudessa', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(90000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // 1. Merkitse päättötodistus keskeneräiseksi: arvosanat piilottuvat
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()
    await page
      .getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
      )
      .click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // 2. Merkitse valmiiksi tulevaisuuden vahvistuspäivällä
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseValmiiksi'
      )
      .click()

    const modal = page.locator('.Modal')
    await expect(modal).toBeVisible()

    // Aseta päivä tulevaisuuteen
    const dateInput = modal.locator('[data-testid$="date.edit.input"]')
    await dateInput.clear()
    await dateInput.fill('11.4.2117')

    // Lisää myöntäjähenkilö
    await modal
      .locator('[data-testid$="organisaatiohenkilöt.edit.add.input"]')
      .click()
    await modal
      .locator('.Select__optionLabel')
      .filter({ hasText: 'Lisää henkilö' })
      .click()
    await modal
      .locator('[data-testid$="newHenkilö.nimi.input"]')
      .fill('Reijo Reksi')
    await modal
      .locator('[data-testid$="newHenkilö.titteli.input"]')
      .fill('rehtori')

    await page
      .getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.edit.modal.submit'
      )
      .click()

    // Poista Biologia (indeksi 11): sen yksilöllistetty oppimäärä ei ole
    // sallittu vahvistuksen jälkeen 31.8.2026. Muuten tallennus hylätään.
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.11.delete').click()

    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // 3. Arvosanat näytetään vaikka vahvistus on tulevaisuudessa
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.value')
    ).toContainText('9')
  })

  test('Käyttäytymisen arvosanan muutos 8. vuosiluokalla', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // 8. vuosiluokka on tab 2. Käyttäytymisen arviointi on oletuksena S.
    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()
    await expect(
      page.getByTestId('oo.0.suoritukset.2.kayttaytyminen.arvosana')
    ).toContainText('S')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Vaihda käyttäytymisen arvosana 10:ksi (koodiston nimi "erinomainen")
    await page
      .getByTestId('oo.0.suoritukset.2.kayttaytyminen.kayttaytyminen.input')
      .click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^erinomainen$/ })
      .first()
      .click()

    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Muutettu arvosana näkyy view-modessa
    await expect(
      page.getByTestId('oo.0.suoritukset.2.kayttaytyminen.arvosana')
    ).toContainText('10')
  })

  test('Käyttäytymisen sanallisen kuvauksen lisäys', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // 8. vuosiluokka on tab 2.
    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Syötä sanallinen kuvaus
    const kuvausInput = page.getByTestId(
      'oo.0.suoritukset.2.kayttaytyminen.kuvaus.edit.input'
    )
    await expect(kuvausInput).toBeVisible()
    await kuvausInput.fill('Hyvää käytöstä')

    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Kuvaus näkyy view-modessa
    await expect(
      page.getByTestId('oo.0.suoritukset.2.kayttaytyminen.kuvaus.value')
    ).toContainText('Hyvää käytöstä')
  })

  test('Valinnaisen oppiaineen laajuuden muutos päättötodistuksella', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Päättötodistus on tab 0 (oletus). Siirry muokkaustilaan.
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Valinnainen Liikunta on osasuoritukset-taulukon indeksissä 20
    // (ks. PerusopetusExampleData.oppiaineSuoritukset).
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.20.nimi')
    ).toContainText('Liikunta')

    const laajuusInput = page.getByTestId(
      'oo.0.suoritukset.0.osasuoritukset.20.laajuus.input'
    )
    await laajuusInput.clear()
    await laajuusInput.fill('1.5')

    // Tallenna
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Laajuus näkyy view-modessa (v2 käyttää pistettä desimaalierottimena)
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.20.laajuus.value')
    ).toContainText('1.5')
  })
})
