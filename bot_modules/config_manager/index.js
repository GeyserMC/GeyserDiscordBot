const path = require('path')
const fs = require('fs')

const logger = require('../log_manager/index.js')

const configEditingUsers = JSON.parse(process.env.CONFIG_EDITING_USERS)
const configEditingGroups = JSON.parse(process.env.CONFIG_EDITING_GROUPS)
const configFileLocation = './config.json'

// Create the config based on the example if it doesnt exist
if (!fs.existsSync(configFileLocation)) {
  fs.copyFileSync(configFileLocation + '.example', configFileLocation)
}

let config = require(path.resolve(configFileLocation))

// A basic config controller for saving and returning the config
exports.configEditor = {
  get: function () { return config },
  set: function (newConfig) {
    config = newConfig
    fs.writeFile(configFileLocation,
      JSON.stringify(newConfig, null, 2),
      function (err) {
        if (err) throw err
        logger.log('Saved config!')
      }
    )
  }
}

exports.init = function init (client) {
  client.on('message', async msg => {
    if (!configEditingUsers.includes(msg.author.id) && !(msg.member !== null && msg.member.roles.cache.some(r => configEditingGroups.includes(r.id)))) {
      return
    }

    if (msg.content.startsWith('!config')) {
      const args = msg.content.split(' ')

      // Pretty print the config and send it in a message
      if (args[1] === 'view') {
        msg.channel.send('```json\n' + JSON.stringify(config, null, 2) + '\n```')
      }

      // The client wants to edit the config
      if (args[1] === 'edit') {
        if (args[2] === 'all') {
          try {
            exports.configEditor.set(JSON.parse(args.slice(3).join(' ')))
            msg.channel.send('Done!')
          } catch (err) {
            msg.channel.send('Error while editing config:\n```\n' + err + '\n```')
          }
        } else {
          msg.channel.send('Only "all" is supported for config editing at the moment.')
        }
      }
    }
  })
}
