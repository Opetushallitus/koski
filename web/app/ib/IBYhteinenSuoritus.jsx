import React from 'react'

import {modelData, modelItems, modelLookup, wrapOptional} from '../editor/EditorModel'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {SelectAlternativeByEnumValueEditor} from '../editor/SelectAlternativeByEnumValueEditor'
import {KurssitEditor} from '../kurssi/KurssitEditor'
import {ArvosanaEditor} from '../suoritus/ArvosanaEditor'
import {FootnoteHint} from '../components/footnote'

const YhteisetEditorit = ({model}) => (
  (model.context.edit || modelItems(model, 'arviointi').length > 0) &&
  <div className='arviointi'>
    {
      <ArvosanaEditor
        model={wrapOptional(model)}
        key={'arvosana'}
      />
    }
    {model.context.edit
      ? (
        <PropertiesEditor
          model={modelLookup(model, 'arviointi.-1')}
          propertyFilter={p => p.key === 'predicted'}
          key={'properties'}
        />
      )
      : modelData(model, 'arviointi.-1.predicted') && <FootnoteHint title='Ennustettu arvosana' hint='*'/>
    }
  </div>
)

YhteisetEditorit.displayName = 'YhteisetEditorit'

const CreativityActionService = ({model}) => <YhteisetEditorit model={model}/>

CreativityActionService.displayName = 'CreativityActionService'

const TheoryOfKnowledge = ({model}) => (
  <div>
    <YhteisetEditorit model={model}/>
    <KurssitEditor model={wrapOptional(model)}/>
  </div>
)

TheoryOfKnowledge.displayName = 'TheoryOfKnowledge'

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
      <YhteisetEditorit model={model}/>
    </div>
  )
}

ExtendedEssay.displayName = 'ExtendedEssay'

export {
  TheoryOfKnowledge,
  CreativityActionService,
  ExtendedEssay
}
