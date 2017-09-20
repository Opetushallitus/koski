import React from 'baret'
import Http from '../http'
import Bacon from 'baconjs'
import Dropdown from '../Dropdown.jsx'
import {elementWithLoadingIndicator} from '../AjaxLoadingIndicator.jsx'
import {t} from '../i18n'

export const PerusteDropdown = ({suoritusTyyppiP, perusteAtom}) => {
  let diaarinumerotP = suoritusTyyppiP.flatMapLatest(tyyppi =>  !tyyppi ? [] : diaarinumerot(tyyppi)).toProperty()
  let selectedOptionP = Bacon.combineWith(diaarinumerotP, perusteAtom, (options, selected) => options.find(o => o.koodiarvo == selected))
  let selectOption = (option) => {
    perusteAtom.set(option && option.koodiarvo)
  }

  diaarinumerotP.onValue(options => {
    let current = perusteAtom.get()
    if (!current || !options.map(k => k.koodiarvo).includes(current)) {
      selectOption(options[0])
    }
  })

  return (<span>
    { elementWithLoadingIndicator(diaarinumerotP.map(diaarinumerot => diaarinumerot.length > 1
        ? <Dropdown
            options={diaarinumerotP}
            keyValue={option => option.koodiarvo}
            displayValue={option => option.koodiarvo + ' ' + t(option.nimi)}
            onSelectionChanged={selectOption}
            selected={selectedOptionP}/>
        : <span>{ perusteAtom }</span>
    ))}
  </span>)
}


export const diaarinumerot = suoritusTyyppi => {
  let koulutustyyppi = koulutustyyppiKoodi(suoritusTyyppi.koodiarvo)
  return koulutustyyppi ? Http.cachedGet(`/koski/api/tutkinnonperusteet/diaarinumerot/koulutustyyppi/${koulutustyyppi}`) : []
}

const koulutustyyppiKoodi = tyyppi => {
  if (tyyppi == 'perusopetuksenoppimaara' || tyyppi == 'perusopetuksenvuosiluokka') {
    return '16'
  }
  if (tyyppi == 'aikuistenperusopetuksenoppimaara' || tyyppi == 'perusopetuksenoppiaineenoppimaara' || tyyppi == 'aikuistenperusopetuksenoppimaaranalkuvaihe'){
    return '17'
  }
  if (tyyppi == 'perusopetuksenlisaopetus') {
    return '6'
  }
  if (tyyppi == 'perusopetukseenvalmistavaopetus') {
    return '22'
  }
  if (tyyppi == 'esiopetuksensuoritus') {
    return '15'
  }
}
