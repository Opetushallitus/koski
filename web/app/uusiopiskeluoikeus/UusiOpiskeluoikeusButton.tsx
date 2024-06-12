import { useState } from 'react'
import React from 'react'
import { UusiOpiskeluoikeusDialog } from './UusiOpiskeluoikeusDialog'

export const UusiOpiskeluoikeusButton = () => {
  const [isVisible, setVisible] = useState(false)
  return isVisible ? (
    <UusiOpiskeluoikeusDialog
      onClose={() => setVisible(false)}
      onSubmit={(oo) => {
        console.log('Uusi opiskeluoikeus:', oo)
        setVisible(false)
      }}
    />
  ) : (
    <button onClick={() => setVisible(true)}>Uusi opiskeluoikeus</button>
  )
}
