const fs = require('fs')
const configEditingUsers = JSON.parse(process.env.CONFIG_EDITING_USERS)
let config = require('../../config.json')

const configEditor = {
  get: function () { return config },
  set: function (newConfig) {
    config = newConfig
    fs.writeFile('./config.json',
      JSON.stringify(newConfig, null, 2),
      function (err) {
        if (err) throw err
        console.log('Saved config!')
      }
    )
  }
}

exports.init = function init (client) {
  client.on('message', async msg => {
    if (!configEditingUsers.includes(msg.author.id)) {
      return
    }
    if (msg.content.startsWith('!config')) {
      if (msg.content.startsWith('!config view')) {
        msg.channel.send('```' + JSON.stringify(config, null, 2) + '```')
      }
      if (msg.content.startsWith('!config edit')) {
        const args = msg.content.split(' ')
        if (args[2] === 'all') {
          try {
            configEditor.set(JSON.parse(args.slice(3).join(' ')))
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
  return configEditor
}
