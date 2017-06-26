export default () => {
  let delay = isInTestFrame() ? (() => 0) : (d => d)
  return {
    stringInput: delay(1000),
    delay
  }
}
const isInTestFrame = () => !!window.parent