import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { koodistoValues } from './koodisto'
import {
  makeEBSuoritus,
  makeSuoritus
} from '../esh/europeanschoolofhelsinkiSuoritus'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import Text from '../i18n/Text'
import DateInput from '../date/DateInput'
import { ift } from '../util/util'

export default ({ suoritusAtom, oppilaitosAtom }) => {
  // EB-opiskeluoikeuden suorituksen Curriculum
  const curriculumAtom = Atom()
  const curriculumP = koodistoValues('europeanschoolofhelsinkicurriculum').map(
    (curriculums) => curriculums
  )
  curriculumP.onValue((curriculums) => {
    curriculumAtom.set(curriculums[0])
  })

  Bacon.combineWith(oppilaitosAtom, curriculumAtom, makeEBSuoritus).onValue(
    (suoritus) => {
      suoritusAtom.set(suoritus)
    }
  )

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
