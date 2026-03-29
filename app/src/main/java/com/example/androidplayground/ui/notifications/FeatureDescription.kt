package com.example.androidplayground.ui.notifications

data class FeatureDescription(
    val title: String,
    val description: String,
    val documentationUrl: String
)

object FeatureDescriptions {
    val map = mapOf(
        "notifications" to FeatureDescription(
            title = "Notification Dashboard",
            description = "A comprehensive developer sandbox feature to test dynamic notification channel creation (Urgent, High, Default, Low), visibility flags (Public, Private, Secret with public versions), and pending intents.\n\nUse this dashboard to evaluate how notifications appear on different Android versions and device states.",
            documentationUrl = "https://developer.android.com/develop/ui/views/notifications"
        ),
        "notification_title" to FeatureDescription(
            title = "Content Title",
            description = "The primary title of the notification. Keep it short and descriptive. It is traditionally limited to a single line on most Android variants.",
            documentationUrl = "https://developer.android.com/develop/ui/views/notifications/build-notification#setNotificationContent"
        ),
        "notification_text" to FeatureDescription(
            title = "Content Text",
            description = "The main body text of the notification. Should provide contextual information or a summary of the event.",
            documentationUrl = "https://developer.android.com/develop/ui/views/notifications/build-notification#setNotificationContent"
        ),
        "notification_app_name" to FeatureDescription(
            title = "App Name (SubText)",
            description = "Often displayed next to the app's small icon in the notification header. Primarily used to distinguish content sources or profiles.",
            documentationUrl = "https://developer.android.com/develop/ui/views/notifications/build-notification"
        ),
        "notification_importance" to FeatureDescription(
            title = "Channel Importance",
            description = "Controls the level of interruption given to a notification (e.g., sound, vibration, heads-up display). From Android 8.0 (API 26), this must be set on the NotificationChannel.",
            documentationUrl = "https://developer.android.com/develop/ui/views/notifications/channels#importance"
        ),
        "notification_lock_visibility" to FeatureDescription(
            title = "Lock Screen Visibility",
            description = "Dictates how notification content is presented on a secure lock screen. \n\nPUBLIC: Show everything.\nPRIVATE: Hide sensitive details (uses Public Version).\nSECRET: Do not show on lock screen at all.",
            documentationUrl = "https://developer.android.com/develop/ui/views/notifications/build-notification#lockscreenNotification"
        ),
        "notification_action_button" to FeatureDescription(
            title = "Action Button",
            description = "Allows users to perform tasks directly from the notification shade (e.g., Reply, Archive) without needing to open the app.",
            documentationUrl = "https://developer.android.com/develop/ui/views/notifications/build-notification#Actions"
        ),
        "notification_deep_link" to FeatureDescription(
            title = "Deep Link Target",
            description = "The explicit destination UI triggered by tapping the notification or its action button, traditionally backed by a PendingIntent mapped to an Android Jetpack NavController route.",
            documentationUrl = "https://developer.android.com/guide/navigation/navigation-deep-link"
        )
    )
}
