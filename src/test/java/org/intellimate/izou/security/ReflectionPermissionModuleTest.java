package org.intellimate.izou.security;

import classLoaderUtil.DifferentClassLoaderUtils;
import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.identification.Identification;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.ArrayList;

import static org.junit.Assert.fail;

/**
 * @author LeanderK
 * @version 1.0
 */
public class ReflectionPermissionModuleTest {
    private DifferentClassLoaderUtils differentClassLoaderUtils;
    @Before
    public void setUp() throws Exception {
        java.lang.SecurityManager securityManager = new java.lang.SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                if (perm instanceof ReflectPermission) {
                    //call methods to test reflection permission handling
                    System.out.println("reflection");
                }
            }
        };
        System.setSecurityManager(securityManager);
        differentClassLoaderUtils = new DifferentClassLoaderUtils();
    }

    @Test
    public void testNonIllegalPackageAddon() throws Exception{
        differentClassLoaderUtils.doMethodDiffClassloader(this.getClass(), "getArrayListSizeReflection", null)
                .supply();
        differentClassLoaderUtils.doMethodSameClassloader(this.getClass(), "getArrayListSizeReflection", null)
                .supply();
    }

    public void getArrayListSizeReflection() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("test");
        try {
            Field size = arrayList.getClass().getDeclaredField("size");
            size.setAccessible(true);
            Object o = size.get(arrayList);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testIllegalPackageAddOn() throws Exception {
        differentClassLoaderUtils.doMethodDiffClassloader(this.getClass(), "createIllegalIDperReflection", null)
                .supply();
        differentClassLoaderUtils.doMethodSameClassloader(this.getClass(), "createIllegalIDperReflection", null)
                .supply();
    }

    public void createIllegalIDperReflection() {
        Identifiable identifiable = () -> "testID";
        try {
            Constructor<Identification> constructor = Identification.class.getDeclaredConstructor(Identifiable.class, Boolean.TYPE);
            constructor.setAccessible(true);
            Identification identification = constructor.newInstance(identifiable, true);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            fail(e.getMessage());
        }
    }
}