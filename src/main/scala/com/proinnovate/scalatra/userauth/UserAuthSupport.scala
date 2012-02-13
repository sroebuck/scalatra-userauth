package com.proinnovate.scalatra.userauth

import com.weiglewilczek.slf4s.Logging
import org.scalatra.{Initializable, Handler, ScalatraKernel}

trait UserAuthSupport[U] extends Handler with Initializable with Logging {
  self: ScalatraKernel =>

  /**
   * Method for retrieving the current user from the session.
   *
   * @return Some(UserType) or None if no user is currently logged in.
   */
  def userOptionFromSession: Option[U]


  /**
   * Method for storing a user in the current session, most likely by using a unique user ID.
   *
   * @param user Some User or None.  If None then remove the user details from the session completely.
   */
  def recordUserInSession(user: Option[U])


  /**
   *
   */
  lazy val userAuthStrategies: Seq[UserAuthStrategy[U]] = Seq(
    new UserPasswordStrategy[U]()
  )


  /**
   * Obtain the current user as an Option.
   *
   * @return Some(UserType) or None if no user is logged in.
   */
  def userOption: Option[U] = userOptionFromSession


  /**
   * Check to see if a valid user is currently logged in.
   *
   * @return true if the user is logged in, false if not.
   */
  def userIsAuthenticated: Boolean = {
    userOptionFromSession.isDefined
  }


  /**
   * Authenticate the user using any of the registered strategies that is valid and authenticates.
   *
   * @param app the ScalatraKernel object current at this time.
   * @param authenticate an implicit method that takes a username and password and returns an Option[UserType] for the
   *                     specific UserType required.  In other words, there has to be an implicit method in scope which
   *                     can look up the username and password and return the matching user (if there is one).
   */
  def userAuthenticate(app: ScalatraKernel)
                      (implicit authenticate: (String, String) => Option[U]) {
    logger.debug("Trying to authenticate!")
    val matchingUsers: Set[U] = userAuthStrategies.collect {
      case s if s.authIsValid(app) && s.authenticateUser(app).isDefined => s.authenticateUser(app)
    }.flatten.toSet
    if (matchingUsers.size > 1) {
      logger.error("Multiple authentication schemes should never authenticate to different users at the same time!")
      logger.debug("matchs = " + matchingUsers)
    }
    recordUserInSession(matchingUsers.headOption)
  }

  /**
   * Logout the user.
   *
   * Clear the currently logged in user so that no user is currently authenticated.
   */
  def userLogout() {
    logger.debug("Cancelling authentication of user")
    recordUserInSession(None)
  }

  def redirectIfUserAuthenticated(path: String = "/") {
    if (userIsAuthenticated) {
      redirect(path)
    }
  }

  def redirectIfUserNotAuthenticated(path: String = "/login") {
    if (!userIsAuthenticated) {
      session.put("destination", requestPath)
      redirect(path)
    }
  }

  def onlyIfUserAuthenticated(doSomething: => Any): Any = {
    if (!userIsAuthenticated) {
      response.setStatus(404)
    } else {
      doSomething
    }
  }


}
