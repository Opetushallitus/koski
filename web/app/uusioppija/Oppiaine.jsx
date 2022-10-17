import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as L from 'partial.lenses'
import { UusiOppiaineDropdown } from '../oppiaine/UusiOppiaineDropdown'
import {
  accumulateModelState,
  modelData,
  modelLookup,
  modelSet,
  modelSetValues,
  modelValid,
  validateModel
} from '../editor/EditorModel'
import editorMapping from '../oppija/editors'
import { Editor } from '../editor/Editor'
import { PropertyEditor } from '../editor/PropertyEditor'
import Text from '../i18n/Text'
import Peruste from './Peruste'

export default ({
  suoritusPrototypeP,
  oppiaineenSuoritusAtom,
  perusteAtom,
  oppilaitos,
  suorituskieli
}) => {
  // suoritusPrototypeP = prototyyppi oppiaineen oppimäärän suoritukselle
  return (
    <span>
      {Bacon.combineWith(
        suoritusPrototypeP,
        oppilaitos,
        suorituskieli,
        (oppiaineenSuoritus, toimipiste, kieli) => {
          if (!oppiaineenSuoritus) return null

          oppiaineenSuoritus = modelSetValues(
            Editor.setupContext(oppiaineenSuoritus, {
              edit: true,
              editorMapping
            }),
            {
              toimipiste: { data: toimipiste },
              suorituskieli: { data: kieli }
            }
          )

          const oppiainePrototypeAtom = Atom(undefined) // Valittu oppiaine/koulutusmoduuliprototyyppi

          const suoritusModelP = oppiainePrototypeAtom
            .flatMapLatest((oppiainePrototype) => {
              return (
                oppiainePrototype &&
                accumulateModelState(
                  modelSet(
                    oppiaineenSuoritus,
                    oppiainePrototype,
                    'koulutusmoduuli'
                  )
                )
              )
            })
            .toProperty()

          const suoritusDataP = suoritusModelP.map((model) =>
            model && modelValid(validateModel(model)) ? modelData(model) : null
          )

          const suoritusP = Bacon.combineWith(
            suoritusDataP,
            perusteAtom,
            (suoritus, diaarinumero) => {
              if (suoritus && diaarinumero)
                return L.set(
                  L.compose('koulutusmoduuli', 'perusteenDiaarinumero'),
                  diaarinumero,
                  suoritus
                )
            }
          )

          suoritusP.onValue((suoritus) => oppiaineenSuoritusAtom.set(suoritus))

          return (
            <span>
              <Peruste
                suoritusTyyppiP={Bacon.constant(
                  modelData(oppiaineenSuoritus, 'tyyppi')
                )}
                perusteAtom={perusteAtom}
              />
              <label className="oppiaine">
                <Text name="Oppiaine" />{' '}
                <UusiOppiaineDropdown
                  oppiaineenSuoritukset={
                    (oppiaineenSuoritus && [oppiaineenSuoritus]) || []
                  }
                  organisaatioOid={modelData(
                    oppiaineenSuoritus,
                    'toimipiste.oid'
                  )}
                  selected={oppiainePrototypeAtom}
                  resultCallback={(s) => oppiainePrototypeAtom.set(s)}
                  pakollinen={true}
                  enableFilter={false}
                  allowPaikallinen={false}
                />
              </label>
              {suoritusModelP.map(
                (model) =>
                  model && (
                    <React.Fragment>
                      <label>
                        <PropertyEditor
                          model={modelLookup(model, 'koulutusmoduuli')}
                          propertyName="kieli"
                        />
                      </label>
                      <label>
                        <PropertyEditor
                          model={modelLookup(model, 'koulutusmoduuli')}
                          propertyName="oppimäärä"
                        />
                      </label>
                    </React.Fragment>
                  )
              )}
            </span>
          )
        }
      )}
    </span>
  )
}
