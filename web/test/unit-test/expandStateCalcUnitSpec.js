import {expandStateCalc} from '../../app/editor/Suoritustaulukko.jsx'
import * as assert from 'assert'

let suoritukset = ['a','b','c','d','e']

describe('Suoritustaulukon suoritusten avaaminen', () => {
  describe('Kun kaikki on suljettu', () => {
    let initialState = {expanded: [], allExpandedToggle: false}
    it('Avaa kaikki-linkki avaa kaikki avattavissa olevat suoritukset', () => {
      assert.deepEqual(calculator(initialState).toggleExpandAll(), {expanded: ['a','b','d','e'], allExpandedToggle: true})
    })
    it('Yksittäisen suorituksen avaus avaa vain kyseisen suorituksen', () => {
      assert.deepEqual(calculator(initialState).toggleExpand('b', true), {expanded: ['b'], allExpandedToggle: false})
    })
    it('Kun kaikki suoritukset avataan yksitellen, näytetään Sulje-kaikki linkki', () => {
      let st1 = calculator(initialState).toggleExpand('a', true)
      let st2 = calculator(st1).toggleExpand('b', true)
      let st3 = calculator(st2).toggleExpand('d', true)
      let st4 = calculator(st3).toggleExpand('e', true)
      assert.deepEqual(st4, {expanded: ['a','b','d','e'], allExpandedToggle: true})
    })
  })
  describe('Kun kaikki on avattu', () => {
    let initialState = {expanded: ['a','b','d','e'], allExpandedToggle: true}
    it('Sulje kaikki-linkki sulkee kaikki auki olevat suoritukset', () => {
      assert.deepEqual(calculator(initialState).toggleExpandAll(), {expanded: [], allExpandedToggle: false})
    })
    it('Yksittäisen suorituksen sulkeminen sulkee vain kyseisen suorituksen', () => {
      assert.deepEqual(calculator(initialState).toggleExpand('b', false), {expanded: ['a','d','e'], allExpandedToggle: true})
    })
    it('Kun kaikki suoritukset suljetaan yksitellen, näytetään Avaa-kaikki linkki', () => {
      let st1 = calculator(initialState).toggleExpand('a', false)
      let st2 = calculator(st1).toggleExpand('b', false)
      let st3 = calculator(st2).toggleExpand('d', false)
      let st4 = calculator(st3).toggleExpand('e', false)
      assert.deepEqual(st4, {expanded: [], allExpandedToggle: false})
    })
  })
})

let calculator = initialState => expandStateCalc(initialState, suoritukset, s => s, s => s !== 'c')


