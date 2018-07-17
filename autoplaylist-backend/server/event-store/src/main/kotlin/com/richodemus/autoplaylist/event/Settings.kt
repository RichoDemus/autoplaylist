package com.richodemus.autoplaylist.event

internal class Settings {
    val gcsProject = System.getProperty("autoplaylist.gcs.project").emptyToNull()
            ?: throw IllegalArgumentException("Missing property GCS_PROJECT/autoplaylist.gcs.project")

    val gcsBucket = System.getProperty("autoplaylist.gcs.bucket").emptyToNull()
            ?: throw IllegalArgumentException("Missing property GCS_BUCKET/autoplaylist.gcs.bucket")

    override fun toString() = "Settings(gcsProject='$gcsProject', gcsBucket='$gcsBucket')"
}

private fun String.emptyToNull(): String? {
    if (this.isBlank())
        return null
    return this
}
