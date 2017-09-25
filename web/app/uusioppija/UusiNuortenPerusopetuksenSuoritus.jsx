import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import KoodistoDropdown from '../KoodistoDropdown.jsx'
import {koodiarvoMatch, koodistoValues} from './koodisto'
import {PerusteDropdown} from '../editor/PerusteDropdown.jsx'
import Text from '../Text.jsx'
import {makeSuoritus, oppiaineetP} from './PerusopetuksenSuoritus'

export default ({suoritusAtom, oppilaitosAtom, suorituskieliAtom}) => {
  const suoritustyyppiAtom = Atom()
  const perusteAtom = Atom()
  const suoritustyypitP = koodistoValues('suorituksentyyppi/perusopetuksenoppimaara')
  suoritustyypitP.onValue(tyypit => suoritustyyppiAtom.set(tyypit.find(koodiarvoMatch('perusopetuksenoppimaara'))))

  Bacon.combineWith(oppilaitosAtom, suoritustyyppiAtom, perusteAtom, oppiaineetP(suoritustyyppiAtom), suorituskieliAtom, makeSuoritus)
    .onValue(suoritus => suoritusAtom.set(suoritus))

  return (<span>
    <Suoritustyyppi suoritustyyppiAtom={suoritustyyppiAtom} suoritustyypitP={suoritustyypitP}/>
    <Peruste {...{suoritusTyyppiP: suoritustyyppiAtom, perusteAtom}} />
  </span>)
}

const Suoritustyyppi = ({suoritustyyppiAtom, suoritustyypitP}) => {
  return (<div>
    <KoodistoDropdown
      className="oppimaara"
      title="Oppimäärä"
      options = { suoritustyypitP }
      selected = { suoritustyyppiAtom }
    />
  </div> )
}

const Peruste = ({suoritusTyyppiP, perusteAtom}) => <label className="peruste"><Text name="Peruste"/><PerusteDropdown {...{suoritusTyyppiP, perusteAtom}}/></label>
