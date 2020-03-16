package ru.rain.ifmo.atask.data.models

enum class VKDocType {
    ALL,
    TEXT,
    ARCHIVE,
    GIF,
    IMAGE,
    AUDIO,
    VIDEO,
    E_BOOK,
    UNDEFINED;

    fun toInt(): Int = when(this) {
        ALL -> 0
        TEXT -> 1
        ARCHIVE -> 2
        GIF -> 3
        IMAGE -> 4
        AUDIO -> 5
        VIDEO -> 6
        E_BOOK -> 7
        UNDEFINED -> 8
    }
}