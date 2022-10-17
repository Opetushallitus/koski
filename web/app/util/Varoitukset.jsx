import React from 'baret'
import Text from '../i18n/Text'

export const Varoitukset = ({ varoitukset }) => {
  return (
    varoitukset.length > 0 && (
      <div>
        {varoitukset.map((v) => (
          <div className="varoitus" key={v}>
            <Text name={`warning:${v}`} />
          </div>
        ))}
      </div>
    )
  )
}
