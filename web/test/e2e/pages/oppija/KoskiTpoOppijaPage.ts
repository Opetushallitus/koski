import { Page } from '@playwright/test'
import { arrayOf, build, BuiltIdNode } from './uiV2builder/builder'
import { Button, Label } from './uiV2builder/controls'

export class KoskiTpoOppijaPage {
  page: Page
  $: BuiltIdNode<typeof TaiteenPerusopetusTestIds>

  constructor(page: Page) {
    this.page = page
    this.$ = build(page, TaiteenPerusopetusTestIds)
  }
}

const TaiteenPerusopetusTestIds = {
  opiskeluoikeus: {
    nimi: Label,
    oid: Label,
    voimassaoloaika: Label,
    edit: Button,
    value: arrayOf({
      date: Label,
      tila: Label
    })
  },
  suoritukset: arrayOf({
    tab: Button,
    koulutuksenToteutustapa: {
      value: Label
    },
    laajuus: {
      value: Label
    },
    oppilaitos: {
      value: Label
    },
    oppimäärä: {
      value: Label
    },
    taiteenala: {
      value: Label
    },
    osasuoritukset: arrayOf({
      expand: Button,
      arvosana: {
        value: Label
      },
      laajuus: {
        value: Label
      },
      nimi: {
        value: Label
      }
    })
  })
}
