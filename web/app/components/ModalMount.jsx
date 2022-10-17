import React from 'react'
import ReactDOM from 'react-dom'

const root = document.body
const contentRoot = document.getElementById('content')

export default class ModalMount extends React.Component {
  constructor(props) {
    super(props)
    this.container = document.createElement('div')
    this.container.classList.add('koski-modal')
  }

  componentDidMount() {
    root.appendChild(this.container)
    contentRoot.setAttribute('aria-hidden', true)
  }

  componentWillUnmount() {
    root.removeChild(this.container)
    contentRoot.removeAttribute('aria-hidden')
  }

  render() {
    return ReactDOM.createPortal(this.props.children, this.container)
  }
}
