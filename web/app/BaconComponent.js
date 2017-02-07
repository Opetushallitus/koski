import React from 'react'
import R from 'ramda'
import Bacon from 'baconjs'

const BaconComponent = (props) => React.createClass(R.merge({
  unmountE: Bacon.Bus(),
  componentWillUnmount() {
    if (this.unmountE) this.unmountE.push()
  }
}, props))
export default BaconComponent