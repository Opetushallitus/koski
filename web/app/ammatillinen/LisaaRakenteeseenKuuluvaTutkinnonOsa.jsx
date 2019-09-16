import React from 'baret'
import Atom from 'bacon.atom'
import {modelSetTitle, modelSetValues} from '../editor/EditorModel'
import LisaaTutkinnonOsaDropdown from './LisaaTutkinnonOsaDropdown'

export const LisääRakenteeseenKuuluvaTutkinnonOsa = ({lisättävätTutkinnonOsat, addTutkinnonOsa, koulutusmoduuliProto, placeholder = lisättävätTutkinnonOsat.osanOsa ? t('Lisää tutkinnon osan osa-alue') : t('Lisää tutkinnon osa')}) => {
  let selectedAtom = Atom(undefined)
  selectedAtom.filter(R.identity).onValue((newItem) => {
    const tutkinnonOsa = modelSetValues(koulutusmoduuliProto(newItem), {tunniste: newItem})
    addTutkinnonOsa(modelSetTitle(tutkinnonOsa, newItem.title))
  })
  return lisättävätTutkinnonOsat.osat.length > 0 && (<span className="osa-samasta-tutkinnosta">
      <LisaaTutkinnonOsaDropdown selectedAtom={selectedAtom} osat={lisättävätTutkinnonOsat.osat} placeholder={placeholder}/>
  </span>)
}
