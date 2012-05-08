package com.proinnovate.scalatra.userauth

import com.weiglewilczek.slf4s.Logging
import org.scalatra.{ Initializable, ScalatraKernel }
import javax.servlet.http.HttpSession
import java.util.concurrent.ConcurrentHashMap

trait UserAuthSupport[U] extends ScalatraKernel with Initializable with Logging {

  /**
   * The default path for logging in.  Override this to set a new default.
   */
  lazy val userLoginPath: String = "/login"

  lazy val userSessionKey: String = "UserID"

  /**
   * Retrieve the current user from the session.
   *
   * @note This method uses a session passed to it to allow it to be used in contexts where the scoped `session`
   *       function will not return a valid result.
   *
   * @param session the HttpSession to read from.
   * @return Some(UserType) or None if no user is currently logged in.
   */
  def userOptionFromSession(session: HttpSession) = {
    // Use the in scope `session` variable to access the current session and return a Option[U] for the particular
    // User class of your project...
    session.get(userSessionKey) match {
      case Some(id: String) ⇒ userOptForId(id)
      case _                ⇒ None
    }
  }

  /**
   * Store a user in the current session, most likely by using a unique user ID.
   *
   * @param userOption Some User or None.  If None then remove the user details from the session completely.
   */
  def recordUserInSession(session: HttpSession, userOption: Option[U]) {
    // Record the given User in the current session accessed through the in scope `session` variable. e.g. code like the
    // following...
    userOption match {
      case Some(user) ⇒
        val userId = userIdForUser(user)
        session.put(userSessionKey, userId)
        userSessions.put(userId, session)
      case None ⇒
        try {
          for (user ← session.get(userSessionKey)) userSessions.remove(user)
          session.remove(userSessionKey)
        } catch {
          case e: IllegalStateException ⇒
          // This occurs when an attempt is made to set an attribute of a session that is no longer valid.  If the
          // session has been invalidated then there is no need to clear the UserID attribute, so just ignore this
          // exception and carry on.
        }
    }
  }

  /**
   * Remove record of this user in whichever session they are stored in.
   *
   * @param user user to be logged out.
   */
  def logoutUserFromSessions(user: U) {
    val userId = userIdForUser(user)
    for (session ← Option(userSessions.get(userId))) recordUserInSession(session, None)
  }

  /**
   * Return the unique userID string for a given user.
   *
   * @param user the user object for the user.
   * @return the unique ID String for the user.
   */
  def userIdForUser(user: U): String

  /**
   * Return the user for a given user ID.
   *
   * @param id the unique String ID for the user.
   * @return Some user object for the identified user or None if none is identified.
   */
  def userOptForId(id: String): Option[U]

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

  def userLogin(username: String, password: String): Either[String, U]

  /**
   * Check to see if a valid user is currently logged in.
   *
   * @return true if the user is logged in, false if not.
   */
  def userIsAuthenticated: Boolean = {
    userOption.isDefined
  }

  def userPostLogin(user: U) {
    // Override this to do something once someone has logged in, e.g. recording their login date.
  }

  def userPostLogout(user: U) {
    // Override this to do something once someone has logged out, e.g. recording their logout date.
  }

  /**
   * Authenticate the user using any of the registered strategies that is valid and authenticates.
   *
   * @param app the ScalatraKernel object current at this time.
   * @return either the user who logged in (Right) or the error message resulting from a failed attempt to log in
   *         (Left).  If the error message is an empty String then the login failed but there was no error.  This
   *         might occur when an authentication method like RememberMe is used all the time but is only counted as
   *         failing if the user has a cookie that is out of date.
   */
  def userAuthenticate(app: ScalatraKernel): Either[String, U] = {
    implicit def login(username: String, password: String): Either[String, U] = userLogin(username, password)

    logger.debug("Trying to authenticate!")
    val authResults: Seq[Either[String, U]] = userAuthStrategies.collect {
      case s if s.authIsValid(app) ⇒ s.authenticateUser(app)
    }
    val uniqueMatchingUsers: Set[U] = authResults.collect {
      case Right(u) ⇒ u
    }.toSet
    val authenticationErrors: Seq[String] = authResults.collect {
      case Left(u) if u != "" ⇒ u
    }
    if (uniqueMatchingUsers.size > 1) {
      logger.error("Multiple authentication schemes should never authenticate to different users at the same time!")
      logger.debug("matchs = " + uniqueMatchingUsers)
    }
    recordUserInSession(app.session, uniqueMatchingUsers.headOption)
    // Give every authentication strategy an opportunity to do some further authentication work just after
    // authentication has taken place.
    userAuthStrategies.foreach(_.afterAuthProcessing(app))
    uniqueMatchingUsers.headOption.foreach(user ⇒ userPostLogin(user: U))
    uniqueMatchingUsers.headOption.map(Right(_)).getOrElse(Left(authenticationErrors.headOption.getOrElse("")))
  }

  /**
   * Logout the user.
   *
   * Clear the currently logged in user so that no user is currently authenticated.
   */
  def userLogout() {
    // Give every authentication strategy an opportunity to do something before final logout.
    userAuthStrategies.foreach(_.beforeLogout(this))
    // Logout
    val uOpt = this.userOption
    logger.debug("Cancelling authentication of user")
    recordUserInSession(session, None)
    // Call postLogout for user who was logged in...
    uOpt.foreach(user ⇒ userPostLogout(user))
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

  def onlyIfUserAuthenticated(doSomething: ⇒ Any): Any = {
    if (!userIsAuthenticated) {
      response.setStatus(404)
    } else {
      doSomething
    }
  }

  // PRIVATE

  // Mapping from userID to Session object for a logged in user.
  private val userSessions = new ConcurrentHashMap[String, HttpSession]()

}
