import { useEffect } from 'react'

export const useConfirmUnload = (enabled: boolean) => {
  useEffect(() => {
    if (enabled) {
      const prevent = (event: BeforeUnloadEvent) => {
        event.preventDefault()
      }
      window.addEventListener('beforeunload', prevent)
      return () => window.removeEventListener('beforeunload', prevent)
    }
  }, [enabled])
}
