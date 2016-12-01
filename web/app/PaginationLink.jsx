import React from 'react'
import Bacon from 'baconjs'
import { addClass } from './classnames'

export default React.createClass({
  render() {
    const { pager } = this.props
    return pager.mayHaveMore()
      ? <div id="pagination-marker" onClick={pager.next} ref={(link) => {this.paginationMarker = link}}></div>
      : <span></span>
  },

  componentDidMount() {
    this.unmountBus = Bacon.Bus()
    Bacon.fromEvent(window, 'scroll')
      .takeUntil(this.unmountBus)
      .throttle(100)
      .filter(() => isElementInViewport(this.paginationMarker))
      .onValue(() => {
        this.props.pager.next()
        addClass(this.paginationMarker, 'loading')
      })
  },

  componentWillUnmount() {
    if (this.unmountBus) this.unmountBus.push()
  }
})

function isElementInViewport (el) {
  let rect = el.getBoundingClientRect()
  return (
    rect.top >= 0 &&
    rect.left >= 0 &&
    rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && /*or $(window).height() */
    rect.right <= (window.innerWidth || document.documentElement.clientWidth) /*or $(window).width() */
  )
}