import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Pudotusvalikon (components-v2/controls/Select) yleinen käyttäytyminen.
 *
 * Valikon syötekenttä toimii hakukenttänä: siihen kirjoitettu teksti suodattaa
 * vaihtoehtoja. Tekstin ja mallin arvon on silti pysyttävä samassa tilassa,
 * eikä kenttä saa jäädä tyhjän näköiseksi arvo tallessa - silloin lomake
 * näyttäisi tyhjältä mutta olisi validi eikä käyttäjä saisi virhettä.
 */

const kaisaUrl =
  '1.2.246.562.24.00000000007?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true'

// Kieliaineen kieli on skeemassa pakollinen eikä tarjoa "Ei valintaa" -
// vaihtoehtoa, joten sen arvoa ei saa voida poistaa.
const KIELI = 'oo.0.suoritukset.0.osasuoritukset.2.kieli.edit'

test.describe('Pudotusvalikko', () => {
  test.use({ storageState: virkailija('kalle') })

  const avaaMuokkaus = async (page: any, oppijaPage: any, tabi: number) => {
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId(`oo.0.suoritusTabs.${tabi}.tab`).click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
  }

  test('Tekstin tyhjentäminen palauttaa kentän valitsemattomaan tilaan', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await avaaMuokkaus(page, oppijaPage, 0)

    const kieli = page.getByTestId(`${KIELI}.input`)
    const kentta = page.locator('.Select', {
      has: page.getByTestId(`${KIELI}.input`)
    })
    await expect(kieli).toHaveValue('ruotsi')

    await kieli.click()
    await kieli.fill('')
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.nimi').click()

    // Tyhjä tila on sama, jossa kenttä on kun oppiaine juuri lisättiin:
    // koodiviite ilman koodiarvoa. Kenttä on siis yhä olemassa, tyhjä ja
    // merkitty virheelliseksi - ei palautunut entiseen arvoonsa.
    await expect(kieli).toBeVisible()
    await expect(kieli).toHaveValue('')
    await expect(kentta).toHaveClass(/Select--error/)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeDisabled()

    // Kenttä on yhä käytettävissä: uuden arvon voi valita normaalisti.
    await kieli.click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^saksa$/ })
      .first()
      .click()
    await expect(kieli).toHaveValue('saksa')
  })

  test('Enter ilman korostettua vaihtoehtoa ei hävitä kenttää', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await avaaMuokkaus(page, oppijaPage, 0)

    const kieli = page.getByTestId(`${KIELI}.input`)
    await expect(kieli).toHaveValue('ruotsi')

    await kieli.click()
    await kieli.fill('')
    await page.keyboard.press('Enter')

    // Aiemmin tämä kutsui onClickOption(undefined), jolloin pakollinen kieli
    // katosi mallista ja koko kielivalikko hävisi riviltä.
    await expect(kieli).toBeVisible()
    await expect(kieli).toHaveValue('')
  })

  test('Arvosanan tyhjentäminen poistaa arvioinnin', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await avaaMuokkaus(page, oppijaPage, 0)

    const arvosana = page.getByTestId(
      'oo.0.suoritukset.0.osasuoritukset.0.arvosana.edit.input'
    )
    await expect(arvosana).not.toHaveValue('')

    await arvosana.click()
    await arvosana.fill('')
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.1.nimi').click()

    // Tyhjennys poistaa koko arvioinnin: päättötodistus on vahvistettu, joten
    // puuttuva arvosana estää tallennuksen kuten juuri lisätyllä oppiaineella.
    await expect(arvosana).toBeVisible()
    await expect(arvosana).toHaveValue('')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeDisabled()
  })

  test('Tekstin tyhjentäminen poistaa arvon kun "Ei valintaa" on tarjolla', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await avaaMuokkaus(page, oppijaPage, 2)

    const suoritustapa = page.getByTestId(
      'oo.0.suoritukset.2.suoritustapa.edit.input'
    )
    await expect(suoritustapa).toBeVisible()

    // Valitaan ensin arvo, jotta on jotain tyhjennettävää.
    await suoritustapa.click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^Koulutus$/ })
      .first()
      .click()
    await expect(suoritustapa).toHaveValue('Koulutus')

    // Tyhjennys vastaa "Ei valintaa" -vaihtoehdon valitsemista, joten arvo
    // poistuu myös mallista eikä palaudu valikon sulkeutuessa.
    await suoritustapa.click()
    await suoritustapa.fill('')
    await page.getByTestId('oo.0.suoritukset.2.luokka.edit.input').click()
    await expect(suoritustapa).toHaveValue('')
  })
})
