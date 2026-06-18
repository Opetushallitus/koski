import React from 'react'
import { navigateWithExitHook } from '../util/exitHook'
import { parseBool } from '../util/util'

// Link to a location _within_the_single_page_app_. Use just like the <a> tag, with the benefit that a full page
// load is prevented.

export default class extends React.Component {
  render() {
    let {
      href,
      className,
      exitHook = true,
      children,
      ...otherProps
    } = this.props
    exitHook = parseBool(exitHook)
    return (
      <a
        {...otherProps}
        href={href}
        className={className}
        onClick={navigateWithExitHook(href, exitHook)}
      >
        {children}
      </a>
    )
  }
}
