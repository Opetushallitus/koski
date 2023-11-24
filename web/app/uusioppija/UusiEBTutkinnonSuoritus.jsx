import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { koodistoValues } from './koodisto'
import { makeEBSuoritus } from '../esh/europeanschoolofhelsinkiSuoritus'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'

export default ({
  suoritusAtom,
  dateAtom, // dateAtom mukana vain bacon/React-yhteistoiminnan "korjaamiseksi": päivämäärän vaihto dialogilla sotkee muuten suoritus-atomin väärän tyyppiseksi
  oppilaitosAtom
}) => {
  // EB-opiskeluoikeuden suorituksen Curriculum
  const curriculumAtom = Atom()
  const curriculumP = koodistoValues('europeanschoolofhelsinkicurriculum').map(
    (curriculums) => curriculums
  )
  curriculumP.onValue((curriculums) => {
    curriculumAtom.set(curriculums[0])
  })

  Bacon.combineWith(
    oppilaitosAtom,
    dateAtom, // dateAtom mukana vain bacon/React-yhteistoiminnan "korjaamiseksi": päivämäärän vaihto dialogilla sotkee muuten suoritus-atomin väärän tyyppiseksi
    curriculumAtom,
    makeEBSuoritus
  ).onValue((suoritus) => {
    suoritusAtom.set(suoritus)
  })

  return (
    <>
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
