import React from 'baret'
import {
  contextualizeModel,
  contextualizeSubModel,
  ensureArrayKey,
  modelData,
  modelItems,
  modelLookup,
  modelProperty,
  modelSet,
  modelSetTitle,
  modelSetValues,
  pushModel,
  resolveActualModel,
  wrapOptional
} from '../editor/EditorModel'
import { zeroValue } from '../editor/EnumEditor'
import { t } from '../i18n/i18n'
import { newOsasuoritusProto, newOsasuoritusProtos } from '../suoritus/Suoritus'
import { isOneOfModel } from '../types/EditorModels'
import { luokkaAsteenOsasuorituksenAlaosasuoritukset } from './esh'
import { UusiEshOsasuoritusDropdown } from './UusiEshOsasuoritusDropdown'

export const UusiEuropeanSchoolOfHelsinkiOsasuoritusDropdown = ({
  model,
  nestedLevel = 0
}) => {
  if (!model || !model.context.edit) return null
  const isOsasuoritus = nestedLevel === 0
  const isAlaosasuoritus = nestedLevel === 1
  const luokkaAste = modelData(model, 'koulutusmoduuli.tunniste.koodiarvo')

  const addOsasuoritus = (koulutusmoduuli) => {
    const baseOsasuorituksetModel = modelSet(
      newOsasuoritusProto(model, koulutusmoduuli.parent.value.classes[0]),
      modelSetTitle(
        koulutusmoduuli,
        t(modelData(koulutusmoduuli, 'tunniste.nimi'))
      ),
      'koulutusmoduuli'
    )

    // Pusketaan ensin base-osasuoritus changeBus:iin
    pushModel(baseOsasuorituksetModel)
    ensureArrayKey(baseOsasuorituksetModel)

    // Suorituskielen ja kielioppiaineen kielivalinnan nollaus
    if (isOsasuoritus) {
      if (
        modelProperty(baseOsasuorituksetModel, 'koulutusmoduuli.kieli') !==
        undefined
      ) {
        pushModel(
          modelSetValues(baseOsasuorituksetModel, {
            suorituskieli: zeroValue,
            'koulutusmoduuli.kieli': zeroValue
          })
        )
      } else {
        pushModel(
          modelSetValues(baseOsasuorituksetModel, {
            suorituskieli: zeroValue
          })
        )
      }
    }

    if (isOsasuoritus) {
      // Jos osasuoritukselle löytyy alaosasuorituksia, prefillataan ne.
      luokkaAsteenOsasuorituksenAlaosasuoritukset(
        luokkaAste,
        modelData(koulutusmoduuli, 'tunniste.koodiarvo') || 'NULL'
      ).onValue((alaosasuorituksetPrefillattuEditorModel) => {
        if (
          Array.isArray(alaosasuorituksetPrefillattuEditorModel.value) &&
          alaosasuorituksetPrefillattuEditorModel.value.length > 0
        ) {
          const osasuoritusModel = contextualizeSubModel(
            alaosasuorituksetPrefillattuEditorModel,
            baseOsasuorituksetModel,
            'osasuoritukset'
          )
          pushModel(osasuoritusModel)
        }
      })
    }

    if (isAlaosasuoritus) {
      // wrapOptional poistaa valinnaisuuden modelista, jotta seuraavassa vaiheessa voidaan resolvaa uuden arvioinnin model käyttämällä sen arrayPrototypeä
      const arviointiArrayModel = wrapOptional(
        contextualizeModel(
          modelLookup(wrapOptional(baseOsasuorituksetModel), 'arviointi'),
          model.context
        )
      )

      // console.log('arviointiArray', arviointiArrayModel)

      const uusiArviointiModel = contextualizeSubModel(
        arviointiArrayModel.arrayPrototype,
        arviointiArrayModel,
        modelItems(arviointiArrayModel).length
      )

      // console.log('uusiArviointiModel', uusiArviointiModel)

      const resolvedArviointimodel = isOneOfModel(uusiArviointiModel)
        ? resolveActualModel(uusiArviointiModel, uusiArviointiModel.parent)
        : uusiArviointiModel

      // console.log('resolved', resolvedArviointimodel)
      /* console.log(
        'Path of resolved arviointi model',
        resolvedArviointimodel.path
      ) */
      pushModel(resolvedArviointimodel)
    }
  }

  const suoritusProtos = newOsasuoritusProtos(model)

  return (
    <UusiEshOsasuoritusDropdown
      osasuoritukset={suoritusProtos}
      resultCallback={addOsasuoritus}
      isAlaosasuoritus={isAlaosasuoritus}
      pakollinen={true}
    />
  )
}
