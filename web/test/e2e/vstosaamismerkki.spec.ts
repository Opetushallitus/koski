import { test } from './base'
import { virkailija } from './setup/auth'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'

test.describe('Vapaan sivistyön VST osaamismerkki', () => {
  test.use({ storageState: virkailija('pää') })
  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Uuden VST osaamismerkki -opiskeluoikeuden voi luoda, muokata validiksi ja löytää oppijahaulla', async ({
    page,
    virkailijaPage,
    vstOppijaPage,
    oppijaHaku
  }) => {
    // Luonti
    const uusiOppijaPage = new KoskiUusiOppijaPage(page)
    await uusiOppijaPage.goTo('210610A426P')
    await uusiOppijaPage.fill(
      {
        oppilaitos: 'Varsinais-Suomen kansanopisto',
        etunimet: 'Merkki',
        sukunimi: 'Merkkimestari',
        opiskeluoikeus: 'Vapaan sivistystyön koulutus',
        suoritustyyppi: 'Vapaan sivistystyön osaamismerkki',
        osaamismerkki: '1022 Digitaalinen tiedonhaku',
        opiskeluoikeudenTila: 'Hyväksytysti suoritettu'
      },
      true
    )
    await uusiOppijaPage.submitAndExpectSuccess()

    // Ensimuokkaus
    await vstOppijaPage.edit()

    // Lisää arviointi
    await page.getByTestId('suoritukset.0.arvosana.edit.input').click()
    await page
      .getByTestId(
        'suoritukset.0.arvosana.edit.options.arviointiasteikkovst_Hyväksytty.item'
      )
      .click()
    await page.getByTestId('suoritukset.0.arviointi.0.date.edit.input').click()
    await page
      .getByTestId('suoritukset.0.arviointi.0.date.edit.input')
      .fill('1.1.2024')

    // Lisää vahvistus
    await vstOppijaPage.vahvistaSuoritusUudellaHenkilöllä(
      'Reijo',
      'Rehtori',
      '1.1.2024'
    )

    // Vaihda tilan viimeinen päivä samaksi kuin arviointi- ja vahvistuspäivät
    await page
      .getByTestId('opiskeluoikeus.tila.edit.items.0.date.edit.input')
      .click()
    await page
      .getByTestId('opiskeluoikeus.tila.edit.items.0.date.edit.input')
      .fill('1.1.2024')

    // Tallenna muutokset
    await page.getByTestId('opiskeluoikeus.save').click()

    // Oppija löytyy oppijahaulla uudestaan
    await virkailijaPage.goto()
    const search = await oppijaHaku.search('210610A426P')
    await search.clickOnFirst()

    // Varmista versiohistoriaa käyttämällä, että versio 2 tuli luotua muokatessa
    await page.getByTestId('opiskeluoikeus.versiohistoria.button').click()
    await page.getByTestId('opiskeluoikeus.versiohistoria.list.2').click()
  })
})
