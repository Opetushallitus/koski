import React from 'react'
import Bacon from 'baconjs'
import BaconComponent from './BaconComponent'
import { addClass } from './classnames'
import delays from '../util/delays'

export default class PaginationLink extends BaconComponent {
  render() {
    const { pager } = this.props
    return pager.mayHaveMore() ? (
      <div
        id="pagination-marker"
        onClick={pager.next}
        ref={(link) => {
          this.paginationMarker = link
        }}
      ></div>
    ) : (
      <span></span>
    )
  }

  componentDidMount() {
    Bacon.fromEvent(window, 'scroll')
      .throttle(delays().delay(100))
      .filter(
        () =>
          this.paginationMarker && isElementInViewport(this.paginationMarker)
      )
      .takeUntil(this.unmountE)
      .onValue(() => {
        this.props.pager.next()
        addClass(this.paginationMarker, 'loading')
      })
  }
}

function isElementInViewport(el) {
  const rect = el.getBoundingClientRect()
  return (
    rect.top >= 0 &&
    Math.floor(rect.bottom) <=
      (window.innerHeight || document.documentElement.clientHeight)
  )
}
