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
    this.listeners.forEach((l) => l(data))
  }
}

export const isHealthDataEntry = (a: unknown): a is HealthDataEntry =>
  typeof a === 'object' &&
  a !== null &&
  'subsystem' in a &&
  typeof a.subsystem === 'string'
