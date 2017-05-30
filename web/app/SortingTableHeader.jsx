import React from 'react'
import { navigateWithQueryParams, currentLocation } from './location'
import Text from './Text.jsx'

export default React.createClass({
  render() {
    var { field, titleKey, defaultSort } = this.props
    let params = currentLocation().params
    let [ sortBy, sortOrder ] = params.sort
      ? params.sort.split(':')
      : defaultSort
        ? [field, defaultSort]
        : []

    let selected = sortBy == field

    return (<th className={sortBy == field ? field + ' sorted' : field}>
      <div className="sorting" onClick={() => navigateWithQueryParams({ sort: field + ':' + (selected ? (sortOrder == 'asc' ? 'desc' : 'asc') : 'asc')})}>
        <div className="title"><Text name={titleKey}/></div>
        <div className="sort-indicator">
          <div className={selected && sortOrder == 'asc' ? 'asc selected' : 'asc'}></div>
          <div className={selected && sortOrder == 'desc' ? 'desc selected' : 'desc'}></div>
        </div>
      </div>
      {this.props.children}
    </th>)
  }
})
