import React from 'baret'
import Atom from 'bacon.atom'
import Bacon from 'baconjs'
import {PerusteDropdown} from '../editor/PerusteDropdown.jsx'

export default ({suoritusAtom, oppilaitosAtom}) => {
  const perusteAtom = Atom()
  const makeSuoritus = (oppilaitos, peruste) => {
    if (oppilaitos) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999905',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste: oppilaitos,
        tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'perusopetukseenvalmistavaopetus'}
      }
    }
  }
  let suoritusP = Bacon.combineWith(oppilaitosAtom, perusteAtom, makeSuoritus)
  suoritusP.onValue(suoritus => suoritusAtom.set(suoritus))
  return <Peruste {...{suoritusP, perusteAtom}} />
}

const Peruste = ({suoritusP, perusteAtom}) => <label className="peruste">Peruste<PerusteDropdown {...{suoritusP, perusteAtom}}/></label>
