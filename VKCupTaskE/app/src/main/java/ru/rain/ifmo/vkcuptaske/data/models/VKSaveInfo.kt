package ru.rain.ifmo.vkcuptaske.data.models

data class VKSaveInfo(val id: Int,
                 val albumId: Int,
                 val ownerId: Int) {
    fun getAttachment() = "photo${ownerId}_$id"
}
