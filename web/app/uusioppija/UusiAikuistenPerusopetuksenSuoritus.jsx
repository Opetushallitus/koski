import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as L from 'partial.lenses'
import Http from '../http'
import {UusiPerusopetuksenOppiaineDropdown} from '../editor/UusiPerusopetuksenOppiaineDropdown'
import {
  accumulateModelState,
  modelData,
  modelLookup,
  modelSet,
  modelSetValues,
  modelValid,
  validateModel
} from '../editor/EditorModel'
import {editorMapping} from '../editor/Editors'
import {Editor} from '../editor/Editor'
import {PropertyEditor} from '../editor/PropertyEditor'
import {koodiarvoMatch, koodistoValues} from './koodisto'
import Text from '../Text'
import {makeSuoritus, oppiaineetP} from './PerusopetuksenSuoritus'
import Suoritustyyppi from './Suoritustyyppi'
import Peruste from './Peruste'

export default ({suoritusAtom, oppilaitosAtom, suorituskieliAtom}) => {
  const suoritustyyppiAtom = Atom()
  const oppiaineenSuoritusAtom = Atom()
  const perusteAtom = Atom()
  const suoritustyypitP = koodistoValues('suorituksentyyppi/aikuistenperusopetuksenoppimaara,aikuistenperusopetuksenoppimaaranalkuvaihe,perusopetuksenoppiaineenoppimaara')
  suoritustyypitP.onValue(tyypit => suoritustyyppiAtom.set(tyypit.find(koodiarvoMatch('aikuistenperusopetuksenoppimaara'))))

  const suoritusPrototypeP = suoritustyyppiAtom.map('.koodiarvo').flatMap(suorituksenTyyppi => {
    if (suorituksenTyyppi == 'perusopetuksenoppiaineenoppimaara') {
      return Http.cachedGet('/koski/api/editor/prototype/fi.oph.koski.schema.PerusopetuksenOppiaineenOppimääränSuoritus')
    }
  }).toProperty()

  Bacon.combineWith(oppilaitosAtom, suoritustyyppiAtom, perusteAtom, oppiaineetP(suoritustyyppiAtom), suorituskieliAtom, oppiaineenSuoritusAtom, makeSuoritus)
    .onValue(suoritus => suoritusAtom.set(suoritus))

  return (<span>
    <Suoritustyyppi suoritustyyppiAtom={suoritustyyppiAtom} suoritustyypitP={suoritustyypitP} title="Oppimäärä"/>
    {
      suoritustyyppiAtom.map( tyyppi => koodiarvoMatch('perusopetuksenoppimaara', 'aikuistenperusopetuksenoppimaara','aikuistenperusopetuksenoppimaaranalkuvaihe')(tyyppi)
        ? <Peruste {...{suoritusTyyppiP: suoritustyyppiAtom, perusteAtom}} />
        : <Oppiaine suoritusPrototypeP={suoritusPrototypeP} oppiaineenSuoritusAtom={oppiaineenSuoritusAtom} perusteAtom={perusteAtom} oppilaitos={oppilaitosAtom} suorituskieli={suorituskieliAtom}/>
      )
    }
  </span>)
}

const Oppiaine = ({suoritusPrototypeP, oppiaineenSuoritusAtom, perusteAtom, oppilaitos, suorituskieli}) => { // suoritusPrototypeP = prototyyppi oppiaineen oppimäärän suoritukselle
  return (<span>
    {
      Bacon.combineWith(suoritusPrototypeP, oppilaitos, suorituskieli, (oppiaineenSuoritus, toimipiste, kieli) => {
        if (!oppiaineenSuoritus) return null

        oppiaineenSuoritus = modelSetValues(Editor.setupContext(oppiaineenSuoritus, {edit:true, editorMapping}), {
          toimipiste: { data: toimipiste },
          suorituskieli: { data: kieli}
        })

        let oppiainePrototypeAtom = Atom(undefined) // Valittu oppiaine/koulutusmoduuliprototyyppi

        let suoritusModelP = oppiainePrototypeAtom.flatMapLatest(oppiainePrototype => {
          return oppiainePrototype && accumulateModelState(modelSet(oppiaineenSuoritus, oppiainePrototype, 'koulutusmoduuli'))
        }).toProperty()

        let suoritusDataP = suoritusModelP.map(model => model && modelValid(validateModel(model)) ? modelData(model) : null)

        let suoritusP = Bacon.combineWith(suoritusDataP, perusteAtom, (suoritus, diaarinumero) => {
          if (suoritus && diaarinumero) return L.set(L.compose('koulutusmoduuli', 'perusteenDiaarinumero'), diaarinumero, suoritus)
        })

        suoritusP.onValue(suoritus => oppiaineenSuoritusAtom.set(suoritus))

        return (<span>
          <Peruste suoritusTyyppiP={Bacon.constant(modelData(oppiaineenSuoritus, 'tyyppi'))} perusteAtom={perusteAtom} />
          <label className="oppiaine"><Text name="Oppiaine"/>{' '}<UusiPerusopetuksenOppiaineDropdown oppiaineenSuoritus={oppiaineenSuoritus} selected={oppiainePrototypeAtom} resultCallback={s => oppiainePrototypeAtom.set(s)} pakollinen={true} enableFilter={false}/></label>
          { suoritusModelP.map(model =>
            model && <label><PropertyEditor model={modelLookup(model, 'koulutusmoduuli')} propertyName="kieli"/></label> )
          }
        </span>)
      })
    }
  </span>)
}