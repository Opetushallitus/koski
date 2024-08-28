import { expect, Locator, Page } from '@playwright/test'
import { KoskiOppijaPageV2 } from './KoskiOppijaPageV2'
import { BuiltIdNode } from './uiV2builder/builder'
import { Button } from './uiV2builder/Button'
import { Checkbox } from './uiV2builder/Checkbox'
import { Input } from './uiV2builder/Input'
import { Select } from './uiV2builder/Select'
import { formatFinnishDate } from '../../../../app/date/date'
import { RadioButtons } from './uiV2builder/RadioButtons'
import { DateInput } from './uiV2builder/DateInput'

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
  opintojenMaksuttomuus?: 'eiOvlPiirissä' | 'maksuton' | 'maksullinen'
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
  async fill(
    oppija: Partial<BaseOppija> & { hankintakoulutus?: 'tpo' },
    pääkäyttäjä: boolean = false
  ) {
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
      if (oppija.hankintakoulutus || pääkäyttäjä) {
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
    if (oppija.aloituspäivä) {
      await $.aloituspäivä.set(formatFinnishDate(oppija.aloituspäivä)!)
    }
    if (oppija.oppimäärä) {
      await $.oppimäärä.setByLabel(oppija.oppimäärä)
    }
    if (oppija.peruste) {
      await $.peruste.setByLabel(oppija.peruste)
    }
    if (oppija.rahoitus) {
      await $.opintojenRahoitus.setByLabel(oppija.rahoitus)
    }
    if (oppija.opintojenMaksuttomuus) {
      await $.maksuton.set(oppija.opintojenMaksuttomuus)
    }
    if (oppija.suoritustyyppi) {
      await $.suoritustyyppi.setByLabel(oppija.suoritustyyppi)
    }
    if (oppija.taiteenala) {
      console.log('oppija.taiteenala', oppija.taiteenala)
      await $.taiteenala.setByLabel(oppija.taiteenala)
    }
    if (oppija.osaamismerkki) {
      await $.osaamismerkki.setByLabel(oppija.osaamismerkki)
    }
    if (oppija.opiskeluoikeudenTila) {
      await $.tila.setByLabel(oppija.opiskeluoikeudenTila)
    }
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
  osaamismerkki: Select,
  suorituskieli: Select,
  tila: Select,
  opintojenRahoitus: Select,
  jotpaAsianumero: Select,
  aloituspäivä: DateInput,
  maksuton: RadioButtons('eiOvlPiirissä', 'maksuton', 'maksullinen'),
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
