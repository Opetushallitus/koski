import * as assert from 'assert'
import { foldFinnish } from '../../app/components/Highlight'

describe('foldFinnish', () => {
  it('folds like the perustiedot OpenSearch index', () => {
    // Expected value is the token output of the index's default analyzer
    assert.equal(
      foldFinnish('Søren Straße Łódź Æbeltoft Eéro Äänekoski Åsa Öljy ﬁ Ｆｕｌｌ'),
      'soren strasse lodz aebeltoft eero äänekoski åsa öljy fi full'
    )
  })

  it('folds a decomposed ä to a like OpenSearch', () => {
    assert.equal(foldFinnish('Ä'.normalize('NFD')), 'a')
  })
})
