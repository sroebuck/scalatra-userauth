package com.proinnovate.scalatra.userauth

trait RememberMeSupport[U] extends UserAuthSupport[U] {

  override def calculatedUserAuthStrategies =
    super.calculatedUserAuthStrategies :+ new RememberMeStrategy[U]()

  /**
   * Take a unique token String and store it as the remember me token for the given user.  This should overwrite any
   * existing remember me token for the user.
   *
   * @param user the user to store the token for.
   * @param tokenOpt Some unique String token to be stored or None to delete any existing token.
   */
  def storeRememberMeTokenForUser(user: U, tokenOpt: Option[String])

  /**
   * Validate remember me token for user by comparing the provided one with the one stored for the user.
   *
   * @param userId the ID of the user associated with the token.
   * @param token the unique String token for the user.
   * @return Some user if the user validates or None if they don't.
   */
  def validateRememberMeToken(userId: String, token: String): Option[U]

}
