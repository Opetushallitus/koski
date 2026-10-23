import { expect, test } from './base'

test.describe('Valppaan käyttämä kansalaisen paikallinen kirjautuminen', () => {
  test('ohjaa redirect-parametrin osoitteeseen Valppaan hyväksymällä istunnolla', async ({
    page,
    baseURL,
    kansalainenLoginPage
  }) => {
    // Paluuosoitteena käytetään Valppaan GET-rajapintaa, jotta uudelleenohjaus
    // voidaan testata lataamatta Valppaan käyttöliittymää. Testi tarvitsee vain Valppaan backendin.
    const returnUrl = new URL(
      '/koski/valpas/api/kansalainen/user?date=2021-09-05',
      baseURL
    ).href

    await page.goto(
      `/koski/login/oppija/local?redirect=${encodeURIComponent(returnUrl)}`
    )
    await kansalainenLoginPage.hetuInput.pressSequentially('030300-5215')
    await kansalainenLoginPage.loginButton.click()

    await expect(page).toHaveURL(returnUrl)

    const user = await page.request.get('/koski/valpas/api/kansalainen/user')
    expect(await user.json()).toMatchObject({
      oid: '1.2.246.562.24.00000000078',
      name: 'Faija EiOpintojaKoskessa',
      kansalainen: true,
      huollettavat: {
        huollettavat: expect.arrayContaining([
          {
            etunimet: 'Essi',
            sukunimi: 'Eskari',
            oid: '1.2.246.562.24.00000000066',
            hetu: '300996-870E'
          }
        ])
      }
    })
  })
})
