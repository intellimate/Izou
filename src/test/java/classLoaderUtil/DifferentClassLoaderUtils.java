package classLoaderUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Supplier;

/**
 * @author LeanderK
 * @version 1.0
 */
public class DifferentClassLoaderUtils {
    /**
     * do not use this method! It does nothing....but i don't want to mess with access checks since this class
     * can be used for testing reflection.
     * @param supplier the supplier
     * @param <A> the return type
     * @return returns the result from the supplier
     */
    public  <A> A doMethod(Supplier<A> supplier) {
        return supplier.get();
    }

    public <A> SupplierExcpts<A> doMethodWithDiffClassloader(Supplier<A> supplier) throws Exception{
        URL url = this.getClass().getClassLoader().getResource(this.getClass().getName());
        URL[] urls = {url};
        URLClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return super.findClass(name);
            }
        };
        Class<?> aClass = classLoader.loadClass(this.getClass().getName());
        Object classObj = aClass.newInstance();
        Method doMethod = aClass.getMethod("doMethod", Supplier.class);
        Object[] param = {supplier};
        return () -> {
                Object result = doMethod.invoke(classObj, param);
                return (A) result;
             };
    }

    public <A> SupplierExcpts<A> doMethodDiffClassloader(Class<?> clazz, String methodName, Object param) throws Exception{
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        URL[] urls = {url};
        URLClassLoader classLoader = new URLClassLoader(urls, clazz.getClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(clazz.getName())) {
                    return super.findClass(name);
                } else {
                    return super.loadClass(name);
                }
            }
        };
        Class<?> aClass = classLoader.loadClass(clazz.getName());
        Object classObj = aClass.getDeclaredConstructors()[0].newInstance();
        Method doMethod;
        if (param == null) {
            doMethod = aClass.getMethod(methodName);
        } else {
            doMethod = aClass.getMethod(methodName, param.getClass());
        }
        Object[] params = param == null? null : new Object[] {param};
        //no lambda because it uses reflection
        return new SupplierExcpts<A>() {
            @Override
            public A supply() throws IllegalAccessException, InvocationTargetException {
                Object result = doMethod.invoke(classObj, params);
                return (A) result;
            }
        };
    }

    public <A> SupplierExcpts<A> doMethodSameClassloader(Class<?> clazz, String methodName, Object param) throws Exception{
        Object classObj = clazz.getDeclaredConstructors()[0].newInstance();
        Method doMethod;
        if (param == null) {
            doMethod = clazz.getMethod(methodName);
        } else {
            doMethod = clazz.getMethod(methodName, param.getClass());
        }
        Object[] params = param == null? null : new Object[] {param};
        //no lambda because it uses reflection
        return new SupplierExcpts<A>() {
            @Override
            public A supply() throws IllegalAccessException, InvocationTargetException {
                Object result = doMethod.invoke(classObj, params);
                return (A) result;
            }
        };
    }

    public interface SupplierExcpts<A> {
        A supply() throws java.lang.IllegalAccessException, java.lang.reflect.InvocationTargetException;
    }
}
