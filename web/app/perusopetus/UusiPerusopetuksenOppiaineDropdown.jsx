import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import DropDown from '../components/Dropdown'
import R from 'ramda'
import {modelData, modelLookup, modelSetData} from '../editor/EditorModel'
import {deleteOrganizationalPreference, getOrganizationalPreferences} from '../virkailija/organizationalPreferences'
import {isPaikallinen, isUusi, koulutusModuuliprototypes} from '../suoritus/Koulutusmoduuli'
import {fetchAlternativesBasedOnPrototypes} from '../editor/EnumEditor'
import {paikallinenOppiainePrototype} from './PerusopetuksenOppiaineEditor'
import {elementWithLoadingIndicator} from '../components/AjaxLoadingIndicator'
import {t} from '../i18n/i18n'

export const UusiPerusopetuksenOppiaineDropdown = ({suoritukset = [], organisaatioOid, oppiaineenSuoritus, pakollinen, selected = Bacon.constant(undefined), resultCallback, placeholder, enableFilter=true}) => {
  if (!oppiaineenSuoritus || !oppiaineenSuoritus.context.edit) return null
  let käytössäolevatKoodiarvot = suoritukset.map(s => modelData(s, 'koulutusmoduuli')).filter(k => !k.kieli).map(k => k.tunniste.koodiarvo)
  let oppiaineModels = koulutusModuuliprototypes(oppiaineenSuoritus)
    .filter(R.complement(isPaikallinen))
    .map(oppiaineModel => pakollinen != undefined ? modelSetData(oppiaineModel, pakollinen, 'pakollinen') : oppiaineModel)
  let valtakunnallisetOppiaineet = fetchAlternativesBasedOnPrototypes(oppiaineModels, 'tunniste')
  let paikallinenProto = !pakollinen && paikallinenOppiainePrototype(oppiaineenSuoritus)
  let paikallisetOppiaineet = Atom([])
  let setPaikallisetOppiaineet = oppiaineet => paikallisetOppiaineet.set(oppiaineet)

  if (paikallinenProto) {
    getOrganizationalPreferences(organisaatioOid, paikallinenProto.value.classes[0]).onValue(setPaikallisetOppiaineet)
  }

  let oppiaineet = Bacon.combineWith(paikallisetOppiaineet, valtakunnallisetOppiaineet, (x,y) => x.concat(y))
    .map(aineet => aineet.filter(oppiaine => !pakollinen || !käytössäolevatKoodiarvot.includes(modelData(oppiaine, 'tunniste').koodiarvo)))

  let poistaPaikallinenOppiaine = oppiaine => deleteOrganizationalPreference(organisaatioOid, paikallinenProto.value.classes[0], oppiaine).onValue(setPaikallisetOppiaineet)

  return (<div className={'uusi-oppiaine'}>
    {
      elementWithLoadingIndicator(oppiaineet.map('.length').map(length => length || paikallinenProto
        ? <DropDown
          options={oppiaineet}
          keyValue={oppiaine => isUusi(oppiaine) ? 'uusi' : modelData(oppiaine, 'tunniste').koodiarvo}
          displayValue={oppiaine => isUusi(oppiaine) ? 'Lisää...' : modelLookup(oppiaine, 'tunniste').value.title}
          onSelectionChanged={resultCallback}
          selectionText={placeholder}
          newItem={paikallinenProto}
          enableFilter={enableFilter}
          selected={selected}
          isRemovable={isPaikallinen}
          onRemoval={poistaPaikallinenOppiaine}
          removeText={t('Poista paikallinen oppiaine. Poistaminen ei vaikuta olemassa oleviin suorituksiin.')}
        />
        : null
      ))
    }
  </div>)
}