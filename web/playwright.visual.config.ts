import type { PlaywrightTestConfig } from '@playwright/test'
import baseConfig from './playwright.config'

/**
 * Konfiguraatio, joka ajaa VAIN visuaaliset regressiotestit
 * (`*.visual.spec.ts`). Tätä käyttävät sekä paikallinen ajo että CI:
 *
 *   make visual-test     # vertaa nykytilaa baseline-kuviin
 *   make visual-update   # luo/päivittää baseline-kuvat
 *
 * Molemmat menevät `scripts/koski-visual.sh`in kautta, joka ajaa Playwrightin
 * tällä konfiguraatiolla pinnatussa Linux-kontissa. CI ajaa saman skriptin
 * KoskiVisualFrontSpecistä. Älä siis kutsu `pnpm run playwright:visual`ia
 * suoraan.
 * Sovelluksen (backend) tulee olla käynnissä osoitteessa BACKEND_HOST
 * (oletus http://localhost:7021) kuvia generoitaessa.
 */
const config: PlaywrightTestConfig = {
  ...baseConfig,
  // Oletuskonfiguraatio jättää visuaalitestit pois; tässä ne ovat nimenomaan
  // se mitä ajetaan.
  testIgnore: undefined,
  testMatch: '**/*.visual.spec.ts',
  use: {
    ...baseConfig.use,
    // Kuvakaappauksen oma tyylitiedosto ei muuten läpäise Kosken CSP:tä.
    // Ohitus rajataan visuaalitesteihin, jotta muut e2e-testit käyttävät oikeaa CSP:tä.
    bypassCSP: true
  },
  // Pitkien sivujen valmistelu ja kaappaus eivät aina mahdu 30 sekuntiin.
  timeout: 60 * 1000
}

export default config
