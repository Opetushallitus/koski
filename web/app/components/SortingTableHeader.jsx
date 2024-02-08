import React from 'react'
import { navigateWithQueryParams, currentLocation } from '../util/location'
import Text from '../i18n/Text'

export default class extends React.Component {
  render() {
    const { field, titleKey, defaultSort } = this.props
    const params = currentLocation().params
    const [sortBy, sortOrder] = params.sort
      ? params.sort.split(':')
      : defaultSort
        ? [field, defaultSort]
        : []

    const selected = sortBy === field

    return (
      <th className={sortBy === field ? field + ' sorted' : field}>
        <div
          className="sorting"
          onClick={() =>
            navigateWithQueryParams({
              sort:
                field +
                ':' +
                (selected ? (sortOrder === 'asc' ? 'desc' : 'asc') : 'asc')
            })
          }
        >
          <div className="title">
            <Text name={titleKey} />
          </div>
          <div className="sort-indicator">
            <div
              className={
                selected && sortOrder === 'asc' ? 'asc selected' : 'asc'
              }
            ></div>
            <div
              className={
                selected && sortOrder === 'desc' ? 'desc selected' : 'desc'
              }
            ></div>
          </div>
        </div>
        {this.props.children}
      </th>
    )
  }
}
