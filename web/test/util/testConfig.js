export const initialTestTimeoutValue = 10000
let testTimeoutDefaultValue = initialTestTimeoutValue
export function overrideTestTimeoutDefault(val) {
  testTimeoutDefaultValue = val
}
export const testTimeoutPageLoad = 20000
export const testTimeoutDefault = () => testTimeoutDefaultValue
