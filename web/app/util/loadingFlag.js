window.reqCount = 0

export const increaseLoading = () => {
  window.reqCount++
  document.body.classList.add('loading')
}
export const decreaseLoading = () => {
  window.reqCount--
  if (window.reqCount < 0) {
    console.error('reqCount', window.reqCount)
  }
  if (window.reqCount === 0) {
    document.body.classList.remove('loading')
  }
}
