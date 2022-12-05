import React from 'baret'
import Bacon from 'baconjs'
import * as R from 'ramda'
import * as L from 'partial.lenses'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import {
  accumulateModelStateAndValidity,
  addContext,
  modelData,
  modelItems,
  modelLens,
  modelProperties,
  modelSetValue
} from '../editor/EditorModel'
import { EnumEditor } from '../editor/EnumEditor'
import ModalDialog from '../editor/ModalDialog'
import { doActionWhileMounted } from '../util/util'
import Text from '../i18n/Text'
import {
  isToimintaAlueittain,
  luokkaAste,
  luokkaAsteenOsasuoritukset
} from '../perusopetus/Perusopetus'
import {
  copyToimipiste,
  newSuoritusProto,
  nuortenPerusopetuksenOppiaineenOppimääränSuoritus
} from '../suoritus/Suoritus'

const UusiPerusopetuksenVuosiluokanSuoritusPopup = ({
  opiskeluoikeus,
  resultCallback
}) => {
  const submitBus = Bacon.Bus()
  let initialSuoritusModel = newSuoritusProto(
    opiskeluoikeus,
    'perusopetuksenvuosiluokansuoritus'
  )

  initialSuoritusModel = L.modify(
    L.compose(modelLens('koulutusmoduuli.tunniste'), 'alternativesPath'),
    (url) => url + '/' + puuttuvatLuokkaAsteet(opiskeluoikeus).join(','),
    initialSuoritusModel
  )
  const viimeisin = viimeisinLuokkaAste(opiskeluoikeus)
  if (viimeisin) {
    initialSuoritusModel = copyToimipiste(viimeisin, initialSuoritusModel)
  }

  initialSuoritusModel = addContext(initialSuoritusModel, { editAll: true })

  const defaultLuokkaAsteP = valittuLuokkaAsteP(initialSuoritusModel)

  return (
    <div>
      {defaultLuokkaAsteP.last().map((valittuLuokkaAste) => {
        initialSuoritusModel = modelSetValue(
          initialSuoritusModel,
          valittuLuokkaAste,
          'koulutusmoduuli.tunniste'
        )
        initialSuoritusModel = addContext(initialSuoritusModel, {
          suoritus: initialSuoritusModel
        })

        const { modelP, errorP } =
          accumulateModelStateAndValidity(initialSuoritusModel)

        const hasToimipisteP = modelP.map(
          (m) => !!modelData(m, 'toimipiste.oid')
        )
        const hasAlkamispäiväP = modelP.map(
          (m) => !!modelData(m, 'alkamispäivä')
        )
        const validP = errorP.not().and(hasToimipisteP).and(hasAlkamispäiväP)

        const finalSuoritus = submitBus
          .filter(validP)
          .map(modelP)
          .flatMapFirst((suoritus) => {
            const oppiaineidenSuoritukset =
              luokkaAste(suoritus) === '9'
                ? Bacon.constant([])
                : luokkaAsteenOsasuoritukset(
                    luokkaAste(suoritus),
                    isToimintaAlueittain(opiskeluoikeus)
                  )
            return oppiaineidenSuoritukset.map((oppiaineet) =>
              modelSetValue(suoritus, oppiaineet.value, 'osasuoritukset')
            )
          })

        const paths = [
          'koulutusmoduuli.perusteenDiaarinumero',
          'koulutusmoduuli.tunniste',
          'luokka',
          'toimipiste',
          'alkamispäivä'
        ]

        return (
          <div>
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
              <PropertiesEditor
                baret-lift
                context={initialSuoritusModel.context}
                properties={modelP.map((model) =>
                  modelProperties(model, paths)
                )}
              />
            </ModalDialog>
            {doActionWhileMounted(finalSuoritus, resultCallback)}
          </div>
        )
      })}
    </div>
  )
}

UusiPerusopetuksenVuosiluokanSuoritusPopup.canAddSuoritus = (
  opiskeluoikeus
) => {
  return (
    modelData(opiskeluoikeus, 'tyyppi.koodiarvo') === 'perusopetus' &&
    puuttuvatLuokkaAsteet(opiskeluoikeus).length > 0 &&
    !nuortenPerusopetuksenOppiaineenOppimääränSuoritus(opiskeluoikeus)
  )
}

UusiPerusopetuksenVuosiluokanSuoritusPopup.addSuoritusTitle = () => (
  <Text name="lisää vuosiluokan suoritus" />
)
UusiPerusopetuksenVuosiluokanSuoritusPopup.addSuoritusTitleKey =
  'lisää vuosiluokan suoritus'

export default UusiPerusopetuksenVuosiluokanSuoritusPopup

const valittuLuokkaAsteP = (model) => {
  const luokkaAsteLens = modelLens('koulutusmoduuli.tunniste')
  const luokkaAsteModel = L.get(luokkaAsteLens, model)
  return EnumEditor.fetchAlternatives(luokkaAsteModel).map('.0')
}

const puuttuvatLuokkaAsteet = (opiskeluoikeus) => {
  const olemassaOlevatLuokkaAsteet = olemassaolevatLuokkaAsteenSuoritukset(
    opiskeluoikeus
  )
    .filter(siirretäänSeuraavalleLuokalle)
    .map(suorituksenLuokkaAste)
  return [1, 2, 3, 4, 5, 6, 7, 8, 9].filter(
    (x) => !olemassaOlevatLuokkaAsteet.includes(x)
  )
}

const siirretäänSeuraavalleLuokalle = (suoritus) =>
  !modelData(suoritus, 'jääLuokalle')

const olemassaolevatLuokkaAsteenSuoritukset = (opiskeluoikeus) =>
  modelItems(opiskeluoikeus, 'suoritukset').filter(
    (suoritus) =>
      modelData(suoritus, 'tyyppi.koodiarvo') === 'perusopetuksenvuosiluokka'
  )

const suorituksenLuokkaAste = (suoritus) =>
  parseInt(modelData(suoritus, 'koulutusmoduuli.tunniste.koodiarvo'))

const viimeisinLuokkaAste = (opiskeluoikeus) => {
  const suoritukset = olemassaolevatLuokkaAsteenSuoritukset(opiskeluoikeus)
  if (suoritukset.length) {
    return suoritukset.reduce(R.maxBy(suorituksenLuokkaAste))
  }
}
