import React from 'baret'
import Http from '../http'
import Bacon from 'baconjs'
import Dropdown from '../Dropdown.jsx'
import {elementWithLoadingIndicator} from '../AjaxLoadingIndicator.jsx'
import {t} from '../i18n'

export const PerusteDropdown = ({suoritusTyyppiP, perusteAtom}) => {
  let diaarinumerotP = diaarinumerot(suoritusTyyppiP)
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
    { elementWithLoadingIndicator(diaarinumerotP.map(diaarinumerot => diaarinumerot > 1
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


export const diaarinumerot = suoritusTyyppiP => {
  const koulutustyyppiP = suoritusTyyppiP.map(tyyppi => {
    if (tyyppi.koodiarvo == 'perusopetuksenoppimaara' || tyyppi.koodiarvo == 'perusopetuksenvuosiluokka') {
      return '16'
    }
    if (tyyppi.koodiarvo == 'aikuistenperusopetuksenoppimaara' || tyyppi.koodiarvo == 'perusopetuksenoppiaineenoppimaara' || tyyppi.koodiarvo == 'aikuistenperusopetuksenoppimaaranalkuvaihe'){
      return '17'
    }
    if (tyyppi.koodiarvo == 'perusopetuksenlisaopetus') {
      return '6'
    }
    if (tyyppi.koodiarvo == 'perusopetukseenvalmistavaopetus') {
      return '22'
    }
    if (tyyppi.koodiarvo == 'esiopetuksensuoritus') {
      return '15'
    }
  }).skipDuplicates()

  return koulutustyyppiP.flatMapLatest(tyyppi => tyyppi
    ? Http.cachedGet(`/koski/api/tutkinnonperusteet/diaarinumerot/koulutustyyppi/${tyyppi}`)
    : []).toProperty()
}
