const Discord = require('discord.js')
const client = new Discord.Client()

// Load in the .env file
require('dotenv').config()

function initialiseModule (name) {
  return require('./bot_modules/' + name + '/index.js').init(client, config)
}

client.on('ready', () => {
  console.log(`Logged in as ${client.user.tag}!`)
})

// Load the config
const config = require('./bot_modules/config_manager/index.js').init(client)

// Load the modules
initialiseModule('dump_analyse')

client.login(process.env.BOT_TOKEN)
