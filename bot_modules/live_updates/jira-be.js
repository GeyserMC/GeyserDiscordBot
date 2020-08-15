const axios = require('axios')

const jiraVersionsCache = []

/**
 * Get all the current versions and cache them for comparing later
 */
exports.populateInitialJiraVersions = async () => {
  const versions = await getJiraVersions()

  // Add each version name to the cache
  versions.forEach(version => {
    jiraVersionsCache.push(version.name)
  })

  console.log(`Loaded ${jiraVersionsCache.length} initial Bedrock jira versions`)
}

/**
 * Fetch the latest versions and compare them with the cached list
 *
 * @param {Function} callback The function to call when a new version is found, takes a message as a string
 */
exports.jiraUpdateCheck = async (callback) => {
  const versions = await getJiraVersions()

  versions.forEach(version => {
    if (!jiraVersionsCache.includes(version.name)) {
      jiraVersionsCache.push(version.name)

      // Make sure its not a future version
      if (!version.name.includes('Future Version')) {
        callback(jiraAsString(version))
      }
    }
  })
}

/**
 * Get the version data from the bug tracker
 *
 * @returns {Array} Each version as an object
 */
async function getJiraVersions () {
  // Fetch the raw response data
  let response
  try {
    response = await axios.get('https://bugs.mojang.com/rest/api/latest/project/MCPE/versions')
  } catch (error) {
    console.error(error)
    return
  }

  return response.data
}

/**
 * Take a version object and fomat it into a nice message for discord
 *
 * @param {Object} version The version data to insert into the message
 */
function jiraAsString (version) {
  return `A new Bedrock version (${version.name}) has been added to the Minecraft issue tracker!`
}
