package eu.depau.etchdroid.plugins.reviews

interface IWriteReviewHelper {
    val isGPlayFlavor: Boolean
    fun launchReviewFlow()
}