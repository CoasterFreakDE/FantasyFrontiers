package net.fantasyfrontiers.utils

import dev.fruxz.ascend.extension.logging.getItsLogger
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import io.sentry.Sentry
import io.sentry.SpanStatus
import io.sentry.TransactionContext

class SentryWrapperEventListener(private val originalListener: EventListener) : EventListener {
    override fun onEvent(event: GenericEvent) {
        // Start a new Sentry transaction
        val transaction = Sentry.startTransaction(TransactionContext("eventListener", event.javaClass.simpleName))

        try {
            // Forward the event to the original EventListener
            originalListener.onEvent(event)

            // Set transaction status to OK
            transaction.status = SpanStatus.OK
        } catch (e: Exception) {
            // Capture the exception in Sentry
            Sentry.captureException(e)
            getItsLogger().severe("An exception occurred while handling an event: $e")

            // Set transaction status to ERROR
            transaction.status = SpanStatus.INTERNAL_ERROR
        } finally {
            // Finish the transaction
            transaction.finish()
        }
    }
}
