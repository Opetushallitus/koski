import { expect, test } from '../e2e/base'
import { virkailija } from '../e2e/setup/auth'
import { replaceScreenshotData } from '../e2e/fragments/screenshotDataReplacements'
import path from 'node:path'
import fs from 'node:fs/promises'

/**
 * Ei regressiotesti - vertailutyökalu TOR-2777:n lukio 2019 -v2-editorin
 * rakentamisen ajalle. Ottaa kuvan vanhasta ja uudesta käyttöliittymästä
 * samasta oppijasta ja käyttää Playwrightin toHaveScreenshot-vertailua
 * niiden välillä, vaikka eroa ei ole tarkoitus korjata - lopputulos EPÄONNISTUU
 * aina tarkoituksella, ja raportin rinnakkaisnäkymä (actual / expected / diff)
 * on se, mitä varten tämä on olemassa.
 *
 * EI osa `make test`/`make visual-test`/CI:tä - ks. playwright.lukio-
 * vertailu.config.ts (eri testDir kuin tavalliset tai visuaaliset testit).
 * Aja: make lukio-vertailu, avaa sitten tulostettu HTML-raportti.
 */
const ullaUusilukio = '1.2.246.562.24.00000000015' // 250605A518Y

const screenshotStylePath = path.join(
  __dirname,
  '../e2e/fragments/fullPageScreenshot.css'
)

// Raaka page.screenshot() ei odota sisällön vakautumista kuten
// toHaveScreenshot tekee - ilman tätä "vanha"-kuva saatetaan ottaa ennen kuin
// suoritustiedot ovat ehtineet renderöityä, jolloin sivu on vielä lyhyt.
const waitForContent = async (page: import('@playwright/test').Page) => {
  await page.getByTestId('opiskeluoikeustyyppi-lukiokoulutus').waitFor()
  await page.waitForLoadState('networkidle')
  await page.evaluate(
    () =>
      new Promise<void>((resolve) =>
        requestAnimationFrame(() => requestAnimationFrame(() => resolve()))
      )
  )
}

test.describe('Lukio 2019: vanha vs. uusi käyttöliittymä', () => {
  test.use({ storageState: virkailija('pää') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Lukion oppimäärä, katselunäkymä', async ({
    page,
    oppijaPageV2
  }, testInfo) => {
    const snapshotName = 'lukio2019-vanha-vs-uusi.png'
    const snapshotPath = testInfo.snapshotPath(snapshotName)

    await oppijaPageV2.goto(ullaUusilukio)
    await waitForContent(page)
    const { restore: restoreVanha } = await replaceScreenshotData(page)
    await page.addStyleTag({ path: screenshotStylePath })
    const vanha = await page.screenshot({ fullPage: true })
    await restoreVanha()

    await fs.mkdir(path.dirname(snapshotPath), { recursive: true })
    await fs.writeFile(snapshotPath, vanha)

    await oppijaPageV2.gotoWithQueryParams(ullaUusilukio, {
      'lukio-2019-v2': '1'
    })
    await waitForContent(page)
    const { restore: restoreUusi } = await replaceScreenshotData(page)
    await page.addStyleTag({ path: screenshotStylePath })

    // Epäonnistuu aina tarkoituksella - ks. tiedoston yläreunan selite.
    await expect(page).toHaveScreenshot(snapshotName, {
      fullPage: true,
      timeout: 15_000
    })
    await restoreUusi()
  })
})
