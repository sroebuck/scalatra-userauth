package com.proinnovate.scalatra.userauth

import com.weiglewilczek.slf4s.Logging
import org.scalatra.{Initializable, Handler, ScalatraKernel}
import javax.servlet.http.HttpSession

trait UserAuthSupport[U] extends ScalatraKernel with Initializable with Logging {

  /**
   * The default path for logging in.  Override this to set a new default.
   */
  lazy val userLoginPath: String = "/login"


  /**
   * Method for retrieving the current user from the session.
   *
   * @note This method uses a session passed to it to allow it to be used in contexts where the scoped `session`
   *       function will not return a valid result.
   *
   * @param session the HttpSession to read from.
   * @return Some(UserType) or None if no user is currently logged in.
   */
  def userOptionFromSession(session: HttpSession): Option[U]


  /**
   * Method for storing a user in the current session, most likely by using a unique user ID.
   *
   * @param user Some User or None.  If None then remove the user details from the session completely.
   */
  def recordUserInSession(session: HttpSession, user: Option[U])


  def calculatedUserAuthStrategies: Seq[UserAuthStrategy[U]] = Seq(
    new UserPasswordStrategy[U]()
  )

  /**
   *
   */
  final lazy val userAuthStrategies: Seq[UserAuthStrategy[U]] = calculatedUserAuthStrategies


  /**
   * Obtain the current user as an Option.
   *
   * @return Some(UserType) or None if no user is logged in.
   */
  def userOption: Option[U] = if (request != null && session != null) userOptionFromSession(session) else None


  /**
   * Check to see if a valid user is currently logged in.
   *
   * @return true if the user is logged in, false if not.
   */
  def userIsAuthenticated: Boolean = {
    userOption.isDefined
  }

  def postLogin(user: U) {
    // Override this to do something once someone has been logged, e.g. recording their last login date.
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
    recordUserInSession(app.session, matchingUsers.headOption)
    // Give every authentication strategy an opportunity to do some further authentication work just after
    // authentication has taken place.
    userAuthStrategies.foreach( _.afterAuthProcessing(app) )
    matchingUsers.headOption.foreach(user => postLogin(user: U))
  }


  /**
   * Logout the user.
   *
   * Clear the currently logged in user so that no user is currently authenticated.
   */
  def userLogout() {
    // Give every authentication strategy an opportunity to do something before final logout.
    userAuthStrategies.foreach( _.beforeLogout(this) )
    // Logout
    logger.debug("Cancelling authentication of user")
    recordUserInSession(session, None)
  }


  def redirectIfUserAuthenticated(path: String = "/") {
    if (userIsAuthenticated) {
      redirect(path)
    }
  }


  def redirectIfUserNotAuthenticated(path: String = userLoginPath) {
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
