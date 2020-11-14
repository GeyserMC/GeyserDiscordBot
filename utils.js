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
    return { status: 'status' in error.response ? error.response.status : error.code, data: 'data' in error.response ? error.response.data : '' }
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
    return { status: 'status' in error.response ? error.response.status : error.code, data: 'data' in error.response ? error.response.data : '' }
  }

  return { status: response.status, data: response.data }
}
