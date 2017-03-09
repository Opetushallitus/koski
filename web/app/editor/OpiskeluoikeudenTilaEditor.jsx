import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'
import * as L from 'partial.lenses'
import {childContext, contextualizeModel, modelData, modelItems, modelLookup} from './EditorModel.js'
import {ArrayEditor} from './ArrayEditor.jsx'
import {OpiskeluoikeusjaksoEditor} from './OpiskeluoikeusjaksoEditor.jsx'
import {EnumEditor} from './EnumEditor.jsx'
import {DateEditor} from './DateEditor.jsx'

export const OpiskeluoikeudenTilaEditor = React.createClass({
  render() {
    let {model} = this.props
    let {saveChangesBus, cancelBus, changeBus, newTilaModel, items = modelItems(model)} = this.state

    let showAddDialog = () => {
      let tilaModel = contextualizeModel(model.prototype, R.merge(childContext(model.context, items.length), {changeBus}))
      changeBus.push([childContext(tilaModel.context, 'alku'), {data: modelData(tilaModel, 'alku')}])
      this.setState({newTilaModel: tilaModel})
    }

    let add = () => {
      saveChangesBus.push()
    }

    let cancel = () => {
      cancelBus.push()
    }

    return (
      model.context.edit ?
        <div>
          <ul ref="ul" className="array">
            {
              items.map((item, i) => {
                // TODO: nasty copy paste from ArrayEditor
                let removeItem = () => {
                  let newItems = L.set(L.index(i), undefined, items)
                  item.context.changeBus.push([item.context, {data: undefined}])
                  this.setState({newTilaModel: null, items: newItems})
                }
                return (<li key={i}>
                  <OpiskeluoikeusjaksoEditor model={item}/>
                  <a className="remove-item" onClick={removeItem}></a>
                </li>)
              })
            }
            {
              <li className="add-item"><a onClick={showAddDialog}>Lisää opiskeluoikeuden tila</a></li>
            }
          </ul>
          {
            newTilaModel && (
              <div className="lisaa-opiskeluoikeusjakso-modal">
                <div className="lisaa-opiskeluoikeusjakso">
                  <a className="close-modal" onClick={cancel}>&#10005;</a>
                  <h2>Opiskeluoikeuden tilan lisäys</h2>
                  <table>
                    <tbody>
                    <tr className="property alku">
                      <td className="label">{newTilaModel.value.properties.find(p => p.key == 'alku').title}</td>
                      <td className="value"><DateEditor model={modelLookup(newTilaModel, 'alku')}/></td>
                    </tr>
                    <tr className="property tila">
                      <td className="label">{newTilaModel.value.properties.find(p => p.key == 'tila').title}</td>
                      <td className="value"><EnumEditor asRadiogroup="true" model={modelLookup(newTilaModel, 'tila')}/></td>
                    </tr>
                    </tbody>
                  </table>
                  <a className="button" onClick={add}>Lisää</a>
                  <a onClick={cancel}>Peruuta</a>
                </div>
              </div>
            )
          }
        </div> :
        <div><ArrayEditor reverse={true} model={model}/></div>
    )
  },
  getInitialState() {
    let {model} = this.props
    let changeBus = Bacon.Bus()
    let saveChangesBus = Bacon.Bus()
    let cancelBus = Bacon.Bus()

    let changesP = Bacon.update([],
      changeBus, (xs, x) => xs.concat(x),
      cancelBus, () => []
    )

    saveChangesBus.merge(cancelBus).onValue(() => this.setState({newTilaModel: undefined}))

    changesP.sampledBy(saveChangesBus).onValue((changes) => {
      model.context.changeBus.push(changes)
      model.context.doneEditingBus.push()
      cancelBus.push()
    })

    return {
      saveChangesBus: saveChangesBus,
      cancelBus: cancelBus,
      changeBus: changeBus
    }
  }
})
