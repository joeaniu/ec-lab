package net.thiki.gist

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GistTest {


    @Test
    fun test() {

    }


}

/**
 * 1. check if user exist
 * 2. password is valid
 * 3. captcha is correct
 */
class LoginController(
        private val loginService: LoginService
){


    fun login(params: Map<String, String>): String{

        //模拟session
        val session = mapOf(
                "captcha" to "21314"
        )

        loginService.login(
                userName = params.getValue("userName"),
                passwd = params.getValue("passwd"),
                captcha = params.getValue("captcha"),
                sessionCaptcha = session.getValue("captcha")
        )
        return "ok"
    }
}

class LoginService(
        private val userRepo: UserRepo,
        private val passwdValidator: PasswdValidator
) {
    fun login(userName: String, passwd: String, captcha: String, sessionCaptcha: String) {

        val captchaValidator = DefaultCaptchaValidator(sessionCaptcha)
        captchaValidator.check(captcha)
        val user: User? = userRepo.get(userName)
        if (user == null){
            throw RuntimeException("The user[$userName] is not found.")
        }

        // or:  userRepo.existByName(userName)

        assert(passwdValidator.isValid(passwd, user.passwd)){
            "The password is wrong"
        }
    }

}
class DefaultCaptchaValidator(private val expectedCode: String): CaptchaValidator {
    override fun check(captcha: String) {
        assert(captcha == expectedCode){
            "The captcha is not valid!"
        }
    }
}
data class User(val id: Long, val name: String, val passwd: String)

/**
 * encryption and validation
 */
interface PasswdValidator {
    fun isValid(input: String, saved: String): Boolean
}

interface CaptchaValidator {
    /**
     * if not valid throws an ex
     */
    fun check(captcha: String)
}


interface UserRepo {
    fun existByName(userName: String): Boolean
    fun get(userName: String): User?
}
