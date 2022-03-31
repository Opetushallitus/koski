import React from 'react'
import {modelLookup} from '../editor/EditorModel'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {hasArviointi} from '../suoritus/Suoritus'

const properties = isEdit => isEdit ? ['effort'] : ['arvosana', 'effort']

const IBKurssinArviointiEditor = ({model}) => hasArviointi(model) && (
  <PropertiesEditor
    model={modelLookup(model, 'arviointi.-1')}
    propertyFilter={p => properties(model.context.edit).includes(p.key)}
    key={'properties'}
  />
)

IBKurssinArviointiEditor.displayName = 'IBKurssinArviointiEditor'

export default IBKurssinArviointiEditor
