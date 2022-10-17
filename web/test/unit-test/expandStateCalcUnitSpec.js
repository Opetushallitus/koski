import { expandStateCalc } from '../../app/editor/ExpandableItems'
import * as assert from 'assert'

const suoritukset = ['a', 'b', 'c', 'd', 'e']

describe('Suoritustaulukon suoritusten avaaminen', () => {
  describe('Kun kaikki on suljettu', () => {
    const initialState = { expanded: [], allExpandedToggle: false }
    it('Avaa kaikki-linkki avaa kaikki avattavissa olevat suoritukset', () => {
      assert.deepEqual(calculator(initialState).toggleExpandAll(), {
        expanded: ['a', 'b', 'd', 'e'],
        allExpandedToggle: true
      })
    })
    it('Yksittäisen suorituksen avaus avaa vain kyseisen suorituksen', () => {
      assert.deepEqual(calculator(initialState).setExpanded('b', true), {
        expanded: ['b'],
        allExpandedToggle: false
      })
    })
    it('Kun kaikki suoritukset avataan yksitellen, näytetään Sulje-kaikki linkki', () => {
      const st1 = calculator(initialState).setExpanded('a', true)
      const st2 = calculator(st1).setExpanded('b', true)
      const st3 = calculator(st2).setExpanded('d', true)
      const st4 = calculator(st3).setExpanded('e', true)
      assert.deepEqual(st4, {
        expanded: ['a', 'b', 'd', 'e'],
        allExpandedToggle: true
      })
    })
  })
  describe('Kun kaikki on avattu', () => {
    const initialState = {
      expanded: ['a', 'b', 'd', 'e'],
      allExpandedToggle: true
    }
    it('Sulje kaikki-linkki sulkee kaikki auki olevat suoritukset', () => {
      assert.deepEqual(calculator(initialState).toggleExpandAll(), {
        expanded: [],
        allExpandedToggle: false
      })
    })
    it('Yksittäisen suorituksen sulkeminen sulkee vain kyseisen suorituksen', () => {
      assert.deepEqual(calculator(initialState).setExpanded('b', false), {
        expanded: ['a', 'd', 'e'],
        allExpandedToggle: true
      })
    })
    it('Kun kaikki suoritukset suljetaan yksitellen, näytetään Avaa-kaikki linkki', () => {
      const st1 = calculator(initialState).setExpanded('a', false)
      const st2 = calculator(st1).setExpanded('b', false)
      const st3 = calculator(st2).setExpanded('d', false)
      const st4 = calculator(st3).setExpanded('e', false)
      assert.deepEqual(st4, { expanded: [], allExpandedToggle: false })
    })
  })
})

const calculator = (initialState) =>
  expandStateCalc(
    initialState,
    suoritukset,
    (s) => s,
    (s) => s !== 'c'
  )
