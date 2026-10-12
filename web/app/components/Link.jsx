import React from 'react'
import { navigateTo } from '../util/location'

// Link to a location _within_the_single_page_app_. Use just like the <a> tag, with the benefit that a full page
// load is prevented.

export default class extends React.Component {
  render() {
    const { href, className, children, ...otherProps } = this.props
    return (
      <a
        {...otherProps}
        href={href}
        className={className}
        onClick={(event) => navigateTo(href, event)}
      >
        {children}
      </a>
    )
  }
}
