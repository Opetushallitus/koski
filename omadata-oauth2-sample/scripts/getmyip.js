import os from 'node:os'

const addresses = Object.values(os.networkInterfaces())
  .flatMap((iface) => iface || [])
  .filter(Boolean)

const address = addresses.find(
  (entry) => entry.family === 'IPv4' && entry.internal === false
)

if (!address) {
  console.error('No external IPv4 address found')
  process.exit(1)
}

console.log(address.address)
