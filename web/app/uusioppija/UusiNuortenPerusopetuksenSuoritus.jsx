import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import KoodistoDropdown from '../KoodistoDropdown.jsx'
import {koodiarvoMatch, koodistoValues} from './koodisto'
import {PerusteDropdown} from '../editor/PerusteDropdown.jsx'
import Text from '../Text.jsx'
import {makeSuoritus, oppiaineetP} from './PerusopetuksenSuoritus'

export default ({suoritusAtom, oppilaitosAtom, suorituskieliAtom}) => {
  const suoritustyyppiAtom = Atom() // TODO: oppimäärä -> suoritusTyyppi
  const perusteAtom = Atom()
  const oppimäärätP = koodistoValues('suorituksentyyppi/perusopetuksenoppimaara')
  oppimäärätP.onValue(oppimäärät => suoritustyyppiAtom.set(oppimäärät.find(koodiarvoMatch('perusopetuksenoppimaara'))))

  Bacon.combineWith(oppilaitosAtom, suoritustyyppiAtom, perusteAtom, oppiaineetP(suoritustyyppiAtom), suorituskieliAtom, makeSuoritus)
    .onValue(suoritus => suoritusAtom.set(suoritus))

  return (<span>
    <Oppimäärä oppimääräAtom={suoritustyyppiAtom} oppimäärätP={oppimäärätP}/>
    <Peruste {...{suoritusTyyppiP: suoritustyyppiAtom, perusteAtom}} />
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
