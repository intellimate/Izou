package old.org.intellimate.izou.testHelper;

//import intellimate.izouSDK.events.EventImpl;
/**
 * Helper class for Unit-testing
 */
public class IzouTest //implements Identifiable
{
//    private static Main staticMain;
//    public Main main;
//    public final String id;
//    private IdentificationManager identificationManager;
//    private static int identificationNumber;
//    private static int eventNumber;
//    private static final class Lock { }
//    private final Object lock = new Lock();
//    private TestAddOn testAddOn = new TestAddOn(getID());
//    private Context context;
//
//    /**
//     * creates a new instance of IzouTest
//     * @param isolated whether the fields (manager etc.) should be Isolated from all the other instances.
//     */
//    public IzouTest(boolean isolated, String id) {
//        if(isolated) {
//            main = new Main(null, false, true);
//        } else {
//            if(staticMain == null) {
//                staticMain = new Main(null, false, true);
//            }
//            this.main = staticMain;
//        }
//        context = new ContextImplementation(testAddOn, main, "debug");
//        this.id = id;
//        identificationManager = IdentificationManager.getInstance();
//        identificationManager.registerIdentification(this);
//    }
//
//    /**
//     * An ID must always be unique.
//     * A Class like Activator or OutputPlugin can just provide their .class.getCanonicalName()
//     * If you have to implement this interface multiple times, just concatenate unique Strings to
//     * .class.getCanonicalName()
//     *
//     * @return A String containing an ID
//     */
//    @Override
//    public String getID() {
//        return id;
//    }
//
//    public Optional<EventModel> getEvent(String type) {
//        Optional<Identification> id = identificationManager.getIdentification(this);
//        if(!id.isPresent()) return Optional.empty();
//        return EventImpl.createEvent(type, id.get());
//    }
//
//    /**
//     * checks if is working is set everywhere to false, [0] should be reserved for the test here
//     */
//    public void testListenerTrue(final boolean[] isWorking, EventModel event) throws InterruptedException {
//        main.getEventDistributor().registerEventListener(event, id -> {
//            isWorking[0] = true;
//        });
//        try {
//            Identification id = IdentificationManager.getInstance().getIdentification(this).get();
//            main.getLocalEventManager().registerCaller(id).get().fire(event);
//        } catch (MultipleEventsException e) {
//            fail();
//        }
//
//        synchronized (lock) {
//            lock.wait(10);
//            while(!main.getLocalEventManager().getEvents().isEmpty())
//            {
//                lock.wait(2);
//            }
//        }
//        boolean result = false;
//        for (boolean temp : isWorking)
//        {
//            if(temp) result = true;
//        }
//        assertTrue(result);
//    }
//
//    /**
//     * checks if is working is set everywhere to false, [0] should be reserved for the test here
//     */
//    public void testListenerFalse(final boolean[] isWorking, EventModel eventId) throws InterruptedException {
//        main.getEventDistributor().registerEventListener(eventId, id -> isWorking[0] = true);
//        try {
//            Identification id = IdentificationManager.getInstance().getIdentification(this).get();
//            main.getLocalEventManager().registerCaller(id).get().fire(eventId);
//        } catch (MultipleEventsException e) {
//            fail();
//        }
//
//        synchronized (lock) {
//            lock.wait(10);
//            while(!main.getLocalEventManager().getEvents().isEmpty())
//            {
//                lock.wait(2);
//            }
//        }
//        boolean result = false;
//        for (boolean temp : isWorking)
//        {
//            if(temp) result = true;
//        }
//        assertFalse(result);
//    }
//
//    /**
//     * fires the event
//     * @param event the event to fire
//     */
//    public void testFireEvent(EventModel event) {
//        try {
//            Identification id = IdentificationManager.getInstance().getIdentification(this).get();
//            main.getLocalEventManager().registerCaller(id).get().fire(event);
//        } catch (MultipleEventsException e) {
//            fail();
//        }
//    }
//
//    /**
//     * waits for multitasking
//     * @throws InterruptedException
//     */
//    public void waitForMultith(EventModel event) throws InterruptedException {
//        synchronized (lock) {
//            final java.util.concurrent.locks.Lock lock = new ReentrantLock();
//            final Condition processing = lock.newCondition();
//
//            Consumer<List<Identification>> consumer = noParam -> {
//                lock.lock();
//                processing.signal();
//                lock.unlock();
//            };
//            event.getEventBehaviourController().controlOutputPluginBehaviour(identifications -> {
//                consumer.accept(identifications);
//                return new HashMap<>();
//            });
//            while(!main.getEventDistributor().getEvents().isEmpty())
//            {
//                lock.lock();
//                processing.await(10, TimeUnit.SECONDS);
//                lock.unlock();
//            }
//            this.lock.wait(10);
//        }
//    }
//
//    public Context getContext() {
//        return context;
//    }
//
//    public Optional<Identification> getNextIdentification() {
//        Identifiable identifiable = () -> id + identificationNumber;
//        identificationManager.registerIdentification(identifiable);
//        identificationNumber++;
//        return identificationManager.getIdentification(identifiable);
//    }
//
//    public String getNextID() {
//        String idStr = id + identificationNumber;
//        identificationNumber++;
//        return idStr;
//    }
//
//    public Optional<EventModel> getNextEvent() {
//        eventNumber++;
//        return getEvent(id + eventNumber);
//    }
//
//    public Main getMain() {
//        return main;
//    }
//
//    public static synchronized  Main getNewMain(List<AddOnModel> list) {
//        JavaFXInitializer jfx = new JavaFXInitializer();
//        try {
//            jfx.stop();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        return new Main(list, true);
//    }
}
