package com.proinnovate.scalatra.userauth

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.scalatra.ScalatraKernel


trait UserAuthStrategy[U] {

  def authIsValid(app: ScalatraKernel): Boolean

  def authenticateUser(app: ScalatraKernel)(implicit authenticate: (String, String) => Option[U]): Option[U]

}
