import React from 'react'
import R from 'ramda'
import Bacon from 'baconjs'

const BaconComponent = (props) => {
  let givenWillMount = props.componentWillMount || function () {}
  let givenWillUnmount = props.componentWillUnmount || function () {}
  let givenWillReceiveProps = props.componentWillReceiveProps || function () {}
  return React.createClass(R.mergeAll([{
      getInitialState() {
        return {}
      }
    },
    props,
    {
      componentWillMount() {
        this.unmountE = Bacon.Bus()
        this.propsE = Bacon.Bus()
        givenWillMount.call(this)
        this.propsE.push(this.props)
      },
      componentWillUnmount() {
        if (this.unmountE) this.unmountE.push()
        givenWillUnmount.call(this)
      },

      componentWillReceiveProps(newProps) {
        if (!R.equals(newProps, this.props)) {
          this.propsE.push(newProps)
        }
        givenWillReceiveProps.call(this, newProps)
      }
    }
  ]))
}
export default BaconComponent