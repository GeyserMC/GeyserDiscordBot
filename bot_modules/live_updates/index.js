const jiraJE = require('./jira-je')
const jiraBE = require('./jira-be')
const minecraft = require('./minecraft')

const { configEditor: config } = require('../config_manager/index.js')

exports.init = async (client) => {
  await jiraJE.populateInitialJiraVersions()
  await jiraBE.populateInitialJiraVersions()

  await minecraft.populateInitialMinecraftVersions()

  setInterval(() => doUpdateCheck(client), 1000 * 60)
}

function doUpdateCheck (client) {
  const callback = (message) => {
    console.log(message)
    config.get().discordUpdateChannels.forEach(channel => {
      client.channels.fetch(channel)
        .then(channel => channel.send(message))
        .catch(console.error)
    })
  }

  jiraJE.jiraUpdateCheck(callback)
  jiraBE.jiraUpdateCheck(callback)

  minecraft.minecraftUpdateCheck(callback)
}
