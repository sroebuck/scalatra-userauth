package com.proinnovate.scalatra.userauth

import com.weiglewilczek.slf4s.Logging
import org.scalatra.ScalatraKernel

/**
 * Authentication strategy to authenticate a user from a username (or email) and password combination.
 */
class UserPasswordStrategy[U]() extends UserAuthStrategy[U] with Logging {

  override def authIsValid(app: ScalatraKernel) = {
    logger.info("app = " + app)
    val login = app.params.get("username")
    val password = app.params.get("password")
    logger.info("login: %s, password: %s".format(login, password))
    login.isDefined && password.isDefined
  }

  /**
   * Authenticates a user by validating the username (or email) and password request params.
   */
  def authenticateUser(app: ScalatraKernel)
                      (implicit authenticate: (String, String) => Option[U]): Option[U] = {
    val a: Option[Option[U]] = for {
      login <- app.params.get("username")
      password <- app.params.get("password")
    } yield authenticate(login, password)
    a.getOrElse(None)
  }

}
