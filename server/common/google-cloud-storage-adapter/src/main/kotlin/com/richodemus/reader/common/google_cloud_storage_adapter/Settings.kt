package com.richodemus.reader.common.google_cloud_storage_adapter

internal class Settings {
    val gcsProject = System.getProperty("reader.gcs.project").emptyToNull() ?:
            throw IllegalArgumentException("Missing property GCS_PROJECT/syncheror.gcs.project")

    val gcsBucket = System.getProperty("reader.gcs.bucket").emptyToNull() ?:
            throw IllegalArgumentException("Missing property GCS_BUCKET/syncheror.gcs.bucket")

    override fun toString() = "Settings(gcsProject='$gcsProject', gcsBucket='$gcsBucket')"
}

private fun String.emptyToNull(): String? {
    if (this.isBlank())
        return null
    return this
}
