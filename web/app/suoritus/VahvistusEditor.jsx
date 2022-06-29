import React from 'react'
import {modelTitle, modelItems, modelData} from '../editor/EditorModel'
import {Editor} from '../editor/Editor'

export class VahvistusEditor extends React.Component {
  render() {
    let { model } = this.props
    return (<span className="vahvistus inline">
      <span className="date"><Editor model={model} path="päivä" edit="false"/></span>&nbsp;
      <span className="allekirjoitus">{modelTitle(model, 'paikkakunta')}</span>&nbsp;
      {
        (modelItems(model, 'myöntäjäHenkilöt') || []).map( (henkilö,i) =>
            (<span key={i}>
              <Editor model={henkilö} path="nimi"/>{(modelData(henkilö, 'titteli') || model.context.edit) && <span>{', '}<Editor model={henkilö} path="titteli"/></span>}
            </span>)
        )
      }
    </span>)
  }
}
