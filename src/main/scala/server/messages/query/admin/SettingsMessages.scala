package server.messages.query.admin

import server.messages.query.ReplyInfo

/**
  * Created by eliamaino on 02/06/16.
  */

/**
  * SettingMessages are used to manage settings properties.
  */
object SettingsMessages {

  /**
    * Trait that every message that belongs to settings operations has to extend.
    */
  trait SettingMessage extends AdminMessage

  /**
    * A RefreshSettingsMessage requests a reload of the settings from the configuration files.
    *
    * @see SettingMessage
    */
  case class RefreshSettingsMessage () extends SettingMessage

  /**
    * A RefreshSettingsInfo is a ReplyInfo used by ReplyBuilder to handle the response to a RefreshSettingsMessage
    */
  case class RefreshSettingsInfo() extends ReplyInfo
}
