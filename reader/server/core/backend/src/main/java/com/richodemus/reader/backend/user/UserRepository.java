package com.richodemus.reader.backend.user;

import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;

public interface UserRepository {
    UserId getUserId(final Username username);
}
