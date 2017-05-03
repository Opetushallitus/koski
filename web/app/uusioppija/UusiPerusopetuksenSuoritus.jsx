import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import R from 'ramda'
import Http from '../http'
import {UusiPerusopetuksenOppiaineEditor} from '../editor/UusiPerusopetuksenOppiaineEditor.jsx'
import {accumulateModelState, modelLookup, modelData} from '../editor/EditorModel'
import {editorMapping} from '../editor/Editors.jsx'
import {Editor} from '../editor/Editor.jsx'
import {PropertyEditor} from '../editor/PropertyEditor.jsx'
import KoodistoDropdown from './KoodistoDropdown.jsx'
import {koodistoValues, koodiarvoMatch} from './koodisto'

export default ({suoritusAtom, oppilaitosAtom}) => {
  const oppimääräAtom = Atom()
  const oppiaineenSuoritusAtom = Atom()
  const opetussuunnitelmaAtom = Atom()
  const oppimäärätP = koodistoValues('suorituksentyyppi/perusopetuksenoppimaara,perusopetuksenoppiaineenoppimaara')
  oppimäärätP.onValue(oppimäärät => oppimääräAtom.set(oppimäärät.find(koodiarvoMatch('perusopetuksenoppimaara'))))

  const opetussuunnitelmatP = koodistoValues('perusopetuksenoppimaara')
  opetussuunnitelmatP.onValue(tilat => opetussuunnitelmaAtom.set(tilat.find(koodiarvoMatch('perusopetus'))))

  const suoritusPrototypeP = oppimääräAtom.map('.koodiarvo').flatMap(oppimäärä => {
    if (oppimäärä == 'perusopetuksenoppiaineenoppimaara') {
      return Http.cachedGet('/koski/api/editor/prototype/fi.oph.koski.schema.PerusopetuksenOppiaineenOppimääränSuoritus')
    }
  })

  const makeSuoritus = (oppilaitos, oppimäärä, opetussuunnitelma, oppiaineenSuoritus) => {
    if (oppilaitos && opetussuunnitelma && koodiarvoMatch('perusopetuksenoppimaara')(oppimäärä)) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '201101',
            koodistoUri: 'koulutus'
          }
        },
        toimipiste: oppilaitos,
        tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
        oppimäärä: opetussuunnitelma,
        suoritustapa: { koodistoUri: 'perusopetuksensuoritustapa', koodiarvo: 'koulutus'},
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'perusopetuksenoppimaara'}
      }
    } else if (oppilaitos && koodiarvoMatch('perusopetuksenoppiaineenoppimaara')(oppimäärä) && oppiaineenSuoritus) {
      var suoritusTapaJaToimipiste = {toimipiste: oppilaitos, suoritustapa: {koodistoUri: 'perusopetuksensuoritustapa', koodiarvo: 'koulutus'}}
      return R.merge(oppiaineenSuoritus, suoritusTapaJaToimipiste)
    }
  }

  Bacon.combineWith(oppilaitosAtom, oppimääräAtom, opetussuunnitelmaAtom, oppiaineenSuoritusAtom, makeSuoritus)
    .onValue(suoritus => suoritusAtom.set(suoritus))

  return (<span>
    <Oppimäärä oppimääräAtom={oppimääräAtom} oppimäärätP={oppimäärätP}/>
   <Opetussuunnitelma opetussuunnitelmaAtom={opetussuunnitelmaAtom} opetussuunnitelmatP={opetussuunnitelmatP} oppimääräP={oppimääräAtom}/>
   <OppiaineEditor suoritusPrototypeP={suoritusPrototypeP} oppiaineenSuoritusAtom={oppiaineenSuoritusAtom}/>
  </span>)
}

const Oppimäärä = ({oppimääräAtom, oppimäärätP}) => {
  return (<div>
    <KoodistoDropdown
      className="oppimaara"
      title="Oppimäärä"
      optionsP = { oppimäärätP }
      atom = {oppimääräAtom}
    />
  </div> )
}

const Opetussuunnitelma = ({opetussuunnitelmaAtom, oppimääräP, opetussuunnitelmatP}) => {
  let shouldShowP = oppimääräP.map(koodiarvoMatch('perusopetuksenoppimaara')).skipDuplicates()
  return (<div>
    {
      shouldShowP.map(show => show && <KoodistoDropdown
        className="opetussuunnitelma"
        title="Opetussuunnitelma"
        optionsP = { opetussuunnitelmatP }
        atom = { opetussuunnitelmaAtom } />
      )
    }
  </div> )
}

const OppiaineEditor = ({suoritusPrototypeP, oppiaineenSuoritusAtom}) => { // Yleinen prototyyppi suoritukselle
  return (<span>
    {
      suoritusPrototypeP.map(oppiaineenSuoritus => {
        let suoritusPrototypeAtom = Atom(undefined) // Valittu prototyyppi suoritukselle, valitaan UusiPerusopetuksenOppiaineEditorilla
        if (!oppiaineenSuoritus) return null
        oppiaineenSuoritus = Editor.setupContext(oppiaineenSuoritus, {edit:true, editorMapping})

        let suoritusModelP = suoritusPrototypeAtom.flatMapLatest(oppiainePrototype => {
          return oppiainePrototype && accumulateModelState(oppiainePrototype)
        }).toProperty()

        suoritusModelP.map(modelData).onValue(suoritus => oppiaineenSuoritusAtom.set(suoritus))
        return (<span>
          <label className="oppiaine">Oppiaine <UusiPerusopetuksenOppiaineEditor oppiaineenSuoritus={oppiaineenSuoritus} selected={suoritusPrototypeAtom} resultCallback={s => suoritusPrototypeAtom.set(s)} pakollinen={true} enableFilter={false}/></label>
          { suoritusModelP.map(model =>
          model && <label><PropertyEditor model={modelLookup(model, 'koulutusmoduuli')} propertyName="kieli"/></label> )
          }
        </span>)
      })
    }
  </span>)
}