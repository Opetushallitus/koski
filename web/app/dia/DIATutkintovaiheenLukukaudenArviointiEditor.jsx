import React from 'react'
import { modelLookup } from '../editor/EditorModel'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { hasArviointi } from '../suoritus/Suoritus'

export const hasLasketaanKokonaispistemäärään = (suoritus) =>
  modelLookup(suoritus, 'arviointi.-1.lasketaanKokonaispistemäärään') !==
  undefined
const properties = (isEdit) =>
  isEdit
    ? ['lasketaanKokonaispistemäärään']
    : ['arvosana', 'lasketaanKokonaispistemäärään']

export default ({ model }) =>
  hasArviointi(model) && (
    <PropertiesEditor
      model={modelLookup(model, 'arviointi.-1')}
      propertyFilter={(p) => properties(model.context.edit).includes(p.key)}
      key={'properties'}
    />
  )
