import { expect, test } from './base'
import { virkailija } from './setup/auth'

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`
const vahvistuspäivä = '4.6.2016'

// Opiskeluoikeuden tilan ja suorituksen vahvistuksen muutostestit. Jokainen
// testi alustaa fixturet erikseen, koska ne olettavat valmis-tilan alussa
// ja tekevät tilasiirtymän omassa suorituksessaan.
test.describe('Perusopetuksen uusi käyttöliittymä: tila ja vahvistus', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Oppimäärän merkitseminen keskeneräiseksi', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Tarkista alkutila: suoritus valmis
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.status')
    ).toContainText('Suoritus valmis')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.details')
    ).toContainText('Vahvistus: 4.6.2016 Jyväskylä')

    // Siirry muokkaustilaan
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Poista valmistunut-tila
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()

    // Merkitse keskeneräiseksi
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
    ).click()

    // Tallenna
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // Suoritus on nyt kesken, vahvistustiedot eivät näy
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.status')
    ).toContainText('Suoritus kesken')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.details')
    ).not.toBeVisible()
  })

  test('Opiskeluoikeuden tilan poisto', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Tarkista alkutila: valmistunut + läsnä
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).toContainText('Valmistunut')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.tila')
    ).toContainText('Läsnä')

    // Siirry muokkaustilaan
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Poista valmistunut-tila (viimeisin, alkuperäinen indeksi 1)
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()

    // Merkitse oppimäärä keskeneräiseksi
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
    ).click()

    // Tallenna
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // Nyt vain läsnä-tila näkyy
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).toContainText('Läsnä')
  })

  test('Opiskeluoikeuden tilan lisäys', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Poista ensin valmistunut-tila ja vahvistus, jotta tilan lisäys on mahdollista
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
    ).click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // Lataa sivu uudelleen varmistaaksemme puhtaan tilan
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Lisää eronnut-tila
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.add').click()

    // Valitse eronnut-tila UusiOpiskeluoikeudenTilaModal-dialogissa
    await page.locator('label[for*="eronnut"]').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.modal.submit').click()

    // Tallenna
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // Eronnut näkyy nyt ensimmäisenä (uusin tila)
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).toContainText('Eronnut')
  })

  test('Suorituksen merkitseminen valmiiksi -dialogi', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Ensin merkitään oppimäärä keskeneräiseksi
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
    ).click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // Varmista, että suoritus on kesken
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.status')
    ).toContainText('Suoritus kesken')

    // Siirry muokkaustilaan ja merkitse valmiiksi
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // "Merkitse valmiiksi" -painike on näkyvissä
    const merkitseValmiiksiBtn = page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseValmiiksi'
    )
    await expect(merkitseValmiiksiBtn).toBeVisible()
    await merkitseValmiiksiBtn.click()

    // Dialogi aukeaa
    const modal = page.locator('.Modal')
    await expect(modal).toBeVisible()

    // Päivämäärä on esitäytetty
    const dateInput = modal.locator('[data-testid$="date.edit.input"]')
    await expect(dateInput).toBeVisible()
    const dateValue = await dateInput.inputValue()
    expect(dateValue).toBeTruthy()
    await dateInput.fill(vahvistuspäivä)

    // Paikkakunta on esitäytetty organisaation kotipaikalla ("Jyväskylä")
    await expect(
      modal.locator('[data-testid$="kunta.edit.input"]')
    ).toHaveValue('Jyväskylä')

    // Lisää myöntäjähenkilö: klikkaa "Lisää henkilö" -valintaa
    const addHenkilöSelect = modal.locator(
      '[data-testid$="organisaatiohenkilöt.edit.add.input"]'
    )
    await addHenkilöSelect.click()
    // Valitse "Lisää henkilö"
    await modal.locator('.Select__optionLabel').filter({ hasText: 'Lisää henkilö' }).click()

    // Syötä nimi ja titteli uudelle henkilölle
    const nimiInput = modal.locator(
      '[data-testid$="newHenkilö.nimi.input"]'
    )
    await nimiInput.fill('Reijo Reksi')

    const titteliInput = modal.locator(
      '[data-testid$="newHenkilö.titteli.input"]'
    )
    await titteliInput.fill('rehtori')

    // Klikkaa "Merkitse valmiiksi" -painiketta dialogissa
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.modal.submit'
    ).click()

    // Tallenna
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // Tarkista vahvistustiedot
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.status')
    ).toContainText('Suoritus valmis')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.details')
    ).toContainText(`${vahvistuspäivä} Jyväskylä`)
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.value.henkilö.0'
      )
    ).toContainText('Reijo Reksi (rehtori)')
  })

  test('Vuosiluokan merkitseminen valmiiksi -dialogissa näytetään seuraavalle luokalle siirtämisen valinta', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()
    await page.getByTestId(
      'oo.0.suoritukset.2.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
    ).click()

    const merkitseValmiiksiBtn = page.getByTestId(
      'oo.0.suoritukset.2.suorituksenVahvistus.edit.merkitseValmiiksi'
    )
    await expect(merkitseValmiiksiBtn).toBeVisible()
    await merkitseValmiiksiBtn.click()

    const siirretäänSeuraavalleLuokalle = page.getByTestId(
      'oo.0.suoritukset.2.suorituksenVahvistus.edit.modal.luokalleSiirtyminen'
    )
    await expect(siirretäänSeuraavalleLuokalle).toContainText(
      'Siirretään seuraavalle luokalle'
    )
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.2.suorituksenVahvistus.edit.modal.luokalleSiirtyminen.input'
      )
    ).toBeChecked()
  })

  test('Merkitse valmiiksi: peruuta-painike sulkee dialogin', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Merkitään oppimäärä keskeneräiseksi ensin
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
    ).click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // Avaa dialogi
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseValmiiksi'
    ).click()

    const modal = page.locator('.Modal')
    await expect(modal).toBeVisible()

    // Peruuta-painike sulkee dialogin
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.modal.cancel'
    ).click()

    // Dialogi on suljettu, suoritus on edelleen kesken
    await expect(modal).not.toBeVisible()
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseValmiiksi'
      )
    ).toBeVisible()
  })

  test('Merkitse valmiiksi -nappi on disabloitu ilman myöntäjää', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Merkitään oppimäärä keskeneräiseksi ensin
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
    ).click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // Avaa dialogi
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseValmiiksi'
    ).click()

    const modal = page.locator('.Modal')
    await expect(modal).toBeVisible()

    // Submit-painike on disabloitu kun myöntäjää ei ole lisätty
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.edit.modal.submit'
      )
    ).toBeDisabled()

    // Peruuta
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.modal.cancel'
    ).click()
  })

  test('Merkitse valmiiksi: tallennettu myöntäjä löytyy listalta', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // 1. Merkitään oppimäärä keskeneräiseksi
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
    ).click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // 2. Merkitään valmiiksi uudella myöntäjällä (tallentaa Testi Testaajan preferensseihin)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseValmiiksi'
    ).click()

    const modal = page.locator('.Modal')
    await expect(modal).toBeVisible()
    await modal
      .locator('[data-testid$="date.edit.input"]')
      .fill(vahvistuspäivä)

    // Lisää uusi myöntäjä
    await modal.locator('[data-testid$="organisaatiohenkilöt.edit.add.input"]').click()
    await modal.locator('.Select__optionLabel').filter({ hasText: 'Lisää henkilö' }).click()
    await modal.locator('[data-testid$="newHenkilö.nimi.input"]').fill('Testi Testaaja')
    await modal.locator('[data-testid$="newHenkilö.titteli.input"]').fill('rehtori')

    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.modal.submit'
    ).click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // 3. Merkitään keskeneräiseksi uudelleen
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
    ).click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // 4. Avaa merkitse valmiiksi -dialogi uudelleen
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseValmiiksi'
    ).click()
    await expect(modal).toBeVisible()
    await modal
      .locator('[data-testid$="date.edit.input"]')
      .fill(vahvistuspäivä)

    // Klikkaa myöntäjä-dropdownia
    await modal.locator('[data-testid$="organisaatiohenkilöt.edit.add.input"]').click()

    // "Testi Testaaja" pitäisi löytyä tallennetuista myöntäjistä
    await expect(
      modal.locator('.Select__optionLabel').filter({ hasText: 'Testi Testaaja' })
    ).toBeVisible()

    // Valitse tallennettu myöntäjä
    await modal.locator('.Select__optionLabel').filter({ hasText: 'Testi Testaaja' }).click()

    // Submit ja tallenna
    await page.getByTestId(
      'oo.0.suoritukset.0.suorituksenVahvistus.edit.modal.submit'
    ).click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible({ timeout: 15000 })

    // Vahvistus näkyy Testi Testaajan nimellä
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.value.henkilö.0'
      )
    ).toContainText('Testi Testaaja')
  })

  test('Merkitse valmiiksi: uuden myöntäjän voi lisätä tallennetun myöntäjän nimellä', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    const vahvistus = 'oo.0.suoritukset.0.suorituksenVahvistus'
    const modal = page.locator('.Modal')
    const nimiInput = modal.locator('[data-testid$="newHenkilö.nimi.input"]')
    const titteliInput = modal.locator(
      '[data-testid$="newHenkilö.titteli.input"]'
    )

    const avaaVahvistusDialogi = async () => {
      await page
        .getByTestId(`${vahvistus}.edit.merkitseKeskeneräiseksi`)
        .click()
      await page.getByTestId(`${vahvistus}.edit.merkitseValmiiksi`).click()
      await expect(modal).toBeVisible()
      await modal
        .locator('[data-testid$="organisaatiohenkilöt.edit.add.input"]')
        .click()
    }

    const lisääUusiMyöntäjä = async (nimi: string, titteli: string) => {
      await modal
        .locator('.Select__optionLabel')
        .filter({ hasText: 'Lisää henkilö' })
        .click()
      await nimiInput.fill(nimi)
      // Nimen kirjoittaminen ei saa vaihtaa riviä tallennetun myöntäjän valintalistaksi
      await expect(nimiInput).toHaveValue(nimi)
      await titteliInput.fill(titteli)
      await page.getByTestId(`${vahvistus}.edit.modal.submit`).click()
      await expect(modal).not.toBeVisible()
    }

    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()

    // Tallentaa Maija Myöntäjän preferensseihin
    await avaaVahvistusDialogi()
    await lisääUusiMyöntäjä('Maija Myöntäjä', 'rehtori')

    await avaaVahvistusDialogi()
    await expect(
      modal
        .locator('.Select__optionLabel')
        .filter({ hasText: 'Maija Myöntäjä (rehtori)' })
    ).toBeVisible()
    await lisääUusiMyöntäjä('Maija Myöntäjä', 'apulaisrehtori')

    await expect(page.getByTestId(`${vahvistus}.edit.henkilö.0`)).toHaveText(
      'Maija Myöntäjä (apulaisrehtori)'
    )
  })

  test('Merkitse valmiiksi: tallennetun myöntäjän voi poistaa, vaikka nimessä on /-merkki', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    const nimi = 'Maija Myöntäjä/sijainen'
    // Kaisan suorituksen toimipiste ja opiskeluoikeuden koulutustoimija
    const myöntäjät =
      '/koski/api/preferences/1.2.246.562.10.14613773812/myöntäjät?koulutustoimijaOid=1.2.246.562.10.77055527103'
    const tallennetut = async () => {
      const response = await page.request.get(myöntäjät)
      return ((await response.json()) as Array<{ nimi: string }>).map(
        (h) => h.nimi
      )
    }
    const vahvistus = 'oo.0.suoritukset.0.suorituksenVahvistus'
    const modal = page.locator('.Modal')
    const lisääInput = modal.locator(
      '[data-testid$="organisaatiohenkilöt.edit.add.input"]'
    )
    const myöntäjäOptio = modal
      .locator('li.Select__option')
      .filter({ hasText: `${nimi} (rehtori)` })

    await fixtures.reset()
    const tallennus = await page.request.put(myöntäjät, {
      data: {
        key: nimi,
        value: {
          nimi,
          titteli: { fi: 'rehtori' },
          organisaatio: { oid: '1.2.246.562.10.14613773812' }
        }
      }
    })
    expect(tallennus.ok()).toBeTruthy()

    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()
    await page.getByTestId(`${vahvistus}.edit.merkitseKeskeneräiseksi`).click()
    await page.getByTestId(`${vahvistus}.edit.merkitseValmiiksi`).click()
    await lisääInput.click()
    await myöntäjäOptio.locator('[data-testid$=".delete"]').click()

    await expect.poll(tallennetut).not.toContain(nimi)

    await lisääInput.click()
    await expect(
      modal.locator('.Select__optionLabel').filter({ hasText: 'Lisää henkilö' })
    ).toBeVisible()
    await expect(myöntäjäOptio).toHaveCount(0)
  })
})
