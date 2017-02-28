import React from 'react'
import { modelData, modelTitle } from './EditorModel.js'
import * as GenericEditor from './GenericEditor.jsx'
import { PäivämääräväliEditor, KoulutusmoduuliEditor } from './CommonEditors.jsx'

export const NäytönSuorituspaikkaEditor = React.createClass({
  render() {
    let {model} = this.props
    if (model.context.edit) return <GenericEditor.ObjectEditor {...this.props}/>
    return <span>{modelTitle(model, 'kuvaus')}</span>
  }
})

export const NäytönArvioitsijaEditor = React.createClass({
  render() {
    let {model} = this.props
    if (model.context.edit) return <GenericEditor.ObjectEditor {...this.props}/>
    return <span>{modelTitle(model, 'nimi')} { modelData(model, 'ntm') ? ' (näyttötutkintomestari)' : ''}</span>
  }
})
NäytönArvioitsijaEditor.canShowInline = () => true

export const TyössäoppimisjaksoEditor = React.createClass({
  render() {
    let {model} = this.props
    if (model.context.edit) return <GenericEditor.ObjectEditor {...this.props}/>
    return (
      <div className="tyossaoppimisjakso">
        <PäivämääräväliEditor model={model}/> { modelTitle(model, 'paikkakunta')}, { modelTitle(model, 'maa')}
        <GenericEditor.PropertiesEditor
          model = {model}
          propertyFilter={p => !['alku', 'loppu', 'paikkakunta', 'maa'].includes(p.key)}
        />
      </div>
    )
  }
})

export const editorMapping = {
  'ammatillisentutkinnonosa': KoulutusmoduuliEditor,
  'naytonsuorituspaikka': NäytönSuorituspaikkaEditor,
  'naytonarvioitsija': NäytönArvioitsijaEditor,
  'naytonsuoritusaika': PäivämääräväliEditor,
  'tyossaoppimisjakso': TyössäoppimisjaksoEditor
}