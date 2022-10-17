import * as R from 'ramda'
import Bacon from 'baconjs'

/** component is used to persist state inside a React.createClass component */
export const accumulateExpandedState = ({
  suoritukset,
  keyF = (s) => s.arrayKey,
  filter = R.identity,
  component
}) => {
  const toggleExpandAllBus = Bacon.Bus()
  const setExpandedBus = Bacon.Bus()
  const initialState = initialStateFromComponent(component)
  const stateP = Bacon.update(
    initialState,
    toggleExpandAllBus,
    (currentState) =>
      expandStateCalc(
        currentState,
        suoritukset,
        keyF,
        filter
      ).toggleExpandAll(),
    setExpandedBus,
    (currentState, { suoritus, expanded }) =>
      expandStateCalc(currentState, suoritukset, keyF, filter).setExpanded(
        suoritus,
        expanded
      )
  )

  stateP.onValue((state) => {
    if (!R.equals(initialStateFromComponent(component), state)) {
      component.setState(state)
    }
  })

  return {
    toggleExpandAll: () => toggleExpandAllBus.push(),
    setExpanded: (suoritus) => (expanded) => {
      setExpandedBus.push({ suoritus, expanded })
    },
    isExpandedP: (suoritus) =>
      stateP
        .map('.expanded')
        .map((expanded) => expanded.includes(keyF(suoritus)))
        .skipDuplicates(),
    allExpandedP: stateP.map('.allExpandedToggle')
  }
}

const initialStateFromComponent = (component) => {
  const { allExpandedToggle = false, expanded = [] } = component.state || {}
  return { allExpandedToggle, expanded }
}

export const expandStateCalc = (currentState, suoritukset, keyF, filter) => {
  return {
    toggleExpandAll() {
      const { allExpandedToggle } = currentState
      const newExpanded = !allExpandedToggle
        ? suoritukset.reduce(
            (acc, s) => (filter(s) ? acc.concat(keyF(s)) : acc),
            []
          )
        : []
      return { expanded: newExpanded, allExpandedToggle: !allExpandedToggle }
    },
    setExpanded(suoritus, expand) {
      const { expanded, allExpandedToggle } = currentState
      const newExpanded = expand
        ? expanded.concat(keyF(suoritus))
        : R.without([keyF(suoritus)], expanded)
      return {
        expanded: newExpanded,
        allExpandedToggle:
          suoritukset.filter(filter).length === newExpanded.length
            ? true
            : newExpanded.length === 0
            ? false
            : allExpandedToggle
      }
    }
  }
}
