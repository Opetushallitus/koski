import React from 'baret'
import {
  contextualizeModel,
  contextualizeSubModel,
  ensureArrayKey,
  modelData,
  modelItems,
  modelLookup,
  modelSet,
  modelSetTitle,
  pushModel,
  resolvePrototypeReference
} from '../editor/EditorModel'
import { t } from '../i18n/i18n'
import { newOsasuoritusProto, newOsasuoritusProtos } from '../suoritus/Suoritus'
import { UusiEshOsasuoritusDropdown } from './UusiEshOsasuoritusDropdown'

function resolveArrayPrototype(model) {
  if (model.arrayPrototype !== undefined) {
    return model.arrayPrototype
  }
  if (
    model.optionalPrototype !== undefined &&
    model.optionalPrototype.arrayPrototype !== undefined
  ) {
    return model.optionalPrototype.arrayPrototype
  }
}

const isS7 = (model) =>
  model.value.classes.includes('secondaryupperoppiaineensuorituss7')
const isS6 = (tunniste, model) =>
  tunniste === 'S6' &&
  model.value.classes.includes('secondaryuppervuosiluokansuoritus')
const isS5OrS4 = (tunniste, model) =>
  (tunniste === 'S5' || tunniste === 'S4') &&
  model.value.classes.includes('secondarylowervuosiluokansuoritus')
const isS1OrS2OrS3 = (tunniste, _model) =>
  tunniste === 'S1' || tunniste === 'S2' || tunniste === 'S3'

export const UusiEuropeanSchoolOfHelsinkiOsasuoritusDropdown = ({
  model,
  nestedLevel = 0
}) => {
  if (!model || !model.context.edit) return null
  const isAlaosasuoritus = nestedLevel > 0
  const tunniste = modelData(model, 'koulutusmoduuli.tunniste.koodiarvo')

  // TODO: TOR-1685: Refaktoroi tämä siistimmäksi, jotta copypastea on vähemmän. Prototyyppien valinta olisi myös hyvä saada hieman siistimmäksi.
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

    const protoKey =
      isAlaosasuoritus && isS7(model)
        ? 'secondarys7preliminarymarkarviointi'
        : isS5OrS4(tunniste, model) || isS6(tunniste, model)
        ? 'secondarynumericalmarkarviointi'
        : isS1OrS2OrS3(tunniste, model)
        ? 'secondarygradearviointi'
        : undefined

    if (protoKey !== undefined) {
      // Haetaan arvioinnille arraymodel
      const arviointiArrayModel = contextualizeModel(
        modelLookup(baseOsasuorituksetModel, 'arviointi'),
        model.context
      )
      // Luodaan arvioinnille uusi model, jolle syötetään arrayPrototype
      const uusiArviointiModel = contextualizeSubModel(
        resolveArrayPrototype(arviointiArrayModel),
        arviointiArrayModel,
        modelItems(arviointiArrayModel).length
      )

      const proto = {
        type: 'prototype',
        key: protoKey
      }

      const resolvedArviointiModel = contextualizeSubModel(
        resolvePrototypeReference(proto, uusiArviointiModel.context),
        uusiArviointiModel
      )

      pushModel(resolvedArviointiModel)
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
