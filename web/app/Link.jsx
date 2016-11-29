import React from 'react'
import {navigateTo} from './location.js'

export default React.createClass({
  render() {
    let {href, className} = this.props
    return (<a href={href} className={className} onClick={(e) => navigateTo(href, e)}>{this.props.children}</a>)
  }
})