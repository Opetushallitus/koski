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
  pushModelValue,
  resolvePrototypeReference
} from '../editor/EditorModel'
import { t } from '../i18n/i18n'
import { newOsasuoritusProto, newOsasuoritusProtos } from '../suoritus/Suoritus'
import {
  eshSuoritus,
  eshSynteettisetKoodistot
} from './europeanschoolofhelsinkiSuoritus'
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

/**
 * Selvittää synteettisen koodiston oletusarvon ja URI:n suorituksen tunnisteen ja mallinimien perusteella
 * @param {string} tunniste
 * @param {string[]} modelValueClasses
 * @returns {[string, string]}
 */
const resolveSyntheticKoodisto = (tunniste, modelValueClasses) => {
  // S7
  if (modelValueClasses.includes(eshSuoritus.secondaryUppers7)) {
    return [eshSynteettisetKoodistot.preliminary, '0']
  }
  // S6, S5, S4
  if (tunniste === 'S6' || tunniste === 'S5' || tunniste === 'S4') {
    return [eshSynteettisetKoodistot.numerical, '0']
  }
  throw new Error(`No synthetic koodisto found for osasuoritus ${tunniste}`)
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

    // Huom. seuraava koodi on vain synteettisiä koodistoja käyttäviä suorituksia varten
    // Vain S7 alaosasuoritukset ja S6-, S5- ja S4- osasuoritukset täytetään automaattisesti
    // ESH:n S7, S6, S5 ja S4 -vuosiluokan suorituksille on lisättävä oikea prototype synteettiselle koodistolle.
    if (
      (isAlaosasuoritus && isS7(model)) ||
      isS6(tunniste, model) ||
      isS5OrS4(tunniste, model)
    ) {
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
        key: 'secondarynumericalmarkarviointi'
      }

      const resolvedArviointiModel = contextualizeSubModel(
        resolvePrototypeReference(proto, uusiArviointiModel.context),
        uusiArviointiModel
      )

      const [koodistoUri, _defaultKoodiarvo] = resolveSyntheticKoodisto(
        tunniste,
        model.value.classes
      )

      // Pusketaan koodistoUri changeBus:iin
      pushModelValue(
        resolvedArviointiModel,
        {
          data: koodistoUri
        },
        'arvosana.koodistoUri'
      )
    } else if (isS1OrS2OrS3(tunniste, model)) {
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
        key: 'secondarygradearviointi'
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
