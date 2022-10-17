import React from 'baret'
import Bacon from 'baconjs'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import {
  accumulateModelStateAndValidity,
  modelData,
  modelItems,
  modelLookup,
  modelProperties,
  pushModelValue
} from '../editor/EditorModel'
import ModalDialog from '../editor/ModalDialog'
import { doActionWhileMounted } from '../util/util'
import { UusiPerusopetuksenOppiaineDropdown } from '../perusopetus/UusiPerusopetuksenOppiaineDropdown'
import Text from '../i18n/Text'
import {
  copyToimipiste,
  newSuoritusProto,
  nuortenPerusopetuksenOppiaineenOppimääränSuoritus,
  perusopetuksenOppiaineenOppimääränSuoritus,
  suorituksenTyyppi
} from '../suoritus/Suoritus'

const UusiPerusopetuksenOppiaineenSuoritusPopup = ({
  opiskeluoikeus,
  resultCallback
}) => {
  const koulutusmoduuli = (suoritus) => modelLookup(suoritus, 'koulutusmoduuli')
  const submitBus = Bacon.Bus()
  const isAikuistenPerusopetus =
    modelData(opiskeluoikeus, 'tyyppi.koodiarvo') === 'aikuistenperusopetus'
  let initialSuoritusModel = newSuoritusProto(
    opiskeluoikeus,
    isAikuistenPerusopetus
      ? 'aikuistenperusopetuksenoppiaineenoppimaaransuoritus'
      : 'nuortenperusopetuksenoppiaineenoppimaaransuoritus'
  )
  const edellinenOppiaine = isAikuistenPerusopetus
    ? perusopetuksenOppiaineenOppimääränSuoritus(opiskeluoikeus)
    : nuortenPerusopetuksenOppiaineenOppimääränSuoritus(opiskeluoikeus)
  if (edellinenOppiaine) {
    initialSuoritusModel = copyToimipiste(
      edellinenOppiaine,
      initialSuoritusModel
    )
  }
  const { modelP, errorP } =
    accumulateModelStateAndValidity(initialSuoritusModel)
  const validP = errorP.not()

  return (
    <ModalDialog
      className="lisaa-suoritus-modal"
      onDismiss={resultCallback}
      onSubmit={() => submitBus.push()}
      okTextKey="Lisää"
      validP={validP}
    >
      <h2>
        <Text name="Suorituksen lisäys" />
      </h2>
      {modelP.map((oppiaineenSuoritus) => {
        return (
          <div key="props">
            <PropertiesEditor
              context={oppiaineenSuoritus.context}
              properties={modelProperties(oppiaineenSuoritus, [
                'koulutusmoduuli.tunniste',
                'koulutusmoduuli.kieli',
                'toimipiste'
              ])}
              getValueEditor={(p, getDefault) => {
                return p.key == 'tunniste' ? (
                  <UusiPerusopetuksenOppiaineDropdown
                    organisaatioOid={modelData(
                      oppiaineenSuoritus,
                      'toimipiste.oid'
                    )}
                    oppiaineenSuoritus={oppiaineenSuoritus}
                    selected={koulutusmoduuli(oppiaineenSuoritus)}
                    resultCallback={(oppiaine) =>
                      pushModelValue(
                        oppiaineenSuoritus,
                        oppiaine.value,
                        'koulutusmoduuli'
                      )
                    }
                    pakollinen={true}
                    enableFilter={false}
                    suoritukset={modelItems(opiskeluoikeus, 'suoritukset')}
                    allowSelectingDuplicates={true}
                  />
                ) : (
                  getDefault()
                )
              }}
            />
          </div>
        )
      })}

      {doActionWhileMounted(
        modelP.sampledBy(submitBus.filter(validP)),
        resultCallback
      )}
    </ModalDialog>
  )
}

UusiPerusopetuksenOppiaineenSuoritusPopup.canAddSuoritus = (opiskeluoikeus) => {
  return (
    ['aikuistenperusopetus', 'perusopetus'].includes(
      modelData(opiskeluoikeus, 'tyyppi.koodiarvo')
    ) &&
    !!modelItems(opiskeluoikeus, 'suoritukset').find(
      (suoritus) =>
        suorituksenTyyppi(suoritus) === 'perusopetuksenoppiaineenoppimaara' ||
        suorituksenTyyppi(suoritus) ===
          'nuortenperusopetuksenoppiaineenoppimaara'
    )
  )
}

UusiPerusopetuksenOppiaineenSuoritusPopup.addSuoritusTitle = () => (
  <Text name="lisää oppiaineen suoritus" />
)

export default UusiPerusopetuksenOppiaineenSuoritusPopup
