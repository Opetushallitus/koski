import React from 'baret'
import Bacon from 'baconjs'
import R from 'ramda'
import Atom from 'bacon.atom'
import DropDown from '../Dropdown.jsx'
import {modelData, modelLookup, modelSetValue, modelTitle} from './EditorModel'
import {deleteOrganizationalPreference, getOrganizationalPreferences} from '../organizationalPreferences'
import {isPaikallinen, isUusi} from './Koulutusmoduuli'
import {elementWithLoadingIndicator} from '../AjaxLoadingIndicator.jsx'
import {t} from '../i18n'
import Http from '../http'
import {parseLocation} from '../location'
export const UusiKurssiDropdown = ({oppiaine, suoritukset = [], paikallinenKurssiProto, valtakunnallisetKurssiProtot, organisaatioOid, selected = Bacon.constant(undefined), resultCallback, placeholder, enableFilter=true}) => {
  let käytössäolevatKoodiarvot = suoritukset.map(s => modelData(s, 'koulutusmoduuli.tunniste').koodiarvo)
  let valtakunnallisetKurssit = completeWithFieldAlternatives(oppiaine, valtakunnallisetKurssiProtot)
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
          displayValue={kurssi => isUusi(kurssi) ? t('Lisää paikallinen kurssi...') : displayValue(kurssi) }
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

const completeWithFieldAlternatives = (oppiaine, kurssiPrototypes) => {
  let oppiaineKoodisto = modelData(oppiaine, 'tunniste.koodistoUri')
  let oppiaineKoodiarvo = modelData(oppiaine, 'tunniste.koodiarvo')
  let kieliKoodisto = modelData(oppiaine, 'kieli.koodistoUri')
  let kieliKoodiarvo = modelData(oppiaine, 'kieli.koodiarvo')
  const alternativesForField = (model) => {
    let kurssiKoodistot = modelLookup(model, 'tunniste').alternativesPath.split('/').last()
    let loc = parseLocation(`/koski/api/editor/kurssit/${oppiaineKoodisto}/${oppiaineKoodiarvo}/${kurssiKoodistot}`).addQueryParams({kieliKoodisto, kieliKoodiarvo})
    return Http.cachedGet(loc.toString())
      .map(alternatives => alternatives.map(enumValue => modelSetValue(model, enumValue, 'tunniste')))
  }
  return Bacon.combineAsArray(kurssiPrototypes.map(alternativesForField)).last().map(x => x.flatten())
}