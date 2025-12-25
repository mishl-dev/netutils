package org.netutils.event

/**
 * Annotation to mark methods as event handlers.
 * Methods annotated with @Subscribe must:
 * - Have exactly one parameter
 * - The parameter must be a subclass of Event
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Subscribe
