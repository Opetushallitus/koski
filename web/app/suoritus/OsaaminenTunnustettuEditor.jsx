import React from 'baret'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import {Editor} from '../editor/Editor'
import {modelLookup, resetOptionalModel} from '../editor/EditorModel'
import {modelEmpty, optionalPrototypeModel, pushModel} from '../editor/EditorModel'
import {PropertiesEditor} from '../editor/PropertiesEditor'

const addValue = model => event => {
  event.nativeEvent.stopImmediatePropagation()
  pushModel(optionalPrototypeModel(model))
}

const removeValue = model => event => {
  event.nativeEvent.stopImmediatePropagation()
  resetOptionalModel(model)
}

export class OsaaminenTunnustettuEditor extends React.Component {
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
                <a className="remove-value" onClick={removeValue(model)}></a>
                <PropertiesEditor model={model} propertyFilter={(p) => ['rahoituksenPiirissä'].includes(p.key)}/>
              </span>)
            : <span><a className="add-value" onClick={addValue(model)}><Text name="Lisää osaamisen tunnustaminen"/></a></span>
        }
      </div>
    )
  }
}
OsaaminenTunnustettuEditor.handlesOptional = () => true
OsaaminenTunnustettuEditor.writeOnly = true

