import type { Locator, Page } from '@playwright/test'
import { Oppija } from '../../app/types/fi/oph/koski/schema/Oppija'
import { Raw } from '../../app/util/schema'
import { expect, test } from './base'
import { getVirkailijaSession, virkailija } from './setup/auth'

// Monia Useitan perusopetuksen välilehdellä aineopiskelijan opiskeluoikeus
// näkyy vanhalla käyttöliittymällä ja kaksi muuta uudella (oo.0 ja oo.1).
const moniaOid = '1.2.246.562.24.00000000165'
const moniaUrl = `${moniaOid}?opiskeluoikeudenTyyppi=perusopetus`

const vanhaOpiskeluoikeus = (page: Page) =>
  page
    .locator('[data-testid="opiskeluoikeuksientiedot"] > li')
    .filter({ has: page.locator('.versiohistoria') })
const lisätieto = (page: Page) =>
  page.locator(
    '[data-testid^="oo.0.suoritukset."][data-testid$="todistuksellaNäkyvätLisätiedot.edit.input"]'
  )
const osoite = (page: Page) => decodeURIComponent(page.url())

test.describe('Vanhan ja uuden käyttöliittymän muokkaustila samalla sivulla', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures, oppijaPage }) => {
    await fixtures.reset()
    await oppijaPage.goto(moniaUrl)
  })

  test('Vain yksi opiskeluoikeus on kerrallaan muokkaustilassa', async ({
    page
  }) => {
    const vanhanMuokkaa = page.locator('.toggle-edit')
    const lisääOpiskeluoikeus = page.locator('.add-opiskeluoikeus')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeVisible()
    await expect(page.getByTestId('oo.1.opiskeluoikeus.edit')).toHaveCount(0)
    await expect(vanhanMuokkaa).toHaveCount(0)
    await expect(lisääOpiskeluoikeus).toHaveClass(/disabled/)

    await page.getByTestId('oo.0.opiskeluoikeus.cancelEdit').click()
    await expect(page.getByTestId('oo.1.opiskeluoikeus.edit')).toBeVisible()
    await expect(vanhanMuokkaa).toBeVisible()
    await expect(lisääOpiskeluoikeus).not.toHaveClass(/disabled/)

    await vanhanMuokkaa.click()
    await expect(page).toHaveURL(/edit=/)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(0)
    await expect(page.getByTestId('oo.1.opiskeluoikeus.edit')).toHaveCount(0)
  })

  test('Uuden käyttöliittymän tallentamattomat muutokset säilyvät vanhan käyttöliittymän navigoinnissa saman välilehden sisällä', async ({
    page
  }) => {
    const kysymykset: string[] = []
    page.on('dialog', (dialog) => {
      kysymykset.push(dialog.message())
      return dialog.dismiss()
    })

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await lisätieto(page).fill('Tallentamaton muutos')
    await vanhaOpiskeluoikeus(page).locator('a', { hasText: 'Lisätiedot' }).click()

    await expect.poll(() => osoite(page)).toContain('lisätiedot-expanded=true')
    await expect(lisätieto(page)).toHaveValue('Tallentamaton muutos')
    expect(kysymykset).toHaveLength(0)
  })

  test('Välilehden vaihto kysyy ennen kuin uuden käyttöliittymän tallentamattomat muutokset hylätään', async ({
    page
  }) => {
    const välilehti = page
      .getByTestId('opiskeluoikeustyyppi-esiopetus')
      .locator('a')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await lisätieto(page).fill('Tallentamaton muutos')

    const kysymykset: string[] = []
    page.once('dialog', (dialog) => {
      kysymykset.push(dialog.message())
      return dialog.dismiss()
    })
    await välilehti.click()
    await expect.poll(() => kysymykset).toHaveLength(1)
    expect(kysymykset[0]).toContain('Tallentamattomat muutokset menetetään')
    expect(osoite(page)).toContain('opiskeluoikeudenTyyppi=perusopetus')
    await expect(lisätieto(page)).toHaveValue('Tallentamaton muutos')

    page.once('dialog', (dialog) => dialog.accept())
    await välilehti.click()
    await expect.poll(() => osoite(page)).toContain('opiskeluoikeudenTyyppi=esiopetus')
    await expect(lisätieto(page)).toHaveCount(0)
  })

  test('Vanhan käyttöliittymän version sulkeminen kysyy ennen kuin uuden käyttöliittymän tallentamattomat muutokset hylätään', async ({
    page
  }) => {
    const vanha = vanhaOpiskeluoikeus(page)
    await vanha.locator('.versiohistoria > a').click()
    await vanha.locator('.versiohistoria .modal a').filter({ hasText: 'v1' }).click()
    await expect.poll(() => osoite(page)).toContain('versionumero=1')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await lisätieto(page).fill('Tallentamaton muutos')

    const kysymykset: string[] = []
    page.once('dialog', (dialog) => {
      kysymykset.push(dialog.message())
      return dialog.dismiss()
    })
    await vanha.locator('.versiohistoria > a').click()
    await expect.poll(() => kysymykset).toHaveLength(1)
    expect(osoite(page)).toContain('versionumero=1')
    await expect(lisätieto(page)).toHaveValue('Tallentamaton muutos')
  })

  test('Vanhan käyttöliittymän varoitus säilyy saman välilehden navigoinnissa', async ({
    page
  }) => {
    const vanha = vanhaOpiskeluoikeus(page)
    await vanha.locator('.toggle-edit').click()
    await vanha
      .locator(
        '.property.todistuksellaNäkyvätLisätiedot textarea, .property.todistuksellaNäkyvätLisätiedot input'
      )
      .first()
      .fill('Tallentamaton muutos')
    await expect(page.locator('#edit-bar-wrapper.dirty')).toBeVisible()

    const kysymykset: string[] = []
    page.on('dialog', (dialog) => {
      kysymykset.push(dialog.message())
      return dialog.dismiss()
    })
    await vanha.locator('a', { hasText: 'Lisätiedot' }).click()
    await expect.poll(() => osoite(page)).toContain('lisätiedot-expanded=true')
    expect(kysymykset).toHaveLength(0)

    await page
      .getByTestId('opiskeluoikeustyyppi-esiopetus')
      .locator('a')
      .click()
    await expect.poll(() => kysymykset).toHaveLength(1)
    expect(osoite(page)).toContain('opiskeluoikeudenTyyppi=perusopetus')
  })
})

