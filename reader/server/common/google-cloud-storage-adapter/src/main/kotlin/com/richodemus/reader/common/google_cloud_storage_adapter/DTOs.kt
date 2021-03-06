package com.richodemus.reader.common.google_cloud_storage_adapter

internal data class Offset(val value: Long)
internal data class Key(val value: String)
internal data class Data(val value: String)
internal data class Event(val offset: Offset, val key: Key, val data: Data)
