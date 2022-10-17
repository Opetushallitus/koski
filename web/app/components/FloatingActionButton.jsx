import React from 'react'
import Text from '../i18n/Text'

class FloatingActionButton extends React.Component {
  constructor(props) {
    super(props)
    this.state = { isVisible: false }
    this.updateOffset = this.updateOffset.bind(this)
  }

  componentDidMount() {
    window.addEventListener('scroll', this.updateOffset)
  }

  componentWillUnmount() {
    window.removeEventListener('scroll', this.updateOffset)
  }

  updateOffset() {
    this.setState({ isVisible: window.scrollY > this.props.visibilityOffset })
  }

  render() {
    const { text, onClick } = this.props
    const visibility = this.state.isVisible ? 'visible' : 'hidden'

    return (
      <button
        className={`floating-action-button--${visibility}`}
        onClick={onClick}
        aria-hidden={true}
        tabIndex={-1}
      >
        <Text name={text} />
      </button>
    )
  }
}

export default FloatingActionButton
