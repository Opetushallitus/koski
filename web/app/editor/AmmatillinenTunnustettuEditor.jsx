import React from 'baret'
import Atom from 'bacon.atom'
import Text from '../Text.jsx'
import {Editor} from './Editor.jsx'
import {modelLookup, resetOptionalModel} from './EditorModel.js'
import {modelEmpty, optionalPrototypeModel, pushModel} from './EditorModel'
import {PropertiesEditor} from './PropertiesEditor.jsx'

export class AmmatillinenTunnustettuEditor extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      popupVisibleA: Atom(false)
    }
  }

  render() {
    const model = this.props.model
    const hasData = !modelEmpty(model)
    let seliteModel = modelLookup(model, 'selite')

    return (
      <div>
        {
          hasData
            ? (<span>
                <span className="selite"><Editor model={seliteModel} autoFocus={true}/></span>
                <a className="remove-value" onClick={() => resetOptionalModel(model)}></a>
                <PropertiesEditor model={model} propertyFilter={(p) => ['rahoituksenPiirissä'].includes(p.key)}/>
              </span>)
            : <span><a className="add-value" onClick={() => pushModel(optionalPrototypeModel(model))}><Text name="Lisää ammattiosaamisen tunnustaminen"/></a></span>
        }
      </div>
    )
  }
}
AmmatillinenTunnustettuEditor.handlesOptional = () => true
AmmatillinenTunnustettuEditor.writeOnly = true

