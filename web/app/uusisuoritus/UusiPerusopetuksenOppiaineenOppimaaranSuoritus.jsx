import React from 'baret'
import Bacon from 'baconjs'
import {PropertiesEditor} from '../editor/PropertiesEditor.jsx'
import {
  accumulateModelStateAndValidity,
  modelData,
  modelItems,
  modelLookup,
  modelProperties,
  pushModelValue
} from '../editor/EditorModel'
import ModalDialog from '../editor/ModalDialog.jsx'
import {doActionWhileMounted} from '../util'
import {UusiPerusopetuksenOppiaineDropdown} from '../editor/UusiPerusopetuksenOppiaineDropdown.jsx'
import Text from '../Text.jsx'
import {newSuoritusProto, suorituksenTyyppi} from '../editor/Suoritus'

const UusiPerusopetuksenOppiaineenSuoritusPopup = ({opiskeluoikeus, resultCallback}) => {
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

UusiPerusopetuksenOppiaineenSuoritusPopup.canAddSuoritus = (opiskeluoikeus) => {
  return modelData(opiskeluoikeus, 'tyyppi.koodiarvo') == 'aikuistenperusopetus' &&
    !!modelItems(opiskeluoikeus, 'suoritukset').find(suoritus => suorituksenTyyppi(suoritus) == 'perusopetuksenoppiaineenoppimaara')
}

UusiPerusopetuksenOppiaineenSuoritusPopup.addSuoritusTitle = () =>
  <Text name="lisää oppiaineen suoritus"/>

export default UusiPerusopetuksenOppiaineenSuoritusPopup