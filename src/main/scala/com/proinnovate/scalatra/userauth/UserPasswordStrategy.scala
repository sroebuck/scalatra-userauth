package com.proinnovate.scalatra.userauth

import com.weiglewilczek.slf4s.Logging
import org.scalatra.ScalatraKernel

/**
 * Authentication strategy to authenticate a user from a username (or email) and password combination.
 */
class UserPasswordStrategy[U]() extends UserAuthStrategy[U] with Logging {

  logger.info("Scalatra-UserAuth Password Strategy Initialised")

  final def authIsValid(app: ScalatraKernel) = {
    val login = app.params.get("username")
    val password = app.params.get("password")
    logger.debug("login: %s, password: %s".format(login, password))
    login.isDefined && password.isDefined
  }

  /**
   * Authenticates a user by validating the username (or email) and password request params.
   */
  final def authenticateUser(app: ScalatraKernel)(implicit validate: (String, String) ⇒ Option[U]): Either[String, U] = {
    val a: Option[Option[U]] = for {
      login ← app.params.get("username")
      password ← app.params.get("password")
    } yield validate(login, password)
    (a.getOrElse(None): Option[U]) match {
      case Some(user: U) ⇒ Right(user)
      case None          ⇒ Left("Username or password invalid!")
    }
  }

  final def afterAuthProcessing(app: ScalatraKernel) {}

  final def beforeLogout(app: ScalatraKernel) {}

}
