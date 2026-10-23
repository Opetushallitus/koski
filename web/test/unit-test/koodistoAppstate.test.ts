import { describe, expect, it } from 'vitest'

import {
  uniqueKoodistoUris,
  uniqueKoodistot
} from '../../app/appstate/koodistoUtils'

describe('koodisto appstate', () => {
  it('poistaa duplikaatti-URIt säilyttäen järjestyksen', () => {
    expect(
      uniqueKoodistoUris([
        'arviointiasteikkoyleissivistava',
        'kielivalikoima',
        'arviointiasteikkoyleissivistava',
        null,
        undefined,
        'kielivalikoima'
      ])
    ).toStrictEqual(['arviointiasteikkoyleissivistava', 'kielivalikoima'])
  })

  it('poistaa duplikaattikoodit id:n perusteella säilyttäen ensimmäisen arvon', () => {
    const ensimmäinen = {
      id: 'arviointiasteikkoyleissivistava_10',
      label: '10'
    }
    const toinen = { id: 'arviointiasteikkoyleissivistava_10', label: '10b' }
    const kolmas = { id: 'arviointiasteikkoyleissivistava_9', label: '9' }

    expect(uniqueKoodistot([ensimmäinen, toinen, kolmas])).toStrictEqual([
      ensimmäinen,
      kolmas
    ])
  })
})
