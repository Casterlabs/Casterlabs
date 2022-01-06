package co.casterlabs.caffeinated.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import xyz.e3ndr.reflectionlib.helpers.AccessHelper;

@AllArgsConstructor
public class ReflectiveProxy implements InvocationHandler {
    private Object instance;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invokeMethod(this.instance, method.getName(), args);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getProxy(@NonNull Class<T> clazz, @NonNull Object instance) {
        return (T) Proxy.newProxyInstance(
            ReflectiveProxy.class.getClassLoader(),
            new Class[] {
                    clazz
            },
            new ReflectiveProxy(instance)
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokeMethod(Object instance, String methodName, Object... args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?>[] parameters = new Class<?>[args.length];

        for (int i = 0; i != args.length; i++) {
            parameters[i] = args[i].getClass();
        }

        Method method = deepMethodSearch(instance.getClass(), methodName, parameters);

        AccessHelper.makeAccessible(method);

        return (T) method.invoke(instance, args);
    }

    private static Method deepMethodSearch(Class<?> clazz, String methodName, Class<?>[] parameters) {
        if (clazz == null) {
            throw new IllegalArgumentException("Cannot find field: " + methodName);
        } else {
            try {
                return clazz.getDeclaredMethod(methodName, parameters);
            } catch (NoSuchMethodException e) {
                return deepMethodSearch(clazz.getSuperclass(), methodName, parameters);
            }
        }
    }

}
