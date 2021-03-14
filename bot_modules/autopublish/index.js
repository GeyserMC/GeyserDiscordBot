const { configEditor: config } = require('../config_manager/index.js')

exports.init = function (client) {
  client.on('message', msg => {
    if (config.get().autopublishChannels.includes(msg.channel.id) && msg.crosspostable) {
      msg.crosspost()
    }
  })
}
