package com.github.nestorm001.autoclicker.model

import android.view.View

/** Base for for all possible actions for an Event. */
sealed class Action {

    /** The unique identifier for the action. Use 0 for creating a new action. Default value is 0. */
    abstract var id: Long

    /** The identifier of the event for this action. */
    abstract var eventId: Long

    /** The name of the action. */
    abstract var name: String?

    /** @return true if this action is complete and can be transformed into its entity. */
    internal open fun isComplete(): Boolean = name != null

    /** Cleanup all ids contained in this action. Ideal for copying. */
    internal abstract fun cleanUpIds()

    /** @return creates a deep copy of this action. */
    abstract fun deepCopy(): Action

    /**
     * Click action.
     *
     * @param id the unique identifier for the action. Use 0 for creating a new action. Default value is 0.
     * @param eventId the identifier of the event for this action.
     * @param name the name of the action.
     * @param pressDuration the duration between the click down and up in milliseconds.
     * @param x the x position of the click.
     * @param y the y position of the click.
     */
    data class Click(
        override var id: Long = 0,
        override var eventId: Long,
        override var name: String? = null,
        var pressDuration: Long? = null,
        var x: Int? = null,
        var y: Int? = null,
        var clickOnCondition: Boolean,
        var view: View
    ) : Action() {

        override fun isComplete(): Boolean =
            super.isComplete() && pressDuration != null && ((x != null && y != null) || clickOnCondition)

        override fun cleanUpIds() {
            id = 0
            eventId = 0
        }

        override fun deepCopy(): Click = copy(name = "" + name)
    }
}