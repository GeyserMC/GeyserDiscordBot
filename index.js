require('dotenv').config()
const Discord = require('discord.js')
const client = new Discord.Client()

function initialiseModule (name) {
  return require('./bot_modules/' + name + '/index.js').init(client, config)
}

client.on('ready', () => {
  console.log(`Logged in as ${client.user.tag}!`)
})

const config = require('./bot_modules/config_manager/index.js').init(client)
initialiseModule('dump_analyse')

client.login(process.env.BOT_TOKEN)
