import { Page } from '@playwright/test'
import { build, BuiltIdNode } from '../oppija/uiV2builder/builder'

export class JsonSuoritusotePage {
  $: BuiltIdNode<JsonSuoritusoteUIV2TestIds>

  constructor(private readonly page: Page) {
    this.$ = build(page, JsonSuoritusoteUIV2TestIds)
  }

  static create(page: Page) {
    return new this(page)
  }

  async jsonContent() {
    return await this.page.innerText('body')
  }
}

type JsonSuoritusoteUIV2TestIds = typeof JsonSuoritusoteUIV2TestIds
const JsonSuoritusoteUIV2TestIds = {}
