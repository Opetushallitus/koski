import { expect, test } from './base'

const storageKey = 'koskiLocalizationEditorLanguage'

const editorLanguage = (page: import('@playwright/test').Page) =>
  page.evaluate((key) => sessionStorage.getItem(key), storageKey)

test.describe('Lokalisointien muokkauksen kielivalinta', () => {
  test.beforeEach(async ({ page }) => {
    const response = await page.request.post('/koski/user/login', {
      data: { username: 'pää', password: 'pää' }
    })
    expect(response.ok()).toBeTruthy()
  })

  test('kieliohitus on vain selaimen tilaa ja säilyy latauksessa', async ({
    page,
    context
  }) => {
    await page.goto('/koski/virkailija')
    await page.locator('.edit-localizations').click()
    const toolbar = page.locator('.localization-edit-bar.visible')
    await toolbar.locator('.languages a.sv').click()

    await expect(page.locator('.oppijataulukko-header')).toContainText(
      'Studerande'
    )
    await expect(toolbar).toBeVisible()
    expect(await editorLanguage(page)).toBe('sv')
    // Palvelin ei tiedä esikatselusta: lang-evästettä ei kirjoiteta eikä html-attribuutti muutu.
    expect(
      (await context.cookies()).find((c) => c.name === 'lang')
    ).toBeUndefined()
    await expect(page.locator('html')).toHaveAttribute('lang', 'fi')

    await page.reload()
    await expect(toolbar).toBeVisible()
    await expect(page.locator('.oppijataulukko-header')).toContainText(
      'Studerande'
    )

    await toolbar.locator('.cancel').click()
    await expect(toolbar).toHaveCount(0)
    expect(await editorLanguage(page)).toBeNull()
    await expect(page.locator('.oppijataulukko-header')).not.toContainText(
      'Studerande'
    )
  })

  test('tallennus ei päätä muokkausta eikä poista kieliohitusta', async ({
    page
  }) => {
    await page.goto('/koski/virkailija')
    await page.locator('.edit-localizations').click()
    const toolbar = page.locator('.localization-edit-bar.visible')
    await toolbar.locator('.languages a.sv').click()
    expect(await editorLanguage(page)).toBe('sv')

    let finishSave: () => void = () => {}
    const saveResponse = new Promise<void>((resolve) => {
      finishSave = resolve
    })
    await page.route('**/koski/api/localization', async (route) => {
      await saveResponse
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: '{}'
      })
    })
    await page
      .locator('.oppijataulukko-header > .localized [contenteditable]')
      .fill('Testikäännös')
    const saveRequest = page.waitForRequest(
      (request) =>
        request.url().endsWith('/koski/api/localization') &&
        request.method() === 'PUT'
    )
    const saveButton = toolbar.locator('button.koski-button')
    await saveButton.click()
    const request = await saveRequest
    expect(request.postDataJSON()).toEqual([
      expect.objectContaining({ locale: 'sv', value: 'Testikäännös' })
    ])
    // Muutoksia ei nollata ennen kuin tallennus on onnistunut.
    await expect(saveButton).toBeEnabled()

    finishSave()
    await expect(saveButton).toBeDisabled()
    await expect(toolbar).toBeVisible()
    expect(await editorLanguage(page)).toBe('sv')
  })

  test('kieliohitus poistuu käyttäjältä, jolla ei ole muokkausoikeutta', async ({
    page
  }) => {
    await page.goto('/koski/virkailija')
    await page.locator('.edit-localizations').click()
    await page.locator('.localization-edit-bar .languages a.sv').click()
    expect(await editorLanguage(page)).toBe('sv')

    // Uloskirjautuminen tehdään pyyntönä: /koski/user/logout ohjaa eteenpäin, ja page.goto
    // kilpailee seuraavan navigaation kanssa (net::ERR_ABORTED). Evästeet jaetaan contextin kanssa.
    await page.request.get('/koski/user/logout')
    const response = await page.request.post('/koski/user/login', {
      data: { username: 'kalle', password: 'kalle' }
    })
    expect(response.ok()).toBeTruthy()

    await page.goto('/koski/virkailija')
    await expect.poll(async () => await editorLanguage(page)).toBeNull()
    await expect(page.locator('.localization-edit-bar.visible')).toHaveCount(0)
  })
})
