import {sortLanguages} from '../../app/sorting'
import * as assert from 'assert'

let kielet = [
  {'value': '97', 'title': 'Ei suoritusta'},
  {'value': 'EN', 'title': 'englanti'},
  {'value': 'ES', 'title': 'espanja'},
  {'value': 'IT', 'title': 'italia'},
  {'value': 'JA', 'title': 'japani'},
  {'value': 'ZH', 'title': 'kiina'},
  {'value': 'EL', 'title': 'kreikka'},
  {'value': 'LA', 'title': 'latina'},
  {'value': 'LV', 'title': 'latvia, lätti'},
  {'value': 'LT', 'title': 'liettua'},
  {'value': 'XX', 'title': 'muu kieli'},
  {'value': 'PT', 'title': 'portugali'},
  {'value': 'FR', 'title': 'ranska'},
  {'value': 'SV', 'title': 'ruotsi'},
  {'value': 'SE', 'title': 'saame, lappi'},
  {'value': 'DE', 'title': 'saksa'},
  {'value': 'FI', 'title': 'suomi'},
  {'value': 'RU', 'title': 'venäjä'},
  {'value': 'VK', 'title': 'viittomakieli'},
  {'value': 'ET', 'title': 'viro, eesti'}]

describe('When sorting languages', () => {
  it('languages are sorted in correct order', () => {
    assert.deepEqual(sortLanguages(kielet), [
      {'value': 'FI', 'title': 'suomi'},
      {'value': 'SV', 'title': 'ruotsi'},
      {'value': 'EN', 'title': 'englanti'},
      {'value': '97', 'title': 'Ei suoritusta'},
      {'value': 'ES', 'title': 'espanja'},
      {'value': 'IT', 'title': 'italia'},
      {'value': 'JA', 'title': 'japani'},
      {'value': 'ZH', 'title': 'kiina'},
      {'value': 'EL', 'title': 'kreikka'},
      {'value': 'LA', 'title': 'latina'},
      {'value': 'LV', 'title': 'latvia, lätti'},
      {'value': 'LT', 'title': 'liettua'},
      {'value': 'XX', 'title': 'muu kieli'},
      {'value': 'PT', 'title': 'portugali'},
      {'value': 'FR', 'title': 'ranska'},
      {'value': 'SE', 'title': 'saame, lappi'},
      {'value': 'DE', 'title': 'saksa'},
      {'value': 'RU', 'title': 'venäjä'},
      {'value': 'VK', 'title': 'viittomakieli'},
      {'value': 'ET', 'title': 'viro, eesti'}])
  })
})
