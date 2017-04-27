import React from 'react'
import {modelTitle, modelItems} from './EditorModel.js'
import {Editor} from './Editor.jsx'

export const VahvistusEditor = React.createClass({
  render() {
    let { model } = this.props
    return (<span className="vahvistus inline">
      <span className="date"><Editor model={model} path="päivä" edit="false"/></span>&nbsp;
      <span className="allekirjoitus">{modelTitle(model, 'paikkakunta')}</span>&nbsp;
      {
        (modelItems(model, 'myöntäjäHenkilöt') || []).map( (henkilö,i) =>
          <span key={i}><Editor model={henkilö} path="nimi"/>, <Editor model={henkilö} path="titteli"/></span>
        )
      }
    </span>)
  }
})
