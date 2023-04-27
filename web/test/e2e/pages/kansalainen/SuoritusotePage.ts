import { Page } from '@playwright/test'
import { build, BuiltIdNode } from '../oppija/uiV2builder/builder'
import { JsonSuoritusotePage } from './JsonSuoritusotePage'

export class SuoritusotePage {
  $: BuiltIdNode<SuoritusoteUIV2TestIds>

  constructor(private readonly page: Page) {
    this.$ = build(page, SuoritusoteUIV2TestIds)
  }

  static create(page: Page) {
    return new this(page)
  }

  async avaaJsonSuoritusotePage() {
    const jsonSuoritusotePagePromise = this.page.waitForEvent('popup')
    await this.page
      .getByRole('link', { name: 'Tiedot koneluettavassa muodossa' })
      .click()
    return new JsonSuoritusotePage(await jsonSuoritusotePagePromise)
  }
}

type SuoritusoteUIV2TestIds = typeof SuoritusoteUIV2TestIds
const SuoritusoteUIV2TestIds = {}
