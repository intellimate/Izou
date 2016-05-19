package org.intellimate.izou.events;

import ro.fortsoft.pf4j.AddonAccessible;

/**
 * Exception thrown if there are multiple Events fired at the same time.
 */
//extends because evil hack for backward-compatibility
@AddonAccessible
@SuppressWarnings({"WeakerAccess", "deprecation"})
public class MultipleEventsException extends LocalEventManager.MultipleEventsException {
}
