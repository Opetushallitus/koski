export default () => window.delays || {
  stringInput: 1000,
  delay: function(d) { return d }
}