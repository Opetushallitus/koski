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
    let {model, opiskeluoikeusModel} = this.props
    let {saveChangesBus, cancelBus, changeBus, newTilaModel, items = modelItems(model).slice(0).reverse()} = this.state

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

    let removeItem = () => {
      if (this.onLopputila(modelData(items[0], 'tila').koodiarvo)) {
        let paattymispaivaModel = modelLookup(opiskeluoikeusModel, 'päättymispäivä')
        model.context.changeBus.push([paattymispaivaModel.context, {data: undefined}])
      }
      model.context.changeBus.push([items[0].context, {data: undefined}])
      model.context.doneEditingBus.push()
      let newItems = L.set(L.index(0), undefined, items)
      this.setState({newTilaModel: undefined, items: newItems})
    }

    let lastOpiskeluoikeudenTila = modelData(modelLookup(items[0], 'tila'))
    let showLisaaTila = !lastOpiskeluoikeudenTila || lastOpiskeluoikeudenTila.koodiarvo === 'lasna'

    return (
      model.context.edit ?
        <div>
          <ul ref="ul" className="array">
            {
              items.map((item, i) => {
                return (<li key={i}>
                  <OpiskeluoikeusjaksoEditor model={item}/>
                  {i === 0 && <a className="remove-item" onClick={removeItem}></a>}
                </li>)
              })
            }
            {
              showLisaaTila && <li className="add-item"><a onClick={showAddDialog}>Lisää opiskeluoikeuden tila</a></li>
            }
          </ul>
          {
            newTilaModel && (
              <div className="lisaa-opiskeluoikeusjakso-modal">
                <div className="lisaa-opiskeluoikeusjakso">
                  <a className="close-modal" onClick={cancel}>&#10005;</a>
                  <h2>Opiskeluoikeuden tilan lisäys</h2>
                  <div className="property alku">
                    <label>{newTilaModel.value.properties.find(p => p.key == 'alku').title}:</label>
                    <DateEditor model={modelLookup(newTilaModel, 'alku')}/>
                  </div>
                  <div className="property tila">
                    <label>{newTilaModel.value.properties.find(p => p.key == 'tila').title}:</label>
                    <EnumEditor asRadiogroup="true" model={modelLookup(newTilaModel, 'tila')}/>
                  </div>
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
    let {model, opiskeluoikeusModel} = this.props
    let changeBus = Bacon.Bus()
    let saveChangesBus = Bacon.Bus()
    let cancelBus = Bacon.Bus()

    let changesP = Bacon.update([],
      changeBus, (xs, x) => xs.concat(x),
      cancelBus, () => []
    )

    saveChangesBus.merge(cancelBus).onValue(() => this.setState({newTilaModel: undefined}))

    changesP.sampledBy(saveChangesBus).onValue((changes) => {
      let opiskeluoikeudenPaattymispaiva = this.opiskeluoikeudenPaattymispaiva(changes)
      if (opiskeluoikeudenPaattymispaiva) {
        let paattymispaivaModel = modelLookup(opiskeluoikeusModel, 'päättymispäivä')
        model.context.changeBus.push([paattymispaivaModel.context, {data: opiskeluoikeudenPaattymispaiva}])
      }

      model.context.changeBus.push(changes)
      model.context.doneEditingBus.push()
      cancelBus.push()
    })

    return {
      saveChangesBus: saveChangesBus,
      cancelBus: cancelBus,
      changeBus: changeBus
    }
  },
  opiskeluoikeudenPaattymispaiva(changes) {
    let changePairs = R.splitEvery(2, changes)
    let findLastValue = (path) => {
      let lastPair = R.findLast((c) => c[0].path && c[0].path.endsWith(path), changePairs)
      return lastPair ? lastPair[1] : undefined
    }

    let viimeinenTila = findLastValue('.tila')
    return viimeinenTila && this.onLopputila(viimeinenTila.value) ? findLastValue('.alku').data : undefined
  },
  onLopputila(tila) {
    return tila === 'eronnut' || tila === 'valmistunut'
  }
})
