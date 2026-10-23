import { describe, expect, it } from 'vitest'
import { expandStateCalc } from '../../app/editor/ExpandableItems'

const suoritukset = ['a', 'b', 'c', 'd', 'e']

describe('Suoritustaulukon suoritusten avaaminen', () => {
  describe('Kun kaikki on suljettu', () => {
    const initialState = { expanded: [], allExpandedToggle: false }
    it('Avaa kaikki-linkki avaa kaikki avattavissa olevat suoritukset', () => {
      expect(calculator(initialState).toggleExpandAll()).toStrictEqual({
        expanded: ['a', 'b', 'd', 'e'],
        allExpandedToggle: true
      })
    })
    it('Yksittäisen suorituksen avaus avaa vain kyseisen suorituksen', () => {
      expect(calculator(initialState).setExpanded('b', true)).toStrictEqual({
        expanded: ['b'],
        allExpandedToggle: false
      })
    })
    it('Kun kaikki suoritukset avataan yksitellen, näytetään Sulje-kaikki linkki', () => {
      const st1 = calculator(initialState).setExpanded('a', true)
      const st2 = calculator(st1).setExpanded('b', true)
      const st3 = calculator(st2).setExpanded('d', true)
      const st4 = calculator(st3).setExpanded('e', true)
      expect(st4).toStrictEqual({
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
      expect(calculator(initialState).toggleExpandAll()).toStrictEqual({
        expanded: [],
        allExpandedToggle: false
      })
    })
    it('Yksittäisen suorituksen sulkeminen sulkee vain kyseisen suorituksen', () => {
      expect(calculator(initialState).setExpanded('b', false)).toStrictEqual({
        expanded: ['a', 'd', 'e'],
        allExpandedToggle: true
      })
    })
    it('Kun kaikki suoritukset suljetaan yksitellen, näytetään Avaa-kaikki linkki', () => {
      const st1 = calculator(initialState).setExpanded('a', false)
      const st2 = calculator(st1).setExpanded('b', false)
      const st3 = calculator(st2).setExpanded('d', false)
      const st4 = calculator(st3).setExpanded('e', false)
      expect(st4).toStrictEqual({ expanded: [], allExpandedToggle: false })
    })
  })
})

const calculator = (initialState: {
  expanded: string[]
  allExpandedToggle: boolean
}) =>
  expandStateCalc(
    initialState,
    suoritukset,
    (s: string) => s,
    (s: string) => s !== 'c'
  )
