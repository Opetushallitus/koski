import { Page } from '@playwright/test'
import { KoskiOppijaPageV2 } from './KoskiOppijaPageV2'
import { arrayOf } from './uiV2builder/builder'
import { Button } from './uiV2builder/Button'
import { FormField } from './uiV2builder/controls'
import { Input } from './uiV2builder/Input'
import { Label } from './uiV2builder/Label'
import { OpiskeluoikeusHeader } from './uiV2builder/OpiskeluoikeusHeader'
import { Select } from './uiV2builder/Select'
import { SuorituksenVahvistus } from './uiV2builder/SuorituksenVahvistus'

export class KoskiVSTOppijaPage extends KoskiOppijaPageV2<
  typeof VapaanSivistystyönTestIds
> {
  constructor(page: Page) {
    super(page, VapaanSivistystyönTestIds)
  }
}

const VapaanSivistystyönTestIds = {
  opiskeluoikeus: OpiskeluoikeusHeader(),
  suoritukset: arrayOf({
    tab: Button,
    oppilaitos: FormField(Label),
    koulutus: FormField(Label),
    opetuskieli: FormField(Label),
    lisatiedot: FormField(Label),

    suorituksenVahvistus: SuorituksenVahvistus(),
    expand: Button,
    taso: arrayOf({
      osasuoritukset: arrayOf({
        expand: Button,
        arvosana: FormField(Label, Select),
        taitotaso: FormField(Label, Select),
        laajuus: FormField(Label, Input),
        nimi: FormField(Label),
        properties: {
          arvosana: FormField(Label),
          arvostelunPvm: FormField(Label),
          tunnustettu: FormField(Label)
        },
        delete: Button
      })
    }),

    addOsasuoritus: {
      select: Select,
      modal: {
        nimi: FormField(Input, Input),
        cancel: Button,
        submit: Button
      }
    }
  })
}
