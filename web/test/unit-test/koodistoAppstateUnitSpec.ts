import * as assert from 'assert'
import {
  uniqueKoodistoUris,
  uniqueKoodistot
} from '../../app/appstate/koodistoUtils'

describe('koodisto appstate', () => {
  it('poistaa duplikaatti-URIt säilyttäen järjestyksen', () => {
    assert.deepStrictEqual(
      uniqueKoodistoUris([
        'arviointiasteikkoyleissivistava',
        'kielivalikoima',
        'arviointiasteikkoyleissivistava',
        null,
        undefined,
        'kielivalikoima'
      ]),
      ['arviointiasteikkoyleissivistava', 'kielivalikoima']
    )
  })

  it('poistaa duplikaattikoodit id:n perusteella säilyttäen ensimmäisen arvon', () => {
    const ensimmäinen = {
      id: 'arviointiasteikkoyleissivistava_10',
      label: '10'
    }
    const toinen = { id: 'arviointiasteikkoyleissivistava_10', label: '10b' }
    const kolmas = { id: 'arviointiasteikkoyleissivistava_9', label: '9' }

    assert.deepStrictEqual(uniqueKoodistot([ensimmäinen, toinen, kolmas]), [
      ensimmäinen,
      kolmas
    ])
  })
})
