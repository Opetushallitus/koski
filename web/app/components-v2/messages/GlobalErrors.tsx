import { isNonEmpty } from 'fp-ts/lib/Array'
import React from 'react'
import { useGlobalErrors } from '../../appstate/globalErrors'
import { ContentContainer } from '../containers/ContentContainer'

export const GlobalErrors: React.FC = () => {
  const state = useGlobalErrors()

  return isNonEmpty(state.errors) ? (
    <div className="GlobalErrors">
      <ContentContainer>
        <div className="GlobalErrors__container">
          <ul className="GlobalErrors__list">
            {state.errors.map((error, index) => (
              <li className="GlobalErrors__message" key={index}>
                {error.message}
              </li>
            ))}
          </ul>
          <button className="GlobalErrors__clear" onClick={state.clearAll}>
            ×
          </button>
        </div>
      </ContentContainer>
    </div>
  ) : null
}
