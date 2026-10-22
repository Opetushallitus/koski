import { useState } from 'react'

// React 16:ssa ei ole useId:tä. Selainpuolen renderöinti vain, joten laskuri riittää.
let idCounter = 0

export const useUniqueId = (prefix: string): string => {
  const [id] = useState(() => `${prefix}-${(idCounter += 1)}`)
  return id
}
