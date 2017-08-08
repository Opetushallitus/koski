import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Text from '../Text.jsx'
import ModalDialog from './ModalDialog.jsx'
import {Editor} from './Editor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {modelData, modelLookup, resetOptionalModel, accumulateModelStateAndValidity, pushModel} from './EditorModel.js'
import {PropertiesEditor} from './PropertiesEditor.jsx'

const UusiNäyttöPopup = ({model, doneCallback}) => {
  const {modelP, errorP} = accumulateModelStateAndValidity(model)
  const validP = errorP.not()
  const submitB = Bacon.Bus()

  submitB.map(modelP).onValue(m => {
    pushModel(m, model.context.changeBus)
    doneCallback()
  })

  return (
    <ModalDialog className="lisää-näyttö-modal" onDismiss={doneCallback} onSubmit={() => submitB.push()} okTextKey="Lisää" validP={validP}>
      <h2><Text name="Ammattiosaamisen näyttö"/></h2>

      <PropertiesEditor
        baret-lift
        model={modelP}
        propertyFilter={p => !['arviointikohteet', 'haluaaTodistuksen', 'arvioitsijat', 'hylkäyksenPeruste', 'suoritusaika'].includes(p.key)}
        getValueEditor={(p, getDefault) => {
          if (p.key === 'suorituspaikka') {return (
            <table><tbody><tr>
              {
                modelP.map(m => [
                  <td><Editor model={modelLookup(m, 'suorituspaikka.tunniste')}/></td>,
                  <td><Editor model={modelLookup(m, 'suorituspaikka.kuvaus')}/></td>
                ])
              }
            </tr></tbody></table>
          )}
          return getDefault()
        }}
      />
    </ModalDialog>
  )
}

const YksittäinenNäyttöEditor = ({edit, model, popupVisibleA}) => {
  const hasSuoritusaika = !!modelData(model, 'suoritusaika')
  const hasTyöpaikka = !!modelData(model, 'suorituspaikka.kuvaus')
  const hasArvosana = !!modelData(model, 'arviointi.arvosana')

  return (<div>
    <div>
      {edit && <a className="remove-value fa fa-times-circle-o" onClick={() => resetOptionalModel(model)}></a>}
      {edit && <a className="edit-value fa fa-pencil-square-o" onClick={() => popupVisibleA.set(true)}></a>}
      {hasSuoritusaika && <span className="suoritusaika"><Editor model={modelLookup(model, 'suoritusaika')} edit={false}/></span>}
      {hasTyöpaikka && <span>{'Työpaikka: '}<Editor className="työpaikka" model={modelLookup(model, 'suorituspaikka.kuvaus')} edit={false}/></span>}
      {hasArvosana && <span className="arvosana"><Editor className="arvosana" model={modelLookup(model, 'arviointi.arvosana')} edit={false}/></span>}
    </div>
    <div>
      <p className="kuvaus"><Editor className="kuvaus" model={modelLookup(model, 'kuvaus')} edit={false}/></p>
    </div>
  </div>)
}

export const AmmatillinenNäyttöEditor = React.createClass({
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
    const hasData = !!modelData(wrappedModel, 'kuvaus')

    return (
      <div>
        {popupVisibleA.map(visible => visible
          ? <UusiNäyttöPopup edit={edit} model={wrappedModel} doneCallback={() => popupVisibleA.set(false)}/>
          : null)
        }
        {hasData &&
          <YksittäinenNäyttöEditor edit={edit} model={model} popupVisibleA={popupVisibleA}/>
        }
        {edit && !hasData &&
          <a className="add-value" onClick={() => popupVisibleA.set(true)}><Text name="Lisää ammattiosaamisen näyttö"/></a>
        }
      </div>
    )
  }
})

AmmatillinenNäyttöEditor.handlesOptional = () => true
AmmatillinenNäyttöEditor.writeOnly = true