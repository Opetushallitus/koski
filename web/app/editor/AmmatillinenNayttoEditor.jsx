import React from 'baret'
import R from 'ramda'
import Text from '../Text.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {modelData, modelTitle} from './EditorModel.js'
import {ISO2FinnishDate} from '../date'

const YksittäinenNäyttöEditor = ({wm}) => {
  let alku  = ISO2FinnishDate(modelTitle(wm, 'suoritusaika.alku'))
  let loppu = ISO2FinnishDate(modelTitle(wm, 'suoritusaika.loppu'))

  return (<li>
    <div>
      <a className="remove-value">{''}</a>
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
  let wrappedModel = wrapOptional({model})

  console.log(wrappedModel)
  let data = modelData(wrappedModel, 'kuvaus')

  let sections = []
  if (data !== undefined) {
    sections.push(<YksittäinenNäyttöEditor wm={wrappedModel}/>)
  }

  return (
    <div>
      <ul>{sections}</ul>
      <a><Text name="Lisää ammattiosaamisen näyttö"/></a>
    </div>
  )
}

AmmatillinenNäyttöEditor.handlesOptional = () => true
