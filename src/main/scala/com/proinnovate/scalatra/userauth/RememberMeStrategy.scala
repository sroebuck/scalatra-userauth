package com.proinnovate.scalatra.userauth

import com.weiglewilczek.slf4s.Logging
import org.scalatra.{CookieSupport, ScalatraKernel, CookieOptions, Cookie}
import java.security.SecureRandom

/**
 * Authentication strategy to authenticate a user from a cookie.
 *
 * The basic idea is that a unique random token is generated for the user.  This token is stored as a cookie on the
 * client and recorded by the server.  If a user returns to the site, the token in the cookie is used to recall the
 * user details from the server based lookup and the user is logged in automatically.
 *
 * If this system is used on a web site that is not running using SSL then the cookie content will be sent unencrypted
 * and a third party could capture the cookie and use it to log in themselves.
 *
 * Note that this issue is not unique to cookies and remember me.  The normal way of tracking sessions with session IDs
 * means that session IDs can also be used to copy and use someone else's identity unless they are connecting with SSL.
 */
class RememberMeStrategy[U] extends UserAuthStrategy[U] with Logging {

  /**
   * Given the current ScalatraKernel, determine whether the authentication strategy is valid for use at the current
   * time.
   *
   * return true if this authentication scheme can be used at this time.
   */
  final def authIsValid(app: ScalatraKernel): Boolean = {
    app match {
      case a: CookieSupport =>
        a.cookies.get(COOKIE_KEY).isDefined
      case _ =>
        logger.error("The ScalatraKernel must mixin the CookieSupport trait in order to use RememberMe authentication!")
        false
    }
  }

  /**
   * return Some(User) or None if no user was authenticated.
   */
  final def authenticateUser(app: ScalatraKernel)(implicit authenticate: (String, String) => Option[U]): Option[U] = {
    val token: String = app match {
      case a: CookieSupport =>
        a.cookies.get(COOKIE_KEY) match {
          case Some(v) => v.toString
          case None => ""
        }
      case _ => ""
    }

    logger.info("rememberMe: trying for token[" + token + "]")

    app match {
      case r: RememberMeSupport[U] =>
        r.getUserForRememberMeToken(token)
      case _ =>
        logger.error("The ScalatraKernel must mixin the RememberMeSupport trait in order to provide RememberMe " +
          "authentication!")
        None
    }
  }


  /**
   * After authentication, sets the remember-me cookie on the response.
   */
  def afterAuthProcessing(app: ScalatraKernel) {
    if (checkbox2boolean(app.params.get("rememberMe").getOrElse("").toString)) {

      val token = generateToken("x")
      app.response.addHeader("Set-Cookie",
        Cookie(COOKIE_KEY, token)(CookieOptions(secure = false, maxAge = oneWeek, httpOnly = true)).toCookieString)
      app match {
        case r: RememberMeSupport[U] with UserAuthSupport[U] =>
          r.storeRememberMeTokenForUser(token, r.userOption.get)
        case _ =>
          logger.error("The ScalatraKernel must mixin the RememberMeSupport trait in order to provide RememberMe " +
            "authentication!")
          None
      }
    }
  }


  /**
   * Clears the remember-me cookie for the specified user.
   */
   def beforeLogout(user: U) {
//    if (user != null) {
//      user.forgetMe()
//    }
//    app.cookies.get(COOKIE_KEY) foreach {
//      _ => app.cookies.update(COOKIE_KEY, null)
//    }
  }


  // PRIVATE


  private final val COOKIE_KEY = "rememberMe"
  private final val oneWeek = 7 * 24 * 3600


  /**
  * Used to easily match a checkbox value
  */
  private def checkbox2boolean(s: String): Boolean = {
    s match {
      case "yes" => true
      case "y" => true
      case "1" => true
      case "true" => true
      case _ => false
    }
  }


  private def generateToken(s: String): String = {
    // FIXME: This token generation seems to give no guarantees of token uniqueness.
    val random = SecureRandom.getInstance("SHA1PRNG")
    val str = new Array[Byte](16)
    random.nextBytes(str)
    str.toString
  }


}


trait RememberMeSupport[U] {

  def storeRememberMeTokenForUser(token: String, user: U)

  def getUserForRememberMeToken(token: String): Option[U]

}