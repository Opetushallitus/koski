import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Text from '../Text.jsx'
import ModalDialog from './ModalDialog.jsx'
import { Editor } from './Editor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {modelData, modelTitle, modelLookup, resetOptionalModel} from './EditorModel.js'
import {ISO2FinnishDate} from '../date'

const UusiNäyttöPopup = ({model, doneCallback}) => {
  let validP = Bacon.constant(true)
  let submitBus = Bacon.Bus()

  submitBus.onValue(() => {
    // TODO
    doneCallback()
  })

  return (
    <ModalDialog className="lisää-näyttö-modal" onDismiss={doneCallback} onSubmit={() => submitBus.push()} okTextKey="Lisää" validP={validP}>
      <h2><Text name="Ammattiosaamisen näyttö"/></h2>
      <div className="properties">
        <table>
          <tbody>
            <tr className="property">
              <td className="label"><Text name="Kuvaus"/></td>
              <td><Editor model={modelLookup(model, 'kuvaus')}/></td>
            </tr>
            <tr className="property">
              <td className="label"><Text name="Suorituspaikka"/></td>
              <td>
                <table><tbody><tr>
                  <td><Editor model={modelLookup(model, 'suorituspaikka.tunniste')}/></td>
                  <td><Editor model={modelLookup(model, 'suorituspaikka.kuvaus')}/></td>
                </tr></tbody></table>
              </td>
            </tr>
            <tr className="property">
              <td className="label"><Text name="Työssäoppimisen yhteydessä"/></td>
              <td><Editor model={modelLookup(model, 'työssäoppimisenYhteydessä')}/></td>
            </tr>
            <tr className="property">
              <td className="label"><Text name="Suoritusaika"/></td>
              <td><Editor model={modelLookup(model, 'suoritusaika')}/></td>
            </tr>
            <tr className="property">
              <td className="label"><Text name="Arviointi"/></td>
              <td>
                <table><tbody><tr>
                  <td><Editor model={modelLookup(model, 'arviointi.päivä')}/></td>
                  <td><Editor model={modelLookup(model, 'arviointi.arvosana')}/></td>
                </tr></tbody></table>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </ModalDialog>
  )
}

const YksittäinenNäyttöEditor = ({edit, wm, popupVisibleA}) => {
  let alku  = ISO2FinnishDate(modelTitle(wm, 'suoritusaika.alku'))
  let loppu = ISO2FinnishDate(modelTitle(wm, 'suoritusaika.loppu'))

  return (<div>
    <div>
      {edit && <a className="remove-value fa fa-times-circle-o" onClick={() => resetOptionalModel(wm)}></a>}
      {edit && <a className="fa fa-pencil-square-o" onClick={() => popupVisibleA.set(true)}></a>}
      {alku === loppu
        ? <span className="pvm">{alku}</span>
        : <span><span className="alku pvm">{alku}</span>{' - '}<span className="loppu pvm">{loppu}</span></span>
      }
      <span>{'Työpaikka: '}{modelTitle(wm, 'suorituspaikka.kuvaus')}</span>
      <span>{modelTitle(wm, 'arviointi.arvosana')}</span>
    </div>
    <div>
      <p className="kuvaus">{modelTitle(wm, 'kuvaus')}</p>
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
          <YksittäinenNäyttöEditor edit={edit} wm={wrappedModel} popupVisibleA={popupVisibleA}/>
        }
        {edit && !hasData &&
          <a onClick={() => popupVisibleA.set(true)}><Text name="Lisää ammattiosaamisen näyttö"/></a>
        }
      </div>
    )
  }
})

AmmatillinenNäyttöEditor.handlesOptional = () => true
