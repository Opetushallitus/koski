import React from 'react'
import {withExitHook, navigateWithExitHook} from './exitHook'
import {parseBool} from './util'

// Link to a location _within_the_single_page_app_. Use just link the <a> tag. The benefit is that a full page
// load is prevented.

export default React.createClass({
  render() {
    let {href, className, exitHook = true} = this.props
    exitHook = parseBool(exitHook)
    return (<a href={href} className={className} onClick={navigateWithExitHook(href, exitHook)}>{this.props.children}</a>)
  }
})