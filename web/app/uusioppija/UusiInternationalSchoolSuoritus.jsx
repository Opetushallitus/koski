import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { koodiarvoMatch, koodistoValues } from './koodisto'
import { makeSuoritus } from '../internationalschool/internationalschoolSuoritus'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'

export default ({
  suoritusAtom,
  dateAtom,
  oppilaitosAtom,
  suorituskieliAtom,
  without = []
}) => {
  const gradeAtom = Atom()

  koodistoValues('kieli/EN').onValue((kielet) =>
    suorituskieliAtom.set(kielet.find(koodiarvoMatch('EN')))
  )

  Bacon.combineWith(
    oppilaitosAtom,
    gradeAtom,
    dateAtom,
    suorituskieliAtom,
    makeSuoritus
  ).onValue((suoritus) => suoritusAtom.set(suoritus))

  return <GradeDropDown gradeAtom={gradeAtom} without={without} />
}

const GradeDropDown = ({ gradeAtom, without }) => {
  const gradesP = koodistoValues('internationalschoolluokkaaste')
    .map((grades) =>
      grades.filter((grade) => !without.includes(grade.koodiarvo))
    )
    .map((grades) => grades.sort(byGrade))

  gradesP.onValue((grades) => gradeAtom.set(grades[0]))

  return (
    <KoodistoDropdown
      className="property international-school-grade"
      title="Grade"
      options={gradesP}
      selected={gradeAtom}
      enableFilter={true}
    />
  )
}

const byGrade = (a, b) =>
  a.koodiarvo === 'explorer'
    ? -1
    : b.koodiarvo === 'explorer'
    ? 1
    : a.koodiarvo - b.koodiarvo
