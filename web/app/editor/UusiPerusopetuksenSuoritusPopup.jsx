import React from 'baret'
import Bacon from 'baconjs'
import R from 'ramda'
import Http from '../http'
import * as L from 'partial.lenses'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {
  contextualizeSubModel,
  modelItems,
  modelLookup,
  accumulateModelStateAndValidity,
  modelSet,
  addContext,
  modelData,
  modelLens,
  modelProperties,
  modelSetValue,
  pushModelValue
} from './EditorModel'
import {EnumEditor} from './EnumEditor.jsx'
import ModalDialog from './ModalDialog.jsx'
import {doActionWhileMounted} from '../util'
import {isToimintaAlueittain} from './PerusopetuksenOppiaineetEditor.jsx'
import {UusiPerusopetuksenOppiaineDropdown} from './UusiPerusopetuksenOppiaineDropdown.jsx'
import Text from '../Text.jsx'

const UusiPerusopetuksenSuoritusPopup = ({opiskeluoikeus, resultCallback}) => isOppiaineenSuoritus(opiskeluoikeus)
  ? oppiaineenSuoritusPopup({opiskeluoikeus, resultCallback})
  : vuosiluokanSuoritusPopup({opiskeluoikeus, resultCallback})

UusiPerusopetuksenSuoritusPopup.canAddSuoritus = (opiskeluoikeus) => {
    let tyyppi = modelData(opiskeluoikeus, 'tyyppi.koodiarvo')
    return tyyppi == 'perusopetus' && puuttuvatLuokkaAsteet(opiskeluoikeus).length > 0
  }
UusiPerusopetuksenSuoritusPopup.addSuoritusTitle = (opiskeluoikeus) =>
  <Text name={isOppiaineenSuoritus(opiskeluoikeus) ? 'lisää oppiaineen suoritus' : 'lisää vuosiluokan suoritus'}/>

let isOppiaineenSuoritus = (opiskeluoikeus) => modelData(opiskeluoikeus, 'suoritukset').map(suoritus => suoritus.tyyppi.koodiarvo).includes('perusopetuksenoppiaineenoppimaara')

export default UusiPerusopetuksenSuoritusPopup

let oppiaineenSuoritusPopup = ({opiskeluoikeus, resultCallback}) => {
  let koulutusmoduuli = (suoritus) => modelLookup(suoritus, 'koulutusmoduuli')
  let submitBus = Bacon.Bus()
  let initialSuoritusModel = newSuoritusProto(opiskeluoikeus, 'perusopetuksenoppiaineenoppimaaransuoritus')
  let { modelP, errorP } = accumulateModelStateAndValidity(initialSuoritusModel)
  let validP = errorP.not()

  return (<ModalDialog className="lisaa-suoritus-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()} okTextKey="Lisää" validP={validP}>
    <h2><Text name="Suorituksen lisäys"/></h2>
    {
      modelP.map(oppiaineenSuoritus => {
        return (<div key="props">
          <PropertiesEditor
            context={oppiaineenSuoritus.context}
            properties={modelProperties(oppiaineenSuoritus, ['koulutusmoduuli.tunniste', 'koulutusmoduuli.kieli', 'toimipiste'])}
            getValueEditor={(p, getDefault) => {
              return p.key == 'tunniste'
                ? <UusiPerusopetuksenOppiaineDropdown
                    oppiaineenSuoritus={oppiaineenSuoritus}
                    selected={koulutusmoduuli(oppiaineenSuoritus)}
                    resultCallback={oppiaine => pushModelValue(oppiaineenSuoritus, oppiaine.value, 'koulutusmoduuli')}
                    pakollinen={true} enableFilter={false}
                    suoritukset={modelItems(opiskeluoikeus, 'suoritukset')}
                  />
                : getDefault()
              }
            }
          />
        </div>)
      })
    }

    { doActionWhileMounted(modelP.sampledBy(submitBus.filter(validP)), resultCallback) }
  </ModalDialog>)
}

