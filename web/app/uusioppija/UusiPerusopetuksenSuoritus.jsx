import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../http'
import {modelData} from '../editor/EditorModel'
import KoodistoDropdown from '../KoodistoDropdown.jsx'
import {koodiarvoMatch, koodistoValues} from './koodisto'
import {PerusteDropdown} from '../editor/PerusteDropdown.jsx'
import Text from '../Text.jsx'

export default ({suoritusAtom, oppilaitosAtom, suorituskieliAtom}) => {
  const suoritustyyppiAtom = Atom() // TODO: oppimäärä -> suoritusTyyppi
  const perusteAtom = Atom()
  const oppimäärätP = koodistoValues('suorituksentyyppi/perusopetuksenoppimaara')
  oppimäärätP.onValue(oppimäärät => suoritustyyppiAtom.set(oppimäärät.find(koodiarvoMatch('perusopetuksenoppimaara'))))

  const oppiaineetP = suoritustyyppiAtom.flatMapLatest((tyyppi) => {
    if (koodiarvoMatch('perusopetuksenoppimaara')(tyyppi)) {
      return Http.cachedGet(`/koski/api/editor/suoritukset/prefill/koulutus/201101?tyyppi=${tyyppi.koodiarvo}`).map(modelData)
    } else {
      return []
    }
  }).toProperty()

  Bacon.combineWith(oppilaitosAtom, suoritustyyppiAtom, perusteAtom, oppiaineetP, suorituskieliAtom, (oppilaitos, oppimäärä, peruste, oppiaineet, suorituskieli) => {
    return makePerusopetuksenOppimääränSuoritus(oppilaitos, oppimäärä, peruste, oppiaineet, suorituskieli)
  }).onValue(suoritus => suoritusAtom.set(suoritus))

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

function makePerusopetuksenOppimääränSuoritus(oppilaitos, oppimäärä, peruste, oppiaineet, suorituskieli) {
  return {
    suorituskieli : suorituskieli,
    koulutusmoduuli: {
      tunniste: {
        koodiarvo: '201101',
        koodistoUri: 'koulutus'
      },
      perusteenDiaarinumero: peruste
    },
    toimipiste: oppilaitos,
    tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
    suoritustapa: { koodistoUri: 'perusopetuksensuoritustapa', koodiarvo: 'koulutus'},
    tyyppi: oppimäärä,
    osasuoritukset: oppiaineet
  }
}

const Peruste = ({suoritusTyyppiP, perusteAtom}) => <label className="peruste"><Text name="Peruste"/><PerusteDropdown {...{suoritusTyyppiP, perusteAtom}}/></label>
