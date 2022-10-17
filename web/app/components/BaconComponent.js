import React from 'react'
import Bacon from 'baconjs'

export default class BaconComponent extends React.Component {
  UNSAFE_componentWillMount() {
    this.unmountE = Bacon.Bus()
    this.propsE = Bacon.Bus()
    this.propsE.push(this.props)
  }

  componentWillUnmount() {
    if (this.unmountE) this.unmountE.push()
  }

  UNSAFE_componentWillReceiveProps(newProps) {
    this.propsE.push(newProps)
  }
}
