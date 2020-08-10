const childProcess = require('child_process')

const configEditingUsers = JSON.parse(process.env.CONFIG_EDITING_USERS)
const configEditingGroups = JSON.parse(process.env.CONFIG_EDITING_GROUPS)

exports.init = function init (client) {
  client.on('message', async msg => {
    if (!configEditingUsers.includes(msg.author.id) && !(msg.member !== null && msg.member.roles.cache.some(r => configEditingGroups.includes(r.id)))) {
      return
    }

    if (msg.content.startsWith('!restart')) {
      msg.channel.send('Restarting...')
      // Delay the restart to prevent exit before we have sent the message
      setTimeout(() => {
        process.exit(0) // Bot is set up to auto-restart on exit
      }, 500)
    } else if (msg.content.startsWith('!pull-restart')) {
      const child = childProcess.spawn('git', ['pull'])
      let logText = 'Updating...'
      let logMessage = await msg.channel.send('```\n' + logText + '\n```')
      logText += '\n'

      const outputHandler = async function (data) {
        logText += '\n' + data.toString()
          .split('@').join('@ ') // Makes pings impossble
          .split('`').join('\\`') // Escape backticks
        logMessage = await logMessage.edit('```\n' + logText + '\n```')
      }

      child.stdout.on('data', outputHandler)
      child.stderr.on('data', outputHandler)

      child.on('error', outputHandler)

      child.on('exit', function () {
        logText += '\nRestarting...'
        logMessage.edit('```\n' + logText + '\n```')

        // Delay the restart to prevent exit before we have sent the message
        setTimeout(() => {
          process.exit(0)
        }, 500)
      })
    }
  })
}
