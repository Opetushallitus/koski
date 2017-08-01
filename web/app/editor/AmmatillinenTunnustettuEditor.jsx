import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Text from '../Text.jsx'
import ModalDialog from './ModalDialog.jsx'
import {Editor} from './Editor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {modelLookup, resetOptionalModel} from './EditorModel.js'
import {wrapOptional} from './OptionalEditor.jsx'


const AmmatillinenTunnustettuPopup = ({model, doneCallback}) => {
  let validP = Bacon.constant(true)

  return (
    <ModalDialog className="lisää-tunnustettu-modal" onDismiss={doneCallback} onSubmit={doneCallback}  okTextKey="Lisää" validP={validP}>
      <h2><Text name="Ammattiosaamisen tunnustaminen"/></h2>
      <PropertiesEditor model={model} propertyFilter={p => p.key === 'selite'} />
    </ModalDialog>
  )
}

const TunnustettuItemEditor = ({model, popupVisibleA}) => {
  return (
    <div>
      <a className="remove-value fa fa-times-circle-o" onClick={() => resetOptionalModel(model)}></a>
      <a className="fa fa-pencil-square-o" onClick={() => popupVisibleA.set(true)}></a>
      <Editor model={modelLookup(model, 'selite')} edit={false}/>
    </div>
  )
}

export const AmmatillinenTunnustettuEditor = React.createClass({
  getInitialState() {
    return {
      popupVisibleA: Atom(false)
    }
  },
  render() {
    const model = this.props.model
    const popupVisibleA = this.state.popupVisibleA
    const edit = model.context.edit

    const wrappedModel = wrapOptional({model})
    const hasData = model.modelId !== 0

    return (
      <div>
        {popupVisibleA.map(v => v ? <AmmatillinenTunnustettuPopup model={wrappedModel} doneCallback={() => popupVisibleA.set(false)}/> : '')}
        {edit && (hasData
          ? <TunnustettuItemEditor model={wrappedModel} popupVisibleA={popupVisibleA}/>
          : <a onClick={() => popupVisibleA.set(true)}><Text name="Lisää ammattiosaamisen tunnustaminen"/></a>
        )}
      </div>
    )
  }
})
AmmatillinenTunnustettuEditor.handlesOptional = () => true

