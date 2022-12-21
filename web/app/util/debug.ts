import { useEffect, useState } from 'react'

export const useDebugChanges = (a: any) => {
  const json = JSON.stringify(a, null, 2)
  const [prev, setPrev] = useState(json)
  useEffect(() => {
    if (json !== prev) {
      console.log('Changed:', prev, '---->', json)
      setPrev(json)
    }
  }, [json, prev])
}
