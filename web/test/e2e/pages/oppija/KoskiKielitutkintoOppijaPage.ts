import { Page } from '@playwright/test'
import { KoskiOppijaPageV2 } from './KoskiOppijaPageV2'
import { arrayOf } from './uiV2builder/builder'
import { Button } from './uiV2builder/Button'

import { Label } from './uiV2builder/Label'
import { OpiskeluoikeusHeader } from './uiV2builder/OpiskeluoikeusHeader'
import { Select } from './uiV2builder/Select'
import { TodistusTemplateVariant } from '../../../../app/kielitutkinto/YleinenKielitutkintoTodistusLataus'

export class KoskiKielitutkintoOppijaPage extends KoskiOppijaPageV2<
  typeof KielitutkintoTestIds
> {
  constructor(page: Page) {
    super(page, KielitutkintoTestIds)
  }

  async setKielitutkintoTodistusLanguage(lang: TodistusTemplateVariant) {
    await this.$.suoritukset(0).kielitutkintoTodistus.language.set(lang)
  }

  async generateKielitutkintoTodistus() {
    await this.$.suoritukset(0).kielitutkintoTodistus.start.click()
    await this.$.suoritukset(0).kielitutkintoTodistus.loading.waitFor()
    await this.$.suoritukset(
      0
    ).kielitutkintoTodistus.loading.waitForToDisappear(10000)
  }

  async getKielitutkintoTodistusFile() {
    const downloadPromise = this.page.waitForEvent('download')
    await this.$.suoritukset(0).kielitutkintoTodistus.open.click()
    const download = await downloadPromise
    // Tarkista että lataus onnistui
    await download.path()
  }

  async getKielitutkintoTodistusError(): Promise<string> {
    return await this.$.suoritukset(0).kielitutkintoTodistus.error.value()
  }
}

const KielitutkintoTestIds = {
  opiskeluoikeus: OpiskeluoikeusHeader(),
  suoritusTabs: arrayOf({ tab: Button }),
  suoritukset: arrayOf({
    kielitutkintoTodistus: {
      language: Select,
      start: Button,
      loading: Label,
      error: Label,
      open: Button,
      openPreview: Button
    }
  })
}
