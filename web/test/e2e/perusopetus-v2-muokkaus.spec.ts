import { expect, test } from './base'
import { virkailija } from './setup/auth'

const kaisaOid = '1.2.246.562.24.00000000007'
const tommiOid = '1.2.246.562.24.00000000051'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`
const tommiUrl = `${tommiOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: muokkaustila', () => {
  test.use({ storageState: virkailija('kalle') })

  // Kaisan kenttäkohtaiset muutokset: testit muuttavat toisistaan riippumattomia
  // kenttiä, joten ne jakavat yhden fixturien alustuksen ja ajaa peräkkäin.
  test.describe.serial('Kaisan kenttämuutokset', () => {
    test.beforeAll(async ({ fixtures }) => {
      await fixtures.reset()
    })

    test('Muokkaustilaan siirtyminen ja peruuttaminen', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)
      await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

      // Muokkaa-painike näkyy
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toContainText(
        'Muokkaa'
      )

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Peruuta- ja Tallenna-painikkeet näkyvät
      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.cancelEdit')
      ).toBeVisible()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeVisible()

      // Tallenna on disabloitu (ei muutoksia)
      await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeDisabled()

      // Status näyttää "Ei tallentamattomia muutoksia"
      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.editStatus')
      ).toContainText('Ei tallentamattomia muutoksia')

      // Peruuta muokkaus
      await page.getByTestId('oo.0.opiskeluoikeus.cancelEdit').click()

      // Muokkaa-painike näkyy taas
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toContainText(
        'Muokkaa'
      )
    })

    test('Luokka-kentän tyhjentäminen estää tallennuksen', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)

      // Vaihda 8. vuosiluokan välilehdelle
      await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Tyhjennä luokka
      const luokkaInput = page.getByTestId(
        'oo.0.suoritukset.2.luokka.edit.input'
      )
      await luokkaInput.clear()

      // Tallenna-painike on disabloitu
      await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeDisabled()

      // Peruuta
      await page.getByTestId('oo.0.opiskeluoikeus.cancelEdit').click()

      // Alkuperäinen arvo palautuu
      await expect(
        page.getByTestId('oo.0.suoritukset.2.luokka.value')
      ).toContainText('8C')
    })

    test('Luokka-kentän muokkaus 8. vuosiluokalla', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)

      // Vaihda 8. vuosiluokan välilehdelle (tab-järjestys: oppimäärä, 9, 8, 7)
      await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

      // Tarkista alkutila
      await expect(
        page.getByTestId('oo.0.suoritukset.2.luokka.value')
      ).toContainText('8C')

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Muuta luokkaa
      const luokkaInput = page.getByTestId(
        'oo.0.suoritukset.2.luokka.edit.input'
      )
      await luokkaInput.clear()
      await luokkaInput.fill('8D')

      // Tallenna on nyt enabloitu
      await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeEnabled()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()

      // Odota tallennus
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Tarkista muutettu arvo
      await expect(
        page.getByTestId('oo.0.suoritukset.2.luokka.value')
      ).toContainText('8D')
    })

    test('Suorituskielen muokkaus oppimäärällä', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)
      await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

      // Tarkista alkutila
      await expect(
        page.getByTestId('oo.0.suoritukset.0.suorituskieli.value')
      ).toContainText('suomi')

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Vaihda suorituskieli (Select: testId="suorituskieli.edit")
      const suorituskieliInput = page.getByTestId(
        'oo.0.suoritukset.0.suorituskieli.edit.input'
      )
      await suorituskieliInput.click()
      await suorituskieliInput.fill('ruotsi')
      await page
        .locator('.Select__optionLabel')
        .filter({ hasText: /^ruotsi$/ })
        .first()
        .click()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Tarkista muutettu arvo
      await expect(
        page.getByTestId('oo.0.suoritukset.0.suorituskieli.value')
      ).toContainText('ruotsi')
    })

    test('Suoritustavan muokkaus oppimäärällä', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)
      await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

      // Tarkista alkutila
      await expect(
        page.getByTestId('oo.0.suoritukset.0.suoritustapa.value')
      ).toContainText('Koulutus')

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Vaihda suoritustapa (Select: testId="suoritustapa.edit")
      const suoritustapaInput = page.getByTestId(
        'oo.0.suoritukset.0.suoritustapa.edit.input'
      )
      await suoritustapaInput.click()
      await suoritustapaInput.fill('Erityinen')
      await page
        .locator('.Select__optionLabel')
        .filter({ hasText: 'Erityinen tutkinto' })
        .click()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Tarkista muutettu arvo
      await expect(
        page.getByTestId('oo.0.suoritukset.0.suoritustapa.value')
      ).toContainText('Erityinen tutkinto')
    })
    test('Käyttäytymisen arvioinnin lisäys 7. vuosiluokalle', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)

      // Vaihda 7. vuosiluokan välilehdelle
      await page.getByTestId('oo.0.suoritusTabs.3.tab').click()

      // Tarkista, ettei käyttäytymistä näy view-modessa (7. luokalla ei ole oletuksena)
      await expect(
        page.getByTestId('oo.0.suoritukset.3.kayttaytyminen.arvosana')
      ).not.toBeVisible()

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Lisää käyttäytymisen arviointi
      await page
        .getByTestId('oo.0.suoritukset.3.kayttaytyminen.kayttaytyminen.lisaa')
        .click()

      // Oletusarvosana on S — Select näyttää koodiston nimen "hyväksytty"
      await expect(
        page.getByTestId(
          'oo.0.suoritukset.3.kayttaytyminen.kayttaytyminen.input'
        )
      ).toHaveValue('hyväksytty', { timeout: 10000 })

      // Tallenna oletusarvosanalla S
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Lataa sivu kokonaan uudelleen varmistaaksemme, että data tallentui
      await oppijaPage.goto(kaisaUrl)

      // Navigoi 7. vuosiluokalle
      await page.getByTestId('oo.0.suoritusTabs.3.tab').click()

      // Varmista, että 7. vuosiluokan sisältö latautui
      await expect(
        page.getByTestId('oo.0.suoritukset.3.koulutus')
      ).toContainText('7. vuosiluokka', { timeout: 10000 })

      // Tarkista, että käyttäytymisen arvosana "S" näkyy
      await expect(
        page.getByTestId('oo.0.suoritukset.3.kayttaytyminen.arvosana')
      ).toContainText('S', { timeout: 10000 })
    })

    test('Käyttäytymisen arvioinnin poisto', async ({ page, oppijaPage }) => {
      await oppijaPage.goto(kaisaUrl)

      // Vaihda 8. vuosiluokan välilehdelle (jolla on käyttäytyminen)
      await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

      // Tarkista alkutila
      await expect(
        page.getByTestId('oo.0.suoritukset.2.kayttaytyminen.arvosana')
      ).toContainText('S')

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Poista käyttäytymisen arviointi
      await page
        .getByTestId('oo.0.suoritukset.2.kayttaytyminen.kayttaytyminen.remove')
        .click()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Tarkista, ettei käyttäytymistä enää näy
      await expect(
        page.getByTestId('oo.0.suoritukset.2.kayttaytyminen.arvosana')
      ).not.toBeVisible()
    })

    test('Oppiaineen arvosanan muokkaus 8. vuosiluokalla', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)

      // Vaihda 8. vuosiluokan välilehdelle (tab-järjestys: oppimäärä, 9, 8, 7)
      await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

      // Tarkista alkutila: ensimmäinen oppiaine (äidinkieli, arvosana 9)
      await expect(
        page.getByTestId('oo.0.suoritukset.2.osasuoritukset.0.arvosana.value')
      ).toContainText('9')

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Muuta arvosanaa (Select: testId="arvosana.edit" sisällä TestIdLayer id=0)
      await page
        .getByTestId('oo.0.suoritukset.2.osasuoritukset.0.arvosana.edit.input')
        .click()
      await page
        .locator('.Select__optionLabel')
        .filter({ hasText: /^5$/ })
        .click()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Tarkista muutettu arvosana
      await expect(
        page.getByTestId('oo.0.suoritukset.2.osasuoritukset.0.arvosana.value')
      ).toContainText('5')
    })
    test('Yksilöllistäminen: footnote * tulee näkyviin', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)
      await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

      // Äidinkieli (osasuoritukset.0) ei ole yksilöllistetty → ei footnote-merkintää
      await expect(
        page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.footnote')
      ).not.toBeVisible()

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Avaa äidinkielen (index 0) laajennettu sisältö
      await page
        .getByTestId('oo.0.suoritukset.0.osasuoritukset.0.expand')
        .click()

      await page
        .getByTestId(
          'oo.0.suoritukset.0.osasuoritukset.0.properties.yksilöllistettyOppimäärä.edit.input'
        )
        .check()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Footnote * näkyy äidinkielellä
      await expect(
        page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.footnote')
      ).toHaveText('*')
    })

    test('Painotettu opetus: footnote ** tulee näkyviin', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)
      await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

      // Matematiikka (osasuoritukset.7) ei ole painotettu → ei **-merkintää
      await expect(
        page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.footnote')
      ).not.toBeVisible()

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Avaa matematiikan (index 7) laajennettu sisältö
      await page
        .getByTestId('oo.0.suoritukset.0.osasuoritukset.7.expand')
        .click()

      await page
        .getByTestId(
          'oo.0.suoritukset.0.osasuoritukset.7.properties.painotettuOpetus.edit.input'
        )
        .check()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Footnote ** näkyy matematiikalla
      await expect(
        page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.footnote')
      ).toHaveText('**')
    })

    test('Todistuksella näkyvät lisätiedot: lisäys ja poisto', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)
      await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

      // Alkutila: todistuksella näkyvät lisätiedot ei näy (ei dataa)
      await expect(
        page.locator('text=Todistuksella näkyvät lisätiedot')
      ).not.toBeVisible()

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Edit-modessa kenttä näkyy (tyhjänä)
      await expect(
        page.locator('text=Todistuksella näkyvät lisätiedot')
      ).toBeVisible()

      const lisätiedotInput = page.getByTestId(
        'oo.0.suoritukset.0.todistuksellaNäkyvätLisätiedot.edit.input'
      )
      await lisätiedotInput.fill('Testitesti')
      await expect(lisätiedotInput).toHaveValue('Testitesti')

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Tarkista, että lisätiedot näkyvät
      await expect(
        page
          .locator('.KeyValueRow')
          .filter({ hasText: 'Todistuksella näkyvät lisätiedot' })
      ).toContainText('Testitesti')

      // Poista: tyhjennä kenttä
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await lisätiedotInput.clear()
      await expect(lisätiedotInput).toHaveValue('')

      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Lisätiedot piilotetaan kun arvo on tyhjä
      await expect(
        page
          .locator('.KeyValueRow')
          .filter({ hasText: 'Todistuksella näkyvät lisätiedot' })
      ).not.toBeVisible()
    })

    test('Valinnaisen oppiaineen lisäys ja poisto', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)

      // Vaihda 8. vuosiluokan välilehdelle
      await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Lisää valinnainen oppiaine (valinnaiset-osion KoodistoSelect)
      const valinnaisetSection = page.getByTestId('oppiaineet-valinnaiset')
      await valinnaisetSection
        .getByTestId('oo.0.suoritukset.2.uusi-oppiaine.input')
        .click()
      await page
        .locator('.Select__optionLabel')
        .filter({ hasText: 'Historia' })
        .click()

      // Odota, että uusi Historia-rivi on renderöity ennen kuin haetaan sen
      // arvosanakenttä: muuten `.last()` voi osua vielä edelliseen viimeiseen
      // riviin (esim. Tietokoneen hyötykäyttö), jolloin Historian arvosana jää
      // tyhjäksi ja tallennus epäonnistuu keskeneräisen osasuorituksen takia.
      await expect(
        valinnaisetSection
          .locator('[data-testid$=".nimi"]')
          .filter({ hasText: /^Historia$/ })
      ).toBeVisible()

      // Aseta arvosana uudelle valinnaiselle (uusi oppiaine on nyt viimeinen)
      const uusiHistoriaArvosana = valinnaisetSection
        .locator('[data-testid*="arvosana.edit.input"]')
        .last()
      await uusiHistoriaArvosana.click()
      await page
        .locator('.Select__optionLabel')
        .filter({ hasText: /^9$/ })
        .click()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Vaihda takaisin 8. vuosiluokan välilehdelle (tab saattaa resetoitua)
      await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

      // Tarkista, että Historia näkyy valinnaisissa
      await expect(valinnaisetSection.locator('text=Historia')).toBeVisible({
        timeout: 10000
      })

      // Poista lisätty valinnainen Historia
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Navigoi takaisin 8. vuosiluokan välilehdelle (edit mode saattaa resetoida tabin)
      await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

      // Etsi Historia-rivin poistopainike valinnaisista: uusi oppiaine on viimeinen
      // Klikataan viimeisen valinnainen-rivin delete-painiketta
      const deleteButtons = valinnaisetSection.locator(
        '[data-testid$=".delete"]'
      )
      await deleteButtons.last().click()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Vaihda takaisin 8. vuosiluokan välilehdelle
      await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

      // Historia ei enää näy valinnaisissa
      await expect(
        valinnaisetSection
          .locator('[data-testid*="nimi"]')
          .filter({ hasText: 'Historia' })
      ).not.toBeVisible()
    })

    test('Pakollisen oppiaineen lisäys ja poisto', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)
      await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

      // Siirry muokkaustilaan oppimäärällä
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Lisää pakollinen oppiaine (pakolliset-osion KoodistoSelect)
      const pakollisSection = page.getByTestId('oppiaineet-pakolliset')
      await pakollisSection
        .getByTestId('oo.0.suoritukset.0.uusi-oppiaine.input')
        .click()
      await page
        .locator('.Select__optionLabel')
        .filter({ hasText: 'Filosofia' })
        .click()

      // Odota, että uusi Filosofia-rivi on renderöity ennen kuin haetaan sen
      // arvosanakenttä: muuten `.last()` voi osua edelliseen viimeiseen riviin,
      // jolloin Filosofian arvosana jää tyhjäksi ja tallennus epäonnistuu.
      await expect(
        pakollisSection
          .locator('[data-testid$=".nimi"]')
          .filter({ hasText: /^Filosofia$/ })
      ).toBeVisible()

      // Aseta arvosana (uusi oppiaine on nyt viimeinen)
      const uusiFiArvosana = pakollisSection
        .locator('[data-testid*="arvosana.edit.input"]')
        .last()
      await uusiFiArvosana.click()
      await page
        .locator('.Select__optionLabel')
        .filter({ hasText: /^8$/ })
        .click()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Tarkista, että Filosofia 8 näkyy pakollisissa
      await expect(pakollisSection.locator('text=Filosofia')).toBeVisible({
        timeout: 10000
      })

      // Poista Filosofia
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Klikataan viimeisen pakollisen rivin delete-painiketta (Filosofia on viimeisenä)
      const deleteButtons = pakollisSection.locator('[data-testid$=".delete"]')
      await deleteButtons.last().click()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Filosofia ei enää näy
      await expect(
        pakollisSection
          .locator('[data-testid*="nimi"]')
          .filter({ hasText: 'Filosofia' })
      ).not.toBeVisible()
    })

    test('Opiskeluoikeuden lisätiedot: vuosiluokkiinSitoutumatonOpetus muokkaus', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)
      await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

      // Alkutila: vuosiluokkiinSitoutumatonOpetus ei ole asetettu (ei näy)
      await expect(
        page.getByTestId(
          'oo.0.opiskeluoikeus.lisätiedot.vuosiluokkiinSitoutumatonOpetus.value'
        )
      ).not.toBeVisible()

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Aseta vuosiluokkiinSitoutumatonOpetus päälle
      await page
        .getByTestId(
          'oo.0.opiskeluoikeus.lisätiedot.vuosiluokkiinSitoutumatonOpetus.edit.input'
        )
        .check()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Tarkista, että arvo on "Kyllä"
      await expect(
        page.getByTestId(
          'oo.0.opiskeluoikeus.lisätiedot.vuosiluokkiinSitoutumatonOpetus.value'
        )
      ).toContainText('Kyllä')

      // Poista asetus
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await page
        .getByTestId(
          'oo.0.opiskeluoikeus.lisätiedot.vuosiluokkiinSitoutumatonOpetus.edit.input'
        )
        .uncheck()

      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Kenttä ei enää näy
      await expect(
        page.getByTestId(
          'oo.0.opiskeluoikeus.lisätiedot.vuosiluokkiinSitoutumatonOpetus.value'
        )
      ).not.toBeVisible()
    })
    test('Jää luokalle -kentän muokkaus 7. vuosiluokalla', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)

      // Vaihda 7. vuosiluokan välilehdelle
      await page.getByTestId('oo.0.suoritusTabs.3.tab').click()

      // Alkutila: jää luokalle = Kyllä
      await expect(
        page.getByTestId('oo.0.suoritukset.3.jääLuokalle.value')
      ).toContainText('Kyllä')

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Vaihda jääLuokalle pois päältä (Checkbox: testId input = jääLuokalle.edit.input)
      await page
        .getByTestId('oo.0.suoritukset.3.jääLuokalle.edit.input')
        .uncheck()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Tarkista, ettei "Jää luokalle" enää näy (koska arvo on false, kenttä piilotetaan)
      await expect(
        page.getByTestId('oo.0.suoritukset.3.jääLuokalle.value')
      ).not.toBeVisible()
    })
  })

  // Tommi (toiminta-alueittain opiskeleva) on eri oppija, joten oma describe.
  test.describe('Toiminta-alueittain opiskeleva', () => {
    test('Toiminta-alueittain opiskeleva: toiminta-alueen poisto', async ({
      page,
      oppijaPage,
      fixtures
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(tommiUrl)
      await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

      // Tarkista alkutila: 5 toiminta-aluetta
      await expect(
        page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.nimi')
      ).toContainText('motoriset taidot')
      await expect(
        page.getByTestId('oo.0.suoritukset.0.osasuoritukset.2.nimi')
      ).toContainText('sosiaaliset taidot')
      await expect(
        page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.nimi')
      ).toContainText('kognitiiviset taidot')

      // Siirry muokkaustilaan
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Poista valmistunut-tila ensin
      await page
        .getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove')
        .click()

      // Merkitse keskeneräiseksi
      await page
        .getByTestId(
          'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
        )
        .click()

      // Poista sosiaaliset taidot (index 2: testId="delete" sisällä TestIdLayer id=2)
      await page
        .getByTestId('oo.0.suoritukset.0.osasuoritukset.2.delete')
        .click()

      // Tallenna
      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      // Tarkista, että sosiaaliset taidot on poistettu: nyt 4 toiminta-aluetta
      // Index 2 on nyt "päivittäisten toimintojen taidot" (aiemmin index 3)
      await expect(
        page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.nimi')
      ).toContainText('motoriset taidot')
      await expect(
        page.getByTestId('oo.0.suoritukset.0.osasuoritukset.1.nimi')
      ).toContainText('kieli ja kommunikaatio')
      await expect(
        page.getByTestId('oo.0.suoritukset.0.osasuoritukset.2.nimi')
      ).toContainText('päivittäisten toimintojen taidot')
      await expect(
        page.getByTestId('oo.0.suoritukset.0.osasuoritukset.3.nimi')
      ).toContainText('kognitiiviset taidot')
    })

    test('Toiminta-alueittain opiskeleva: puuttuvan toiminta-alueen voi lisätä takaisin', async ({
      page,
      oppijaPage,
      fixtures
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(tommiUrl)
      await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await page
        .getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove')
        .click()
      await page
        .getByTestId(
          'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
        )
        .click()

      await page
        .getByTestId('oo.0.suoritukset.0.osasuoritukset.2.delete')
        .click()

      const uusiToimintaAlueInput = page.getByTestId(
        'oo.0.suoritukset.0.uusi-oppiaine.input'
      )
      await uusiToimintaAlueInput.click()
      await page
        .locator('.Select__optionLabel')
        .filter({ hasText: /^sosiaaliset taidot$/i })
        .click()

      const uusiArvosana = page
        .locator('[data-testid*="oo.0.suoritukset.0.osasuoritukset"]')
        .locator('[data-testid$=".arvosana.edit.input"]')
        .last()
      await uusiArvosana.click()
      await page
        .locator('.Select__optionLabel')
        .filter({ hasText: /^8$/ })
        .click()

      await page.getByTestId('oo.0.opiskeluoikeus.save').click()
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
        timeout: 15000
      })

      const toimintaAlueet = page.locator(
        '[data-testid^="oo.0.suoritukset.0.osasuoritukset."][data-testid$=".nimi"]'
      )
      await expect(toimintaAlueet).toHaveCount(5)
      await expect(
        page
          .locator('[data-testid$=".nimi"]')
          .filter({ hasText: 'sosiaaliset taidot' })
      ).toBeVisible()
    })
  })
})
