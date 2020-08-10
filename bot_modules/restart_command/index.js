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
      process.exit(0) // Bot is set up to auto-restart on exit
    } else if (msg.content.startsWith('!pull-restart')) {
      console.log(process.cwd())
      const child = childProcess.spawn('git', ['pull'])

      const outputHandler = function (data) {
        msg.channel.send(
          '`' + (data.toString()
            .split('@').join('@ ') // Makes pings impossble
            .split('`').join('\\`') // Escape backticks
          ) + '`'
        )
      }

      child.stdout.on('data', outputHandler)
      child.stderr.on('data', outputHandler)

      child.on('error', outputHandler)

      child.on('exit', function () {
        msg.channel.send('Restarting...')
        process.exit(0)
      })
    }
  })
}
