import React from 'react'
import {EnumEditor} from '../editor/EnumEditor'
import {sortGrades} from '../util/sorting'
import {PropertiesEditor} from '../editor/PropertiesEditor'

export const OmaÄidinkieliEditor = ({model}) => {
  console.log('here')
  return (<PropertiesEditor
    model={model}
    getValueEditor={(p, getDefault) =>
      p.key === 'arvosana' ?  <EnumEditor model={p.model} sortBy={sortGrades}/> : getDefault()
    }
  />)
}