// Mitätöinti, päätason suorituksen poisto ja lähdejärjestelmäkytkennän purku
// lataavat sivun uudelleen, joten muokkauksen aikana ne tarjotaan vain
// muokattavalla opiskeluoikeudella.
test.describe('Mitätöinti, suorituksen poisto ja lähdejärjestelmäkytkennän purku muokkauksen aikana', () => {
  const opiskeluoikeus = (page: Page, oppilaitos: string) =>
    page
      .locator('[data-testid="opiskeluoikeuksientiedot"] > li')
      .filter({ hasText: oppilaitos })
  const vanhaMitätöinti = (kohde: Page | Locator) =>
    kohde.locator('.invalidate.invalidate-opiskeluoikeus')
  const uusiMitätöinti = (kohde: Page | Locator) =>
    kohde.getByTestId(/^oo\.\d+\.opiskeluoikeus\.invalidate\.button$/)

  test.describe('Tallentaja', () => {
    test.use({ storageState: virkailija('kalle') })

    test('Mitätöinti tarjotaan vain muokattavalla opiskeluoikeudella', async ({
      page,
      fixtures,
      oppijaPage
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(moniaUrl)
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible()
      await expect(uusiMitätöinti(page)).toHaveCount(0)
      await expect(vanhaMitätöinti(page)).toHaveCount(0)

      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.invalidate.button')
      ).toBeVisible()
      await expect(uusiMitätöinti(page)).toHaveCount(1)
      await expect(vanhaMitätöinti(page)).toHaveCount(0)

      await page.getByTestId('oo.0.opiskeluoikeus.cancelEdit').click()
      await page.locator('.toggle-edit').click()
      await expect(vanhaMitätöinti(page)).toBeVisible()
      await expect(uusiMitätöinti(page)).toHaveCount(0)
    })
  })

  test.describe('Pääkäyttäjä', () => {
    test.use({ storageState: virkailija('pää') })

    test('Mitätöintioikeudellisen katselutilan mitätöinti piilotetaan toisen opiskeluoikeuden muokkauksen ajaksi', async ({
      page,
      fixtures,
      oppijaPage
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(moniaUrl)
      await expect(uusiMitätöinti(page)).toHaveCount(2)
      await expect(vanhaMitätöinti(page)).toBeVisible()
      await expect(page.locator('.toggle-edit')).toBeVisible()

      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.invalidate.button')
      ).toBeVisible()
      await expect(uusiMitätöinti(page)).toHaveCount(1)
      await expect(vanhaMitätöinti(page)).toHaveCount(0)

      await page.getByTestId('oo.0.opiskeluoikeus.cancelEdit').click()
      await expect(uusiMitätöinti(page)).toHaveCount(2)
      await expect(vanhaMitätöinti(page)).toBeVisible()
    })

    test('Katselutilan toiminnot piilotetaan toisen opiskeluoikeuden muokkauksen ajaksi', async ({
      page,
      fixtures,
      oppijaPage
    }) => {
      await fixtures.reset()
      await fixtures.putOppija(käyttöliittymästäTallennettuPerusopetus(perttiOid))
      await oppijaPage.goto(`${perttiOid}?opiskeluoikeudenTyyppi=perusopetus`)

      // Lähdejärjestelmästä siirrettyä opiskeluoikeutta ei voi muokata, joten
      // sen toiminnot ovat katselutilassa.
      const siirretty = opiskeluoikeus(page, 'Stadin ammatti- ja aikuisopisto')
      const tallennettu = page
        .locator('[data-testid="opiskeluoikeuksientiedot"] > li')
        .filter({ hasNotText: 'Stadin ammatti- ja aikuisopisto' })
      const katselutilanToiminnot = [
        uusiMitätöinti(siirretty),
        siirretty.getByTestId(/^oo\.\d+\.suoritukset\.\d+\.button$/),
        siirretty.getByTestId(/^oo\.\d+\.opiskeluoikeus\.puraKytkenta\.button$/)
      ]
      const expectKatselutilanToiminnot = async (näkyvissä: boolean) => {
        for (const toiminto of katselutilanToiminnot) {
          await expect(toiminto).toHaveCount(näkyvissä ? 1 : 0)
        }
      }
      await expectKatselutilanToiminnot(true)

      await tallennettu.getByTestId(/^oo\.\d+\.opiskeluoikeus\.edit$/).click()
      await expect(uusiMitätöinti(tallennettu)).toBeVisible()
      await expectKatselutilanToiminnot(false)

      await tallennettu
        .getByTestId(/^oo\.\d+\.opiskeluoikeus\.cancelEdit$/)
        .click()
      await expectKatselutilanToiminnot(true)
    })

    test('Vanhassa käyttöliittymässä mitätöinti ja suorituksen poisto piilotetaan muilta opiskeluoikeuksilta muokkauksen ajaksi', async ({
      page,
      fixtures,
      oppijaPage
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(
        `${pekkaOid}?opiskeluoikeudenTyyppi=ammatillinenkoulutus`
      )
      await expect(page.locator('.toggle-edit')).toHaveCount(3)
      await expect(vanhaMitätöinti(page)).toHaveCount(3)

      // Stadin oppisopimuskeskuksen opiskeluoikeudella on kaksi päätason
      // suoritusta, joten sillä voisi näkyä myös suorituksen poisto.
      const muokattava = opiskeluoikeus(page, 'Omnia')
      await muokattava.locator('.toggle-edit').click()
      await expect(vanhaMitätöinti(muokattava)).toBeVisible()
      await expect(vanhaMitätöinti(page)).toHaveCount(1)
      await expect(
        page.locator('.invalidate.delete-paatason-suoritus')
      ).toHaveCount(0)
    })
  })
})

// Käyttäjä voi nähdä opiskeluoikeuden, jota hän ei saa muokata, vaikka hänellä
// on muokkausoikeus toisessa oppilaitoksessa.
test.describe('Muokkausoikeus on opiskeluoikeuskohtainen', () => {
  test.use({ storageState: virkailija('hki-tallentaja') })

  test('Muokkaa ei näy toisen oppilaitoksen sisältyvällä opiskeluoikeudella', async ({
    browser,
    page,
    fixtures,
    oppijaPage
  }, testInfo) => {
    await fixtures.reset()

    // Miia Monikoululaisen Jyväskylän normaalikoulun opiskeluoikeus merkitään
    // sisältyväksi Kulosaaren ala-asteen opiskeluoikeuteen. Helsingin kaupungin
    // tallentaja näkee sen silloin, mutta ei saa muokata sitä.
    const pääkäyttäjä = await browser.newContext({
      storageState: await getVirkailijaSession(testInfo, 'pää', 'pää')
    })
    const haku = await pääkäyttäjä.request.get(`/koski/api/oppija/${miiaOid}`)
    const miia = (await haku.json()) as {
      opiskeluoikeudet: Array<{
        oid: string
        oppilaitos: { oid: string; nimi: { fi: string } }
      }>
    }
    const tallennettu = (oppilaitos: string) =>
      miia.opiskeluoikeudet.find((oo) => oo.oppilaitos.nimi.fi === oppilaitos)!
    const kulosaari = tallennettu('Kulosaaren ala-aste')
    const tallennus = await pääkäyttäjä.request.put('/koski/api/oppija', {
      data: {
        henkilö: { oid: miiaOid },
        opiskeluoikeudet: [
          {
            ...tallennettu('Jyväskylän normaalikoulu'),
            sisältyyOpiskeluoikeuteen: {
              oppilaitos: { oid: kulosaari.oppilaitos.oid },
              oid: kulosaari.oid
            }
          }
        ]
      }
    })
    expect(tallennus.ok(), await tallennus.text()).toBeTruthy()
    await pääkäyttäjä.close()

    await oppijaPage.goto(`${miiaOid}?opiskeluoikeudenTyyppi=perusopetus`)
    const muokkaa = (oppilaitos: string) =>
      page
        .locator('[data-testid="opiskeluoikeuksientiedot"] > li')
        .filter({
          has: page
            .getByTestId(/^oo\.\d+\.opiskeluoikeus\.nimi$/)
            .filter({ hasText: oppilaitos })
        })
        .getByTestId(/^oo\.\d+\.opiskeluoikeus\.edit$/)
    await expect(muokkaa('Kulosaaren ala-aste')).toBeVisible()
    await expect(muokkaa('Jyväskylän normaalikoulu')).toHaveCount(0)
  })
})

// Miia Monikoululainen: perusopetuksen opiskeluoikeus sekä Jyväskylän
// normaalikoulussa että Kulosaaren ala-asteella (Helsingin kaupunki).
const miiaOid = '1.2.246.562.24.00000000012'

// Pertti Perusopetuksensiirto: lähdejärjestelmästä siirretty perusopetuksen
// opiskeluoikeus Stadin ammatti- ja aikuisopistossa.
const perttiOid = '1.2.246.562.24.00000000059'

// Paallekkaisia Pekka: kolme ammatillisen koulutuksen opiskeluoikeutta, jotka
// kaikki näkyvät vanhalla käyttöliittymällä.
const pekkaOid = '1.2.246.562.24.00000000104'

const jynNimi = 'Jyväskylän normaalikoulu'
const jynOppilaitos = {
  oid: '1.2.246.562.10.14613773812',
  nimi: { fi: jynNimi, sv: jynNimi, en: jynNimi }
}

const käyttöliittymästäTallennettuPerusopetus = (
  oppijaOid: string
): Raw<Oppija> => ({
  henkilö: { oid: oppijaOid },
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
            tunniste: { koodiarvo: '201101', koodistoUri: 'koulutus' },
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
