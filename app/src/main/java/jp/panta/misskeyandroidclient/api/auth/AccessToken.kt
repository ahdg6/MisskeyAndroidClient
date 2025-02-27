package jp.panta.misskeyandroidclient.api.auth

import jp.panta.misskeyandroidclient.api.users.UserDTO
import kotlinx.serialization.Serializable

@Serializable data class AccessToken(val accessToken: String, val user: UserDTO)

