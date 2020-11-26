const axios = require('axios')

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
    return { status: error.response !== undefined ? error.response.status : error.code, data: error.response !== undefined ? error.response.data : '' }
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
    return { status: error.response !== undefined ? error.response.status : error.code, data: error.response !== undefined ? error.response.data : '' }
  }

  return { status: response.status, data: response.data }
}

/**
 * Used to sanitise the given string to make it safe to be part of a regex match
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions#Escaping
 *
 * @param {String} string The string to escape regex for
 */
exports.escapeRegExp = (string) => {
  return string.replace(/[.*+\-?^${}()|[\]\\]/g, '\\$&') // $& means the whole matched string
}

/**
 * Util function to clean the markdown from a message
 *
 * @param {String} string String to sanitise
 */
exports.cleanText = (string) => {
  return string.replace(/[`#_\-~|*]/g, '')
}
