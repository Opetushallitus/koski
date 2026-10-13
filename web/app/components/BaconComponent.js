import React from 'react'
import Bacon from 'baconjs'

export default class BaconComponent extends React.Component {
  // eslint-disable-next-line @eslint-react/no-unsafe-component-will-mount
  UNSAFE_componentWillMount() {
    this.unmountE = Bacon.Bus()
    this.propsE = Bacon.Bus()
    this.propsE.push(this.props)
  }

  componentWillUnmount() {
    if (this.unmountE) this.unmountE.push()
  }

  // eslint-disable-next-line @eslint-react/no-unsafe-component-will-receive-props
  UNSAFE_componentWillReceiveProps(newProps) {
    this.propsE.push(newProps)
  }
}
