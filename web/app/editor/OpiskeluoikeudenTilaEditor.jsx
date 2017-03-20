import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'
import {childContext, contextualizeModel, modelData, modelItems, modelLookup, addContext, modelSet} from './EditorModel.js'
import {resetOptionalModel} from './OptionalEditor.jsx'
import {ArrayEditor} from './ArrayEditor.jsx'
import {OpiskeluoikeusjaksoEditor} from './OpiskeluoikeusjaksoEditor.jsx'
import {EnumEditor} from './EnumEditor.jsx'
import {DateEditor} from './DateEditor.jsx'

export const OpiskeluoikeudenTilaEditor = React.createClass({
  render() {
    let {model, opiskeluoikeusModel} = this.props
    let {addNewBus, cancelBus, alkuPäiväBus, tilaBus, errorBus, newStateModels} = this.state
    let items = modelItems(model).slice(0).reverse()
    let edellisenTilanAlkupäivä = modelData(items[0], 'alku') && new Date(modelData(items[0], 'alku'))
    let suorituksiaKesken = model.context.edit && R.any(s => s.tila && s.tila.koodiarvo == 'KESKEN')(modelData(opiskeluoikeusModel, 'suoritukset') || [])

    let showAddDialog = () => {
      document.addEventListener('keyup', this.handleKeys)
      let opiskeluoikeusjaksoModel = contextualizeModel(model.arrayPrototype, childContext(model.context, items.length))
      let alkuPäiväModel = addContext(modelLookup(opiskeluoikeusjaksoModel, 'alku'), {changeBus: alkuPäiväBus, errorBus: errorBus})
      let tilaModel = addContext(modelLookup(opiskeluoikeusjaksoModel, 'tila'), {changeBus: tilaBus, errorBus: errorBus})
      alkuPäiväBus.push([alkuPäiväModel.context, alkuPäiväModel])
      this.setState({opiskeluoikeusjaksoModel, newStateModels: {alkuPäiväModel, tilaModel}})
    }

    let removeItem = () => {
      if (this.onLopputila(modelLookup(items[0], 'tila'))) {
        let paattymispaivaModel = modelLookup(opiskeluoikeusModel, 'päättymispäivä')
        resetOptionalModel(paattymispaivaModel)
      }
      model.context.changeBus.push([items[0].context, undefined])
      this.setState({newStateModels: undefined})
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
                  <a className="close-modal" onClick={() => cancelBus.push()}>&#10005;</a>
                  <h2>Opiskeluoikeuden tilan lisäys</h2>
                  <div className="property alku">
                    <label>Päivämäärä:</label>
                    <DateEditor model={newStateModels.alkuPäiväModel} isValid={d => edellisenTilanAlkupäivä ? d >= edellisenTilanAlkupäivä : true }/>
                  </div>
                  <div className="property tila">
                    <label>Tila:</label>
                    <EnumEditor asRadiogroup={true} model={newStateModels.tilaModel} disabledValue={suorituksiaKesken && 'valmistunut'} />
                  </div>
                  <button disabled={!this.state.valid} className="opiskeluoikeuden-tila button" onClick={() => addNewBus.push()}>Lisää</button>
                  <a onClick={() => cancelBus.push()}>Peruuta</a>
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
      addNewBus: Bacon.Bus(),
      cancelBus: Bacon.Bus(),
      alkuPäiväBus: Bacon.Bus(),
      tilaBus: Bacon.Bus(),
      errorBus: Bacon.Bus()
    }
  },
  componentDidMount() {
    let {model, opiskeluoikeusModel} = this.props
    let {alkuPäiväBus, tilaBus, cancelBus, addNewBus, errorBus} = this.state

    let stateP = Bacon.update({},
      alkuPäiväBus, (state, alkuPäivä) => R.merge(state, {alkuPäivä}),
      tilaBus, (state, tila) => R.merge(state, {tila}),
      cancelBus, () => ({}),
      errorBus, (state, [,e]) => R.merge(state, {error: e.error})
    )

    stateP.onValue(state => {
      this.setState({valid: !state.error && state.alkuPäivä && state.tila})
    })

    addNewBus.merge(cancelBus).onValue(() => {
      this.setState({newStateModels: undefined})
      document.removeEventListener('keyup', this.handleKeys)
    })

    errorBus.onValue(e => model.context.errorBus.push(e))

    stateP.sampledBy(addNewBus).onValue((state) => {
      if (this.onLopputila(state.tila[1])) {
        let paattymispaivaModel = modelLookup(opiskeluoikeusModel, 'päättymispäivä')
        model.context.changeBus.push([paattymispaivaModel.context, state.alkuPäivä[1]])
      }
      let withAlku = modelSet(this.state.opiskeluoikeusjaksoModel, state.alkuPäivä[1], 'alku')
      let withTila = modelSet(withAlku, state.tila[1], 'tila')
      model.context.changeBus.push([withTila.context, withTila])
      cancelBus.push()
    })
  },
  componentWillUnmount() {
    document.removeEventListener('keyup', this.handleKeys)
  },
  onLopputila(tilaModel) {
    let tila = modelData(tilaModel).koodiarvo
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
