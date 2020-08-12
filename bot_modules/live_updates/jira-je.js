const axios = require('axios')

const jiraVersionsCache = []

exports.populateInitialJiraVersions = async function () {
  const versions = await getJiraVersions()

  versions.forEach(version => {
    jiraVersionsCache.push(version.name)
  })

  console.log(`Loaded ${jiraVersionsCache.length} initial java jira versions`)
}

exports.jiraUpdateCheck = async function (callback) {
  const versions = await getJiraVersions()

  versions.forEach(version => {
    if (!jiraVersionsCache.includes(version.name)) {
      jiraVersionsCache.push(version.name)
      if (!version.name.includes('Future Version')) {
        callback(jiraAsString(version))
      }
    }
  })
}

async function getJiraVersions () {
  // Fetch the raw response data
  let response
  try {
    response = await axios.get('https://bugs.mojang.com/rest/api/latest/project/MC/versions')
  } catch (error) {
    console.error(error)
    return
  }

  return response.data
}

function jiraAsString (version) {
  return `A new java version (${version.name}) has been added to the minecraft issue tracker!`
}
