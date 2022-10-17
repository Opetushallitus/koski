import React from 'baret'
import { rekursiivisetOsasuoritukset } from '../suoritus/Suoritus'
import { modelData, modelItems } from '../editor/EditorModel'
import * as R from 'ramda'
import Text from '../i18n/Text'

export const AmmatillinenArviointiasteikko = ({ model }) => {
  if (!isAmmatillinenPaatasonSuoritus(model)) return null

  const asteikko = kaytossaOlevaAsteikko(model)

  return asteikko ? (
    <div className="ammatillinenarviointiasteikko">
      <h5>
        <Text name="Tutkinnon osien arviointiasteikko" />
        {':'}
      </h5>
      <Text name={asteikko} />
    </div>
  ) : null
}

const isAmmatillinenPaatasonSuoritus = (model) =>
  model.value.classes.includes('ammatillinenpaatasonsuoritus')

const kaytossaOlevaAsteikko = (model) => {
  const asteikot = rekursiivisetOsasuoritukset(model).map(arviointiasteikko)

  if (asteikot.includes('arviointiasteikkoammatillinen15')) {
    return '1-5, Hylätty tai Hyväksytty'
  }
  if (asteikot.includes('arviointiasteikkoammatillinent1k3')) {
    return '1-3, Hylätty tai Hyväksytty'
  }
  if (asteikot.includes('arviointiasteikkoammatillinenhyvaksyttyhylatty')) {
    return 'Hylätty tai Hyväksytty'
  }
  return undefined
}

const arviointiasteikko = (model) => {
  const viimeisinArviointi = R.last(modelItems(model, 'arviointi'))
  const arvosana = modelData(viimeisinArviointi, 'arvosana')
  return arvosana && arvosana.koodistoUri
}
