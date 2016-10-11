import React from 'react'
import {navigateTo} from './location.js'

export default React.createClass({
  render() {
    let {href, className} = this.props
    return (<a href={href} className={className} onClick={(e) => {
      e.preventDefault()
      navigateTo(href)
    }}>{this.props.children}</a>)
  }
})