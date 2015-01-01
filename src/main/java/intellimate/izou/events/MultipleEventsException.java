package intellimate.izou.events;

/**
 * Exception thrown if there are multiple Events fired at the same time.
 */
//extends because evil hack for backward-compatibility
@SuppressWarnings({"WeakerAccess", "deprecation"})
public class MultipleEventsException extends LocalEventManager.MultipleEventsException {
}
