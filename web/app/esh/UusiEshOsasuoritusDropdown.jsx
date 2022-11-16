import React from 'baret'
import Bacon from 'baconjs'
import DropDown from '../components/Dropdown'
import * as R from 'ramda'
import {
  hasModelProperty,
  modelData,
  modelLookup,
  modelSetData
} from '../editor/EditorModel'
import { koulutusModuuliprototypes } from '../suoritus/Koulutusmoduuli'
import { fetchAlternativesBasedOnPrototypes } from '../editor/EnumEditor'
import { elementWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import { t } from '../i18n/i18n'

const dropdownKey = (oppiaine) => {
  const tunniste = modelData(oppiaine, 'tunniste')
  return `${tunniste.koodistoUri}-${tunniste.koodiarvo}`
}

export const UusiEshOsasuoritusDropdown = ({
  osasuoritukset,
  pakollinen,
  selected = Bacon.constant(undefined),
  resultCallback,
  isAlaosasuoritus,
  oppiainePrototypes = undefined
}) => {
  if (!osasuoritukset || R.any((s) => !s.context.edit, osasuoritukset))
    return null

  const setPakollisuus = (oppiaineModel) =>
    pakollinen !== undefined && hasModelProperty(oppiaineModel, 'pakollinen')
      ? modelSetData(oppiaineModel, pakollinen, 'pakollinen')
      : oppiaineModel

  const prototypes =
    oppiainePrototypes ||
    R.flatten(osasuoritukset.map(koulutusModuuliprototypes))

  const oppiaineModels = prototypes.map(setPakollisuus)

  const dropdownOppiaineet = fetchAlternativesBasedOnPrototypes(
    oppiaineModels,
    'tunniste'
  ).map((aineet) =>
    R.uniqBy((aine) => modelData(aine, 'tunniste.koodiarvo'), aineet)
  )

  const getDropdownDisplayValue = (oppiaine) => {
    const tunniste = modelLookup(oppiaine, 'tunniste')
    return tunniste.value.title
  }

  const dropdownPlaceholder = isAlaosasuoritus
    ? t('description:lisaa_alaosasuoritus')
    : t('Lisää osasuoritus')

  return (
    <div className={'uusi-oppiaine'}>
      {elementWithLoadingIndicator(
        dropdownOppiaineet
          .map('.length')
          .map((length) =>
            length > 0 ? (
              <DropDown
                options={dropdownOppiaineet}
                keyValue={(oppiaine) => dropdownKey(oppiaine)}
                displayValue={(oppiaine) => getDropdownDisplayValue(oppiaine)}
                onSelectionChanged={resultCallback}
                selectionText={dropdownPlaceholder}
                selected={selected}
              />
            ) : (
              <></>
            )
          )
      )}
    </div>
  )
}
