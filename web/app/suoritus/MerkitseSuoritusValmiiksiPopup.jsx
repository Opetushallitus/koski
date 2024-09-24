import {
  accumulateModelStateAndValidity,
  addContext,
  modelData,
  modelItems,
  modelLookup,
  modelSet,
  modelSetValue,
  optionalPrototypeModel,
  pushModel
} from '../editor/EditorModel'
import React from 'baret'
import Http from '../util/http'
import Bacon from 'baconjs'
import * as R from 'ramda'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import ModalDialog from '../editor/ModalDialog'
import { JääLuokalleTaiSiirretäänEditor } from './JaaLuokalleTaiSiirretaanEditor'
import { saveOrganizationalPreference } from '../virkailija/organizationalPreferences'
import Text from '../i18n/Text'
import { showError } from '../util/location'

export const MerkitseSuoritusValmiiksiPopup = ({
  suoritus,
  resultCallback
}) => {
  const submitBus = Bacon.Bus()
  const vahvistus = optionalPrototypeModel(modelLookup(suoritus, 'vahvistus'))
  suoritus = modelSet(suoritus, vahvistus, 'vahvistus')
  const toimipiste = suoritus.context.toimipiste
  suoritus = modelSetValue(
    suoritus,
    toimipiste.value,
    'vahvistus.myöntäjäOrganisaatio'
  )
  const { modelP, errorP } = accumulateModelStateAndValidity(suoritus)
  const validP = errorP.not()

  modelP.sampledBy(submitBus).onValue((updatedSuoritus) => {
    const saveResults = modelItems(
      updatedSuoritus,
      'vahvistus.myöntäjäHenkilöt'
    )
      .filter((h) => h.value.newItem)
      .map((h) => {
        const data = modelData(h)
        const key = data.nimi
        const organisaatioOid = modelData(updatedSuoritus, 'toimipiste').oid
        const koulutustoimijaOid = modelData(
          suoritus.context.opiskeluoikeus,
          'koulutustoimija.oid'
        )
        return saveOrganizationalPreference(
          organisaatioOid,
          'myöntäjät',
          key,
          data,
          koulutustoimijaOid
        )
      })
    Bacon.combineAsArray(saveResults).onValue(() =>
      resultCallback(updatedSuoritus)
    )
  })

  const paikkakuntaModel = modelLookup(suoritus, 'vahvistus.paikkakunta')
  if (paikkakuntaModel) {
    const kotipaikkaE = modelP
      .map((m) => modelData(m, 'vahvistus.myöntäjäOrganisaatio').oid)
      .skipDuplicates()
      .flatMapLatest(getKotipaikka)
      .filter(R.identity)

    modelP
      .sampledBy(kotipaikkaE, (model, kotipaikka) => [model, kotipaikka])
      .onValue(([model, kotipaikka]) => {
        pushModel(modelSetValue(model, kotipaikka, 'vahvistus.paikkakunta'))
      })
  }

  return (
    <ModalDialog
      className="merkitse-valmiiksi-modal"
      onDismiss={resultCallback}
      onSubmit={() => submitBus.push()}
      submitOnEnterKey="false"
      okTextKey="Merkitse valmiiksi"
      validP={validP}
    >
      <h2>
        <Text name="Suoritus valmis" />
      </h2>
      <PropertiesEditor
        baret-lift
        model={modelP.map((s) => setOrgToContext(modelLookup(s, 'vahvistus')))}
      />
      <JääLuokalleTaiSiirretäänEditor baret-lift model={modelP} />
    </ModalDialog>
  )
}

const setOrgToContext = (vahvistus) => {
  const myöntäjäOrganisaatio = modelLookup(vahvistus, 'myöntäjäOrganisaatio')
  return addContext(vahvistus, { myöntäjäOrganisaatio })
}

const getKotipaikka = (oid) =>
  Http.cachedGet(`/koski/api/editor/organisaatio/${oid}/kotipaikka`, {
    errorHandler: (e) => {
      if (e.httpStatus !== 404) {
        showError(e)
      }
    }
  })
