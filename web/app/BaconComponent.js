import React from 'react'
import R from 'ramda'
import Bacon from 'baconjs'

const BaconComponent = (props) => {
  let givenWillMount = props.componentWillMount || function () {}
  let givenWillUnmount = props.componentWillUnmount || function () {}
  return React.createClass(R.merge(props, {
    componentWillMount() {
      this.unmountE = Bacon.Bus()
      givenWillMount.call(this)
    },
    componentWillUnmount() {
      if (this.unmountE) this.unmountE.push()
      givenWillUnmount.call(this)
    }
  }))
}
export default BaconComponent