import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'
import * as L from 'partial.lenses'
import {childContext, contextualizeModel, modelData, modelItems, modelLookup, addContext} from './EditorModel.js'
import {ArrayEditor} from './ArrayEditor.jsx'
import {OpiskeluoikeusjaksoEditor} from './OpiskeluoikeusjaksoEditor.jsx'
import {EnumEditor} from './EnumEditor.jsx'
import {DateEditor} from './DateEditor.jsx'

export const OpiskeluoikeudenTilaEditor = React.createClass({
  render() {
    let {model, opiskeluoikeusModel} = this.props
    let {saveChangesBus, cancelBus, alkuPäiväBus, tilaBus, errorBus, newStateModels, items = modelItems(model).slice(0).reverse()} = this.state

    let showAddDialog = () => {
      document.addEventListener('keyup', this.handleKeys)
      let opiskeluoikeusjaksoModel = contextualizeModel(model.arrayPrototype, childContext(model.context, items.length))
      let alkuPäiväModel = addContext(modelLookup(opiskeluoikeusjaksoModel, 'alku'), {changeBus: alkuPäiväBus, errorBus: errorBus})
      let tilaModel = addContext(modelLookup(opiskeluoikeusjaksoModel, 'tila'), {changeBus: tilaBus, errorBus: errorBus})
      alkuPäiväBus.push([alkuPäiväModel.context, {data: modelData(alkuPäiväModel)}])
      this.setState({newStateModels: {alkuPäiväModel, tilaModel}})
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
      this.setState({newStateModels: undefined, items: newItems})
    }

    let lastOpiskeluoikeudenTila = modelData(modelLookup(items[0], 'tila'))
    let showLisaaTila = !lastOpiskeluoikeudenTila || lastOpiskeluoikeudenTila.koodiarvo === 'lasna' || lastOpiskeluoikeudenTila.koodiarvo === 'valiaikaisestikeskeytynyt'

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
            newStateModels && (
              <div className="lisaa-opiskeluoikeusjakso-modal">
                <div className="lisaa-opiskeluoikeusjakso">
                  <a className="close-modal" onClick={cancel}>&#10005;</a>
                  <h2>Opiskeluoikeuden tilan lisäys</h2>
                  <div className="property alku">
                    <label>Päivämäärä:</label>
                    <DateEditor model={newStateModels.alkuPäiväModel}/>
                  </div>
                  <div className="property tila">
                    <label>Tila:</label>
                    <EnumEditor asRadiogroup="true" model={newStateModels.tilaModel}/>
                  </div>
                  <button disabled={!this.state.valid} className="opiskeluoikeuden-tila button" onClick={add}>Lisää</button>
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
    return {
      saveChangesBus: Bacon.Bus(),
      cancelBus: Bacon.Bus(),
      alkuPäiväBus: Bacon.Bus(),
      tilaBus: Bacon.Bus(),
      errorBus: Bacon.Bus()
    }
  },
  componentDidMount() {
    let {model, opiskeluoikeusModel} = this.props
    let {alkuPäiväBus, tilaBus, cancelBus, saveChangesBus, errorBus} = this.state

    let muutoksetE = alkuPäiväBus.merge(tilaBus)

    let changesP = Bacon.update([],
      muutoksetE, (xs, x) => xs.concat(x),
      cancelBus, () => []
    )

    Bacon.update({},
      alkuPäiväBus, (state, alkuPäivä) => R.merge(state, {alkuPäivä}),
      tilaBus, (state, tila) => R.merge(state, {tila}),
      Bacon.mergeAll(cancelBus, saveChangesBus), () => ({}),
      errorBus, (state, [,e]) => R.merge(state, {error: e.error})
    ).onValue(state => {
      this.setState({valid: !state.error && state.alkuPäivä && state.tila})
    })

    saveChangesBus.merge(cancelBus).onValue(() => {
      this.setState({newStateModels: undefined})
      document.removeEventListener('keyup', this.handleKeys)
    })

    errorBus.onValue(e => model.context.errorBus.push(e))

    changesP.sampledBy(saveChangesBus).onValue((changes) => {
      let opiskeluoikeudenPaattymispaiva = this.opiskeluoikeudenPaattymispaiva(changes)

      if (opiskeluoikeudenPaattymispaiva) {
        let paattymispaivaModel = modelLookup(opiskeluoikeusModel, 'päättymispäivä')
        model.context.changeBus.push([paattymispaivaModel.context, {data: opiskeluoikeudenPaattymispaiva}])
      }
      model.context.changeBus.push(changes)
      model.context.doneEditingBus.push()
      cancelBus.push()
      this.setState({items: undefined})
    })
  },
  componentWillUnmount() {
    document.removeEventListener('keyup', this.handleKeys)
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
    return tila === 'eronnut' || tila === 'valmistunut' || tila === 'katsotaaneronneeksi'
  },
  handleKeys(e) {
    if (e.keyCode == 27) { // esc
      this.state.cancelBus.push()
    } else if (e.keyCode == 13) { // enter
      this.state.valid && this.state.saveChangesBus.push()
    }
  }
})
