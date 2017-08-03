import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Text from '../Text.jsx'
import ModalDialog from './ModalDialog.jsx'
import {Editor} from './Editor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {modelData, modelTitle, modelLookup, resetOptionalModel, accumulateModelStateAndValidity, pushModel} from './EditorModel.js'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {ISO2FinnishDate} from '../date'

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
        propertyFilter={p => !['arviointikohteet', 'haluaaTodistuksen', 'arvioitsijat', 'arvioinnistaPäättäneet', 'arviointikeskusteluunOsallistuneet', 'hylkäyksenPeruste', 'suoritusaika'].includes(p.key)}
        getValueEditor={(p, getDefault) => {
          if (p.key === 'suorituspaikka') {return (
            <table><tbody><tr>
              <td><Editor model={modelLookup(model, 'suorituspaikka.tunniste')}/></td>
              <td><Editor model={modelLookup(model, 'suorituspaikka.kuvaus')}/></td>
            </tr></tbody></table>
          )}
          return getDefault()
        }}
      />
    </ModalDialog>
  )
}

const YksittäinenNäyttöEditor = ({edit, model, popupVisibleA}) => {
  let alku  = ISO2FinnishDate(modelTitle(model, 'suoritusaika.alku'))
  let loppu = ISO2FinnishDate(modelTitle(model, 'suoritusaika.loppu'))

  return (<div>
    <div>
      {edit && <a className="remove-value fa fa-times-circle-o" onClick={() => resetOptionalModel(model)}></a>}
      {edit && <a className="fa fa-pencil-square-o" onClick={() => popupVisibleA.set(true)}></a>}
      {alku === loppu
        ? <span className="pvm">{alku}</span>
        : <span><span className="alku pvm">{alku}</span>{' - '}<span className="loppu pvm">{loppu}</span></span>
      }
      <span>{'Työpaikka: '}{modelTitle(model, 'suorituspaikka.kuvaus')}</span>
      <span>{modelTitle(model, 'arviointi.arvosana')}</span>
    </div>
    <div>
      <p className="kuvaus">{modelTitle(model, 'kuvaus')}</p>
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
          : '')
        }
        {hasData &&
          <YksittäinenNäyttöEditor edit={edit} model={wrappedModel} popupVisibleA={popupVisibleA}/>
        }
        {edit && !hasData &&
          <a onClick={() => popupVisibleA.set(true)}><Text name="Lisää ammattiosaamisen näyttö"/></a>
        }
      </div>
    )
  }
})

AmmatillinenNäyttöEditor.handlesOptional = () => true
AmmatillinenNäyttöEditor.writeOnly = true