import Atom from 'bacon.atom'
import Bacon from 'baconjs'
import {setPeruste} from '../suoritus/PerusteDropdown'

export const esiopetuksenSuoritus = (suoritusAtom, oppilaitosAtom, suorituskieliAtom) => {
  const perusteAtom = Atom()
  const makeSuoritus = (oppilaitos, peruste, suorituskieli) => {
    if (oppilaitos) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '001101',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste: oppilaitos,
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'esiopetuksensuoritus'},
        suorituskieli : suorituskieli
      }
    }
  }

  let suoritusP = Bacon.combineWith(oppilaitosAtom, perusteAtom, suorituskieliAtom, makeSuoritus)
  suoritusP.map('.tyyppi').onValue(suoritustyyppi => setPeruste(perusteAtom, suoritustyyppi))
  suoritusP.filter('.koulutusmoduuli.perusteenDiaarinumero').onValue(suoritus => suoritusAtom.set(suoritus))
}

