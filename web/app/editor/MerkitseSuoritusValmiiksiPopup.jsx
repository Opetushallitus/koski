import {
  modelLookup,
  accumulateModelStateAndValidity,
  optionalPrototypeModel,
  modelSet,
  modelSetValue,
  modelItems,
  modelData,
  addContext,
  pushModel
} from './EditorModel'
import React from 'baret'
import Http from '../http'
import Bacon from 'baconjs'
import R from 'ramda'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import ModalDialog from './ModalDialog.jsx'
import {setTila} from './Suoritus'
import {JääLuokalleTaiSiirretäänEditor} from './JaaLuokalleTaiSiirretaanEditor.jsx'
import {saveOrganizationalPreference} from '../organizationalPreferences'
import Text from '../Text.jsx'

export const MerkitseSuoritusValmiiksiPopup = ({ suoritus, resultCallback }) => {
  let submitBus = Bacon.Bus()
  let vahvistus = optionalPrototypeModel(modelLookup(suoritus, 'vahvistus'))
  suoritus = modelSet(suoritus, vahvistus, 'vahvistus')
  let toimipiste = suoritus.context.toimipiste
  suoritus = modelSetValue(suoritus, toimipiste.value, 'vahvistus.myöntäjäOrganisaatio')
  suoritus = setTila(suoritus, 'VALMIS')
  let { modelP, errorP } = accumulateModelStateAndValidity(suoritus)
  let validP = errorP.not()

  modelP.sampledBy(submitBus).onValue(updatedSuoritus => {
    let saveResults = modelItems(updatedSuoritus, 'vahvistus.myöntäjäHenkilöt').filter(h => h.value.newItem).map(h => {
      let data = modelData(h)
      let key = data.nimi
      let organisaatioOid = modelData(updatedSuoritus, 'toimipiste').oid
      return saveOrganizationalPreference(organisaatioOid, 'myöntäjät', key, data)
    })
    Bacon.combineAsArray(saveResults).onValue(() => resultCallback(updatedSuoritus))
  })

  let kotipaikkaE = modelP.map(m => modelData(m, 'vahvistus.myöntäjäOrganisaatio').oid).skipDuplicates().flatMapLatest(oid => Http.cachedGet(`/koski/api/editor/organisaatio/${oid}/kotipaikka`)).filter(R.identity)

  modelP.sampledBy(kotipaikkaE, (model, kotipaikka) => [model, kotipaikka]).onValue(([model, kotipaikka]) => {
    pushModel(modelSetValue(model, kotipaikka, 'vahvistus.paikkakunta'))
  })

  return (<ModalDialog className="merkitse-valmiiksi-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()} submitOnEnterKey="false" okTextKey="Merkitse valmiiksi" validP={validP}>
    <h2><Text name="Suoritus valmis"/></h2>
    <PropertiesEditor baret-lift model={modelP.map(s => setOrgToContext(modelLookup(s, 'vahvistus')))}  />
    <JääLuokalleTaiSiirretäänEditor baret-lift model={modelP}/>
  </ModalDialog>)
}

let setOrgToContext = (vahvistus) => {
  let myöntäjäOrganisaatio = modelLookup(vahvistus, 'myöntäjäOrganisaatio')
  return addContext(vahvistus, { myöntäjäOrganisaatio })
}