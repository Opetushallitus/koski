const isInTestFrame = window.self !== window.top
const delay = isInTestFrame ? () => 0 : (d) => d

export default () => {
  return {
    stringInput: delay(1000),
    delay
  }
}
