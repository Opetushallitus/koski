import React from 'baret'
import * as R from 'ramda'
import Atom from 'bacon.atom'
import {modelData, modelSetData, pushModel} from '../editor/EditorModel'
import {suorituksenTyyppi} from '../suoritus/Suoritus'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import {koodistoValues} from '../uusioppija/koodisto'

export const InternationalSchoolLevel = ({model}) => {
  if (modelData(model.context.opiskeluoikeus, 'tyyppi.koodiarvo') !== 'internationalschool') return null

  return model.context.edit && suorituksenTyyppi(model.context.suoritus) === 'internationalschooldiplomavuosiluokka'
    ? <DiplomaTypeEditor model={model} />
    : (<span className='international-school-level'>{` (${internationalSchoolLevel(model)})`}</span>)
}

InternationalSchoolLevel.displayName = 'InternationalSchoolLevel'

const internationalSchoolLevel = model => {
  switch (suorituksenTyyppi(model.context.suoritus)) {
    case 'internationalschoolpypvuosiluokka': return 'PYP'
    case 'internationalschoolmypvuosiluokka': return 'MYP'
    case 'internationalschooldiplomavuosiluokka':
      return `${modelData(model, 'diplomaType.koodiarvo') === 'ish' ? 'ISH' : 'IB'} Diploma`
  }
}

const DiplomaTypeEditor = ({model}) => {
  const initialDiplomaType = modelData(model, 'diplomaType')
  const diplomaTypeAtom = Atom(initialDiplomaType)
  diplomaTypeAtom.filter(x => !R.equals(initialDiplomaType, x)).skipDuplicates(R.equals).onValue(diplomaType => {
    pushModel(modelSetData(model, diplomaType, 'diplomaType'))
  })
  return <KoodistoDropdown className='internationalschooldiplomatype' options={koodistoValues('internationalschooldiplomatype')} selected={diplomaTypeAtom}/>
}

DiplomaTypeEditor.displayName = 'DiplomaTypeEditor'

