import React from 'react'
import { modelLookup } from '../editor/EditorModel'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { hasArviointi } from '../suoritus/Suoritus'
import { EnumEditor } from '../editor/EnumEditor'

export default ({ model }) =>
  hasArviointi(model) && (
    <PropertiesEditor
      model={modelLookup(model, 'arviointi.-1')}
      key="properties"
      getValueEditor={(property, defaultEditor) => {
        if (property.key === 'arvosana') {
          return (
            <EnumEditor
              model={enumModelKaikillaKoodistonKoodeilla(property.model)}
            />
          )
        }
        return defaultEditor()
      }}
    />
  )

const enumModelKaikillaKoodistonKoodeilla = (model) => ({
  ...model,
  alternativesPath: '/koski/api/editor/koodit/arviointiasteikkoyleissivistava'
})
