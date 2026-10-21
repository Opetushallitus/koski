import * as assert from 'assert'
import {
  createTextPrefixMatcher,
  createWordPrefixMatcher
} from '../../app/components/Highlight'
import { Chunk } from 'react-highlight-words'

const highlightsOf = (chunks: Chunk[], text: string) =>
  chunks.map(({ start, end }) => text.slice(start, end))

const findHighlights = (searchQuery: string, text: string) =>
  highlightsOf(createWordPrefixMatcher(searchQuery)(text), text)

const findTextPrefixHighlights = (searchQuery: string, text: string) =>
  highlightsOf(createTextPrefixMatcher(searchQuery)(text), text)

describe('createWordPrefixMatcher', () => {
  it('matches each word of the query only as a prefix of a word', () => {
    assert.deepEqual(findHighlights('ain lukio', 'Lukioaineopiskelija, Aino'), [
      'Lukio',
      'Ain'
    ])
  })

  it('splits words where the OpenSearch icu_tokenizer does', () => {
    assert.deepEqual(
      findHighlights('petteri luc', 'Jorma-Petteri Jean.Luc (080154-770R)'),
      ['Petteri']
    )
  })

  it('joins whole words matched one after another', () => {
    assert.deepEqual(
      findHighlights('010101-123n', 'Esimerkki, Eero (010101-123N)'),
      ['010101-123N']
    )
    assert.deepEqual(findHighlights('kaisa koululainen', 'Koululainen, Kaisa'), [
      'Koululainen, Kaisa'
    ])
    assert.deepEqual(findHighlights('koululainen kai', 'Koululainen, Kaisa'), [
      'Koululainen, Kai'
    ])
  })

  it('highlights the longest query word that is a prefix of a word', () => {
    assert.deepEqual(findHighlights('ee eero', 'Eero'), ['Eero'])
  })

  it('ignores case and accents', () => {
    assert.deepEqual(findHighlights('eero', 'Eéro'), ['Eéro'])
  })

  it('keeps å, ä and ö distinct from a and o', () => {
    assert.deepEqual(findHighlights('asa aanekoski oljy', 'Åsa Äänekoski Öljy'), [])
    assert.deepEqual(findHighlights('ås ään öl', 'Åsa Äänekoski Öljy'), [
      'Ås',
      'Ään',
      'Öl'
    ])
  })

  it('folds æ, ø, ü, þ and ı like OpenSearch does', () => {
    assert.deepEqual(
      findHighlights('aebeltoft soren muller', 'Æbeltoft Søren Müller'),
      ['Æbeltoft Søren Müller']
    )
    assert.deepEqual(findHighlights('thor yildiz', 'Þór Yıldız'), [
      'Þór Yıldız'
    ])
  })

  it('removes combining marks like OpenSearch does, also from a decomposed å, ä or ö', () => {
    assert.deepEqual(findHighlights('muller', 'Mu\u0308ller'), ['Mu\u0308ller'])
    assert.deepEqual(findHighlights('an as', 'A\u0308nes A\u030asa'), [
      'A\u0308n',
      'A\u030as'
    ])
    assert.deepEqual(findHighlights('än ås', 'A\u0308nes A\u030asa'), [])
    assert.deepEqual(findHighlights('a\u0308n a\u030as', 'Änes Åsa'), [])
  })

  it('matches a letter that equals two', () => {
    assert.deepEqual(findHighlights('strass', 'Straße'), ['Straß'])
  })
})

describe('createTextPrefixMatcher', () => {
  it('matches the query only as a prefix of the whole text', () => {
    assert.deepEqual(findTextPrefixHighlights('9', '9C9'), ['9'])
    assert.deepEqual(findTextPrefixHighlights('c', '9C'), [])
  })

  it('ignores case and surrounding whitespace of the query', () => {
    assert.deepEqual(findTextPrefixHighlights(' 9c ', '9C'), ['9C'])
  })

  it('does not ignore accents, as the index does not fold luokka', () => {
    assert.deepEqual(findTextPrefixHighlights('e', 'É'), [])
  })

  it('matches nothing with a blank query', () => {
    assert.deepEqual(findTextPrefixHighlights(' ', '9C'), [])
  })
})
