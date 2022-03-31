import React from 'react'
import {modelData, modelLookup} from '../editor/EditorModel'
import {PäivämääräväliEditor} from '../date/PaivamaaravaliEditor'
import {DateEditor} from '../editor/DateEditor'
import {BooleanEditor} from '../editor/BooleanEditor'
import Text from '../i18n/Text'


export const MaksuttomuusEditor = ({model}) => {
  const alkupäiväModel = modelLookup(model, 'alku')
  const maksullisuusModel = modelLookup(model, 'maksuton')

  if (model.context.edit) {
    return (
      <>
        <DateEditor model={alkupäiväModel} />
        <Text name={'Maksuton'} />
        {': '}
        <BooleanEditor model={maksullisuusModel}/>
      </>
    )
  } else {
    return (
      <>
        <PäivämääräväliEditor model={model}/>
        {' '}
        <Text name={modelData(maksullisuusModel) ? 'Maksuton' : 'Maksullinen'}/>
      </>
    )
  }
}

MaksuttomuusEditor.displayName = 'MaksuttomuusEditor'
