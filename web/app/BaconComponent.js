import React from 'react'
import Bacon from 'baconjs'

export default class BaconComponent extends React.Component {
  componentWillMount() {
    this.unmountE = Bacon.Bus()
    this.propsE = Bacon.Bus()
    this.propsE.push(this.props)
  }
  componentWillUnmount() {
    if (this.unmountE) this.unmountE.push()
  }

  componentWillReceiveProps(newProps) {
    this.propsE.push(newProps)
  }
}