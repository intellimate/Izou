package org.intellimate.izou.main;


import org.intellimate.izou.threadpool.ThreadPoolManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author LeanderK
 * @version 1.0
 */
public class MainTest {
    public static Main getMockMain() {
        Main mainMock = mock(Main.class);

        ThreadPoolManager threadPoolManager = mock(ThreadPoolManager.class);
        ExecutorService executorService = Executors.newCachedThreadPool();
        when(threadPoolManager.getAddOnsThreadPool()).thenReturn(executorService);
        when(threadPoolManager.getIzouThreadPool()).thenReturn(executorService);

        when(mainMock.getThreadPoolManager()).thenReturn(threadPoolManager);

        return mainMock;
    }
}