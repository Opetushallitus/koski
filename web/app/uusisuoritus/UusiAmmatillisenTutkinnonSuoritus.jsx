import React from 'baret'
import Atom from 'bacon.atom'
import Bacon from 'baconjs'
import R from 'ramda'
import * as L from 'partial.lenses'
import {PropertiesEditor} from '../editor/PropertiesEditor.jsx'
import {
  accumulateModelStateAndValidity,
  addContext,
  modelData,
  modelItems,
  modelLens,
  modelLookup,
  modelProperties, modelSetValue,
  modelSetValues,
  modelTitle, modelValueLens,
  pushModel
} from '../editor/EditorModel'
import {lang, t} from '../i18n'
import ModalDialog from '../editor/ModalDialog.jsx'
import {doActionWhileMounted} from '../util'
import Text from '../Text.jsx'
import {
  copyToimipiste, newSuoritusProto, näyttötutkintoonValmistavanKoulutuksenSuoritus,
  suorituksenTyyppi
} from '../editor/Suoritus'
import SuoritustapaDropdown from '../uusioppija/SuoritustapaDropdown.jsx'
import TutkintoAutocomplete from '../TutkintoAutocomplete.jsx'
import {enumValueToKoodiviiteLens, toKoodistoEnumValue} from '../koodistot'

let tutkintoLens = L.lens(
  (m) => {
    if (modelData(m).tunniste) {
      return { tutkintoKoodi: modelData(m).tunniste.koodiarvo, diaarinumero: modelData(m).perusteenDiaarinumero, nimi: { [lang]: modelLookup(m, 'tunniste').value.title} }
    }
  },
  (tutkinto, m) => {
    return modelSetValues(m, {
      'tunniste': toKoodistoEnumValue('koulutus', tutkinto.tutkintoKoodi, t(tutkinto.nimi)),
      'perusteenDiaarinumero': { data: tutkinto.diaarinumero}
    })
  }
)
let koulutusModuuliTutkintoLens = L.compose(modelLens('koulutusmoduuli'), tutkintoLens)
let suoritusTapaKoodiarvoLens = L.compose(modelLens('suoritustapa'), modelValueLens, enumValueToKoodiviiteLens)

const UusiAmmatillisenTutkinnonSuoritus = ({opiskeluoikeus, resultCallback}) => {
  let koulutusmoduuli = (suoritus) => modelLookup(suoritus, 'koulutusmoduuli')
  let submitBus = Bacon.Bus()
  let initialSuoritusModel = newSuoritusProto(opiskeluoikeus, 'ammatillisentutkinnonsuoritus')
  initialSuoritusModel = addContext(initialSuoritusModel, { editAll: true })
  initialSuoritusModel = copyToimipiste(modelItems(opiskeluoikeus, 'suoritukset')[0], initialSuoritusModel)
  let valmistavanKoulutuksenTutkinto = modelLookup(näyttötutkintoonValmistavanKoulutuksenSuoritus(opiskeluoikeus), 'tutkinto')
  if (valmistavanKoulutuksenTutkinto) {
    initialSuoritusModel = modelSetValue(initialSuoritusModel, valmistavanKoulutuksenTutkinto.value, 'koulutusmoduuli')
  }


  console.log('t', valmistavanKoulutuksenTutkinto)

  // TODO: default tutkinto näyttötutkintoon valmistavasta opetuksesta

  let { modelP, errorP } = accumulateModelStateAndValidity(initialSuoritusModel)
  let validP = errorP.not()


  return (<ModalDialog className="lisaa-suoritus-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()} okTextKey="Lisää" validP={validP}>
    <h2><Text name="Suorituksen lisäys"/></h2>
    {
      modelP.map(oppiaineenSuoritus => {
        let foundProperties = modelProperties(oppiaineenSuoritus, ['toimipiste', 'koulutusmoduuli', 'suoritustapa'])
        let tutkintoAtom = Atom(L.get(koulutusModuuliTutkintoLens, oppiaineenSuoritus))
        let suoritustapaAtom = Atom(L.get(suoritusTapaKoodiarvoLens, oppiaineenSuoritus))
        tutkintoAtom.changes().filter(R.identity).forEach(tutkinto => {
          let updatedModel = L.set(koulutusModuuliTutkintoLens, tutkinto, oppiaineenSuoritus)
          pushModel(updatedModel)
        })
        suoritustapaAtom.changes().filter(R.identity).forEach(suoritustapa => {
          let updatedModel = L.set(suoritusTapaKoodiarvoLens, suoritustapa, oppiaineenSuoritus)
          pushModel(updatedModel)
        })
        return (<div key="props">
          <PropertiesEditor
            editAll={true}
            context={oppiaineenSuoritus.context}
            properties={foundProperties}
            getValueEditor={(p, getDefault) => {
              switch(p.key) {
                case 'koulutusmoduuli':
                  return <TutkintoAutocomplete tutkintoAtom={tutkintoAtom} oppilaitosP={Bacon.constant(modelData(opiskeluoikeus, 'oppilaitos'))}/>
                case 'suoritustapa':
                  let tutkinto = modelData(oppiaineenSuoritus, 'koulutusmoduuli')
                  return<SuoritustapaDropdown diaarinumero={tutkinto.perusteenDiaarinumero} suoritustapaAtom={suoritustapaAtom}/>
                default: return getDefault()
              }
            }}
          />
        </div>)
      })
    }

    { doActionWhileMounted(modelP.sampledBy(submitBus.filter(validP)), resultCallback) }
  </ModalDialog>)
}

UusiAmmatillisenTutkinnonSuoritus.canAddSuoritus = (opiskeluoikeus) => {
  return modelData(opiskeluoikeus, 'tyyppi.koodiarvo') == 'ammatillinenkoulutus' &&
    !modelItems(opiskeluoikeus, 'suoritukset').find(suoritus => suorituksenTyyppi(suoritus) == 'ammatillinentutkinto')
}

UusiAmmatillisenTutkinnonSuoritus.addSuoritusTitle = () =>
  <Text name="lisää ammatillisen tutkinnon suoritus"/>

export default UusiAmmatillisenTutkinnonSuoritus