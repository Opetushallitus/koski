import type { Locator } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_2.js:1775-1862 "Opiskeluoikeuden versiot"
 * v2-editorille. V2:n versiohistoria-nappi aukaisee listan, jossa näkyy
 * tallennetut versionumerot. Versiolinkin kautta voi selata aiempia
 * versioita.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus`

// Miia Monikoululaisella on kaksi perusopetuksen opiskeluoikeutta eri
// oppilaitoksissa, joten kummankin testId-polut alkavat oo.0.
const miiaOid = '1.2.246.562.24.00000000012'
const miiaUrl = `${miiaOid}?opiskeluoikeudenTyyppi=perusopetus`

test.describe('Perusopetuksen uusi käyttöliittymä: versiohistoria', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Versiohistoria-nappi aukaisee listan versioista', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // Versiohistoria-nappi näkyy opiskeluoikeuden otsikkopalkissa
    const button = page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button')
    await expect(button).toBeVisible()
    await expect(button).toContainText('Versiohistoria')

    // Klikkaa avataksesi lista
    await button.click()
    // Ainakin yksi versio (v1) löytyy listalta
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1')
    ).toBeVisible()
  })

  test('Tallennus lisää uuden version versiohistoriaan', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // Aluksi lista sisältää v1:n (fixturin pohjaversio)
    await page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button').click()
    const v1Link = page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1')
    await expect(v1Link).toBeVisible()

    // Sulje lista klikkaamalla versiohistoria-nappia uudelleen
    await page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button').click()
    // Tee muutos + tallennus
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

    // Muuta käyttäytymisen arvosanaa
    await page
      .getByTestId('oo.0.suoritukset.2.kayttaytyminen.kayttaytyminen.input')
      .click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^10$/ })
      .first()
      .click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Avaa versiohistoria: nyt pitäisi näkyä sekä v1 että v2
    await page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1')
    ).toBeVisible()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.2')
    ).toBeVisible()
  })

  test('Version selaaminen ja siitä poistuminen ei lataa koko sivua uudelleen', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // Merkki häviää, jos koko sivu ladataan uudelleen (vanha käyttäytyminen)
    await page.evaluate(() => {
      ;(window as unknown as { __noReload?: boolean }).__noReload = true
    })
    const notReloaded = () =>
      page.evaluate(
        () =>
          (window as unknown as { __noReload?: boolean }).__noReload === true
      )

    const button = page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button')
    await button.click()
    await page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1').click()

    // Versioon siirrytään asiakaspuolella: otsikko ja osoite päivittyvät
    await expect(button).toContainText('Versionumero: v1')
    await expect(page).toHaveURL(/versionumero=1/)
    // Muokkausta ei tarjota versiota selatessa
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(0)
    expect(await notReloaded()).toBe(true)

    // Suljetaan versiolista, jottei se jää muokkauspalkin poistumispainikkeen
    // päälle, ja poistutaan versiohistoriasta muokkauspalkin painikkeesta.
    await button.click()
    await page.getByRole('button', { name: 'Poistu versiohistoriasta' }).click()
    await expect(button).toContainText('Versiohistoria')
    await expect(page).not.toHaveURL(/versionumero=/)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible()
    expect(await notReloaded()).toBe(true)
  })

  test('Tallennuksen jälkeen vanhan version selaus ja siitä poistuminen näyttää uusimman tallennetun version', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    const marker = 'REGRESSIO_UUSIN_TALLENNETTU'

    // Muokkaa todistuksella näkyviä lisätietoja ja tallenna -> syntyy uusi versio
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .getByTestId(
        'oo.0.suoritukset.1.todistuksellaNäkyvätLisätiedot.edit.input'
      )
      .fill(marker)
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })
    // Nykyinen näkymä näyttää juuri tallennetun arvon
    await expect(page.getByText(marker)).toBeVisible()

    // Selaa vanhinta versiota v1 (jossa juuri tallennettua lisätietoa ei ole)
    await page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button').click()
    await page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1').click()
    await expect(page).toHaveURL(/versionumero=1/)
    await expect(page.getByText(marker)).toHaveCount(0)

    // Versiosta poistuminen näyttää uusimman TALLENNETUN version, ei
    // alkuperäistä (mountin aikaista) tilaa. Tämä oli aiemmin rikki: editori
    // jäi näyttämään vanhentunutta dataa, koska oppijaFetchiä ei haettu
    // uudelleen ja remount-key törmäsi tallennusta edeltäneeseen versioon.
    // Suljetaan ensin versiolista, jottei se peitä muokkauspalkin
    // poistumispainiketta. Varmistus tehdään merkkijonohaulla (ei tiettyä
    // testId-solmua), jottei poistumisen jälkeinen oletusvälilehden valinta
    // vaikuta siihen.
    await page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button').click()
    await page.getByRole('button', { name: 'Poistu versiohistoriasta' }).click()
    await expect(page).not.toHaveURL(/versionumero=/)
    await expect(page.getByText(marker)).toBeVisible({ timeout: 15000 })
  })

  test('Versiohistoria koskee vain valittua opiskeluoikeutta, kun sivulla on useita', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(miiaUrl)

    const opiskeluoikeus = (oppilaitos: string) =>
      page
        .locator('[data-testid="opiskeluoikeuksientiedot"] > li')
        .filter({ hasText: oppilaitos })
    const jyvaskyla = opiskeluoikeus('Jyväskylän normaalikoulu')
    const kulosaari = opiskeluoikeus('Kulosaaren ala-aste')

    await jyvaskyla
      .getByTestId('oo.0.opiskeluoikeus.versiohistoria.button')
      .click()
    await jyvaskyla
      .getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1')
      .click()

    await expect(page).toHaveURL(/versionumero=1/)
    await expect(
      jyvaskyla.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button')
    ).toContainText('Versionumero: v1')
    await expect(jyvaskyla.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(
      0
    )

    // Toinen opiskeluoikeus pysyy nykyisessä versiossaan
    await expect(
      kulosaari.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button')
    ).toContainText('Versiohistoria')
    await expect(
      kulosaari.getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1')
    ).toHaveCount(0)
    await expect(kulosaari.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible()
    await expect(
      kulosaari.getByRole('button', { name: 'Poistu versiohistoriasta' })
    ).toHaveCount(0)
  })

  test('Toisen opiskeluoikeuden version selaus ei palauta tallennettua opiskeluoikeutta vanhaan versioon', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(miiaUrl)

    const opiskeluoikeus = (oppilaitos: string) =>
      page
        .locator('[data-testid="opiskeluoikeuksientiedot"] > li')
        .filter({ hasText: oppilaitos })
    const jyvaskyla = opiskeluoikeus('Jyväskylän normaalikoulu')
    const kulosaari = opiskeluoikeus('Kulosaaren ala-aste')

    const tallennaLisätieto = async (oo: Locator, marker: string) => {
      await oo.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await oo
        .getByTestId(
          'oo.0.suoritukset.1.todistuksellaNäkyvätLisätiedot.edit.input'
        )
        .fill(marker)
      await oo.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(oo.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })
      await expect(oo.getByText(marker)).toBeVisible()
    }

    // Kummallekin tallennetaan uusi versio, jolloin versio 1 erottuu
    // nykyisestä
    const jyvaskylaMarker = 'REGRESSIO_JYVASKYLAN_TALLENNUS'
    const kulosaariMarker = 'REGRESSIO_KULOSAAREN_TALLENNUS'
    await tallennaLisätieto(kulosaari, kulosaariMarker)
    await tallennaLisätieto(jyvaskyla, jyvaskylaMarker)

    // Jyväskylän version selaus ei vaihda Kulosaaren dataa tallennusta
    // edeltävään (sivun avaushetken) versioon. Jyväskylän merkin
    // katoaminen kertoo, että versio on ladattu ja editorit renderöity.
    await jyvaskyla
      .getByTestId('oo.0.opiskeluoikeus.versiohistoria.button')
      .click()
    await jyvaskyla
      .getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1')
      .click()
    await expect(jyvaskyla.getByText(jyvaskylaMarker)).toHaveCount(0)
    await expect(kulosaari.getByText(kulosaariMarker)).toBeVisible()

    // Siirtyminen suoraan Kulosaaren saman numeroiseen versioon hakee sen,
    // vaikka osoitteen versionumero ei muutu. Jyväskylä palaa uusimpaan
    // tallennettuun versioonsa.
    await jyvaskyla
      .getByTestId('oo.0.opiskeluoikeus.versiohistoria.button')
      .click()
    await kulosaari
      .getByTestId('oo.0.opiskeluoikeus.versiohistoria.button')
      .click()
    await kulosaari
      .getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1')
      .click()
    await expect(kulosaari.getByText(kulosaariMarker)).toHaveCount(0)
    await expect(
      kulosaari.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button')
    ).toContainText('Versionumero: v1')
    await expect(jyvaskyla.getByText(jyvaskylaMarker)).toBeVisible()
    await expect(
      jyvaskyla.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button')
    ).toContainText('Versiohistoria')
    await expect(jyvaskyla.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible()
  })
})
