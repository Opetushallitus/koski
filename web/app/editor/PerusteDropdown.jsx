import React from 'baret'
import Http from '../http'
import Bacon from 'baconjs'
import Dropdown from '../Dropdown.jsx'
import {elementWithLoadingIndicator} from '../AjaxLoadingIndicator.jsx';

export const PerusteDropdown = ({suoritusP, perusteAtom}) => {
  let koulutustyyppiP = suoritusP.map(suoritus => {
    if (suoritus.tyyppi.koodiarvo == 'perusopetuksenoppimaara' || suoritus.tyyppi.koodiarvo == 'perusopetuksenvuosiluokka') {
      if (suoritus.oppimäärä && suoritus.oppimäärä.koodiarvo == 'aikuistenperusopetus') return '17'
      return '16'
    }
    if (suoritus.tyyppi.koodiarvo == 'perusopetuksenoppiaineenoppimaara') {
      return '17'
    }
  })

  let diaarinumerotP = koulutustyyppiP.flatMapLatest(tyyppi => tyyppi ? Http.cachedGet(`/koski/api/tutkinnonperusteet/diaarinumerot/koulutustyyppi/${tyyppi}`) : []).toProperty()
  let selectedOptionP = Bacon.combineWith(diaarinumerotP, perusteAtom, (options, selected) => options.find(o => o.koodiarvo == selected))
  let selectOption = (option) => {
    perusteAtom.set(option && option.koodiarvo)
  }
  diaarinumerotP.onValue(options => !perusteAtom.get() && selectOption(options[0]))
  return (<span>
    { elementWithLoadingIndicator(diaarinumerotP.map(diaarinumerot => diaarinumerot.length
        ? <Dropdown
            options={diaarinumerotP}
            keyValue={option => option.koodiarvo}
            displayValue={option => option.koodiarvo + ' ' + option.nimi.fi}
            onSelectionChanged={selectOption}
            selected={selectedOptionP}/>
        : <span>{ perusteAtom }</span>
    ))}
  </span>)
}
