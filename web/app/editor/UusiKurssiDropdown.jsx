import React from 'baret'
import Bacon from 'baconjs'
import R from 'ramda'
import Atom from 'bacon.atom'
import DropDown from '../Dropdown.jsx'
import {modelData, modelTitle} from './EditorModel'
import {deleteOrganizationalPreference, getOrganizationalPreferences} from '../organizationalPreferences'
import {isPaikallinen, isUusi} from './Koulutusmoduuli'
import {completeWithFieldAlternatives} from './PerusopetuksenOppiaineRowEditor.jsx'
import {elementWithLoadingIndicator} from '../AjaxLoadingIndicator.jsx'
import {t} from '../i18n'

export const UusiKurssiDropdown = ({suoritukset = [], paikallinenKurssiProto, valtakunnallisetKurssiProtot, organisaatioOid, selected = Bacon.constant(undefined), resultCallback, placeholder, enableFilter=true}) => {
  let käytössäolevatKoodiarvot = suoritukset.map(s => modelData(s, 'koulutusmoduuli.tunniste').koodiarvo)
  let valtakunnallisetKurssit = completeWithFieldAlternatives(valtakunnallisetKurssiProtot, 'tunniste')

  let paikallisetKurssit = Atom([])
  let setPaikallisetKurssit = kurssit => paikallisetKurssit.set(kurssit)

  if (paikallinenKurssiProto) {
    getOrganizationalPreferences(organisaatioOid, paikallinenKurssiProto.value.classes[0]).onValue(setPaikallisetKurssit)
  }

  let displayValue = (kurssi) => modelData(kurssi, 'tunniste.koodiarvo') + ' ' + modelTitle(kurssi, 'tunniste')
  let kurssit = Bacon.combineWith(paikallisetKurssit, valtakunnallisetKurssit, (x,y) => x.concat(y))
    .map(aineet => aineet.filter(kurssi => !käytössäolevatKoodiarvot.includes(modelData(kurssi, 'tunniste').koodiarvo)))
    .map(R.sortBy(displayValue))

  let poistaPaikallinenKurssi = kurssi => deleteOrganizationalPreference(organisaatioOid, paikallinenKurssiProto.value.classes[0], kurssi).onValue(setPaikallisetKurssit)

  return (<div className={'uusi-kurssi'}>
    {
      elementWithLoadingIndicator(kurssit.map('.length').map(length => length || paikallinenKurssiProto
        ? <DropDown
          options={kurssit}
          keyValue={kurssi => isUusi(kurssi) ? 'uusi' : modelData(kurssi, 'tunniste').koodiarvo}
          displayValue={kurssi => isUusi(kurssi) ? 'Lisää...' : displayValue(kurssi) }
          onSelectionChanged={resultCallback}
          selectionText={placeholder}
          newItem={paikallinenKurssiProto}
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