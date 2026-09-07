import { Locator } from '@playwright/test'
import { expect } from '../../../base'

export class KorkeakouluOpiskeluoikeus {
  readonly container: Locator
  readonly tutkinnonNimi: Locator
  readonly ensimmäisenTutkinnonOsanNimi: Locator
  readonly ensimmäisenOpintojaksonPainike: Locator
  readonly ensimmäisenOpintojaksonLisätiedot: Locator
  readonly lisätiedot: Locator
  readonly maksettavatLukuvuosimaksut: Locator
  readonly maksunSumma: Locator

  constructor(container: Locator) {
    this.container = container
    this.tutkinnonNimi = this.container.locator('.koulutusmoduuli .tunniste')
    this.ensimmäisenTutkinnonOsanNimi = this.container
      .locator('.tutkinnon-osa')
      .first()
      .locator('.suoritus .nimi')
      .first()
    this.ensimmäisenOpintojaksonPainike = this.container
      .locator('.omattiedot-suoritus-taulukko .suoritus button')
      .first()
    this.ensimmäisenOpintojaksonLisätiedot = this.container
      .locator('.omattiedot-suoritus-taulukko .details')
      .first()
    this.lisätiedot = this.container.getByTestId('lisätiedot')
    this.maksettavatLukuvuosimaksut = this.lisätiedot.getByTestId(
      'maksettavatLukuvuosimaksut-value'
    )
    this.maksunSumma =
      this.maksettavatLukuvuosimaksut.getByTestId('summa-value')
  }

  async avaaKaikki(): Promise<void> {
    await this.container
      .getByRole('button', { name: 'Avaa kaikki', exact: true })
      .click()
  }

  async avaaEnsimmäinenOpintojakso(): Promise<void> {
    await this.ensimmäisenOpintojaksonPainike.click()
  }

  async avaaLisätiedot(): Promise<void> {
    const sisältö = this.lisätiedot.getByTestId('lisätiedot-content')
    if (!(await sisältö.isVisible())) {
      await this.lisätiedot.getByText('Lisätiedot', { exact: true }).click()
    }
    await expect(sisältö).toBeVisible()
  }
}
