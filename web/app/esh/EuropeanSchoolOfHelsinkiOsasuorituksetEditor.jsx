import React from 'baret'
import { addContext, modelLookup } from '../editor/EditorModel'
import Text from '../i18n/Text'
import { EuropeanSchoolOfHelsinkiSuoritustaulukko } from './EuropeanSchoolOfHelsinkiSuoritustaulukko'
import { PropertyEditor } from '../editor/PropertyEditor'
import { isEshS7 } from '../suoritus/SuoritustaulukkoCommon'

export const EuropeanSchoolOfHelsinkiOsasuorituksetEditor = ({ model }) => {
  model = addContext(model, { suoritus: model })
  const osasuorituksetModel = modelLookup(model, 'osasuoritukset')

  return (
    <div className="oppiaineet osasuoritukset" data-testid="oppiaineet-list">
      {isEshS7(model) && (
        <div className="esh-s7-jaa-luokalle">
          <PropertyEditor model={model} propertyName="jääLuokalle" />
        </div>
      )}
      <div>
        <h5>
          <Text name={'Oppiaineiden arvosanat'} />
        </h5>
        {osasuorituksetModel && (
          <EuropeanSchoolOfHelsinkiSuoritustaulukko
            parentSuoritus={model}
            suorituksetModel={osasuorituksetModel}
          />
        )}
      </div>
    </div>
  )
}
