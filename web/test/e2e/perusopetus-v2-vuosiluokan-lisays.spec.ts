import { Raw } from '../../app/util/schema'
import { Oppija } from '../../app/types/fi/oph/koski/schema/Oppija'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Testit vuosiluokan suorituksen lisäykselle perusopetuksen v2-editorissa.
 * Portattu tiedostosta `web/test/spec/perusopetusSpec_3.js` lines 22-618.
 */

const jynOid = '1.2.246.562.10.14613773812'
const jynNimi = 'Jyväskylän normaalikoulu'
const jynOppilaitos = {
  oid: jynOid,
  oppilaitosnumero: {
    koodiarvo: '00204',
    nimi: { fi: jynNimi, sv: jynNimi, en: jynNimi },
    koodistoUri: 'oppilaitosnumero'
  },
  nimi: { fi: jynNimi, sv: jynNimi, en: jynNimi },
  kotipaikka: {
    koodiarvo: '179',
    nimi: { fi: 'Jyväskylä', sv: 'Jyväskylä' },
    koodistoUri: 'kunta'
  }
}

// Rakentaa oppijan, jolla on yksi perusopetuksen oppimäärän suoritus mutta
// ei yhtään vuosiluokan suoritusta. Oppija on Tyhjä, Tero (hetu 230872-7258).
const tyhjäTeroPerusopetus = (): Raw<Oppija> => ({
  henkilö: {
    hetu: '230872-7258',
    etunimet: 'Tero',
    kutsumanimi: 'Tero',
    sukunimi: 'Tyhjä'
  },
  opiskeluoikeudet: [
    {
      tyyppi: {
        koodiarvo: 'perusopetus',
        koodistoUri: 'opiskeluoikeudentyyppi'
      },
      oppilaitos: jynOppilaitos,
      tila: {
        opiskeluoikeusjaksot: [
          {
            alku: '2017-01-01',
            tila: {
              koodiarvo: 'lasna',
              koodistoUri: 'koskiopiskeluoikeudentila'
            }
          }
        ]
      },
      suoritukset: [
        {
          tyyppi: {
            koodiarvo: 'perusopetuksenoppimaara',
            koodistoUri: 'suorituksentyyppi'
          },
          koulutusmoduuli: {
            tunniste: {
              koodiarvo: '201101',
              koodistoUri: 'koulutus'
            },
            perusteenDiaarinumero: '104/011/2014'
          },
          toimipiste: jynOppilaitos,
          suorituskieli: { koodiarvo: 'FI', koodistoUri: 'kieli' },
          suoritustapa: {
            koodiarvo: 'koulutus',
            koodistoUri: 'perusopetuksensuoritustapa'
          }
        }
      ]
    }
  ]
})

const tyhjäTeroToimintaAlueittainPerusopetus = (): Raw<Oppija> => ({
  ...tyhjäTeroPerusopetus(),
  opiskeluoikeudet: tyhjäTeroPerusopetus().opiskeluoikeudet.map((oo) => ({
    ...oo,
    lisätiedot: {
      erityisenTuenPäätökset: [
        {
          alku: '2017-01-01',
          loppu: '2026-08-31',
          opiskeleeToimintaAlueittain: true
        }
      ]
    }
  }))
})

