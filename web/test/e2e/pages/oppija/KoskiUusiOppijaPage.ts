import { expect, Locator, Page } from '@playwright/test'
import { KoskiOppijaPageV2 } from './KoskiOppijaPageV2'
import { BuiltIdNode } from './uiV2builder/builder'
import { Button } from './uiV2builder/Button'
import { Checkbox } from './uiV2builder/Checkbox'
import { Input } from './uiV2builder/Input'
import { Select } from './uiV2builder/Select'

interface BaseOppija {
  etunimet: string
  sukunimi: string
  hetu: string
  oppilaitos: string
  aloituspäivä: Date
  opiskeluoikeus: string
  suorituskieli: string
  opiskeluoikeudenTila: string
  opintokokonaisuus?: string
  osaamismerkki?: string
  oppimäärä?: string
  opintojenMaksuttomuus?: string
  peruste?: string
  rahoitus?: string
  suoritustyyppi?: string
  taiteenala?: string
  jotpaAsianumero?: string
}

type ESHOppija =
  | BaseOppija
  | (BaseOppija & {
      opiskeluoikeus: 'European School of Helsinki'
      curriculum: string
      luokkaAste: string
    })

type Oppija = ESHOppija

export class KoskiUusiOppijaPage extends KoskiOppijaPageV2<
  typeof UusiOpiskeluoikeusFormTestIds
> {
  readonly page: Page
  readonly lisaaOppijaButton: Locator
  readonly controls: BuiltIdNode<typeof UusiOpiskeluoikeusFormControls>

  constructor(page: Page) {
    super(page, UusiOpiskeluoikeusFormTestIds)

    this.page = page
    this.controls = this.$.uusiOpiskeluoikeus.modal
    this.lisaaOppijaButton = page.getByLabel('Tunnus')
  }

  async goTo(hetu: Oppija['hetu']) {
    const queryParams = new URLSearchParams({
      hetu
    })
    await this.page.goto(`/koski/uusioppija#${queryParams.toString()}`)
    await expect(this.page).toHaveURL(/\/koski\/uusioppija#hetu=.+/)
  }

  /**
   * Täyttää oppijan tiedot Uuden opiskelijan lisäys -näkymään.
   *
   * **Huom.! Testikoodi ei tarkista, löytyykö mm. perustetta, opintokokonaisuutta tai muita opiskeluoikeus-spesifisiä kenttiä, ennen kuin niitä aletaan täyttämään.**
   * Testaa ensin luoda käyttöliittymän kautta opiskeluoikeus ja tarkista, mitkä kentät käyttöliittymässä näkyvät.
   * @param oppija Oppijan tiedot
   */
  async fill(oppija: Partial<BaseOppija> & { hankintakoulutus?: 'tpo' }) {
    const $ = this.$.uusiOpiskeluoikeus.modal

    if (oppija.hankintakoulutus === 'tpo') {
      await $.hankintakoulutus.tpo.click()
    }
    if (oppija.etunimet) {
      await this.$.uusiOpiskeluoikeus.oppija.etunimet.set(oppija.etunimet)
    }
    if (oppija.sukunimi) {
      await this.$.uusiOpiskeluoikeus.oppija.sukunimi.set(oppija.sukunimi)
    }

    if (oppija.oppilaitos) {
      if (oppija.hankintakoulutus) {
        await $.oppilaitos.type(oppija.oppilaitos)
        await $.oppilaitos.selectFirstOption()
      } else {
        await $.oppilaitos.setByLabel(oppija.oppilaitos)
      }
    }
    if (oppija.opiskeluoikeus) {
      await $.opiskeluoikeus.setByLabel(oppija.opiskeluoikeus)
    }
    if (oppija.suorituskieli) {
      await $.suorituskieli.setByLabel(oppija.suorituskieli)
    }
    if (oppija.opintokokonaisuus) {
      await $.opintokokonaisuus.setByLabel(oppija.opintokokonaisuus)
    }
    // TODO: Aloituspäivä
    // if (oppija.aloituspäivä) {
    //  ...
    // }
    if (oppija.oppimäärä) {
      await $.oppimäärä.setByLabel(oppija.oppimäärä)
    }
    // if (oppija.peruste) {
    //   await this.peruste.selectOptionByClick(oppija.peruste)
    // }
    // if (oppija.rahoitus) {
    //   await this.opintojenRahoitus.selectOptionByClick(oppija.rahoitus)
    // }
    // if (oppija.opintojenMaksuttomuus) {
    //   await this.maksuttomuus.selectOptionByLabel(oppija.opintojenMaksuttomuus)
    // }
    if (oppija.suoritustyyppi) {
      await $.suoritustyyppi.setByLabel(oppija.suoritustyyppi)
    }
    if (oppija.taiteenala) {
      console.log('oppija.taiteenala', oppija.taiteenala)
      await $.taiteenala.setByLabel(oppija.taiteenala)
    }
    // if (oppija.osaamismerkki) {
    //   await this.osaamismerkki.search(oppija.osaamismerkki)
    // }
    // if (oppija.opiskeluoikeudenTila) {
    //   await this.opiskeluoikeudenTila.selectOptionByClick(
    //     oppija.opiskeluoikeudenTila
    //   )
    // }
    if (oppija.jotpaAsianumero) {
      await $.jotpaAsianumero.setByLabel(oppija.jotpaAsianumero)
    }
  }

  async submitAndExpectSuccess() {
    const submitBtn = this.$.uusiOpiskeluoikeus.modal.submit.button
    await expect(submitBtn).toBeEnabled()
    await submitBtn.click()
    await expect(this.page.getByTestId('error')).not.toBeVisible()
    await expect(this.page).toHaveURL(/\/koski\/oppija\/1.2.246.562.24.\d+.*/)
  }
}

const HankintakoulutusControls = {
  esiopetus: Checkbox,
  tpo: Checkbox
}

const UusiOpiskeluoikeusFormControls = {
  hankintakoulutus: HankintakoulutusControls,
  oppilaitos: Select,
  opiskeluoikeus: Select,
  oppimäärä: Select,
  suoritustyyppi: Select,
  peruste: Select,
  taiteenala: Select,
  opintokokonaisuus: Select,
  suorituskieli: Select,
  tila: Select,
  opintojenRahoitus: Select,
  jotpaAsianumero: Select,
  submit: Button
}

const UusiOppijaFormControls = {
  etunimet: Input,
  sukunimi: Input
}

const UusiOpiskeluoikeusFormTestIds = {
  uusiOpiskeluoikeus: {
    oppija: UusiOppijaFormControls,
    modal: UusiOpiskeluoikeusFormControls
  }
}
