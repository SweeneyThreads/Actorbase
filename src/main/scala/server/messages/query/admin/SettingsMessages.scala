package server.messages.query.admin

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
}
