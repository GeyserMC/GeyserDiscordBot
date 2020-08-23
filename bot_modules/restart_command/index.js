const childProcess = require('child_process')

const configEditingUsers = JSON.parse(process.env.CONFIG_EDITING_USERS)
const configEditingGroups = JSON.parse(process.env.CONFIG_EDITING_GROUPS)

exports.init = (client) => {
  client.on('message', async (msg) => {
    if (!configEditingUsers.includes(msg.author.id) && !(msg.member !== null && msg.member.roles.cache.some(r => configEditingGroups.includes(r.id)))) {
      return
    }

    if (msg.content.startsWith('!restart')) {
      msg.channel.send('Restarting...').finally(() => {
        // Exit after we attempted to update the message
        process.exit(0)
      })
    } else if (msg.content.startsWith('!pull-restart')) {
      const gitChild = childProcess.spawn('git', ['pull'])
      let logText = 'Updating...'
      let logMessage = await msg.channel.send('```\n' + logText + '\n```')
      logText += '\n'

      const outputHandler = async (data) => {
        logText += '\n' + data.toString()
          .split('@').join('@ ') // Makes pings impossble
          .split('`').join('\\`') // Escape backticks
        logMessage = await logMessage.edit('```\n' + logText + '\n```')
      }

      gitChild.stdout.on('data', outputHandler)
      gitChild.stderr.on('data', outputHandler)

      gitChild.on('error', outputHandler)

      gitChild.on('exit', () => {
        const npmChild = childProcess.spawn(/^win/.test(process.platform) ? 'npm.cmd' : 'npm', ['i'])

        npmChild.stdout.on('data', outputHandler)
        npmChild.stderr.on('data', outputHandler)

        npmChild.on('error', outputHandler)

        npmChild.on('exit', () => {
          logText += '\nRestarting...'
          logMessage.edit('```\n' + logText + '\n```').finally(() => {
            // Exit after we attempted to update the message
            process.exit(0)
          })
        })
      })
    }
  })
}
