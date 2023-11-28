export interface HealthDataEntry {
  timestamp: string
  instance: string
  subsystem: string
  operational: boolean
  external: boolean
  message?: string
}

export type HealthData = HealthDataEntry[]

export type HealthSourceListener = (data: HealthData) => void

export class HealthSource {
  listeners: HealthSourceListener[] = []

  addListener(listener: HealthSourceListener) {
    this.listeners.push(listener)
  }

  emit(data: HealthData) {
    this.listeners.forEach(l => l(data))
  }
}

export const isHealthDataEntry = (a: any): a is HealthDataEntry =>
  typeof a === 'object' && typeof a.subsystem === 'string'
