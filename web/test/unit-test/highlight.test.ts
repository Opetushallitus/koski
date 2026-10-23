import { describe, expect, it } from 'vitest'

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
    expect(
      findHighlights('ain lukio', 'Lukioaineopiskelija, Aino')
    ).toStrictEqual(['Lukio', 'Ain'])
  })

  it('splits words where the OpenSearch icu_tokenizer does', () => {
    expect(
      findHighlights('petteri luc', 'Jorma-Petteri Jean.Luc (080154-770R)')
    ).toStrictEqual(['Petteri'])
  })

  it('joins whole words matched one after another', () => {
    expect(
      findHighlights('010101-123n', 'Esimerkki, Eero (010101-123N)')
    ).toStrictEqual(['010101-123N'])
    expect(
      findHighlights('kaisa koululainen', 'Koululainen, Kaisa')
    ).toStrictEqual(['Koululainen, Kaisa'])
    expect(
      findHighlights('koululainen kai', 'Koululainen, Kaisa')
    ).toStrictEqual(['Koululainen, Kai'])
  })

  it('highlights the longest query word that is a prefix of a word', () => {
    expect(findHighlights('ee eero', 'Eero')).toStrictEqual(['Eero'])
  })

  it('ignores case and accents', () => {
    expect(findHighlights('eero', 'Eéro')).toStrictEqual(['Eéro'])
  })

  it('keeps å, ä and ö distinct from a and o', () => {
    expect(
      findHighlights('asa aanekoski oljy', 'Åsa Äänekoski Öljy')
    ).toStrictEqual([])
    expect(findHighlights('ås ään öl', 'Åsa Äänekoski Öljy')).toStrictEqual([
      'Ås',
      'Ään',
      'Öl'
    ])
  })

  it('folds æ, ø, ü, þ and ı like OpenSearch does', () => {
    expect(
      findHighlights('aebeltoft soren muller', 'Æbeltoft Søren Müller')
    ).toStrictEqual(['Æbeltoft Søren Müller'])
    expect(findHighlights('thor yildiz', 'Þór Yıldız')).toStrictEqual([
      'Þór Yıldız'
    ])
  })

  it('removes combining marks like OpenSearch does, also from a decomposed å, ä or ö', () => {
    expect(findHighlights('muller', 'Mu\u0308ller')).toStrictEqual([
      'Mu\u0308ller'
    ])
    expect(findHighlights('an as', 'A\u0308nes A\u030asa')).toStrictEqual([
      'A\u0308n',
      'A\u030as'
    ])
    expect(findHighlights('än ås', 'A\u0308nes A\u030asa')).toStrictEqual([])
    expect(findHighlights('a\u0308n a\u030as', 'Änes Åsa')).toStrictEqual([])
  })

  it('matches a letter that equals two', () => {
    expect(findHighlights('strass', 'Straße')).toStrictEqual(['Straß'])
  })
})

describe('createTextPrefixMatcher', () => {
  it('matches the query only as a prefix of the whole text', () => {
    expect(findTextPrefixHighlights('9', '9C9')).toStrictEqual(['9'])
    expect(findTextPrefixHighlights('c', '9C')).toStrictEqual([])
  })

  it('ignores case and surrounding whitespace of the query', () => {
    expect(findTextPrefixHighlights(' 9c ', '9C')).toStrictEqual(['9C'])
  })

  it('does not ignore accents, as the index does not fold luokka', () => {
    expect(findTextPrefixHighlights('e', 'É')).toStrictEqual([])
  })

  it('matches nothing with a blank query', () => {
    expect(findTextPrefixHighlights(' ', '9C')).toStrictEqual([])
  })
})
