package eu.depau.etchdroid.plugins.reviews

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import eu.depau.etchdroid.plugins.telemetry.Telemetry

class WriteReviewHelper(private val mActivity: Activity) : IWriteReviewHelper {
    override val isGPlayFlavor: Boolean
        get() = true

    private var mReviewInfo: ReviewInfo? = null
    private var mLaunchASAP = false
    private var mFailed = false
    private var mReviewed = false

    private val mManager = ReviewManagerFactory.create(mActivity).apply {
        requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Telemetry.addBreadcrumb(
                        "Review flow requested successfully",
                        "reviews"
                )
                mReviewInfo = task.result
            } else {
                Telemetry.addBreadcrumb(
                        "Failed to request review flow: ${task.exception}",
                        "reviews"
                )
                mFailed = true
            }
            if (mLaunchASAP) {
                Telemetry.addBreadcrumb(
                        "Launching previously requested review flow",
                        "reviews"
                )
                launchReviewFlow()
            }
        }
    }

    override fun launchReviewFlow() {
        if (mReviewInfo == null) {
            Telemetry.addBreadcrumb(
                    "Review requested before the Google Play SDK was ready",
                    "reviews"
            )
            mLaunchASAP = true
            return
        }
        if (!mFailed && !mReviewed) {
            Telemetry.addBreadcrumb("Launching regular review flow", "reviews")
            mManager.launchReviewFlow(mActivity, mReviewInfo!!)
            mReviewed = true
        } else {
            Telemetry.addBreadcrumb(
                    "Launching Play Store as fallback for review; failed: $mFailed, already reviewed: $mReviewed",
                    "reviews"
            )
            openFallback(mActivity)
        }
    }

    private fun openFallback(context: Context) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
            )
        )
    }
}