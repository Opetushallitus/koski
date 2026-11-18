import type { PlaywrightTestConfig } from '@playwright/test'
import baseConfig from './playwright.config'

/**
 * Konfiguraatio lukio 2019 vanha-vs-uusi-vertailutyökalulle
 * (test/e2e-dev/lukio2019-vanha-vs-uusi.spec.ts). EI osa tavallista
 * (playwright.config.ts) eikä visuaalista (playwright.visual.config.ts)
 * testiajoa - oma testDir pitää sen tavallisen `playwright test`-haun ja
 * CI:n ulkopuolella, koska testi epäonnistuu tarkoituksella.
 *
 * Aja: make lukio-vertailu (ks. scripts/koski-lukio-vertailu.sh).
 */
const config: PlaywrightTestConfig = {
  ...baseConfig,
  testDir: './test/e2e-dev',
  testIgnore: undefined,
  // Ei pidä osua test/e2e/__screenshots__-hakemistoon (oikeat, versionhallinnassa
  // olevat regressiobaselinet) - tämän työkalun kuvat eivät ole baselineja ja
  // regeneroituvat joka ajolla. test-results/ on jo gitignoroitu.
  snapshotPathTemplate: './test-results/lukio-vertailu/{arg}{ext}',
  use: {
    ...baseConfig.use,
    // Kuvakaappauksen oma tyylitiedosto ei muuten läpäise Kosken CSP:tä.
    bypassCSP: true
  },
  timeout: 60 * 1000
}

export default config
