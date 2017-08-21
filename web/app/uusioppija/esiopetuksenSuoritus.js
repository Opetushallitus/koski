import Atom from 'bacon.atom'
import Bacon from 'baconjs'
import {diaarinumerot} from '../editor/PerusteDropdown.jsx'

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
        tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'esiopetuksensuoritus'},
        suorituskieli : suorituskieli
      }
    }
  }

  let suoritusP = Bacon.combineWith(oppilaitosAtom, perusteAtom, suorituskieliAtom, makeSuoritus)
  diaarinumerot(suoritusP.map('.tyyppi')).map(options => options[0]).map('.koodiarvo').onValue(peruste => {
    let current = perusteAtom.get()
    if (!current || peruste !== current) {
      perusteAtom.set(peruste)
    }
  })

  suoritusP.filter('.koulutusmoduuli.perusteenDiaarinumero').onValue(suoritus => suoritusAtom.set(suoritus))
}

