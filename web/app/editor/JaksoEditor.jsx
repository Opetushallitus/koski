import React from 'react'
import {PäivämääräväliEditor} from './PaivamaaravaliEditor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'

export const JaksoEditor = React.createClass({
  render() {
    let {model} = this.props
    let edit = model.context.edit
    return (
      <div className="jaksollinen">
        { !edit && <PäivämääräväliEditor model={model}/> }
        <PropertiesEditor model={model} propertyFilter={p => edit || !['alku', 'loppu'].includes(p.key)} />
      </div>
    )
  }
})
