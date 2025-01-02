import { Page } from '@playwright/test'
import { KoskiOppijaPageV2 } from './KoskiOppijaPageV2'
import { arrayOf } from './uiV2builder/builder'
import { Button } from './uiV2builder/Button'
import { FormField } from './uiV2builder/controls'
import { Label } from './uiV2builder/Label'
import { Select } from './uiV2builder/Select'
import { OpiskeluoikeusHeader } from './uiV2builder/OpiskeluoikeusHeader'
import { SuorituksenVahvistus } from './uiV2builder/SuorituksenVahvistus'
import { Input } from './uiV2builder/Input'

export class KoskiIBOppijaPage extends KoskiOppijaPageV2<typeof IBTestIds> {
  constructor(page: Page) {
    super(page, IBTestIds)
  }
}

export const IBTestIds = {
  opiskeluoikeus: OpiskeluoikeusHeader(),
  suoritusTabs: arrayOf({ tab: Button }),
  suoritukset: arrayOf({
    koulutus: Label,
    organisaatio: FormField(Label, Select),
    suorituskieli: FormField(Label, Select),
    todistuksellaNäkyvätLisätiedot: FormField(Label, Input),

    suorituksenVahvistus: SuorituksenVahvistus(),

    osasuoritukset: arrayOf({})
  })
}
