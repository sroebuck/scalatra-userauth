package com.proinnovate.scalatra.userauth

trait RememberMeSupport[U] extends UserAuthSupport[U] {

  override def calculatedUserAuthStrategies =
    super.calculatedUserAuthStrategies :+ new RememberMeStrategy[U]()

  def storeRememberMeTokenForUser(token: String, user: U)

  def getUserForRememberMeToken(token: String): Option[U]

}
