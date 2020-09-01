const axios = require('axios')
const mcping = require('mcping-js')
const mcpeping = require('mcpe-ping')
const ipRangeCheck = require('ip-range-check')

// From https://en.wikipedia.org/wiki/Reserved_IP_addresses
const INTERNAL_IP_RANGES = ['0.0.0.0/8', '10.0.0.0/8', '100.64.0.0/10', '127.0.0.0/8', '169.254.0.0/16', '172.16.0.0/12', '192.0.0.0/24', '192.0.2.0/24', '192.88.99.0/24', '192.168.0.0/16', '198.18.0.0/15', '198.51.100.0/24', '203.0.113.0/24', '224.0.0.0/4', '240.0.0.0/4', '255.255.255.255/32']

/**
 * Perform a GET request and return the status
 * code and response data
 *
 * @param {String} url The URL to request data from
 */
exports.getContents = async (url) => {
  let response
  try {
    response = await axios.get(url)
  } catch (error) {
    console.error(error)
    return {
      status: error.response
        ? error.response.status
        : undefined, // If request fails
      data: error.response
        ? error.response.data
        : undefined // If request fails
    }
  }

  return { status: response.status, data: response.data }
}

/**
 * Perform a POST request and return the status
 * code and response data with the passed contents
 *
 * @param {String} url The URL to post data to
 * @param {Object} contents The data to post
 */
exports.postContents = async (url, contents) => {
  let response
  try {
    response = await axios.post(url, contents)
  } catch (error) {
    console.error(error)
    return { status: error.response.status, data: error.response.data }
  }

  return { status: response.status, data: response.data }
}

/**
 * Ping a Minecraft Java Edition server and return the response
 *
 * @param {String} address The address of the Minecraft server to ping
 * @param {Number} port The port of the Mincraft server to ping
 * @param {Number} timeout The time to wait in milliseconds
 * @param {Number} protocolVersion The protocol version to use for the ping
 */
exports.pingJavaServer = function (address, port, timeout, protocolVersion) {
  return new Promise((resolve, reject) => {
    const server = new mcping.MinecraftServer(address, port)
    server.ping(timeout, protocolVersion, (err, res) => {
      if (err) reject(err)
      resolve(res)
    })
  })
}

/**
 * Ping a Minecraft Bedrock Edition server and return the response
 *
 * @param {String} address The address of the Minecraft server to ping
 * @param {Number} port The port of the Mincraft server to ping
 * @param {Number} timeout The time to wait in milliseconds
 */
exports.pingBedrockServer = function (address, port, timeout) {
  console.log(address, port)
  return new Promise((resolve, reject) => {
    mcpeping(address, Number(port), (err, res) => {
      if (err) reject(err)
      resolve(res)
    }, timeout)
  })
}

/**
 * Returns if the IP is an internal IP
 * 
 * @param {String} ip The IP address to check
 */
exports.isInternalIP = function (ip) {
  return ipRangeCheck(ip, INTERNAL_IP_RANGES)
}

/**
 * Splits the address into a JSON object containing the IP and port
 * 
 * @param {String} address The address to split
 * @param {Number} [defaultPort] The default port to use if the address does not include a port
 */
exports.splitAddress = function (address, defaultPort) {
  if (!address.includes(':')) {
    return { ip: address, port: defaultPort }
  }
  const split = address.split(':')
  const port = split[split.length - 1]
  const ip = split.splice(0, split.length - 1).join(':')
  return { ip: ip, port: port }
}
