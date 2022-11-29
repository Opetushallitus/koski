import React from 'baret'
import Atom from 'bacon.atom'
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
  modelLookup,
  modelProperties,
  modelSetValue,
  modelSetValues,
  modelValueLens,
  pushModel
} from '../editor/EditorModel'
import { lang, t } from '../i18n/i18n'
import ModalDialog from '../editor/ModalDialog'
import { doActionWhileMounted } from '../util/util'
import Text from '../i18n/Text'
import {
  ammatillisenTutkinnonSuoritus,
  copyToimipiste,
  newSuoritusProto,
  näyttötutkintoonValmistavanKoulutuksenSuoritus,
  suorituksenTyyppi
} from '../suoritus/Suoritus'
import SuoritustapaDropdown from '../uusioppija/SuoritustapaDropdown'
import TutkintoAutocomplete from '../virkailija/TutkintoAutocomplete'
import {
  enumValueToKoodiviiteLens,
  toKoodistoEnumValue
} from '../koodisto/koodistot'

const tutkintoLens = L.lens(
  (m) => {
    if (modelData(m).tunniste) {
      return {
        tutkintoKoodi: modelData(m).tunniste.koodiarvo,
        diaarinumero: modelData(m).perusteenDiaarinumero,
        nimi: { [lang]: modelLookup(m, 'tunniste').value.title }
      }
    }
  },
  (tutkinto, m) => {
    return modelSetValues(m, {
      tunniste: toKoodistoEnumValue(
        'koulutus',
        tutkinto.tutkintoKoodi,
        t(tutkinto.nimi)
      ),
      perusteenDiaarinumero: { data: tutkinto.diaarinumero }
    })
  }
)

const hasValmistavaTutkinto = (opiskeluoikeus) =>
  modelItems(opiskeluoikeus, 'suoritukset').find(
    (suoritus) =>
      suorituksenTyyppi(suoritus) == 'nayttotutkintoonvalmistavakoulutus'
  )
const hasAmmatillinenTutkinto = (opiskeluoikeus) =>
  modelItems(opiskeluoikeus, 'suoritukset').find(
    (suoritus) => suorituksenTyyppi(suoritus) == 'ammatillinentutkinto'
  )
const hasContradictingSuoritus = (opiskeluoikeus) => {
  const disallowedSuoritustyypit = [
    'telma',
    'valma',
    'ammatillinentutkintoosittainen',
    'muuammatillinenkoulutus',
    'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus'
  ]
  return modelItems(opiskeluoikeus, 'suoritukset')
    .map((suoritus) => modelData(suoritus, 'tyyppi.koodiarvo'))
    .some((suoritustyyppi) => disallowedSuoritustyypit.includes(suoritustyyppi))
}