let vuosiluokanSuoritusPopup = ({opiskeluoikeus, resultCallback}) => {
  let submitBus = Bacon.Bus()
  let initialSuoritusModel = newSuoritusProto(opiskeluoikeus, 'perusopetuksenvuosiluokansuoritus')

  initialSuoritusModel = L.modify(L.compose(modelLens('koulutusmoduuli.tunniste'), 'alternativesPath'), (url => url + '/' + puuttuvatLuokkaAsteet(opiskeluoikeus).join(',')) , initialSuoritusModel)
  let viimeisin = viimeisinLuokkaAste(opiskeluoikeus)
  if (viimeisin) {
    initialSuoritusModel = modelSet(initialSuoritusModel, modelLookup(viimeisin, 'toimipiste'), 'toimipiste')
  }

  initialSuoritusModel = addContext(initialSuoritusModel, { editAll: true })

  let luokkaAsteP = valittuLuokkaAsteP(initialSuoritusModel)
  return (<div>
    {
      Bacon.combineWith(luokkaAsteP.last(), osasuorituksetP(initialSuoritusModel, luokkaAsteP, isToimintaAlueittain(opiskeluoikeus)).last(), (valittuLuokkaAste, osasuoritukset) => {
        initialSuoritusModel = modelSetValue(initialSuoritusModel, valittuLuokkaAste, 'koulutusmoduuli.tunniste')
        initialSuoritusModel = modelSetValue(initialSuoritusModel, osasuoritukset.value, 'osasuoritukset')
        initialSuoritusModel = addContext(initialSuoritusModel, { suoritus: initialSuoritusModel })

        let { modelP, errorP } = accumulateModelStateAndValidity(initialSuoritusModel)

        let hasToimipisteP = modelP.map(m => !!modelData(m, 'toimipiste.oid'))
        let validP = errorP.not().and(hasToimipisteP)

        return (<div>
          <ModalDialog className="lisaa-suoritus-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()} okTextKey="Lisää" validP={validP}>
            <h2><Text name="Suorituksen lisäys"/></h2>
            <PropertiesEditor baret-lift context={initialSuoritusModel.context} properties={modelP.map(model => modelProperties(model, ['koulutusmoduuli.perusteenDiaarinumero', 'koulutusmoduuli.tunniste', 'luokka', 'toimipiste']))} />
          </ModalDialog>
          { doActionWhileMounted(modelP.sampledBy(submitBus.filter(validP)), resultCallback) }
        </div>)
      })
    }
  </div>)
}

let newSuoritusProto = (opiskeluoikeus, prototypeKey) => {
  let suoritukset = modelLookup(opiskeluoikeus, 'suoritukset')
  let indexForNewItem = modelItems(suoritukset).length
  let selectedProto = contextualizeSubModel(suoritukset.arrayPrototype, suoritukset, indexForNewItem).oneOfPrototypes.find(p => p.key === prototypeKey)
  return contextualizeSubModel(selectedProto, suoritukset, indexForNewItem)
}

let valittuLuokkaAsteP = (model) => {
  let luokkaAsteLens = modelLens('koulutusmoduuli.tunniste')
  let luokkaAsteModel = L.get(luokkaAsteLens, model)
  return EnumEditor.fetchAlternatives(luokkaAsteModel).map('.0')
}

let osasuorituksetP = (model, luokkaAsteP, toimintaAlueittain) =>
  luokkaAsteP.map('.data').flatMapLatest(data => {
    if (!data) return []
    return Http.cachedGet(`/koski/api/editor/suoritukset/prefill/${data.koodistoUri}/${data.koodiarvo}?toimintaAlueittain=${toimintaAlueittain}`)
  }).toProperty()

let puuttuvatLuokkaAsteet = (opiskeluoikeus) => {
  var olemassaOlevatLuokkaAsteet = olemassaolevatLuokkaAsteenSuoritukset(opiskeluoikeus).filter(siirretäänSeuraavalleLuokalle).map(suorituksenLuokkaAste)
  return [1, 2, 3, 4, 5, 6, 7, 8, 9].filter(x => !olemassaOlevatLuokkaAsteet.includes(x))
}

let siirretäänSeuraavalleLuokalle = (suoritus) => !modelData(suoritus, 'jääLuokalle')

let olemassaolevatLuokkaAsteenSuoritukset = (opiskeluoikeus) => modelItems(opiskeluoikeus, 'suoritukset')
  .filter(suoritus => modelData(suoritus, 'tyyppi.koodiarvo') == 'perusopetuksenvuosiluokka')

let suorituksenLuokkaAste = (suoritus) => parseInt(modelData(suoritus, 'koulutusmoduuli.tunniste.koodiarvo'))

let viimeisinLuokkaAste = (opiskeluoikeus) => {
  let suoritukset = olemassaolevatLuokkaAsteenSuoritukset(opiskeluoikeus)
  if (suoritukset.length) {
    return suoritukset.reduce(R.maxBy(suorituksenLuokkaAste))
  }
}