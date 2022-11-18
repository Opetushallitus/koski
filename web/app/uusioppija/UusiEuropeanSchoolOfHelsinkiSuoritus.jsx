import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { koodistoValues } from './koodisto'
import { makeSuoritus } from '../esh/europeanschoolofhelsinkiSuoritus'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import Text from '../i18n/Text'
import DateInput from '../date/DateInput'
import { ift } from '../util/util'

export default ({
  suoritusAtom,
  oppilaitosAtom,
  näytäKoulutusValitsin,
  näytäAlkamispäiväValitsin
}) => {
  const dateAtom = näytäAlkamispäiväValitsin ? Atom(new Date()) : undefined

  // ESH-opiskeluoikeuden suorituksen koulutusmoduuli
  const koulutusmoduuliAtom = Atom()

  const koulutusmoduulitP = Bacon.combineWith(
    (p1, p2) => p1.concat(p2),
    koodistoValues('koulutus', ['301104']),
    koodistoValues('europeanschoolofhelsinkiluokkaaste').map(
      (koulutusmoduulit) => koulutusmoduulit.sort(byAste)
    )
  )

  koulutusmoduulitP.onValue((koulutusmoduulit) => {
    koulutusmoduuliAtom.set(koulutusmoduulit[0])
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
    koulutusmoduuliAtom,
    curriculumAtom,
    dateAtom,
    makeSuoritus
  ).onValue((suoritus) => {
    suoritusAtom.set(suoritus)
  })

  return (
    <>
      {näytäKoulutusValitsin ? (
        <KoodistoDropdown
          className="property european-school-of-helsinki-luokkaaste"
          title="Suoritus"
          options={koulutusmoduulitP}
          selected={koulutusmoduuliAtom}
          enableFilter={false}
        />
      ) : null}
      <KoodistoDropdown
        className="property european-school-of-helsinki-curriculum"
        title="Curriculum"
        options={curriculumP}
        selected={curriculumAtom}
        enableFilter={false}
      />
      <div>
        {ift(
          koulutusmoduuliAtom.map((koulutusmoduuli) => {
            return (
              koulutusmoduuli?.koodistoUri ===
              'europeanschoolofhelsinkiluokkaaste'
            )
          }),
          <Aloituspäivä dateAtom={dateAtom} />
        )}
      </div>
    </>
  )
}

const Aloituspäivä = ({ dateAtom }) => {
  return (
    <label className="property aloituspaiva european-school-of-helsinki-aloituspaiva">
      <Text name="Aloituspäivä" />
      <span>
        <DateInput
          value={dateAtom.get()}
          valueCallback={(value) => dateAtom.set(value)}
          validityCallback={(valid) => !valid && dateAtom.set(undefined)}
        />
      </span>
    </label>
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
