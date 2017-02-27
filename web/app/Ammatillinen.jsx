import React from 'react'
import { modelData, modelTitle } from './EditorModel.js'
import * as GenericEditor from './GenericEditor.jsx'
import { PäivämääräväliEditor, KoulutusmoduuliEditor } from './CommonEditors.jsx'

export const NäytönSuorituspaikkaEditor = React.createClass({
  render() {
    let {model, context} = this.props
    if (context.edit) return <GenericEditor.ObjectEditor {...this.props}/>
    return <span>{modelTitle(model, 'kuvaus')}</span>
  }
})

export const NäytönArvioitsijaEditor = React.createClass({
  render() {
    let {model, context} = this.props
    if (context.edit) return <GenericEditor.ObjectEditor {...this.props}/>
    return <span>{modelTitle(model, 'nimi')} { modelData(model, 'ntm') ? ' (näyttötutkintomestari)' : ''}</span>
  }
})
NäytönArvioitsijaEditor.canShowInline = () => true

export const TyössäoppimisjaksoEditor = React.createClass({
  render() {
    let {model, context} = this.props
    if (context.edit) return <GenericEditor.ObjectEditor {...this.props}/>
    return (
      <div className="tyossaoppimisjakso">
        <PäivämääräväliEditor context={context} model={model}/> { modelTitle(model, 'paikkakunta')}, { modelTitle(model, 'maa')}
        <GenericEditor.PropertiesEditor
          properties={model.value.properties.filter(p => !['alku', 'loppu', 'paikkakunta', 'maa'].includes(p.key))}
          context={context}/>
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