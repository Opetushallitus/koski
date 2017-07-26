import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import R from 'ramda'
import Text from '../Text.jsx'
import ModalDialog from './ModalDialog.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {modelData, modelTitle} from './EditorModel.js'
import {ISO2FinnishDate} from '../date'

const UusiNäyttöPopup = ({model, doneCallback}) => {
  let validP = Bacon.constant(true)
  let submitBus = Bacon.Bus()

  submitBus.onValue(() => {
    console.log('sb')
    doneCallback()
  })

  return (
    <ModalDialog className="lisaa-näyttö-modal" onDismiss={doneCallback} onSubmit={() => submitBus.push()} okTextKey="Lisää" validP={validP}>
      <h2><Text name="Ammattiosaamisen näyttö"/></h2>
      <p>{'..FIELDS..'}</p>
    </ModalDialog>
  )
}

const YksittäinenNäyttöEditor = ({edit, wm}) => {
  let alku  = ISO2FinnishDate(modelTitle(wm, 'suoritusaika.alku'))
  let loppu = ISO2FinnishDate(modelTitle(wm, 'suoritusaika.loppu'))

  return (<li>
    <div>
      {edit && <a className="remove-value fa fa-times-circle-o"></a>}
      {alku === loppu
        ? <span className="pvm">{alku}</span>
        : <span><span className="alku pvm">{alku}</span>{' - '}<span className="loppu pvm">{loppu}</span></span>
      }
      <span>{'Työpaikka: '}{modelTitle(wm, 'suorituspaikka.kuvaus')}</span>
      <span>{modelTitle(wm, 'arviointi.arvosana')}</span>
    </div>
    <div>
      <p>{modelTitle(wm, 'kuvaus')}</p>
    </div>
  </li>)
}

export const AmmatillinenNäyttöEditor = ({model}) => {
  const popupVisibleA = Atom(false)

  const edit = model.context.edit
  let wrappedModel = wrapOptional({model})

  console.log(wrappedModel)
  let data = modelData(wrappedModel, 'kuvaus')

  let sections = []
  if (data !== undefined) {
    sections.push(<YksittäinenNäyttöEditor edit={edit} wm={wrappedModel}/>)
  }

  return (
    <div>
      {popupVisibleA.map(visible => visible
        ? <UusiNäyttöPopup edit={edit} model={model} doneCallback={() => popupVisibleA.set(false)}/>
        : '')
      }
      <ul>{sections}</ul>
      {edit &&
        <a onClick={() => popupVisibleA.set(true)}><Text name="Lisää ammattiosaamisen näyttö"/></a>
      }
    </div>
  )
}

AmmatillinenNäyttöEditor.handlesOptional = () => true
