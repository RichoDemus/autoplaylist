import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import javax.servlet.http.HttpSession

internal var HttpSession.userId: UserId?
    get() {
        return this.getAttribute("userId") as UserId?
    }
    set(userId) {
        this.setAttribute("userId", userId)
    }

internal var HttpSession.username: Username?
    get() {
        return this.getAttribute("username") as Username?
    }
    set(userId) {
        this.setAttribute("username", userId)
    }

internal fun HttpSession.isLoggedIn() = this.userId != null
