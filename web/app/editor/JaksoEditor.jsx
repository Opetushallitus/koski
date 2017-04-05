import React from 'react'
import {PäivämääräväliEditor} from './PaivamaaravaliEditor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'

export const JaksoEditor = React.createClass({
  render() {
    let {model} = this.props
    return (
      <div className="jaksollinen">
        <PäivämääräväliEditor model={model}/>
        <PropertiesEditor model={model} propertyFilter={p => !['alku', 'loppu'].includes(p.key)} />
      </div>
    )
  }
})
JaksoEditor.validateModel = PäivämääräväliEditor.validateModel
