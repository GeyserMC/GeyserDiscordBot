const childProcess = require('child_process')

const configEditingUsers = JSON.parse(process.env.CONFIG_EDITING_USERS)
const configEditingGroups = JSON.parse(process.env.CONFIG_EDITING_GROUPS)

exports.commands = [
  {
    name: 'restart',
    description: '',
    run: async (msg, args) => {
      await msg.channel.send('Restarting...')

      // Exit after we attempted to send the message
      process.exit(0)
    },
    canRun: (msg) => {
      return (configEditingUsers.includes(msg.author.id) || (msg.member !== null && msg.member.roles.cache.some(r => configEditingGroups.includes(r.id))))
    }
  },
  {
    name: 'pull-restart',
    description: '',
    run: async (msg, args) => {
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

        npmChild.on('exit', async () => {
          logText += '\nRestarting...'
          await logMessage.edit('```\n' + logText + '\n```')

          // Exit after we attempted to update the message
          process.exit(0)
        })
      })
    },
    canRun: (msg) => {
      return (configEditingUsers.includes(msg.author.id) || (msg.member !== null && msg.member.roles.cache.some(r => configEditingGroups.includes(r.id))))
    }
  }
]
