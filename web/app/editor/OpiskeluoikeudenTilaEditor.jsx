import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'
import {childContext, contextualizeModel, modelData, modelItems} from './EditorModel.js'
import {ArrayEditor} from './ArrayEditor.jsx'
import {OpiskeluoikeusjaksoEditor} from './OpiskeluoikeusjaksoEditor.jsx'
import {Editor} from './GenericEditor.jsx'

export const OpiskeluoikeudenTilaEditor = React.createClass({
  render() {
    let {model} = this.props
    let changeBus = this.state.changeBus
    let adding = this.state && this.state.adding || null
    let items = (this.state && this.state.items) || modelItems(model)

    let showAddDialog = () => {
      let tilaModel = contextualizeModel(model.prototype, R.merge(childContext(model.context, items.length), {changeBus}))
      changeBus.push([childContext(tilaModel.context, 'alku'), {data: modelData(tilaModel, 'alku')}])
      // TODO: push tila once we get the default value from server
      this.setState({adding: tilaModel})
    }
    let add = () => {
      //TODO: clean state
      model.context.changeBus.push(this.state.changes)
    }

    return (
      model.context.edit ?
        <div>
          <ul ref="ul" className="array">
          {
            items.map((item, i) => {
              return <li key={i}><OpiskeluoikeusjaksoEditor model={item}/></li>
            })
          }
          {
            <li className="add-item"><a onClick={showAddDialog}>Lisää opiskeluoikeuden tila</a></li>
          }
          </ul>
          {
            adding && (<div className="lisaa-opiskeluoikeusjakso">
              <Editor model={adding} />
              <a className="button" onClick={add}>Lisää</a>
            </div>)
          }
        </div> :
        <div><ArrayEditor reverse={true} model={model}/></div>
    )
  },
  getInitialState() {
    let changeBus = Bacon.Bus()
    changeBus.onValue(c => {
      this.setState({changes: this.state.changes.concat(c)})
    })
    return {changeBus: changeBus, changes: []}
  }
})
