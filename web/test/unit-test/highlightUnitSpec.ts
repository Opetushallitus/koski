import React from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import * as assert from 'assert'
import { Highlight } from '../../app/components/Highlight'

const highlighted = (
  search: string,
  text: string | undefined,
  ignoreDiacritics?: boolean
) =>
  renderToStaticMarkup(
    React.createElement(Highlight, { search, ignoreDiacritics }, text)
  )
    .replace(/<\/?span[^>]*>/g, '')
    .replace(/<mark class="highlight[^"]*">([^<]*)<\/mark>/g, '[$1]')

describe('Highlight', () => {
  it('marks every match case-insensitively', () => {
    assert.equal(highlighted('KA', 'Kaisa Kankaanpää'), '[Ka]isa [Ka]n[ka]anpää')
  })

  it('matches special characters literally', () => {
    assert.equal(highlighted('a+(', 'a+(b) aa('), '[a+(]b) aa(')
  })

  it('renders text as such without a search', () => {
    assert.equal(highlighted('', 'Kaisa'), 'Kaisa')
    assert.equal(highlighted('kaisa', undefined), '')
  })

  it('matches diacritics exactly by default', () => {
    assert.equal(highlighted('eero', 'Eéro Eero'), 'Eéro [Eero]')
  })

  describe('ignoring diacritics', () => {
    it('folds other diacritics both ways', () => {
      assert.equal(highlighted('eero', 'Eéro', true), '[Eéro]')
      assert.equal(highlighted('éero', 'Eero', true), '[Eero]')
    })

    it('keeps å, ä and ö distinct as in Finnish', () => {
      assert.equal(highlighted('a', 'Åsa Äänekoski', true), 'Ås[a] Äänekoski')
      assert.equal(highlighted('ä', 'Kaisa Äänekoski', true), 'Kaisa [Ä][ä]nekoski')
      assert.equal(highlighted('o', 'Söderström', true), 'Söderström')
      assert.equal(highlighted('ö', 'Söderström', true), 'S[ö]derstr[ö]m')
    })
  })
})