const Popup =
  (isValmistava) =>
  ({ opiskeluoikeus, resultCallback }) => {
    const submitBus = Bacon.Bus()
    let initialSuoritusModel = newSuoritusProto(
      opiskeluoikeus,
      isValmistava
        ? 'nayttotutkintoonvalmistavankoulutuksensuoritus'
        : 'ammatillisentutkinnonsuoritus'
    )
    initialSuoritusModel = addContext(initialSuoritusModel, { editAll: true })
    initialSuoritusModel = copyToimipiste(
      modelItems(opiskeluoikeus, 'suoritukset')[0],
      initialSuoritusModel
    )
    if (isValmistava) {
      const ammatillinenTutkinto = modelLookup(
        ammatillisenTutkinnonSuoritus(opiskeluoikeus),
        'koulutusmoduuli'
      )
      if (ammatillinenTutkinto) {
        initialSuoritusModel = modelSetValue(
          initialSuoritusModel,
          ammatillinenTutkinto.value,
          'tutkinto'
        )
      }
    } else {
      const valmistavanKoulutuksenTutkinto = modelLookup(
        näyttötutkintoonValmistavanKoulutuksenSuoritus(opiskeluoikeus),
        'tutkinto'
      )
      if (valmistavanKoulutuksenTutkinto) {
        initialSuoritusModel = modelSetValue(
          initialSuoritusModel,
          valmistavanKoulutuksenTutkinto.value,
          'koulutusmoduuli'
        )
      }
    }

    const { modelP, errorP } =
      accumulateModelStateAndValidity(initialSuoritusModel)
    const validP = errorP.not()
    const koulutusModuuliTutkintoLens = L.compose(
      modelLens(isValmistava ? 'tutkinto' : 'koulutusmoduuli'),
      tutkintoLens
    )
    const suoritusTapaKoodiarvoLens = L.compose(
      modelLens('suoritustapa'),
      modelValueLens,
      enumValueToKoodiviiteLens
    )

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
          const foundProperties = modelProperties(oppiaineenSuoritus, [
            'toimipiste',
            isValmistava ? 'tutkinto' : 'koulutusmoduuli',
            'suoritustapa'
          ])
          const tutkintoAtom = Atom(
            L.get(koulutusModuuliTutkintoLens, oppiaineenSuoritus)
          )
          const suoritustapaAtom = Atom(
            L.get(suoritusTapaKoodiarvoLens, oppiaineenSuoritus)
          )
          tutkintoAtom
            .changes()
            .filter(R.identity)
            .forEach((tutkinto) => {
              const updatedModel = L.set(
                koulutusModuuliTutkintoLens,
                tutkinto,
                oppiaineenSuoritus
              )
              pushModel(updatedModel)
            })
          suoritustapaAtom
            .changes()
            .filter(R.identity)
            .forEach((suoritustapa) => {
              const updatedModel = L.set(
                suoritusTapaKoodiarvoLens,
                suoritustapa,
                oppiaineenSuoritus
              )
              pushModel(updatedModel)
            })
          return (
            <div key="props">
              <PropertiesEditor
                editAll={true}
                context={oppiaineenSuoritus.context}
                properties={foundProperties}
                getValueEditor={(p, getDefault) => {
                  switch (p.key) {
                    case 'tutkinto':
                    case 'koulutusmoduuli':
                      return (
                        <TutkintoAutocomplete
                          tutkintoAtom={tutkintoAtom}
                          oppilaitosP={Bacon.constant(
                            modelData(opiskeluoikeus, 'oppilaitos')
                          )}
                        />
                      )
                    case 'suoritustapa':
                      // TODO: Korjaa
                      // eslint-disable-next-line no-case-declarations
                      const tutkinto = modelData(
                        oppiaineenSuoritus,
                        'koulutusmoduuli'
                      )
                      return (
                        <SuoritustapaDropdown
                          diaarinumero={tutkinto.perusteenDiaarinumero}
                          suoritustapaAtom={suoritustapaAtom}
                        />
                      )
                    default:
                      return getDefault()
                  }
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

export const UusiAmmatillisenTutkinnonSuoritus = Popup(false)
UusiAmmatillisenTutkinnonSuoritus.canAddSuoritus = (opiskeluoikeus) => {
  return (
    modelData(opiskeluoikeus, 'tyyppi.koodiarvo') == 'ammatillinenkoulutus' &&
    !hasAmmatillinenTutkinto(opiskeluoikeus) &&
    !hasContradictingSuoritus(opiskeluoikeus)
  )
}
UusiAmmatillisenTutkinnonSuoritus.addSuoritusTitle = () => (
  <Text name="lisää ammatillisen tutkinnon suoritus" />
)

UusiAmmatillisenTutkinnonSuoritus.addSuoritusTitleKey =
  'lisää ammatillisen tutkinnon suoritus'

export const UusiNäyttötutkintoonValmistavanKoulutuksenSuoritus = Popup(true)
UusiNäyttötutkintoonValmistavanKoulutuksenSuoritus.canAddSuoritus = (
  opiskeluoikeus
) => {
  return (
    modelData(opiskeluoikeus, 'tyyppi.koodiarvo') == 'ammatillinenkoulutus' &&
    !hasValmistavaTutkinto(opiskeluoikeus) &&
    !hasContradictingSuoritus(opiskeluoikeus)
  )
}
UusiNäyttötutkintoonValmistavanKoulutuksenSuoritus.addSuoritusTitle = () => (
  <Text name="lisää näyttötutkintoon valmistavan koulutuksen suoritus" />
)
UusiNäyttötutkintoonValmistavanKoulutuksenSuoritus.addSuoritusTitleKey =
  'lisää näyttötutkintoon valmistavan koulutuksen suoritus'
