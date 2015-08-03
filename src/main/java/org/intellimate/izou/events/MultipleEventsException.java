package org.intellimate.izou.events;

import org.intellimate.izou.internal.events.LocalEventManager;

/**
 * Exception thrown if there are multiple Events fired at the same time.
 */
//extends because evil hack for backward-compatibility
@SuppressWarnings({"WeakerAccess", "deprecation"})
public class MultipleEventsException extends LocalEventManager.MultipleEventsException {
}
