import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as L from 'partial.lenses'
import Http from '../http'
import {UusiPerusopetuksenOppiaineDropdown} from '../editor/UusiPerusopetuksenOppiaineDropdown.jsx'
import {
  accumulateModelState,
  modelData,
  modelLookup,
  modelSet,
  modelSetValues,
  modelValid,
  validateModel
} from '../editor/EditorModel'
import {editorMapping} from '../editor/Editors.jsx'
import {Editor} from '../editor/Editor.jsx'
import {PropertyEditor} from '../editor/PropertyEditor.jsx'
import KoodistoDropdown from '../KoodistoDropdown.jsx'
import {koodiarvoMatch, koodistoValues} from './koodisto'
import {PerusteDropdown} from '../editor/PerusteDropdown.jsx'
import Text from '../Text.jsx'
import {makeSuoritus, oppiaineetP} from './PerusopetuksenSuoritus'

export default ({suoritusAtom, oppilaitosAtom, suorituskieliAtom}) => {
  const suoritustyyppiAtom = Atom() // TODO: oppimäärä -> suoritusTyyppi
  const oppiaineenSuoritusAtom = Atom()
  const perusteAtom = Atom()
  const oppimäärätP = koodistoValues('suorituksentyyppi/aikuistenperusopetuksenoppimaara,aikuistenperusopetuksenoppimaaranalkuvaihe,perusopetuksenoppiaineenoppimaara')
  oppimäärätP.onValue(oppimäärät => suoritustyyppiAtom.set(oppimäärät.find(koodiarvoMatch('aikuistenperusopetuksenoppimaara'))))

  const suoritusPrototypeP = suoritustyyppiAtom.map('.koodiarvo').flatMap(oppimäärä => {
    if (oppimäärä == 'perusopetuksenoppiaineenoppimaara') {
      // TODO: älä käytä luokkien nimiä
      return Http.cachedGet('/koski/api/editor/prototype/fi.oph.koski.schema.PerusopetuksenOppiaineenOppimääränSuoritus')
    }
  }).toProperty()

  Bacon.combineWith(oppilaitosAtom, suoritustyyppiAtom, perusteAtom, oppiaineetP(suoritustyyppiAtom), suorituskieliAtom, oppiaineenSuoritusAtom, makeSuoritus)
    .onValue(suoritus => suoritusAtom.set(suoritus))

  return (<span>
    <Oppimäärä oppimääräAtom={suoritustyyppiAtom} oppimäärätP={oppimäärätP}/>
    {
      suoritustyyppiAtom.map( oppimäärä => koodiarvoMatch('perusopetuksenoppimaara', 'aikuistenperusopetuksenoppimaara','aikuistenperusopetuksenoppimaaranalkuvaihe')(oppimäärä)
        ? <Peruste {...{suoritusTyyppiP: suoritustyyppiAtom, perusteAtom}} />
        : <Oppiaine suoritusPrototypeP={suoritusPrototypeP} oppiaineenSuoritusAtom={oppiaineenSuoritusAtom} perusteAtom={perusteAtom} oppilaitos={oppilaitosAtom} suorituskieli={suorituskieliAtom}/>
      )
    }
  </span>)
}

const Oppimäärä = ({oppimääräAtom, oppimäärätP}) => {
  return (<div>
    <KoodistoDropdown
      className="oppimaara"
      title="Oppimäärä"
      options = { oppimäärätP }
      selected = {oppimääräAtom}
    />
  </div> )
}

const Peruste = ({suoritusTyyppiP, perusteAtom}) => <label className="peruste"><Text name="Peruste"/><PerusteDropdown {...{suoritusTyyppiP, perusteAtom}}/></label>

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