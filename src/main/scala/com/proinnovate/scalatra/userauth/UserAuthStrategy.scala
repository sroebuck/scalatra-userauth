package com.proinnovate.scalatra.userauth

import org.scalatra.ScalatraKernel

/**
 * This trait should be implemented by any authentication strategy.
 */
trait UserAuthStrategy[U] {

  /**
   * Given the current ScalatraKernel, determine whether the authentication strategy is valid for use at the current
   * time.
   *
   * return true if this authentication scheme can be used at this time.
   */
  def authIsValid(app: ScalatraKernel): Boolean

  /**
   * Given the current ScalatraKernel and an implicit function that will take a username and password and return the
   * authenticated User, try to authenticate a user.
   *
   * return Some(User) or None if no user was authenticated.
   */
  def authenticateUser(app: ScalatraKernel)(implicit authenticate: (String, String) => Option[U]): Option[U]


  def afterAuthProcessing(app: ScalatraKernel)

}
