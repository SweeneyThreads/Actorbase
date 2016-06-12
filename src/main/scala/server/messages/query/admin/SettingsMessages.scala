/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 SWEeneyThreads
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p/>
 *
 * @author SWEeneyThreads
 * @version 0.0.1
 * @since 0.0.1
 */

package server.messages.query.admin

<<<<<<< Updated upstream
import server.messages.query.ReplyInfo

/**
  * Created by eliamaino on 02/06/16.
  */
=======
>>>>>>> Stashed changes

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
