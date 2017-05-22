import R from 'ramda'
import Bacon from 'baconjs'

export const accumulateExpandedState = (suoritukset, keyF = s => s.arrayKey, filter) => {
  let toggleExpandAllBus = Bacon.Bus()
  let toggleExpandBus = Bacon.Bus()
  let stateP = Bacon.update({ allExpandedToggle: false, expanded: [] },
    toggleExpandAllBus, currentState => expandStateCalc(currentState, suoritukset, keyF, filter).toggleExpandAll(),
    toggleExpandBus, (currentState, {suoritus, expanded}) => expandStateCalc(currentState, suoritukset, keyF, filter).setExpanded(suoritus, expanded)
  )
  return {
    toggleExpandAll: () => toggleExpandAllBus.push(),
    setExpanded: (suoritus) => (expanded) => {
      toggleExpandBus.push({suoritus, expanded})
    },
    isExpandedP: (suoritus) => stateP.map('.expanded').map(expanded => expanded.includes(keyF(suoritus))).skipDuplicates(),
    allExpandedP: stateP.map('.allExpandedToggle')
  }
}

export const expandStateCalc = (currentState, suoritukset, keyF, filter) => {
  return {
    toggleExpandAll() {
      let {allExpandedToggle} = currentState
      let newExpanded = !allExpandedToggle ? suoritukset.reduce((acc, s) => filter(s) ? acc.concat(keyF(s)) : acc , []) : []
      return {expanded: newExpanded, allExpandedToggle: !allExpandedToggle}
    },
    setExpanded(suoritus, expand) {
      let {expanded, allExpandedToggle} = currentState
      let newExpanded = expand ? expanded.concat(keyF(suoritus)) : R.without([keyF(suoritus)], expanded)

      return {
        expanded: newExpanded,
        allExpandedToggle: suoritukset.filter(filter).length === newExpanded.length
          ? true
          : newExpanded.length === 0 ? false : allExpandedToggle
      }
    }
  }
}