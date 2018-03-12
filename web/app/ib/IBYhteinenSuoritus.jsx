import React from 'react'

import {ArvosanaEditor} from '../suoritus/ArvosanaEditor'
import {modelLookup, wrapOptional} from '../editor/EditorModel'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {SelectAlternativeByEnumValueEditor} from '../editor/SelectAlternativeByEnumValueEditor'

const yhteisetEditorit = model => (
  <div className='arviointi'>
    <ArvosanaEditor
      model={wrapOptional(model)}
      key={'arvosana'}
    />
    {model.context.edit &&
    <PropertiesEditor
      model={modelLookup(model, 'arviointi.-1')}
      propertyFilter={p => p.key === 'predicted'}
      key={'properties'}
    />}
  </div>
)

const TheoryOfKnowledge = ({model}) => <div>{yhteisetEditorit(model)}</div>
const CreativityActionService = ({model}) => <div>{yhteisetEditorit(model)}</div>

const ExtendedEssay = ({model}) => {
  const tunniste = (
    <SelectAlternativeByEnumValueEditor
      model={modelLookup(model, 'koulutusmoduuli.aine')}
      path={'tunniste'}
    />
  )

  const aine = (
    <PropertiesEditor
      model={modelLookup(model, 'koulutusmoduuli.aine')}
      propertyFilter={p => p.key !== 'pakollinen'}
      getValueEditor={(p, getDefault) => p.key === 'tunniste' ? tunniste  : getDefault()}
    />
  )

  const aineJaAihe = (
    <PropertiesEditor
      model={modelLookup(model, 'koulutusmoduuli')}
      propertyFilter={p => !['tunniste', 'pakollinen'].includes(p.key)}
      getValueEditor={(p, getDefault) => p.key === 'aine' ? aine : getDefault()}
      key={'aine-ja-aihe'}
    />
  )

  return (
    <div>
      {model.context.edit && aineJaAihe}
      {yhteisetEditorit(model)}
    </div>
  )
}

export {
  TheoryOfKnowledge,
  CreativityActionService,
  ExtendedEssay
}
