import {sortLanguages, sortGrades} from '../../app/sorting'
import * as assert from 'assert'

let kielet = [
  {value: '97', title: 'Ei suoritusta', data: {koodiarvo: '97'}},
  {value: 'EN', title: 'englanti', data: {koodiarvo: 'EN'}},
  {value: 'ES', title: 'espanja', data: {koodiarvo: 'ES'}},
  {value: 'IT', title: 'italia', data: {koodiarvo: 'IT'}},
  {value: 'JA', title: 'japani', data: {koodiarvo: 'JA'}},
  {value: 'ZH', title: 'kiina', data: {koodiarvo: 'ZH'}},
  {value: 'EL', title: 'kreikka', data: {koodiarvo: 'EL'}},
  {value: 'LA', title: 'latina', data: {koodiarvo: 'LA'}},
  {value: 'LV', title: 'latvia, lätti', data: {koodiarvo: 'LV'}},
  {value: 'LT', title: 'liettua', data: {koodiarvo: 'LT'}},
  {value: 'XX', title: 'muu kieli', data: {koodiarvo: 'XX'}},
  {value: 'PT', title: 'portugali', data: {koodiarvo: 'PT'}},
  {value: 'FR', title: 'ranska', data: {koodiarvo: 'FR'}},
  {value: 'SV', title: 'ruotsi', data: {koodiarvo: 'SV'}},
  {value: 'SE', title: 'saame, lappi', data: {koodiarvo: 'SE'}},
  {value: 'DE', title: 'saksa', data: {koodiarvo: 'DE'}},
  {value: 'FI', title: 'suomi', data: {koodiarvo: 'FI'}},
  {value: 'RU', title: 'venäjä', data: {koodiarvo: 'RU'}},
  {value: 'VK', title: 'viittomakieli', data: {koodiarvo: 'VK'}},
  {value: 'ET', title: 'viro, eesti', data: {koodiarvo: 'ET'}}]

describe('When sorting languages', () => {
  it('languages are sorted in correct order', () => {
    assert.deepEqual(sortLanguages(kielet), [
      {value: 'FI', title: 'suomi', data: {koodiarvo: 'FI'}},
      {value: 'SV', title: 'ruotsi', data: {koodiarvo: 'SV'}},
      {value: 'EN', title: 'englanti', data: {koodiarvo: 'EN'}},
      {value: '97', title: 'Ei suoritusta', data: {koodiarvo: '97'}},
      {value: 'ES', title: 'espanja', data: {koodiarvo: 'ES'}},
      {value: 'IT', title: 'italia', data: {koodiarvo: 'IT'}},
      {value: 'JA', title: 'japani', data: {koodiarvo: 'JA'}},
      {value: 'ZH', title: 'kiina', data: {koodiarvo: 'ZH'}},
      {value: 'EL', title: 'kreikka', data: {koodiarvo: 'EL'}},
      {value: 'LA', title: 'latina', data: {koodiarvo: 'LA'}},
      {value: 'LV', title: 'latvia, lätti', data: {koodiarvo: 'LV'}},
      {value: 'LT', title: 'liettua', data: {koodiarvo: 'LT'}},
      {value: 'XX', title: 'muu kieli', data: {koodiarvo: 'XX'}},
      {value: 'PT', title: 'portugali', data: {koodiarvo: 'PT'}},
      {value: 'FR', title: 'ranska', data: {koodiarvo: 'FR'}},
      {value: 'SE', title: 'saame, lappi', data: {koodiarvo: 'SE'}},
      {value: 'DE', title: 'saksa', data: {koodiarvo: 'DE'}},
      {value: 'RU', title: 'venäjä', data: {koodiarvo: 'RU'}},
      {value: 'VK', title: 'viittomakieli', data: {koodiarvo: 'VK'}},
      {value: 'ET', title: 'viro, eesti', data: {koodiarvo: 'ET'}}])
  })
})

describe('Sorting grades', () => {
  describe('When sorting grades', () => {
    it('sorts character grades correctly', () => {
      assert.deepEqual(sortGrades([{value: 'S'}, {value: 'H'}]), [{value: 'H'}, {value: 'S'}])
    })
    it('sorts numeric grades correctly', () => {
      assert.deepEqual(sortGrades([{value: 6}, {value: 3}, {value: 10}]), [{value: 3}, {value: 6}, {value: 10}])
    })
    it('sorts mixed grades correctly', () => {
      assert.deepEqual(sortGrades([{value: 6}, {value: 'H'}, {value: 3}, {value: 'S'}, {value: 10}]), [{value: 3}, {value: 6}, {value: 10}, {value: 'H'}, {value: 'S'}])
    })
  })
})