const v2Url = (oid: string) =>
  `${oid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

const addNewTab = (page: import('@playwright/test').Page) =>
  page.getByRole('button', { name: /lisää vuosiluokan suoritus/i })

test.describe('Perusopetuksen uusi käyttöliittymä: vuosiluokan suorituksen lisäys', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Kun opiskeluoikeus on tilassa VALMIS: lisää vuosiluokan suoritus -linkki ei näy', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    // Kaisa Kiiski on valmistunut perusopetuksen oppimäärän suorittanut ja
    // hänellä on vain 7-9 vuosiluokat. Luokat 1-6 ovat "puuttuvia", mutta
    // valmistunut-tilassa lisäyslinkkiä ei tule näkyä.
    await fixtures.reset()
    const kaisaOid = '1.2.246.562.24.00000000007'
    await oppijaPage.goto(v2Url(kaisaOid))

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    await expect(addNewTab(page)).not.toBeVisible()
  })

  test('Kun tilassa LÄSNÄ ennen lisäystä: linkki näkyy ja muut suoritukset näytetään', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    const oppija = await fixtures.putOppija(tyhjäTeroPerusopetus())
    await oppijaPage.goto(v2Url(oppija.henkilö.oid))

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Lisää vuosiluokan suoritus -linkki näkyy
    await expect(addNewTab(page)).toBeVisible()

    // Päätason suoritus (päättötodistus) näkyy ensimmäisenä tabina
    await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toBeVisible()
    await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toContainText(
      'Päättötodistus'
    )
  })

  test.describe('Lisättäessä ensimmäinen vuosiluokan suoritus', () => {
    test('Lisää-nappi disabloitu, auto-valinta pienin puuttuva luokka-aste, syötteet ja tallennus', async ({
      page,
      oppijaPage,
      fixtures
    }) => {
      await fixtures.reset()
      const oppija = await fixtures.putOppija(tyhjäTeroPerusopetus())
      await oppijaPage.goto(v2Url(oppija.henkilö.oid))

      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await addNewTab(page).click()

      const modal = page.locator('.Modal')
      await expect(modal).toBeVisible()

      // Aluksi: Lisää-nappi disabloitu
      const submitBtn = page.getByTestId(
        'oo.0.modal.uusiVuosiluokanSuoritus.submit'
      )
      await expect(submitBtn).toBeDisabled()

      // Auto-valitsee pienimmän puuttuvan luokka-asteen (1)
      const tunnisteInput = page.getByTestId(
        'oo.0.modal.uusiVuosiluokanSuoritus.tunniste.input'
      )
      await expect(tunnisteInput).toHaveValue(/1\. vuosiluokka/)

      // Syötä luokka ja alkamispäivä — toimipiste esitäytetty päättötodistuksen
      // pohjasuorituksen toimipisteestä
      await page
        .getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.luokka.input')
        .fill('1A')
      await expect(submitBtn).toBeDisabled()

      await page
        .getByTestId(
          'oo.0.modal.uusiVuosiluokanSuoritus.alkamispäivä.edit.input'
        )
        .fill('1.1.2017')
      await page
        .getByTestId(
          'oo.0.modal.uusiVuosiluokanSuoritus.alkamispäivä.edit.input'
        )
        .blur()

      await expect(submitBtn).toBeEnabled()

      // Lisää
      await submitBtn.click()

      // Modal sulkeutuu
      await expect(modal).not.toBeVisible()

      // Uusi tab "1. vuosiluokka" näkyy
      await expect(page.getByTestId('oo.0.suoritusTabs.1.tab')).toContainText(
        '1. vuosiluokka'
      )

      // Luokka on oikein (näkyy muokkaustilassa)
      await expect(
        page.getByTestId('oo.0.suoritukset.1.luokka.edit.input')
      ).toHaveValue('1A')

      // Esitäytetyt pakolliset oppiaineet näkyvät — 1-2. vuosiluokalla 9 kpl
      const oppiaineet = page.locator(
        '[data-testid^="oo.0.suoritukset.1.osasuoritukset."][data-testid$=".nimi"]'
      )
      await expect(oppiaineet).toHaveCount(9)
    })
  })

  test.describe('Oppiaineiden esitäyttö luokka-asteen mukaan', () => {
    test('Luokka 1-2: 9 pakollista oppiainetta', async ({
      page,
      oppijaPage,
      fixtures
    }) => {
      await fixtures.reset()
      const oppija = await fixtures.putOppija(tyhjäTeroPerusopetus())
      await oppijaPage.goto(v2Url(oppija.henkilö.oid))
      await lisääVuosiluokka(page, '1', '1A', '1.1.2017')

      const oppiaineet = page.locator(
        '[data-testid^="oo.0.suoritukset.1.osasuoritukset."][data-testid$=".nimi"]'
      )
      await expect(oppiaineet).toHaveCount(9)
      await expect(oppiaineet.nth(0)).toContainText('Äidinkieli')
      await expect(oppiaineet.nth(1)).toContainText('Matematiikka')
      await expect(oppiaineet.nth(2)).toContainText('Ympäristöoppi')
      await expect(oppiaineet.nth(8)).toContainText('Opinto-ohjaus')
    })

    test('Luokka 3-6: 12 pakollista oppiainetta (sis. A1-kieli, historia, yhteiskuntaoppi)', async ({
      page,
      oppijaPage,
      fixtures
    }) => {
      await fixtures.reset()
      const oppija = await fixtures.putOppija(tyhjäTeroPerusopetus())
      await oppijaPage.goto(v2Url(oppija.henkilö.oid))
      await lisääVuosiluokka(page, '3', '3A', '1.1.2017')

      const oppiaineet = page.locator(
        '[data-testid^="oo.0.suoritukset.1.osasuoritukset."][data-testid$=".nimi"]'
      )
      await expect(oppiaineet).toHaveCount(12)
      // A1-kieli toisena (heti äidinkielen jälkeen)
      await expect(oppiaineet.nth(1)).toContainText('A1-kieli')
      await expect(oppiaineet.nth(5)).toContainText('Historia')
      await expect(oppiaineet.nth(6)).toContainText('Yhteiskuntaoppi')
    })

    test('Luokka 7-8: 18 pakollista oppiainetta (sis. B1, luonnontieteet, kotitalous)', async ({
      page,
      oppijaPage,
      fixtures
    }) => {
      await fixtures.reset()
      const oppija = await fixtures.putOppija(tyhjäTeroPerusopetus())
      await oppijaPage.goto(v2Url(oppija.henkilö.oid))
      await lisääVuosiluokka(page, '7', '7A', '1.1.2017')

      const oppiaineet = page.locator(
        '[data-testid^="oo.0.suoritukset.1.osasuoritukset."][data-testid$=".nimi"]'
      )
      await expect(oppiaineet).toHaveCount(18)
      await expect(oppiaineet.nth(1)).toContainText('A1-kieli')
      await expect(oppiaineet.nth(2)).toContainText('B1-kieli')
      await expect(oppiaineet.nth(3)).toContainText('Matematiikka')
      await expect(oppiaineet.nth(4)).toContainText('Biologia')
      await expect(oppiaineet.nth(16)).toContainText('Kotitalous')
    })

    test('Luokka 9: ei esitäytä oppiaineita, oppiainetaulukko ei näy', async ({
      page,
      oppijaPage,
      fixtures
    }) => {
      await fixtures.reset()
      const oppija = await fixtures.putOppija(tyhjäTeroPerusopetus())
      await oppijaPage.goto(v2Url(oppija.henkilö.oid))
      await lisääVuosiluokka(page, '9', '9A', '1.1.2017')

      // Oppiaineita ei esitäytetä
      const oppiaineet = page.locator(
        '[data-testid^="oo.0.suoritukset.1.osasuoritukset."][data-testid$=".nimi"]'
      )
      await expect(oppiaineet).toHaveCount(0)
    })
  })

  test('Toiminta-alueittain opiskelevalle esitäytetään toiminta-alueet', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    const oppija = await fixtures.putOppija(
      tyhjäTeroToimintaAlueittainPerusopetus()
    )
    await oppijaPage.goto(v2Url(oppija.henkilö.oid))

    await lisääVuosiluokka(page, '1', '1A', '1.1.2017')

    const toimintaAlueet = page.locator(
      '[data-testid^="oo.0.suoritukset.1.osasuoritukset."][data-testid$=".nimi"]'
    )
    await expect(toimintaAlueet).toHaveCount(5)
    await expect(toimintaAlueet.nth(0)).toContainText('motoriset taidot')
    await expect(toimintaAlueet.nth(1)).toContainText('kieli ja kommunikaatio')
    await expect(toimintaAlueet.nth(2)).toContainText('sosiaaliset taidot')
    await expect(toimintaAlueet.nth(3)).toContainText(
      'päivittäisten toimintojen taidot'
    )
    await expect(toimintaAlueet.nth(4)).toContainText('kognitiiviset taidot')
  })

  test('Toisen suorituksen lisääminen käyttää edellisen toimipistettä ja auto-valitsee 2. vuosiluokan', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    const oppija = await fixtures.putOppija(tyhjäTeroPerusopetus())
    await oppijaPage.goto(v2Url(oppija.henkilö.oid))

    // Lisää ensimmäinen vuosiluokka (1. vuosiluokka)
    await lisääVuosiluokka(page, '1', '1A', '1.1.2017')

    // Avaa toinen lisäysdialogi
    await addNewTab(page).click()
    const modal = page.locator('.Modal')
    await expect(modal).toBeVisible()

    // Auto-valitsee 2. vuosiluokan (pienin puuttuva)
    const tunnisteInput = page.getByTestId(
      'oo.0.modal.uusiVuosiluokanSuoritus.tunniste.input'
    )
    await expect(tunnisteInput).toHaveValue(/2\. vuosiluokka/)
  })

  test('Peräkkäiset lisäykset aktivoivat aina juuri lisätyn vuosiluokan tabin', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    const oppija = await fixtures.putOppija(tyhjäTeroPerusopetus())
    await oppijaPage.goto(v2Url(oppija.henkilö.oid))

    // Ensimmäinen lisäys: aktiivisen tabin tulee olla 1. vuosiluokka (index 1)
    await lisääVuosiluokka(page, '1', '1A', '1.1.2017')
    await expect(
      page.getByTestId('oo.0.suoritukset.1.luokka.edit.input')
    ).toHaveValue('1A')

    // Käyttäjä vaihtaa takaisin oppimäärän tabille (index 0)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()
    await expect(
      page.getByTestId('oo.0.suoritukset.1.luokka.edit.input')
    ).not.toBeVisible()

    // Toinen lisäys: aktiivisen tabin tulee vaihtua juuri lisättyyn (index 2)
    await addNewTab(page).click()
    const modal = page.locator('.Modal')
    await expect(modal).toBeVisible()
    await page
      .getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.luokka.input')
      .fill('2A')
    await page
      .getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.alkamispäivä.edit.input')
      .fill('1.1.2018')
    await page
      .getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.alkamispäivä.edit.input')
      .blur()
    await page.getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.submit').click()
    await expect(modal).not.toBeVisible()

    await expect(
      page.getByTestId('oo.0.suoritukset.2.luokka.edit.input')
    ).toHaveValue('2A')
  })
})

/** Avaa dialogi, asettaa tunniste/luokka/alkamispäivä ja klikkaa Lisää. */
async function lisääVuosiluokka(
  page: import('@playwright/test').Page,
  luokkaAste: string,
  luokka: string,
  alkamispäivä: string
) {
  // Siirry muokkaustilaan
  const editBtn = page.getByTestId('oo.0.opiskeluoikeus.edit')
  await expect(editBtn).toBeVisible()
  await editBtn.click()
  await expect(addNewTab(page)).toBeVisible()
  await addNewTab(page).click()

  const modal = page.locator('.Modal')
  await expect(modal).toBeVisible()

  // Jos tarvittaessa vaihdetaan luokka-aste (default on pienin puuttuva)
  const tunnisteInput = page.getByTestId(
    'oo.0.modal.uusiVuosiluokanSuoritus.tunniste.input'
  )
  const currentValue = await tunnisteInput.inputValue()
  if (!currentValue.startsWith(`${luokkaAste}.`)) {
    await tunnisteInput.click()
    await modal
      .locator('.Select__optionLabel')
      .filter({ hasText: new RegExp(`^${luokkaAste}\\. vuosiluokka`) })
      .first()
      .click()
  }

  await page
    .getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.luokka.input')
    .fill(luokka)
  await page
    .getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.alkamispäivä.edit.input')
    .fill(alkamispäivä)
  await page
    .getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.alkamispäivä.edit.input')
    .blur()

  await page.getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.submit').click()
  await expect(modal).not.toBeVisible()
}
