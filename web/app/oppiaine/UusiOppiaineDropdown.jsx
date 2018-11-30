import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import DropDown from '../components/Dropdown'
import * as R from 'ramda'
import {hasModelProperty, modelData, modelLookup, modelSetData} from '../editor/EditorModel'
import {deleteOrganizationalPreference, getOrganizationalPreferences} from '../virkailija/organizationalPreferences'
import {isPaikallinen, isUusi, koulutusModuuliprototypes} from '../suoritus/Koulutusmoduuli'
import {fetchAlternativesBasedOnPrototypes} from '../editor/EnumEditor'
import {elementWithLoadingIndicator} from '../components/AjaxLoadingIndicator'
import {t} from '../i18n/i18n'

const key = oppiaine => {
  const tunniste = modelData(oppiaine, 'tunniste')
  const koodisto = isPaikallinen(oppiaine) ? 'paikallinen' : tunniste.koodistoUri
  return `${koodisto}-${tunniste.koodiarvo}`
}

export const UusiOppiaineDropdown = ({suoritukset = [], organisaatioOid, oppiaineenSuoritukset, pakollinen, selected = Bacon.constant(undefined), resultCallback, placeholder, enableFilter=true, allowPaikallinen = true, optionsFilter = R.identity}) => {
  if (!oppiaineenSuoritukset || R.any(s => !s.context.edit, oppiaineenSuoritukset)) return null

  const käytössäolevatKoodiarvot = suoritukset.map(s => modelData(s, 'koulutusmoduuli')).filter(k => !k.kieli).map(k => k.tunniste.koodiarvo)

  const setPakollisuus = oppiaineModel => pakollinen !== undefined && hasModelProperty(oppiaineModel, 'pakollinen')
    ? modelSetData(oppiaineModel, pakollinen, 'pakollinen')
    : oppiaineModel

  const prototypes = R.flatten(oppiaineenSuoritukset.map(koulutusModuuliprototypes))
  const oppiaineModels = prototypes.map(setPakollisuus)
  const valtakunnallisetOppiaineet = fetchAlternativesBasedOnPrototypes(oppiaineModels.filter(R.complement(isPaikallinen)), 'tunniste')
  const paikallinenProto = oppiaineModels.find(isPaikallinen)
  const paikallisetOppiaineet = Atom([])
  const setPaikallisetOppiaineet = oppiaineet => paikallisetOppiaineet.set(oppiaineet.map(setPakollisuus))

  if (paikallinenProto) {
    getOrganizationalPreferences(organisaatioOid, paikallinenProto.value.classes[0]).onValue(setPaikallisetOppiaineet)
  }

  const oppiaineet = Bacon.combineWith(paikallisetOppiaineet, valtakunnallisetOppiaineet, (x,y) => x.concat(y))
    .map(aineet => aineet
      .filter(oppiaine => pakollinen ? !käytössäolevatKoodiarvot.includes(modelData(oppiaine, 'tunniste').koodiarvo) : true)
      .filter(optionsFilter)
    )

  const poistaPaikallinenOppiaine = oppiaine => {
    const data = modelData(oppiaine)
    const localKey = data.tunniste.koodiarvo
    deleteOrganizationalPreference(organisaatioOid, paikallinenProto.value.classes[0], localKey).onValue(setPaikallisetOppiaineet)
  }

  return (<div className={'uusi-oppiaine'}>
    {
      elementWithLoadingIndicator(oppiaineet.map('.length').map(length => length || paikallinenProto
        ? <DropDown
          options={oppiaineet}
          keyValue={oppiaine => isUusi(oppiaine) ? 'uusi' : key(oppiaine)}
          displayValue={oppiaine => isUusi(oppiaine) ? t('Lisää') + '...' : modelLookup(oppiaine, 'tunniste').value.title}
          onSelectionChanged={resultCallback}
          selectionText={placeholder}
          newItem={allowPaikallinen && paikallinenProto}
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
