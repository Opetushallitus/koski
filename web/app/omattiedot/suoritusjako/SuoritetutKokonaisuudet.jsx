import React, { useState, useEffect, useMemo } from 'react'
import Checkbox from '../../components/Checkbox'

const SuoritetutKokonaisuudet = ({ handleSelectedItems, allDisabled }) => {
  const [checkedItems, setCheckedItems] = useState({})

  const checkboxListData = useMemo(
    () => [{ tyyppi: 'suoritetut-tutkinnot', label: 'Suoritetut tutkinnot' }],
    []
  )

  useEffect(() => {
    const selectedItems = checkboxListData.filter(
      ({ tyyppi }) => checkedItems[tyyppi]
    )
    const unselectedItems = checkboxListData.filter(
      ({ tyyppi }) => !checkedItems[tyyppi]
    )
    handleSelectedItems(selectedItems, unselectedItems)
  }, [checkedItems, checkboxListData, handleSelectedItems])

  const handleChange = (event) => {
    const { id, checked } = event.target
    setCheckedItems((prevCheckedItems) => ({
      ...prevCheckedItems,
      [id]: checked
    }))
  }

  return (
    <li>
      {checkboxListData.map((item) => (
        <div key={item.tyyppi}>
          <label>
            <Checkbox
              id={item.tyyppi}
              checked={checkedItems[item.tyyppi] || false}
              onChange={handleChange}
              label={item.label}
              listStylePosition={'inside'}
              disabled={allDisabled}
            />
          </label>
        </div>
      ))}
    </li>
  )
}

export default SuoritetutKokonaisuudet
