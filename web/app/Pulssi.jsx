import './polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import Http from './http'
import Bacon from 'baconjs'

const Pulssi = React.createClass({
  render() {
    return (
    <h1>Hello world</h1>
    )
  }
})

ReactDOM.render(
  (<div>
    <Pulssi/>
  </div>),
  document.getElementById('content')
)

