import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { koodistoValues } from './koodisto'
import { makeSuoritus } from '../esh/europeanschoolofhelsinkiSuoritus'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'

export default ({ suoritusAtom, dateAtom, oppilaitosAtom }) => {
  // ESH-opiskeluoikeuden suorituksen luokka-aste
  const luokkaasteAtom = Atom()
  const luokkaasteP = koodistoValues('europeanschoolofhelsinkiluokkaaste').map(
    (luokkaasteet) => luokkaasteet.sort(byAste)
  )

  luokkaasteP.onValue((luokkaasteet) => {
    luokkaasteAtom.set(luokkaasteet[0])
  })

  // ESH-opiskeluoikeuden suorituksen Curriculum
  const curriculumAtom = Atom()
  const curriculumP = koodistoValues('europeanschoolofhelsinkicurriculum').map(
    (curriculums) => curriculums
  )
  curriculumP.onValue((curriculums) => {
    curriculumAtom.set(curriculums[0])
  })

  Bacon.combineWith(
    oppilaitosAtom,
    luokkaasteAtom,
    curriculumAtom,
    dateAtom,
    makeSuoritus
  ).onValue((suoritus) => {
    suoritusAtom.set(suoritus)
  })

  return (
    <>
      <KoodistoDropdown
        className="property european-school-of-helsinki-luokkaaste"
        title="Luokka-aste"
        options={luokkaasteP}
        selected={luokkaasteAtom}
        enableFilter={false}
      />
      <KoodistoDropdown
        className="property european-school-of-helsinki-curriculum"
        title="Curriculum"
        options={curriculumP}
        selected={curriculumAtom}
        enableFilter={false}
      />
    </>
  )
}

// N < P < S
const byAste = (a, b) => {
  const aL = parseInt(a.koodiarvo.slice(1, a.length), 10)
  const bL = parseInt(a.koodiarvo.slice(1, a.length), 10)
  if (
    (a.koodiarvo.startsWith('N') && b.koodiarvo.startsWith('N')) ||
    (a.koodiarvo.startsWith('P') && b.koodiarvo.startsWith('P')) ||
    (a.koodiarvo.startsWith('S') && b.koodiarvo.startsWith('S'))
  ) {
    if (!isNaN(aL) && !isNaN(bL)) {
      return aL - bL
    }
    return 0
  } else if (
    (a.koodiarvo.startsWith('N') && b.koodiarvo.startsWith('S')) ||
    (a.koodiarvo.startsWith('N') && b.koodiarvo.startsWith('P')) ||
    (a.koodiarvo.startsWith('P') && b.koodiarvo.startsWith('S'))
  ) {
    // N < S, N < P ja P < S
    return -1
  } else {
    // Muuten oleta, että järjestys on käänteinen
    return 1
  }
}
