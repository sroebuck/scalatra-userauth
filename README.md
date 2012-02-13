Scalatra UserAuth
=================

A very simple authentication library for user access control on Scalatra based web projects.

Currently it simply allows logging in with a username and password and user tracking with sessions.  This is not a model of high security but a perfectly good way of getting authentication started.

The code is structured to allow other authentication methods to be added without unduly complicating the work of integrating this code.

Version: 0.1.0-SNAPSHOT

This code is currently under development.

Prerequisites
-------------

* Scalatra based project.
* A `User` class (it doesn't have to be called `User`) that represents a logged in user and can verify a username and password.  In other words, this library does not manage the storage and retreival of users from database - that is down to you.
* Only tested so far with ScalatraServlet (e.g. not tested with ScalatraFilter)

Building and installing
-----------------------

Just include it as a dependency in your project with:

    "com.proinnovate" %% "scalatra-userauth" % "0.1.0-SNAPSHOT"

Using
-----

Simple to use is a key theme of this library.  To add it to a ScalatraServlet import:

    import com.proinnovate.scalatra.userauth.UserAuthSupport

and add this trait to your `ScalatraServlet`, e.g.:

    class MyServlet extends ScalatraServlet with UserAuthSupport[User] {

where `User` is the class you are using to represent Users of your web application.

Then, within your Servlet handling class (e.g. `MyServlet` in the example above), include definitions of the following three methods:

    def userOptionFromSession = {
      // Use the in scope `session` variable to access the current session and return a Option[User] for the particular
      // User class of your project. e.g. code like the following...
      session.get("UserID") match {
        case Some(id: String) => Some(User(id))
        case _ => None
      }

    }

    def recordUserInSession(userOption: Option[User]) {
      // Record the given User in the current session accessed through the in scope `session` variable. e.g. code like the
      // following...
      userOption match {
        case Some(user) => session.put("UserID", user.userID)
        case None => session.remove("UserID")
      }
    }

    // An implicit function which must take two Strings and return an Option[User] for the particular User object you are
    // using to represent users in your project.  Given a valid pair of userid and password, it should return the User object
    // as an Option[User].  e.g. the following...
    implicit def userAuthenticate(userid: String, password: String): Option[User] = {
      User.authenticate(userid, password)
    }

In order to authenticate a user you can just add the following matcher to your request handling code:

    before() {
      if (!userIsAuthenticated) {
        userAuthenticate(this)
      }
    }

This currently makes the assumption that the login process with involve a form being sent with two fields called "username" and "password".  If it detects these in any request it attempt to validate and authenticate the user.  But if you want to change this you will see just how simple the code is if you look at `UserPasswordStrategy.scala`.  You can add a new strategy or use a different strategy by overriding the `UserAuthSupport` lazy val: `userAuthStrategies`. 

With these things in place your Scalatra request matching code you can then use the following methods to control access.  See the code within `UserAuthSupport` for documentation of these:

    redirectIfUserAuthenticated()

    redirectIfUserNotAuthenticated()

    onlyIfUserAuthenticated {
        // Your code goes here.
        // This is intended for ajax responses which should fail and not redirect if the user is not authenticated.
    }

    userLogout()

You can also access:

    // The currently logged on user:
    userOption: Option[User]

    // Boolean check of whether any user is authenticated now:
    userIsAuthenticated: Boolean


License
-------

This software is Copyright (c) 2011, Stuart Roebuck and licensed under a
standard MIT license (more specifically referred to as the Expat license). See
the `LICENSE.md` file for details.

