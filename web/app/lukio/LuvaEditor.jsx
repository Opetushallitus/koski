import React from 'react'
import {LukionOppiaineetEditor} from './LukionOppiaineetEditor'
import Text from '../i18n/Text'
import {modelItems} from '../editor/EditorModel'

export const LuvaEditor = ({suorituksetModel}) => {
  const {edit} = suorituksetModel.context

  const lukionkurssinsuorituksetFilter = s => s.value.classes.includes('lukionoppiaineenopintojensuorituslukioonvalmistavassakoulutuksessa')
  const lukioonvalmistavankurssinsuorituksetFilter = s => s.value.classes.includes('lukioonvalmistavankoulutuksenoppiaineensuoritus')

  const hasLukionKursseja = modelItems(suorituksetModel).filter(lukionkurssinsuorituksetFilter).length > 0
  const hasValmistaviaKursseja = modelItems(suorituksetModel).filter(lukioonvalmistavankurssinsuorituksetFilter).length > 0

  return (
    <div>
      {
        (edit || hasValmistaviaKursseja) &&
        <div className='lukioon-valmistavat-opinnot'>
          <h5><Text name="Lukioon valmistavat opinnot"/></h5>
          <LukionOppiaineetEditor
            suorituksetModel={suorituksetModel}
            classForUusiOppiaineenSuoritus='lukioonvalmistavankoulutuksenoppiaineensuoritus'
            suoritusFilter={lukioonvalmistavankurssinsuorituksetFilter}
          />
        </div>
      }
      {
        (edit || hasLukionKursseja) &&
        <div className='valinnaisena-suoritetut-lukiokurssit'>
          <h5><Text name="Valinnaisena suoritetut lukiokurssit"/></h5>
          <LukionOppiaineetEditor
            suorituksetModel={suorituksetModel}
            classForUusiOppiaineenSuoritus='lukionoppiaineenopintojensuorituslukioonvalmistavassakoulutuksessa'
            suoritusFilter={lukionkurssinsuorituksetFilter}
          />
        </div>
      }
    </div>
  )
}
