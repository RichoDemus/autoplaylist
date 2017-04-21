package com.richodemus.reader.user_service

import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.PasswordHash
import org.mindrot.jbcrypt.BCrypt

internal fun Password.hash() = PasswordHash(BCrypt.hashpw(this.value, BCrypt.gensalt()))
internal fun PasswordHash.isSame(password: Password) = BCrypt.checkpw(password.value, this.value)
