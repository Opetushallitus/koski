import React from 'react'
import Text from '../i18n/Text'

class ButtonWithConfirmation extends React.Component {
  constructor(props) {
    super(props)
    this.state = { isActionRequested: false }
  }

  render() {
    const {
      text,
      confirmationText,
      cancelText,
      action,
      className,
      confirmationClassName
    } = this.props

    const isActionRequested = this.state.isActionRequested

    return isActionRequested ? (
      <div className={className}>
        <button
          className={`koski-button ${confirmationClassName || ''}`}
          onClick={action}
        >
          <Text name={confirmationText} />
        </button>

        <a onClick={() => this.setState({ isActionRequested: false })}>
          <Text name={cancelText} />
        </a>
      </div>
    ) : (
      <a
        className={className}
        onClick={() => this.setState({ isActionRequested: true })}
      >
        <Text name={text} />
      </a>
    )
  }
}

export default ButtonWithConfirmation
