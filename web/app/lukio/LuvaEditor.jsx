import React from 'react'
import {LukionOppiaineetEditor} from './LukionOppiaineetEditor'
import Text from '../i18n/Text'
import {modelItems, modelSetValue} from '../editor/EditorModel'

export const LuvaEditor = ({suorituksetModel}) => {
  const {edit} = suorituksetModel.context
  const suoritukset = modelItems(suorituksetModel)
  const lukionkurssinsuoritukset = suoritukset.filter(s => s.value.classes.includes('lukionoppiaineenopintojensuorituslukioonvalmistavassakoulutuksessa'))
  const lukioonvalmistavankurssinsuoritukset = suoritukset.filter(s => s.value.classes.includes('lukioonvalmistavankoulutuksenoppiaineensuoritus'))

  return (
    <div>
      {
        (edit || lukioonvalmistavankurssinsuoritukset.length > 0) &&
        <div>
          <h5><Text name="Lukioon valmistavat opinnot"/></h5>
          <LukionOppiaineetEditor
            suorituksetModel={modelSetValue(suorituksetModel, lukioonvalmistavankurssinsuoritukset)}
          />
        </div>
      }
      {
        (edit || lukionkurssinsuoritukset.length > 0) &&
        <div>
          <h5><Text name="Valinnaisena suoritetut lukiokurssit"/></h5>
          <LukionOppiaineetEditor
            suorituksetModel={modelSetValue(suorituksetModel, lukionkurssinsuoritukset)}
          />
        </div>
      }
    </div>
  )
}
