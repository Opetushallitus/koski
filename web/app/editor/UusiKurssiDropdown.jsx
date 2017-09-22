import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import DropDown from '../Dropdown.jsx'
import R from 'ramda'
import {modelData, modelLookup} from './EditorModel'
import {deleteOrganizationalPreference, getOrganizationalPreferences} from '../organizationalPreferences'
import {isPaikallinen, isUusi, koulutusModuuliprototypes} from './Koulutusmoduuli'
import {completeWithFieldAlternatives} from './PerusopetuksenOppiaineRowEditor.jsx'
import {elementWithLoadingIndicator} from '../AjaxLoadingIndicator.jsx'
import {t} from '../i18n'

export const UusiKurssiDropdown = ({suoritukset = [], organisaatioOid, kurssinSuoritus, selected = Bacon.constant(undefined), resultCallback, placeholder, enableFilter=true}) => {
  if (!kurssinSuoritus || !kurssinSuoritus.context.edit) return null
  let käytössäolevatKoodiarvot = suoritukset.map(s => modelData(s, 'koulutusmoduuli.tunniste').koodiarvo)
  let kurssiModels = koulutusModuuliprototypes(kurssinSuoritus).filter(R.complement(isPaikallinen))
  let valtakunnallisetKurssit = completeWithFieldAlternatives(kurssiModels, 'tunniste')
  let paikallinenProto = koulutusModuuliprototypes(kurssinSuoritus).find(isPaikallinen)
  let paikallisetKurssit = Atom([])
  let setPaikallisetKurssit = kurssit => paikallisetKurssit.set(kurssit)

  if (paikallinenProto) {
    getOrganizationalPreferences(organisaatioOid, paikallinenProto.value.classes[0]).onValue(setPaikallisetKurssit)
  }

  let kurssit = Bacon.combineWith(paikallisetKurssit, valtakunnallisetKurssit, (x,y) => x.concat(y))
    .map(aineet => aineet.filter(kurssi => !käytössäolevatKoodiarvot.includes(modelData(kurssi, 'tunniste').koodiarvo)))

  let poistaPaikallinenKurssi = kurssi => deleteOrganizationalPreference(organisaatioOid, paikallinenProto.value.classes[0], kurssi).onValue(setPaikallisetKurssit)

  return (<div className={'uusi-kurssi'}>
    {
      elementWithLoadingIndicator(kurssit.map('.length').map(length => length || paikallinenProto
        ? <DropDown
          options={kurssit}
          keyValue={kurssi => isUusi(kurssi) ? 'uusi' : modelData(kurssi, 'tunniste').koodiarvo}
          displayValue={kurssi => isUusi(kurssi) ? 'Lisää...' : modelLookup(kurssi, 'tunniste').value.title}
          onSelectionChanged={resultCallback}
          selectionText={placeholder}
          newItem={paikallinenProto}
          enableFilter={enableFilter}
          selected={selected}
          isRemovable={isPaikallinen}
          onRemoval={poistaPaikallinenKurssi}
          removeText={t('Poista paikallinen kurssi. Poistaminen ei vaikuta olemassa oleviin suorituksiin.')}
        />
        : null
      ))
    }
  </div>)
}