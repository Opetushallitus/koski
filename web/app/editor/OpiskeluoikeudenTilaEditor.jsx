import React from 'react'
import R from 'ramda'
import {modelData, modelItems, modelLookup} from './EditorModel.js'
import {resetOptionalModel} from './OptionalEditor.jsx'
import {ArrayEditor} from './ArrayEditor.jsx'
import {OpiskeluoikeusjaksoEditor} from './OpiskeluoikeusjaksoEditor.jsx'
import {OpiskeluoikeudenUusiTilaPopup} from './OpiskeluoikeudenUusiTilaPopup.jsx'

export const OpiskeluoikeudenTilaEditor = React.createClass({
  render() {
    let {model} = this.props
    let jaksotModel = modelLookup(model, 'tila.opiskeluoikeusjaksot')
    let {addingNew} = this.state
    let items = modelItems(jaksotModel).slice(0).reverse()
    let suorituksiaKesken = jaksotModel.context.edit && R.any(s => s.tila && s.tila.koodiarvo == 'KESKEN')(modelData(model, 'suoritukset') || [])

    let showAddDialog = () => {
      this.setState({addingNew: true})
    }

    let removeItem = () => {
      if (this.onLopputila(modelLookup(items[0], 'tila'))) {
        let paattymispaivaModel = modelLookup(model, 'päättymispäivä')
        resetOptionalModel(paattymispaivaModel)
      }
      jaksotModel.context.changeBus.push([items[0].context, undefined])
      this.setState({addingNew: false})
    }

    let lastOpiskeluoikeudenTila = modelData(modelLookup(items[0], 'tila'))
    let showLisaaTila = !lastOpiskeluoikeudenTila || lastOpiskeluoikeudenTila.koodiarvo === 'lasna' || lastOpiskeluoikeudenTila.koodiarvo === 'valiaikaisestikeskeytynyt'
    let edellisenTilanAlkupäivä = modelData(items[0], 'alku') && new Date(modelData(items[0], 'alku'))

    return (
      jaksotModel.context.edit ?
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
            addingNew && <OpiskeluoikeudenUusiTilaPopup tilaListModel={jaksotModel} suorituksiaKesken={suorituksiaKesken} edellisenTilanAlkupäivä={edellisenTilanAlkupäivä} resultCallback={(uusiJakso) => this.lisääJakso(uusiJakso)} />
          }
        </div> :
        <div><ArrayEditor reverse={true} model={jaksotModel}/></div>
    )
  },
  getInitialState() {
    return {
      addingNew: false
    }
  },
  lisääJakso(uusiJakso) {
    if (uusiJakso) {
      let {model} = this.props
      let tilaModel = modelLookup(uusiJakso, 'tila')
      if (this.onLopputila(tilaModel)) {
        let paattymispaivaModel = modelLookup(model, 'päättymispäivä')
        let uudenJaksonAlku = modelLookup(uusiJakso, 'alku')
        model.context.changeBus.push([paattymispaivaModel.context, uudenJaksonAlku])
      }
      model.context.changeBus.push([uusiJakso.context, uusiJakso])
    }
    this.setState({addingNew: false})
  },
  onLopputila(tilaModel) {
    let tila = modelData(tilaModel).koodiarvo
    return tila === 'eronnut' || tila === 'valmistunut' || tila === 'katsotaaneronneeksi'
  }
})