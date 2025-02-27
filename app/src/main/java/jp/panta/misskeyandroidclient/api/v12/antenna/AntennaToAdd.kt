package jp.panta.misskeyandroidclient.api.v12.antenna

import kotlinx.serialization.Serializable

/**
 * @param i ユーザーのの認証
 * @param antennaId updateのときはUpdate対象のアンテナのIdを指定します
 * @param withReplies 返信を含むのか
 * @param name アンテナの名称
 * @param src 受信するソース "home" "all" "users" "list" "group"
 * @param userListId ユーザーのリストのユーザーのノートを取得リソースにする
 * @param userGroupId 指定したグループのユーザーのノートを取得ソースにする
 * @param keywords 受信キーワード [["key", "words"]] "key" & "words"になる[["key", "words"], ["source"]]の場合は "key" and "words" or "source"になる
 * @param excludeKeywords 除外ワード 指定方式は受信キーワードと同じ
 * @param users ユーザーを指定する
 * @param caseSensitive
 * @param withFile ファイルが添付されたーノートのみ
 * @param notify 新しいノートを通知する
 */
@Serializable
data class AntennaToAdd(
    val i: String,
    val antennaId: String?,
    val name: String,
    val src: String,
    val userListId: String?,
    val userGroupId: String?,
    val keywords: List<List<String>>,
    val excludeKeywords: List<List<String>>,
    val users: List<String> = emptyList(),
    val caseSensitive: Boolean = true,
    val withFile: Boolean = false,
    val withReplies: Boolean = false,
    val notify: Boolean = false,
    val hasUnreadNote: Boolean = false

)