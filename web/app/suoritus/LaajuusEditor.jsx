import React from 'react'
import { Editor } from '../editor/Editor'
import {
  modelData,
  modelEmpty,
  modelLookup,
  modelSetValue,
  modelValid,
  oneOfPrototypes,
  wrapOptional
} from '../editor/EditorModel'
import {
  EnumEditor,
  fetchAlternativesBasedOnPrototypes
} from '../editor/EnumEditor'
import { parseBool } from '../util/util'
import { t } from '../i18n/i18n'
import { hyphenate } from '../util/hyphenate'

export class LaajuusEditor extends React.Component {
  render() {
    const { model, compact, showReadonlyScope = true, disabled } = this.props
    const wrappedModel = wrapOptional(model)
    return (
      <span>
        <span className="property laajuus arvo">
          <span className={modelValid(wrappedModel) ? 'value' : 'value error'}>
            <Editor model={wrappedModel} path="arvo" disabled={disabled} />
          </span>
        </span>
        <LaajuudenYksikköEditor {...{ model, compact, showReadonlyScope }} />
      </span>
    )
  }
}
LaajuusEditor.isEmpty = (m) => modelEmpty(m, 'arvo')
LaajuusEditor.createEmpty = (m) => modelSetValue(m, undefined, 'arvo')
LaajuusEditor.readOnly = false
LaajuusEditor.handlesOptional = () => true

LaajuusEditor.validateModel = (model) => {
  const arvo = modelData(model, 'arvo')

  if ((arvo && isNaN(arvo)) || arvo <= 0) {
    return [{ key: 'invalid.laajuus' }]
  }
  if (!model.optional && !arvo) {
    return [{ key: 'missing' }]
  }
  return []
}

const LaajuudenYksikköEditor = ({ model, compact, showReadonlyScope }) => {
  const arvoData = modelData(model, 'arvo')
  const yksikköModel = modelLookup(model, 'yksikkö')
  const yksikköData = modelData(yksikköModel)
  const yksikkö =
    arvoData === undefined
      ? ''
      : yksikköData && t(yksikköData.lyhytNimi || yksikköData.nimi)
  const alternatives = EnumEditor.knownAlternatives(yksikköModel)

  return model.context.edit ? (
    !yksikköModel ||
    !alternatives ||
    (alternatives.length == 1 && parseBool(compact)) ? null : (
      <span className="property laajuudenyksikko yksikko inline">
        <Editor
          model={yksikköModel}
          edit={alternatives.length !== 1 || !!model.oneOfPrototypes}
          fetchAlternatives={(m) =>
            model.oneOfPrototypes
              ? yksikköAlternativesBasedOnPrototypes(model)
              : EnumEditor.fetchAlternatives(m)
          }
        />
      </span>
    )
  ) : showReadonlyScope ? (
    <span
      className={'property laajuudenyksikko yksikko ' + yksikkö.toLowerCase()}
    >
      {'\u00a0'}
      {hyphenate(yksikkö)}
    </span>
  ) : null
}

const yksikköAlternativesBasedOnPrototypes = (model) =>
  fetchAlternativesBasedOnPrototypes(oneOfPrototypes(model), 'yksikkö')
    .startWith([])
    .map((alts) => alts.map((m) => modelLookup(m, 'yksikkö').value))
