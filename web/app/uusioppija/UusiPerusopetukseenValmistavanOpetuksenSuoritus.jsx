import React from 'baret'
import Atom from 'bacon.atom'
import Bacon from 'baconjs'
import {PerusteDropdown} from '../editor/PerusteDropdown.jsx'
import Text from '../Text.jsx'

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
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'perusopetukseenvalmistavaopetus'},
        suorituskieli : { koodiarvo : 'FI', nimi : { fi : 'suomi' }, koodistoUri : 'kieli' } // TODO: get from GUI
      }
    }
  }
  let suoritusP = Bacon.combineWith(oppilaitosAtom, perusteAtom, makeSuoritus)
  suoritusP.filter('.koulutusmoduuli.perusteenDiaarinumero').onValue(suoritus => suoritusAtom.set(suoritus))
  return <Peruste {...{suoritusP, perusteAtom}} />
}

const Peruste = ({suoritusP, perusteAtom}) => <label className="peruste"><Text name="Peruste"/><PerusteDropdown {...{suoritusP, perusteAtom}}/></label>
