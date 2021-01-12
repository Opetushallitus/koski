import { useEffect, useState } from "react"
import { apiFetch, ApiResponse } from "./apiFetch"

export const useApi = <T>(fetchFn: () => Promise<ApiResponse<T>>) => {
  const [result, setResult] = useState<ApiResponse<T> | null>(null)
  useEffect(() => {
    fetchFn().then(setResult)
  }, [])
  return result
}

export const fetchHello = () => apiFetch<string>("hello")
