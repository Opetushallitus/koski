import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'
import {childContext, contextualizeModel, modelItems, modelLookup, addContext, modelSet} from './EditorModel.js'
import {EnumEditor} from './EnumEditor.jsx'
import {DateEditor} from './DateEditor.jsx'

export const OpiskeluoikeudenUusiTilaPopup = React.createClass({
  render() {
    let {suorituksiaKesken, edellisenTilanAlkupäivä} = this.props

    let {addNewBus, cancelBus, alkuPäiväModel, tilaModel} = this.state

    return (
      <div className="lisaa-opiskeluoikeusjakso-modal">
        <div className="lisaa-opiskeluoikeusjakso">
          <a className="close-modal" onClick={() => cancelBus.push()}>&#10005;</a>
          <h2>Opiskeluoikeuden tilan lisäys</h2>
          <div className="property alku">
            <label>Päivämäärä:</label>
            <DateEditor model={alkuPäiväModel} isAllowedDate={d => edellisenTilanAlkupäivä ? d >= edellisenTilanAlkupäivä : true }/>
          </div>
          <div className="property tila">
            <label>Tila:</label>
            <EnumEditor asRadiogroup={true} model={tilaModel} disabledValue={suorituksiaKesken && 'valmistunut'} />
          </div>
          <button disabled={!this.state.valid} className="opiskeluoikeuden-tila button" onClick={() => addNewBus.push()}>Lisää</button>
          <a onClick={() => cancelBus.push()}>Peruuta</a>
        </div>
      </div>
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
  componentWillMount() {
    let { tilaListModel, resultCallback } = this.props
    let {alkuPäiväBus, tilaBus, cancelBus, addNewBus, errorBus} = this.state

    document.addEventListener('keyup', this.handleKeys)
    let items = modelItems(tilaListModel).slice(0).reverse()
    let opiskeluoikeusjaksoModel = contextualizeModel(tilaListModel.arrayPrototype, childContext(tilaListModel.context, items.length))

    let alkuPäiväModel = addContext(modelLookup(opiskeluoikeusjaksoModel, 'alku'), {changeBus: alkuPäiväBus, errorBus: errorBus})
    let tilaModel = addContext(modelLookup(opiskeluoikeusjaksoModel, 'tila'), {changeBus: tilaBus, errorBus: errorBus})

    let stateP = Bacon.update({},
      alkuPäiväBus, (state, alkuPäivä) => R.merge(state, {alkuPäivä: alkuPäivä[1]}),
      tilaBus, (state, tila) => R.merge(state, {tila: tila[1]}),
      cancelBus, () => ({}),
      errorBus, (state, [,e]) => R.merge(state, {error: e.error})
    )

    stateP.onValue(state => {
      this.setState({valid: !state.error && state.alkuPäivä && state.tila})
    })

    alkuPäiväBus.push([alkuPäiväModel.context, alkuPäiväModel])
    this.setState({alkuPäiväModel, tilaModel})

    errorBus.onValue(e => tilaListModel.context.errorBus.push(e))

    stateP.sampledBy(addNewBus).onValue((state) => {
      let withAlku = modelSet(opiskeluoikeusjaksoModel, state.alkuPäivä, 'alku')
      let withTila = modelSet(withAlku, state.tila, 'tila')
      resultCallback(withTila)
    })

    cancelBus.onValue(() => resultCallback(undefined)) // exit without value
  },
  componentWillUnmount() {
    document.removeEventListener('keyup', this.handleKeys)
  },

  handleKeys(e) {
    if (e.keyCode == 27) { // esc
      this.state.cancelBus.push()
    } else if (e.keyCode == 13) { // enter
      this.state.valid && this.state.addNewBus.push()
    }
  }
})
