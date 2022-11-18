import React from 'baret'
import {
  contextualizeSubModel,
  ensureArrayKey,
  modelData,
  modelProperty,
  modelSet,
  modelSetTitle,
  modelSetValues,
  pushModel
} from '../editor/EditorModel'
import { zeroValue } from '../editor/EnumEditor'
import { t } from '../i18n/i18n'
import { newOsasuoritusProto, newOsasuoritusProtos } from '../suoritus/Suoritus'
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
    const baseOsasuoritusModel = modelSet(
      newOsasuoritusProto(model, koulutusmoduuli.parent.value.classes[0]),
      modelSetTitle(
        koulutusmoduuli,
        t(modelData(koulutusmoduuli, 'tunniste.nimi'))
      ),
      'koulutusmoduuli'
    )

    // Pusketaan ensin base-osasuoritus changeBus:iin
    pushModel(baseOsasuoritusModel)
    ensureArrayKey(baseOsasuoritusModel)

    // Suorituskielen ja kielioppiaineen kielivalinnan nollaus
    if (isOsasuoritus) {
      if (
        modelProperty(baseOsasuoritusModel, 'koulutusmoduuli.kieli') !==
          undefined &&
        // Ei tyhjätä koulutusmoduulin kieltä Ancient Greek ja Latin -kielioppiaineille
        !koulutusmoduuli.value.classes.includes(
          'europeanschoolofhelsinkikielioppiaineancientgreek'
        ) &&
        !koulutusmoduuli.value.classes.includes(
          'europeanschoolofhelsinkikielioppiainelatin'
        )
      ) {
        pushModel(
          modelSetValues(baseOsasuoritusModel, {
            suorituskieli: zeroValue,
            'koulutusmoduuli.kieli': zeroValue
          })
        )
      } else if (
        modelProperty(baseOsasuoritusModel, 'suorituskieli') !== undefined
      ) {
        pushModel(
          modelSetValues(baseOsasuoritusModel, {
            suorituskieli: zeroValue
          })
        )
      }
    }

    // Täytetään P1-P5 ja S1-S7 -vuosiluokkien alaosasuoritukset, muttei EB-tutkinnon
    if (isOsasuoritus && luokkaAste !== '301104') {
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
            baseOsasuoritusModel,
            'osasuoritukset'
          )
          pushModel(osasuoritusModel)
        }
      })
    }
  }

  const suoritusProtos = newOsasuoritusProtos(model)

  return (
    <UusiEshOsasuoritusDropdown
      model={model}
      osasuoritukset={suoritusProtos}
      resultCallback={addOsasuoritus}
      isAlaosasuoritus={isAlaosasuoritus}
      pakollinen={true}
    />
  )
}
