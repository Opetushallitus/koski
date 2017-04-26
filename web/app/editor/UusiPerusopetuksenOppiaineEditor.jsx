import React from 'baret'
import Bacon from 'baconjs'
import DropDown from '../Dropdown.jsx'
import R from 'ramda'
import {modelData, modelLookup, modelSet, modelSetData} from './EditorModel'
import {getOrganizationalPreferences} from '../organizationalPreferences'
import {isPaikallinen, isUusi} from './Koulutusmoduuli'
import {completeWithFieldAlternatives} from './PerusopetuksenOppiaineetEditor.jsx'
import {paikallinenOppiainePrototype, koulutusModuuliprototypes} from './PerusopetuksenOppiaineEditor.jsx'

export const UusiPerusopetuksenOppiaineEditor = ({suoritukset = [], organisaatioOid, oppiaineenSuoritus, pakollinen, selected = Bacon.constant(undefined), resultCallback, placeholder, enableFilter=true}) => {
  if (!oppiaineenSuoritus.context.edit) return null
  let käytössäolevatKoodiarvot = suoritukset.map(s => modelData(s, 'koulutusmoduuli.tunniste').koodiarvo)
  let oppiaineModels = koulutusModuuliprototypes(oppiaineenSuoritus)
    .filter(R.complement(isPaikallinen))
    .map(oppiaineModel => pakollinen != undefined ? modelSetData(oppiaineModel, pakollinen, 'pakollinen') : oppiaineModel)
  let valtakunnallisetOppiaineet = completeWithFieldAlternatives(oppiaineModels, 'tunniste')
  let paikallinenProto = paikallinenOppiainePrototype(oppiaineenSuoritus)
  let paikallisetOppiaineet = pakollinen || !paikallinenProto ? Bacon.constant([]) : getOrganizationalPreferences(organisaatioOid, paikallinenProto.value.classes[0]).startWith([])
  let oppiaineet = Bacon.combineWith(paikallisetOppiaineet, valtakunnallisetOppiaineet, (x,y) => x.concat(y))
    .map(aineet => aineet.filter(oppiaine => !käytössäolevatKoodiarvot.includes(modelData(oppiaine, 'tunniste').koodiarvo)))

  return (<div className={'uusi-oppiaine'}>
    <DropDown
      options={oppiaineet}
      keyValue={oppiaine => isUusi(oppiaine) ? 'uusi' : modelData(oppiaine, 'tunniste').koodiarvo}
      displayValue={oppiaine => isUusi(oppiaine) ? 'Lisää...' : modelLookup(oppiaine, 'tunniste').value.title}
      onSelectionChanged={oppiaine => {
        resultCallback(modelSet(oppiaineenSuoritus, oppiaine, 'koulutusmoduuli'))
      }}
      selectionText={placeholder}
      newItem={!pakollinen && paikallinenProto}
      enableFilter={enableFilter}
      selected={selected.map(suoritus => modelLookup(suoritus, 'koulutusmoduuli'))}
    />
  </div>)
}
