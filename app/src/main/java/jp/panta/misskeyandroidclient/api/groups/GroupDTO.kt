package jp.panta.misskeyandroidclient.api.groups

import jp.panta.misskeyandroidclient.model.group.Group
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.Serializer
import java.io.Serializable
import java.util.*

@kotlinx.serialization.Serializable
data class GroupDTO(
    val id: String,
    @kotlinx.serialization.Serializable(InstantIso8601Serializer::class) val createdAt: Instant,
    val name: String,
    val ownerId: String,
    val userIds: List<String>
): Serializable

fun GroupDTO.toGroup(accountId: Long): Group {
    return Group(
        Group.Id(accountId, id),
        createdAt,
        name,
        User.Id(accountId, ownerId),
        userIds.map {
            User.Id(accountId, it)
        }
    )
}

