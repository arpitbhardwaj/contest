package com.ab.services

import java.util.UUID
import com.ab.persistence.Model.User
import com.ab.persistence.UserDao
import com.ab.util.Util
import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

class UserServiceDBImpl(config: Config, userDao: UserDao) extends UserService {

  private val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  override def all: Future[ServiceResponse[Seq[User]]] = {
    logger.info("[all]")

    userDao
      .all
      .map(users => Right(users))
      .recover {
        case e: Exception => Left(ErrorResponse(e.getMessage, 0))
      }
  }

  override def create(user: User): Future[ServiceResponse[User]] = {
    logger.info("[create] - {}", user.id)

    /*var createdUser:Future[User] = userDao.insert(user)
    //callbacks (the flow is decided by executor)
    createdUser.foreach{
      user => logger.info("[create] - User created with id {}.", user.id)
    }
    createdUser.foreach{
      user => logger.info("[create] - Sending email for successful reg to {}.", user.email)
    }

    //in order to control the flow
    createdUser.andThen{
      case Success(user) => logger.info("[create] - User created with id {}.", user.id)
    }.andThen{
      case Success(user) => logger.info("[create] - Sending email for successful reg to {}.", user.email)
    }

    createdUser.map(user => Right(user))*/

    //composing multiple futures (nested async operation)
    encryptPassword(user) /*Async Operation*/
      .zip(isUsernameUnique(user)) /*Async Operation*/
      .flatMap /*Async Operation*/
      { encUserAndIsUnique: (User, Boolean) =>
        val (pwdEncryptedUser, isUnameUnique) = encUserAndIsUnique
        if(isUnameUnique)
          userDao.insert(pwdEncryptedUser)
        else throw new IllegalArgumentException(s"Username ${pwdEncryptedUser.username} already exists.")
      }
      .map { created =>
        Right(created)
      }
      //handling future failures
      .recover {
        case e: Exception => Left(ErrorResponse(e.getMessage, 0))
      }

  }

  private def encryptPassword(user: User): Future[User] =
    Util.encrypt(user.password)
      .map(encrypted => user.copy(password = encrypted))

  private def isUsernameUnique(user: User): Future[Boolean] =
    userDao.byUsername(user.username)
      .map(_.isEmpty)

  override def byId(id: UUID): Future[ServiceResponse[User]] = {
    logger.info("[byId] - {}", id)

    userDao
      .byId(id)
      .map {
        case None => Left(ErrorResponse(s"Could not read User with Id: $id", 0))
        case Some(user) => Right(user)
      }
      .recover {
        case e: Exception =>
          logger.info("[create] - exception occurred: {}", e.getMessage)
          Left(ErrorResponse(e.getMessage, 0))
      }
  }

  override def delete(id: UUID): Future[ServiceResponse[Boolean]] = {
    logger.info("[delete] - {}", id)

    userDao
      .remove(id)
      .map {
        case true => Right(true)
        case false => Left(ErrorResponse("Could not delete User with Given ID", 0))
      }
      .recover {
        case e: Exception =>
          logger.info("[create] - exception occurred: {}", e.getMessage)
          Left(ErrorResponse(e.getMessage, 0))
      }
  }
}