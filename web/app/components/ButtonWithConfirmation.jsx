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
      confirmationClassName,
      ...rest // Loput propsit sisältävät a-elementin aria-labelin jne..
    } = this.props

    const isActionRequested = this.state.isActionRequested

    return isActionRequested ? (
      <div className={className}>
        <button
          className={`koski-button ${confirmationClassName || ''}`}
          onClick={action}
          aria-label={confirmationText}
          {...rest}
        >
          <Text name={confirmationText} />
        </button>

        <a
          onClick={() => this.setState({ isActionRequested: false })}
          aria-label={cancelText}
          {...rest}
        >
          <Text name={cancelText} />
        </a>
      </div>
    ) : (
      <a
        className={className}
        onClick={() => this.setState({ isActionRequested: true })}
        aria-label={text}
        {...rest}
      >
        <Text name={text} />
      </a>
    )
  }
}

export default ButtonWithConfirmation
