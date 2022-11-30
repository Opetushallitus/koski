import React from 'baret'
import Bacon from 'baconjs'
import DropDown from '../components/Dropdown'
import * as R from 'ramda'
import {
  contextualizeSubModel,
  hasModelProperty,
  modelData,
  modelLookup,
  modelSetData,
  modelSetValue,
  resolveActualModel,
  resolvePrototypeReference
} from '../editor/EditorModel'
import { koulutusModuuliprototypes } from '../suoritus/Koulutusmoduuli'
import { fetchAlternativesBasedOnPrototypes } from '../editor/EnumEditor'
import { elementWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import { t } from '../i18n/i18n'
import { isOneOfModel } from '../types/EditorModels'

const dropdownKey = (oppiaine) => {
  const tunniste = modelData(oppiaine, 'tunniste')
  return `${tunniste.koodistoUri}-${tunniste.koodiarvo}`
}

const muuOppiainePrototype = {
  type: 'prototype',
  key: 'europeanschoolofhelsinkimuuoppiaine'
}

export const UusiEshOsasuoritusDropdown = ({
  osasuoritukset,
  pakollinen,
  selected = Bacon.constant(undefined),
  resultCallback,
  isAlaosasuoritus
}) => {
  if (!osasuoritukset || R.any((s) => !s.context.edit, osasuoritukset))
    return null

  const setPakollisuus = (oppiaineModel) =>
    pakollinen !== undefined && hasModelProperty(oppiaineModel, 'pakollinen')
      ? modelSetData(oppiaineModel, pakollinen, 'pakollinen')
      : oppiaineModel

  const prototypes = R.flatten(osasuoritukset.map(koulutusModuuliprototypes))

  const oppiaineModels = prototypes.map(setPakollisuus)

  const dropdownOppiaineet = fetchAlternativesBasedOnPrototypes(
    oppiaineModels,
    'tunniste'
  ).map(
    (aineet) =>
      R.uniqBy((aine) => modelData(aine, 'tunniste.koodiarvo'), aineet) /* .map(
      (aine) => {
        const isMuuOppiaine = aine.value.classes.includes(
          'europeanschoolofhelsinkimuuoppiaine'
        )
        /*
        Tämä saattanee vaikuttaa hieman häkiltä, mutta tietomallista johtuen muu oppiaine -prototyyppi
        resolvataan manuaalisesti.
        OnlyWhen ja NotWhen -annotaatiot onnistuvat kuitenkin valitsemaan kielioppiaineen / latin / ancient greek oikein.

        return isOneOfModel(aine)
          ? modelSetValue(
              isMuuOppiaine
                ? contextualizeSubModel(
                    resolvePrototypeReference(
                      muuOppiainePrototype,
                      aine.context
                    ),
                    aine.parent
                  )
                : resolveActualModel(aine, aine.parent),
              {
                // Jostain syystä resolveActualModel hävittää kaiken datan tunnisteesta, niin ne pitää syöttää manuaalisesti takaisin.
                data: modelData(aine, 'tunniste'),
                title: t(modelData(aine, 'tunniste.nimi'))
              },
              'tunniste'
            )
          : aine
      }
    ) */
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
                enableFilter={true}
              />
            ) : (
              <></>
            )
          )
      )}
    </div>
  )
}
