import React from 'baret'
import Bacon from 'baconjs'
import * as R from 'ramda'
import Atom from 'bacon.atom'
import DropDown from '../components/Dropdown'
import {modelData, modelLookup, modelSetValue, modelTitle} from '../editor/EditorModel'
import {deleteOrganizationalPreference, getOrganizationalPreferences} from '../virkailija/organizationalPreferences'
import {isPaikallinen, isUusi, isDiaKurssi} from '../suoritus/Koulutusmoduuli'
import {elementWithLoadingIndicator} from '../components/AjaxLoadingIndicator'
import {t} from '../i18n/i18n'
import Http from '../util/http'
import {parseLocation} from '../util/location'
import {findDefaultKoodisto, findKoodistoByDiaarinumero} from './kurssi'
export const UusiKurssiDropdown = (
  {
    oppiaine,
    suoritukset,
    paikallinenKurssiProto,
    valtakunnallisetKurssiProtot,
    organisaatioOid,
    selected = Bacon.constant(undefined),
    resultCallback,
    placeholder,
    enableFilter=true,
    customAlternativesCompletionFn=false
  }) => {
  let käytössäolevatKoodiarvot = suoritukset.map(s => modelData(s, 'koulutusmoduuli.tunniste').koodiarvo)
  let kurssiKoodit = customAlternativesCompletionFn || fetchKurssiKoodit
  let valtakunnallisetKurssit = kurssiKoodit(oppiaine, valtakunnallisetKurssiProtot)
  let paikallisetKurssit = Atom([])
  let setPaikallisetKurssit = kurssit => paikallisetKurssit.set(kurssit)

  if (paikallinenKurssiProto) {
    getOrganizationalPreferences(organisaatioOid, paikallinenKurssiProto.value.classes[0]).onValue(setPaikallisetKurssit)
  }

  let displayValue = (kurssi) => {
    if (isDiaKurssi(kurssi)) {
      return modelTitle(kurssi, 'tunniste')
    } else {
      return modelData(kurssi, 'tunniste.koodiarvo') + ' ' + modelTitle(kurssi, 'tunniste')
    }
  }
  const kaikkiKurssit = Bacon.combineWith(paikallisetKurssit, valtakunnallisetKurssit, (x,y) => x.concat(y))
    .map(kurssit => kurssit.filter(kurssi => isPaikallinen(kurssi) || !käytössäolevatKoodiarvot.includes(modelData(kurssi, 'tunniste').koodiarvo)))
    .map(R.sortBy(displayValue))

  let poistaPaikallinenKurssi = kurssi => {
    const data = modelData(kurssi)
    const localKey = data.tunniste.koodiarvo
    deleteOrganizationalPreference(organisaatioOid, paikallinenKurssiProto.value.classes[0], localKey).onValue(setPaikallisetKurssit)
  }

  return (<div className={'uusi-kurssi'}>
    {
      elementWithLoadingIndicator(kaikkiKurssit.map('.length').map(length => length || paikallinenKurssiProto
        ? <DropDown
          options={kaikkiKurssit}
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

UusiKurssiDropdown.displayName = 'UusiKurssiDropdown'

const fetchKurssiKoodit = (oppiaine, kurssiPrototypes) => {
  const oppiaineKoodisto = modelData(oppiaine, 'tunniste.koodistoUri')
  const oppiaineKoodiarvo = modelData(oppiaine, 'tunniste.koodiarvo')
  const oppimaaraKoodisto = modelData(oppiaine, 'kieli.koodistoUri') || modelData(oppiaine, 'oppimäärä.koodistoUri')
  const oppimaaraKoodiarvo = modelData(oppiaine, 'kieli.koodiarvo') || modelData(oppiaine, 'oppimäärä.koodiarvo')
  const oppimaaraDiaarinumero = modelData(oppiaine.context.suoritus, 'koulutusmoduuli.perusteenDiaarinumero')

  const alternativesForField = (model) => {
    if (!oppiaineKoodisto) return []

    const koodistoAlternativesPath = modelLookup(model, 'tunniste').alternativesPath

    const kurssiKoodistot = koodistoAlternativesPath && R.last(koodistoAlternativesPath.split('/'))

    if (!kurssiKoodistot) return []

    const koodistot = kurssiKoodistot.split(',').filter(koodisto => {
      if ((koodisto === 'lukioonvalmistavankoulutuksenmoduulit2019' && oppimaaraDiaarinumero === '56/011/2015') ||
        (koodisto === 'lukioonvalmistavankoulutuksenkurssit2015' && oppimaaraDiaarinumero === 'OPH-4958-2020')) {
        return false
      }
      return true
    })

    const queryKoodistot =
      findKoodistoByDiaarinumero(koodistot, oppimaaraDiaarinumero) ||
      findDefaultKoodisto(koodistot) ||
      kurssiKoodistot

    const loc = parseLocation(`/koski/api/editor/koodit/${oppiaineKoodisto}/${oppiaineKoodiarvo}/kurssit/${queryKoodistot}`)
      .addQueryParams({oppimaaraKoodisto, oppimaaraKoodiarvo, oppimaaraDiaarinumero})

    return Http.cachedGet(loc.toString())
      .map(alternatives => alternatives.map(enumValue => modelSetValue(model, enumValue, 'tunniste')))
  }

  return Bacon.combineAsArray(kurssiPrototypes.map(alternativesForField)).last().map(R.unnest)
}
