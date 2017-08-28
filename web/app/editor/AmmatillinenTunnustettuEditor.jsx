import React from 'baret'
import Atom from 'bacon.atom'
import Text from '../Text.jsx'
import {Editor} from './Editor.jsx'
import {modelLookup, resetOptionalModel} from './EditorModel.js'
import {optionalPrototypeModel, pushModel} from './EditorModel'

export class AmmatillinenTunnustettuEditor extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      popupVisibleA: Atom(false)
    }
  }

  render() {
    const model = this.props.model
    const hasData = model.modelId !== 0
    let seliteModel = modelLookup(model, 'selite')

    return (
      <div>
        {
          hasData
            ? <span><Editor model={seliteModel} autoFocus={true}/><a className="remove-value" onClick={() => resetOptionalModel(model)}></a></span>
            : <span><a className="add-value" onClick={() => pushModel(optionalPrototypeModel(model))}><Text name="Lisää ammattiosaamisen tunnustaminen"/></a></span>
        }
      </div>
    )
  }
}
AmmatillinenTunnustettuEditor.handlesOptional = () => true
AmmatillinenTunnustettuEditor.writeOnly = true

