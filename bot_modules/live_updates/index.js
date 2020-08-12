const jiraJE = require('./jira-je')
const jiraBE = require('./jira-be')
const minecraft = require('./minecraft')

exports.init = async (client) => {
  await jiraJE.populateInitialJiraVersions()
  await jiraBE.populateInitialJiraVersions()

  await minecraft.populateInitialMinecraftVersions()

  setInterval(() => doUpdateCheck(client), 1000 * 30)
}

function doUpdateCheck (client) {
  const callback = (message) => {
    console.log(message)
    JSON.parse(process.env.DISCORD_UPDATE_CHANNELS).forEach(channel => {
      client.channels.fetch(channel)
        .then(channel => channel.send(message))
        .catch(console.error)
    })
  }

  jiraJE.jiraUpdateCheck(callback)
  jiraBE.jiraUpdateCheck(callback)

  minecraft.minecraftUpdateCheck(callback)
}
