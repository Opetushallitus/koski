import {expandStateCalc} from '../../app/editor/Suoritustaulukko.jsx'
import * as assert from 'assert'

let suoritukset = ['a','b','c','d','e']

describe('Suoritustaulukon suoritusten avaaminen', () => {
  describe('Kun kaikki on suljettu', () => {
    let initialState = {expanded: [], allExpandedToggle: false}
    it('Avaa kaikki-linkki avaa kaikki avattavissa olevat suoritukset', () => {
      assert.deepEqual(calculator(initialState).toggleExpandAll(), {expanded: [0,1,3,4], allExpandedToggle: true})
    })
    it('Yksittäisen suorituksen avaus avaa vain kyseisen suorituksen', () => {
      assert.deepEqual(calculator(initialState).toggleExpand(1, true), {expanded: [1], allExpandedToggle: false})
    })
    it('Kun kaikki suoritukset avataan yksitellen, näytetään Sulje-kaikki linkki', () => {
      let st1 = calculator(initialState).toggleExpand(0, true)
      let st2 = calculator(st1).toggleExpand(1, true)
      let st3 = calculator(st2).toggleExpand(3, true)
      let st4 = calculator(st3).toggleExpand(4, true)
      assert.deepEqual(st4, {expanded: [0,1,3,4], allExpandedToggle: true})
    })
  })
  describe('Kun kaikki on avattu', () => {
    let initialState = {expanded: [0,1,3,4], allExpandedToggle: true}
    it('Sulje kaikki-linkki sulkee kaikki auki olevat suoritukset', () => {
      assert.deepEqual(calculator(initialState).toggleExpandAll(), {expanded: [], allExpandedToggle: false})
    })
    it('Yksittäisen suorituksen sulkeminen sulkee vain kyseisen suorituksen', () => {
      assert.deepEqual(calculator(initialState).toggleExpand(1, false), {expanded: [0,3,4], allExpandedToggle: true})
    })
    it('Kun kaikki suoritukset suljetaan yksitellen, näytetään Avaa-kaikki linkki', () => {
      let st1 = calculator(initialState).toggleExpand(0, false)
      let st2 = calculator(st1).toggleExpand(1, false)
      let st3 = calculator(st2).toggleExpand(3, false)
      let st4 = calculator(st3).toggleExpand(4, false)
      assert.deepEqual(st4, {expanded: [], allExpandedToggle: false})
    })
  })
})

let calculator = initialState => expandStateCalc(initialState, suoritukset, s => s !== 'c')


