const { Client } = require('discord.js')

/**
 * @type {Array}
 */
const { configEditor: config } = require('../config_manager/index.js')

/**
 * @param {Client} client 
 */
exports.init = function(client) {
  client.on('message', msg => {
    if (config.get().autopublishChannels.includes(msg.channel.id) && msg.crosspostable) {
      msg.crosspost()
    }
  })
}